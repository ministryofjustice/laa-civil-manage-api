package uk.gov.justice.laa_civil_manage_api;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import uk.gov.justice.laa_civil_manage_api.models.*;
import uk.gov.justice.laa_civil_manage_api.services.accessdatastore.AccessDataStoreProperties;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class PriorAuthorityIntegrationTest {

  @RegisterExtension
  static WireMockExtension accessDataStore =
      WireMockExtension.newInstance().options(wireMockConfig().dynamicPort()).build();

  @LocalServerPort private int port;

  @MockitoBean private JwtDecoder jwtDecoder;

  @MockitoBean private AccessDataStoreProperties accessDataStoreProperties;

  @MockitoBean private OAuth2AuthorizedClientManager authorizedClientManager;

  private RestClient authenticatedClient;

  @BeforeEach
  void setUp() {
    when(accessDataStoreProperties.urlFor(any())).thenReturn(accessDataStore.baseUrl());

    Jwt mockJwt =
        Jwt.withTokenValue("test-token").header("alg", "none").claim("sub", "test-user").build();

    when(jwtDecoder.decode("test-token")).thenReturn(mockJwt);

    ClientRegistration clientRegistration =
        ClientRegistration.withRegistrationId("entra-obo-access-data-store")
            .clientId("test-client-id")
            .clientSecret("test-client-secret")
            .authorizationGrantType(AuthorizationGrantType.JWT_BEARER)
            .tokenUri("https://test-tenant.example.com/oauth2/v2.0/token")
            .build();

    OAuth2AccessToken accessToken =
        new OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER,
            "downstream-access-token",
            Instant.now(),
            Instant.now().plusSeconds(300));

    OAuth2AuthorizedClient authorizedClient =
        new OAuth2AuthorizedClient(clientRegistration, "test-user", accessToken);

    when(authorizedClientManager.authorize(any())).thenReturn(authorizedClient);

    authenticatedClient =
        RestClient.builder().defaultHeader("Authorization", "Bearer test-token").build();
  }

  @Test
  void postingAPriorAuthorityRequestFlowsThroughToTheAccessDataStore() {
    UUID applicationId = UUID.randomUUID();
    UUID expectedSubmissionId = UUID.randomUUID();

    // Stub the downstream Access Data Store API to return 201 Created
    accessDataStore.stubFor(
        post(urlEqualTo("/api/v0/applications/" + applicationId + "/prior-authority"))
            .withHeader("X-Service-Name", equalTo("CIVIL_APPLY"))
            .willReturn(
                aResponse()
                    .withStatus(201)
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        """
                                            {
                                              "submissionId": "%s",
                                              "submittedAt": "2026-08-20T10:00:00Z"
                                            }
                                            """
                            .formatted(expectedSubmissionId))));

    PriorAuthority body =
        PriorAuthority.builder()
            .applicationId(applicationId)
            .priorAuthorityType(PriorAuthorityType.EXPERT)
            .expertDetails(
                ExpertDetails.builder()
                    .expertType("Psychologist")
                    .expertFullName("Dr John Doe")
                    .expertPostcode("SW1H 9AJ")
                    .expertCosts(
                        ExpertCosts.builder()
                            .billingType(BillingType.FIXED_RATE)
                            .totalAmount(new BigDecimal("249.99"))
                            .costsSharedWithOtherParties(false)
                            .build())
                    .build())
            .justification("Required expert evidence.")
            .build();

    ResponseEntity<PriorAuthorityApplicationResponse> response =
        authenticatedClient
            .post()
            .uri("http://localhost:" + port + "/prior-authority")
            .body(body)
            .retrieve()
            .toEntity(PriorAuthorityApplicationResponse.class);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(expectedSubmissionId, response.getBody().submissionId());
  }

  @Test
  void requestWithoutTokenReturns401Unauthorized() {
    RestClient unauthenticatedClient = RestClient.create();

    PriorAuthority body =
        PriorAuthority.builder()
            .applicationId(UUID.randomUUID())
            .priorAuthorityType(PriorAuthorityType.EXPERT)
            .build();

    HttpStatusCode status =
        unauthenticatedClient
            .post()
            .uri("http://localhost:" + port + "/prior-authority")
            .contentType(MediaType.APPLICATION_JSON)
            .body(body)
            .retrieve()
            .onStatus(_ -> true, (_, _) -> {})
            .toBodilessEntity()
            .getStatusCode();

    assertEquals(HttpStatus.UNAUTHORIZED, status);
  }

  @Test
  void uploadDocumentLargerThanConfiguredMultipartLimitReturns413() {
    byte[] oversizedFile = new byte[(10 * 1024 * 1024) + 1];
    ByteArrayResource fileResource =
        new ByteArrayResource(oversizedFile) {
          @Override
          public String getFilename() {
            return "large.pdf";
          }
        };

    MultiValueMap<String, Object> multipartBody = new LinkedMultiValueMap<>();
    multipartBody.add("file", fileResource);

    HttpStatusCode status =
        authenticatedClient
            .post()
            .uri("http://localhost:" + port + "/prior-authority/documents")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(multipartBody)
            .retrieve()
            .onStatus(_ -> true, (_, _) -> {})
            .toBodilessEntity()
            .getStatusCode();

    assertEquals(HttpStatus.CONTENT_TOO_LARGE, status);
  }
}

package uk.gov.justice.laa_civil_manage_api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.server.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import uk.gov.justice.laa_civil_manage_api.mockaccessdatastore.models.PriorAuthoritySubmission;
import uk.gov.justice.laa_civil_manage_api.models.*;
import uk.gov.justice.laa_civil_manage_api.services.accessdatastore.AccessDataStoreClient;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class PriorAuthorityIntegrationTest {

  @TestConfiguration
  static class Config {

    @Bean
    @Primary
    AccessDataStoreClient testAccessDataStoreClient(ServletWebServerApplicationContext context) {
      return new AccessDataStoreClient() {

        private String url() {
          return "http://localhost:"
              + Objects.requireNonNull(context.getWebServer()).getPort()
              + "/mock-access-data-store";
        }

        @Override
        public PriorAuthorityApplicationResponse submitPriorAuthority(
            PriorAuthority priorAuthority) {
          return RestClient.builder()
              .defaultHeader("Authorization", "Bearer fake-downstream-obo-token")
              .build()
              .post()
              .uri(
                  url() + "/applications/{applicationId}/prior-authority",
                  priorAuthority.applicationId())
              .contentType(MediaType.APPLICATION_JSON)
              .body(PriorAuthoritySubmission.from(priorAuthority))
              .retrieve()
              .body(PriorAuthorityApplicationResponse.class);
        }

        @Override
        public DraftCreatedResponse createDraft(Draft draft) {
          throw new UnsupportedOperationException("Not used in this test");
        }

        @Override
        public ApplicationSummaryResponse getApplications(
            int page, int pageSize, ApplicationStatus status) {
          throw new UnsupportedOperationException("Not used in this test");
        }

        @Override
        public void updateDraft(java.util.UUID draftId, Draft draft) {
          throw new UnsupportedOperationException("Not used in this test");
        }

        @Override
        public Optional<DraftSummary> getDraft(UUID draftId) {
          throw new UnsupportedOperationException("Not used in this test");
        }

        @Override
        public List<DraftSummary> getDrafts(
            String sourceSystem, String userId, String draftType, java.util.UUID applicationId) {
          throw new UnsupportedOperationException("Not used in this test");
        }

        @Override
        public void deleteDraft(java.util.UUID draftId) {
          throw new UnsupportedOperationException("Not used in this test");
        }

        @Override
        public uk.gov.justice.laa_civil_manage_api.models.ApplicationSummary getApplicationById(
            java.util.UUID applicationId) {
          throw new UnsupportedOperationException("Not used in this test");
        }
      };
    }
  }

  @LocalServerPort private int port;

  @MockitoBean private JwtDecoder jwtDecoder;

  @BeforeEach
  void setUp() {
    Jwt mockJwt =
        Jwt.withTokenValue("test-token").header("alg", "none").claim("sub", "test-user").build();

    when(jwtDecoder.decode("test-token")).thenReturn(mockJwt);

    when(jwtDecoder.decode("fake-downstream-obo-token"))
        .thenThrow(new BadJwtException("The aud claim is not valid"));
  }

  @Test
  void postingAPriorAuthorityRequestFlowsThroughToTheMockAccessDataStore() {
    RestClient restClient =
        RestClient.builder().defaultHeader("Authorization", "Bearer test-token").build();

    PriorAuthority body =
        PriorAuthority.builder()
            .applicationId(UUID.randomUUID())
            .priorAuthorityType(PriorAuthorityType.EXPERT)
            .expertType("Psychologist")
            .expertFullName("John Doe")
            .expertBasedInLondon(true)
            .billingType(BillingType.FIXED_RATE)
            .totalAmount(new BigDecimal("249.99"))
            .justification("Required expert evidence.")
            .build();

    ResponseEntity<PriorAuthorityApplicationResponse> response =
        restClient
            .post()
            .uri("http://localhost:" + port + "/prior-authority")
            .body(body)
            .retrieve()
            .toEntity(PriorAuthorityApplicationResponse.class);

    assertEquals(HttpStatusCode.valueOf(201), response.getStatusCode());
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().submissionId());
    assertEquals(SubmissionStatus.ACCEPTED, response.getBody().status());
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
    RestClient restClient =
        RestClient.builder().defaultHeader("Authorization", "Bearer test-token").build();

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
        restClient
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

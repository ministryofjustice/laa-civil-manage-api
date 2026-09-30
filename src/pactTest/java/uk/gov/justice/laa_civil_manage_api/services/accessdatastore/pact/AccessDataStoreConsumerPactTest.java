package uk.gov.justice.laa_civil_manage_api.services.accessdatastore.pact;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody;
import static org.assertj.core.api.Assertions.assertThat;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit.MockServerConfig;
import au.com.dius.pact.consumer.junit5.PactConsumerTest;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import uk.gov.justice.laa_civil_manage_api.services.accessdatastore.AccessDataStoreApplication;
import uk.gov.justice.laa_civil_manage_api.services.accessdatastore.AccessDataStoreProperties;
import uk.gov.justice.laa_civil_manage_api.services.accessdatastore.HttpAccessDataStoreClient;

@PactConsumerTest
@PactTestFor(providerName = AbstractPactTest.PROVIDER, pactVersion = PactSpecVersion.V3)
@MockServerConfig(port = "1232")
@DisplayName("GET /api/v0/applications/{id} Pact tests")
class AccessDataStoreConsumerPactTest extends AbstractPactTest {

  private static final UUID APPLICATION_ID =
      UUID.fromString("11111111-2222-3333-4444-555555555555");
  private static final String AUTHORIZATION = "Bearer swagger-caseworker-token";

  @Pact(consumer = CONSUMER)
  public RequestResponsePact applicationById(PactDslWithProvider builder) {
    return builder
        .given("an application exists by id")
        .uponReceiving("a request for an application by id")
        .method("GET")
        .matchPath("/api/v0/applications/" + UUID_REGEX, "/api/v0/applications/" + APPLICATION_ID)
        .matchHeader("X-Service-Name", SERVICE_NAME, SERVICE_NAME)
        .matchHeader("Authorization", "Bearer .+", AUTHORIZATION)
        .willRespondWith()
        .status(200)
        .body(
            newJsonBody(
                    root -> {
                      root.stringMatcher("applicationId", UUID_REGEX, APPLICATION_ID.toString());
                      root.stringType("laaReference");
                      root.stringType("status");
                      root.stringMatcher("submittedAt", ISO_DATETIME_REGEX, "2026-07-22T10:00:00Z");
                      root.stringType("matterType");
                      root.object("provider", provider -> provider.stringType("officeCode"));
                    })
                .build())
        .toPact();
  }

  @Test
  @DisplayName("Civil Manage API can fetch an application by ID")
  @PactTestFor(pactMethod = "applicationById")
  void getsApplicationByIdFromAds(MockServer mockServer) {
    RestClient.Builder builder =
        RestClient.builder()
            .requestInterceptor(
                (request, body, execution) -> {
                  request.getHeaders().add("X-Service-Name", SERVICE_NAME);
                  request.getHeaders().add("Authorization", AUTHORIZATION);
                  return execution.execute(request, body);
                });
    AccessDataStoreProperties properties =
        new AccessDataStoreProperties(
            mockServer.getUrl(), Duration.ofSeconds(3), Duration.ofSeconds(5), SERVICE_NAME);
    HttpAccessDataStoreClient client = new HttpAccessDataStoreClient(builder.build(), properties);

    AccessDataStoreApplication result = client.getApplicationById(APPLICATION_ID);

    assertThat(result.applicationId()).isEqualTo(APPLICATION_ID);
    assertThat(result.laaReference()).isNotBlank();
    assertThat(result.provider().officeCode()).isNotBlank();
  }
}

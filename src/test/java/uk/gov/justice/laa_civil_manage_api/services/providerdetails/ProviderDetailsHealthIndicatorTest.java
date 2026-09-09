package uk.gov.justice.laa_civil_manage_api.services.providerdetails;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.Status;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class ProviderDetailsHealthIndicatorTest {

  private static final String BASE_URL = "http://provider-details.test";

  private MockRestServiceServer server;
  private ProviderDetailsHealthIndicator indicator;

  @BeforeEach
  void setup() {
    RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
    server = MockRestServiceServer.bindTo(builder).build();
    ProviderDetailsProperties properties =
        new ProviderDetailsProperties(
            BASE_URL, "test-api-key", Duration.ofSeconds(3), Duration.ofSeconds(5), 3, 1, 1.0);
    indicator = new ProviderDetailsHealthIndicator(builder.build(), properties);
  }

  @Test
  void reportsUpWhenTheHealthEndpointRespondsSuccessfully() {
    server
        .expect(requestTo(BASE_URL + "/actuator/health"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess("{\"status\":\"UP\"}", MediaType.APPLICATION_JSON));

    Health health = indicator.health();

    server.verify();
    assertEquals(Status.UP, health.getStatus());
    assertEquals(BASE_URL, health.getDetails().get("baseUrl"));
  }

  @Test
  void reportsDownWhenTheHealthEndpointReturnsAnError() {
    server.expect(requestTo(BASE_URL + "/actuator/health")).andRespond(withServerError());

    Health health = indicator.health();

    assertEquals(Status.DOWN, health.getStatus());
    assertEquals(BASE_URL, health.getDetails().get("baseUrl"));
  }

  @Test
  void reportsDownWhenTheApiIsUnreachable() {
    RestClient unreachableClient =
        RestClient.builder()
            .requestFactory(
                (uri, method) -> {
                  throw new java.io.IOException("Connection refused");
                })
            .build();
    ProviderDetailsProperties properties =
        new ProviderDetailsProperties(
            BASE_URL, "test-api-key", Duration.ofSeconds(3), Duration.ofSeconds(5), 3, 1, 1.0);
    ProviderDetailsHealthIndicator unreachableIndicator =
        new ProviderDetailsHealthIndicator(unreachableClient, properties);

    assertEquals(Status.DOWN, unreachableIndicator.health().getStatus());
  }
}

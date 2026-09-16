package uk.gov.justice.laa_civil_manage_api.services.accessdatastore;

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

class AccessDataStoreHealthIndicatorTest {

  private static final String BASE_URL = "http://access-data-store.test";

  private MockRestServiceServer server;
  private AccessDataStoreHealthIndicator indicator;

  @BeforeEach
  void setup() {
    RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
    server = MockRestServiceServer.bindTo(builder).build();
    AccessDataStoreProperties properties =
        new AccessDataStoreProperties(BASE_URL, Duration.ofSeconds(3), Duration.ofSeconds(5));
    indicator = new AccessDataStoreHealthIndicator(builder, properties);
  }

  @Test
  void reportsUpWhenTheHealthEndpointRespondsWithUpStatus() {
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
  void reportsDownWhenTheHealthEndpointRespondsWithDownStatus() {
    server
        .expect(requestTo(BASE_URL + "/actuator/health"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess("{\"status\":\"DOWN\"}", MediaType.APPLICATION_JSON));

    Health health = indicator.health();

    server.verify();
    assertEquals(Status.DOWN, health.getStatus());
    assertEquals(BASE_URL, health.getDetails().get("baseUrl"));
  }

  @Test
  void reportsDownWhenTheHealthEndpointReturnsAServerError() {
    server.expect(requestTo(BASE_URL + "/actuator/health")).andRespond(withServerError());

    Health health = indicator.health();

    assertEquals(Status.DOWN, health.getStatus());
    assertEquals(BASE_URL, health.getDetails().get("baseUrl"));
  }

  @Test
  void reportsDownWhenTheApiIsUnreachable() {
    RestClient.Builder unreachableBuilder =
        RestClient.builder()
            .requestFactory(
                (uri, method) -> {
                  throw new java.io.IOException("Connection refused");
                });
    AccessDataStoreProperties properties =
        new AccessDataStoreProperties(BASE_URL, Duration.ofSeconds(3), Duration.ofSeconds(5));
    AccessDataStoreHealthIndicator unreachableIndicator =
        new AccessDataStoreHealthIndicator(unreachableBuilder, properties);

    Health health = unreachableIndicator.health();

    assertEquals(Status.DOWN, health.getStatus());
    assertEquals(BASE_URL, health.getDetails().get("baseUrl"));
  }
}

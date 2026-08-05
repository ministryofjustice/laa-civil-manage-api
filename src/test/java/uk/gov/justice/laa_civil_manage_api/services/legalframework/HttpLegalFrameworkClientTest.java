package uk.gov.justice.laa_civil_manage_api.services.legalframework;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

class HttpLegalFrameworkClientTest {

  private static final String BASE_URL = "http://legal-framework.test";

  private MockRestServiceServer server;
  private HttpLegalFrameworkClient client;

  @BeforeEach
  void setup() {
    RestClient.Builder builder = RestClient.builder();
    server = MockRestServiceServer.bindTo(builder).build();
    LegalFrameworkProperties properties =
        new LegalFrameworkProperties(BASE_URL, Duration.ofSeconds(3), Duration.ofSeconds(5));
    client = new HttpLegalFrameworkClient(builder.build(), properties);
  }

  @Test
  void getExpertTypesRequestsTheMatterTypeInThePath() {
    server
        .expect(requestTo(BASE_URL + "/expert_types/KPBLW"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(
            withSuccess(
                """
                [
                  {"code": "child_psychologist", "description": "Child Psychologist"},
                  {"code": "psychologist", "description": "Psychologist"}
                ]
                """,
                MediaType.APPLICATION_JSON));

    List<ExpertType> expertTypes = client.getExpertTypes("KPBLW");

    server.verify();
    assertEquals(2, expertTypes.size());
    assertEquals(
        new ExpertType("child_psychologist", "Child Psychologist"), expertTypes.getFirst());
  }

  @Test
  void getExpertTypesUrlEncodesTheMatterType() {
    server
        .expect(requestTo(BASE_URL + "/expert_types/A%20B%2FC"))
        .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

    assertTrue(client.getExpertTypes("A B/C").isEmpty());
    server.verify();
  }

  @Test
  void getExpertTypesReturnsEmptyListForAMatterTypeWithNoExpertTypes() {
    server
        .expect(requestTo(BASE_URL + "/expert_types/NOT_A_MATTER_TYPE"))
        .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

    assertTrue(client.getExpertTypes("NOT_A_MATTER_TYPE").isEmpty());
  }

  @Test
  void getExpertTypesPropagatesA404SoAMissingEndpointIsNotMistakenForNoResults() {
    server.expect(requestTo(BASE_URL + "/expert_types/KPBLW")).andRespond(withResourceNotFound());

    assertThrows(HttpClientErrorException.NotFound.class, () -> client.getExpertTypes("KPBLW"));
  }

  @Test
  void getExpertTypesPropagates5xx() {
    server.expect(requestTo(BASE_URL + "/expert_types/KPBLW")).andRespond(withServerError());

    assertThrows(HttpServerErrorException.class, () -> client.getExpertTypes("KPBLW"));
  }

  @Test
  void getStatusReportsHealthyWhenEveryCheckPasses() {
    server
        .expect(requestTo(BASE_URL + "/status"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess("{\"checks\":{\"database\":true}}", MediaType.APPLICATION_JSON));

    LegalFrameworkStatus status = client.getStatus();

    server.verify();
    assertEquals(Boolean.TRUE, status.checks().get("database"));
    assertTrue(status.isHealthy());
  }

  @Test
  void getStatusReportsUnhealthyWhenAnyCheckFails() {
    server
        .expect(requestTo(BASE_URL + "/status"))
        .andRespond(
            withSuccess(
                "{\"checks\":{\"database\":true,\"redis\":false}}", MediaType.APPLICATION_JSON));

    assertFalse(client.getStatus().isHealthy());
  }

  @Test
  void statusWithNoChecksIsNotTreatedAsHealthy() {
    assertFalse(new LegalFrameworkStatus(null).isHealthy());
    assertFalse(new LegalFrameworkStatus(java.util.Map.of()).isHealthy());
  }

  @Test
  void requireBaseUrlFailsFastWhenUnconfigured() {
    LegalFrameworkProperties unconfigured =
        new LegalFrameworkProperties("  ", Duration.ofSeconds(1), Duration.ofSeconds(1));

    assertThrows(IllegalStateException.class, unconfigured::requireBaseUrl);
  }
}

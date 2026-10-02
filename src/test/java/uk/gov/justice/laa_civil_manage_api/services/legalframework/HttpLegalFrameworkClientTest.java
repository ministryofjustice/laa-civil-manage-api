package uk.gov.justice.laa_civil_manage_api.services.legalframework;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@ExtendWith(OutputCaptureExtension.class)
class HttpLegalFrameworkClientTest {

  private static final String BASE_URL = "http://legal-framework.test";

  private static AnnotationConfigApplicationContext context;
  private MockRestServiceServer server;
  private LegalFrameworkClient client;

  @BeforeAll
  static void setupContext() {
    context = new AnnotationConfigApplicationContext(RetryTestConfig.class);
  }

  @AfterAll
  static void closeContext() {
    context.close();
  }

  @AfterEach
  void resetServer() {
    server.reset();
  }

  @BeforeEach
  void setup() {
    server = context.getBean(MockRestServiceServer.class);
    client = context.getBean(LegalFrameworkClient.class);
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
  void getExpertTypesPropagatesA404SoAMissingEndpointIsNotMistakenForNoResults(
      CapturedOutput output) {
    server.expect(requestTo(BASE_URL + "/expert_types/KPBLW")).andRespond(withResourceNotFound());

    assertThrows(HttpClientErrorException.NotFound.class, () -> client.getExpertTypes("KPBLW"));
    server.verify();
    assertFalse(output.getOut().contains("Legal Framework API call"));
  }

  @Test
  void getExpertTypesRetriesOn5xxAndEventuallySucceeds(CapturedOutput output) {
    server.expect(requestTo(BASE_URL + "/expert_types/KPBLW")).andRespond(withServerError());
    server
        .expect(requestTo(BASE_URL + "/expert_types/KPBLW"))
        .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

    assertTrue(client.getExpertTypes("KPBLW").isEmpty());
    server.verify();
    assertFalse(output.getOut().contains("Legal Framework API call"));
  }

  @Test
  void getExpertTypesDoesNotRetryOnBadRequest() {
    server
        .expect(requestTo(BASE_URL + "/expert_types/KPBLW"))
        .andRespond(withStatus(HttpStatus.BAD_REQUEST));

    assertThrows(HttpClientErrorException.BadRequest.class, () -> client.getExpertTypes("KPBLW"));
    server.verify();
  }

  @Test
  void getExpertTypesLogsAnErrorAndPropagates5xxAfterExhaustingRetries(CapturedOutput output) {
    for (int i = 0; i < 3; i++) {
      server.expect(requestTo(BASE_URL + "/expert_types/KPBLW")).andRespond(withServerError());
    }

    assertThrows(HttpServerErrorException.class, () -> client.getExpertTypes("KPBLW"));
    server.verify();
    assertLoggedOnceAtError(
        output, "Legal Framework API call getExpertTypes failed after 3 attempts");
  }

  @Test
  void getStatusLogsAnErrorAndPropagatesTheNetworkFailureAfterExhaustingRetries(
      CapturedOutput output) {
    for (int i = 0; i < 3; i++) {
      server
          .expect(requestTo(BASE_URL + "/status"))
          .andRespond(withException(new IOException("Connection refused")));
    }

    assertThrows(ResourceAccessException.class, () -> client.getStatus());
    server.verify();
    assertLoggedOnceAtError(output, "Legal Framework API call getStatus failed after 3 attempts");
  }

  @Test
  void getStatusRetriesOnNetworkFailureAndEventuallySucceeds() {
    server
        .expect(requestTo(BASE_URL + "/status"))
        .andRespond(withException(new IOException("Connection reset")));
    server
        .expect(requestTo(BASE_URL + "/status"))
        .andRespond(withSuccess("{\"checks\":{\"database\":true}}", MediaType.APPLICATION_JSON));

    assertTrue(client.getStatus().isHealthy());
    server.verify();
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

  private static void assertLoggedOnceAtError(CapturedOutput output, String message) {
    List<String> lines = output.getOut().lines().filter(line -> line.contains(message)).toList();
    assertEquals(1, lines.size(), () -> "expected exactly one log line for: " + message);
    assertTrue(lines.getFirst().contains("ERROR"), () -> "not logged at ERROR: " + lines);
  }

  @Configuration
  @EnableRetry
  static class RetryTestConfig {

    @Bean
    LegalFrameworkProperties legalFrameworkProperties() {
      return new LegalFrameworkProperties(
          BASE_URL, Duration.ofSeconds(3), Duration.ofSeconds(5), 3, 200, 2.0);
    }

    @Bean
    RestClient.Builder legalFrameworkRestClientBuilder() {
      return RestClient.builder();
    }

    @Bean
    MockRestServiceServer mockRestServiceServer(
        RestClient.Builder legalFrameworkRestClientBuilder) {
      return MockRestServiceServer.bindTo(legalFrameworkRestClientBuilder).build();
    }

    @Bean
    RestClient legalFrameworkRestClient(RestClient.Builder legalFrameworkRestClientBuilder) {
      return legalFrameworkRestClientBuilder.build();
    }

    @Bean
    HttpLegalFrameworkClient httpLegalFrameworkClient(
        RestClient legalFrameworkRestClient, LegalFrameworkProperties legalFrameworkProperties) {
      return new HttpLegalFrameworkClient(legalFrameworkRestClient, legalFrameworkProperties);
    }
  }
}

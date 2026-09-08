package uk.gov.justice.laa_civil_manage_api.services.providerdetails;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withUnauthorizedRequest;

import java.io.IOException;
import java.time.Duration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class ProviderDetailsServiceTest {

  private static final String BASE_URL = "http://provider-details.test";
  private static final String OFFICE_CODE = "1A234B";

  private static AnnotationConfigApplicationContext context;
  private static MockRestServiceServer server;
  private static ProviderDetailsService service;

  @BeforeAll
  static void initContext() {
    context = new AnnotationConfigApplicationContext();
    context.register(RetryTestConfig.class);
    context.refresh();
    server = context.getBean(MockRestServiceServer.class);
    service = context.getBean(ProviderDetailsService.class);
  }

  @AfterAll
  static void closeContext() {
    context.close();
  }

  @AfterEach
  void resetServerExpectations() {
    server.reset();
  }

  @Test
  void getProviderEmailReturnsTheEmailAddressOn200Ok() {
    server
        .expect(requestTo(BASE_URL + "/api/v1/provider-offices/" + OFFICE_CODE))
        .andExpect(method(HttpMethod.GET))
        .andExpect(header("X-Authorization", "test-api-key"))
        .andRespond(
            withSuccess(
                """
                {"office": {"officeCode": "1A234B", "emailAddress": "office@example.com"}}
                """,
                MediaType.APPLICATION_JSON));

    String email = service.getProviderEmail(OFFICE_CODE);

    server.verify();
    assertEquals("office@example.com", email);
  }

  @Test
  void getProviderEmailReturnsNullWhenEmailAddressIsMissing() {
    server
        .expect(requestTo(BASE_URL + "/api/v1/provider-offices/" + OFFICE_CODE))
        .andRespond(
            withSuccess(
                """
                {"office": {"officeCode": "1A234B", "emailAddress": null}}
                """,
                MediaType.APPLICATION_JSON));

    assertNull(service.getProviderEmail(OFFICE_CODE));
  }

  @Test
  void getProviderEmailReturnsNullWhenOfficeIsMissing() {
    server
        .expect(requestTo(BASE_URL + "/api/v1/provider-offices/" + OFFICE_CODE))
        .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

    assertNull(service.getProviderEmail(OFFICE_CODE));
  }

  @Test
  void getProviderEmailReturnsNullOn204NoContent() {
    server
        .expect(requestTo(BASE_URL + "/api/v1/provider-offices/" + OFFICE_CODE))
        .andRespond(withNoContent());

    assertNull(service.getProviderEmail(OFFICE_CODE));
  }

  @Test
  void getProviderEmailThrowsProviderApiExceptionOn401Unauthorized() {
    server
        .expect(requestTo(BASE_URL + "/api/v1/provider-offices/" + OFFICE_CODE))
        .andRespond(withUnauthorizedRequest());

    assertThrows(ProviderApiException.class, () -> service.getProviderEmail(OFFICE_CODE));
  }

  @Test
  void getProviderEmailThrowsProviderApiExceptionOn403Forbidden() {
    server
        .expect(requestTo(BASE_URL + "/api/v1/provider-offices/" + OFFICE_CODE))
        .andRespond(withStatus(HttpStatus.FORBIDDEN));

    assertThrows(ProviderApiException.class, () -> service.getProviderEmail(OFFICE_CODE));
  }

  @Test
  void getProviderEmailThrowsProviderApiExceptionOn404NotFound() {
    server
        .expect(requestTo(BASE_URL + "/api/v1/provider-offices/" + OFFICE_CODE))
        .andRespond(withStatus(HttpStatus.NOT_FOUND));

    assertThrows(ProviderApiException.class, () -> service.getProviderEmail(OFFICE_CODE));
  }

  @Test
  void getProviderEmailRetriesOn409ConflictAndEventuallySucceeds() {
    server
        .expect(requestTo(BASE_URL + "/api/v1/provider-offices/" + OFFICE_CODE))
        .andRespond(withStatus(HttpStatus.CONFLICT));
    server
        .expect(requestTo(BASE_URL + "/api/v1/provider-offices/" + OFFICE_CODE))
        .andRespond(withStatus(HttpStatus.CONFLICT));
    server
        .expect(requestTo(BASE_URL + "/api/v1/provider-offices/" + OFFICE_CODE))
        .andRespond(
            withSuccess(
                """
                {"office": {"officeCode": "1A234B", "emailAddress": "office@example.com"}}
                """,
                MediaType.APPLICATION_JSON));

    String email = service.getProviderEmail(OFFICE_CODE);

    server.verify();
    assertEquals("office@example.com", email);
  }

  @Test
  void getProviderEmailThrowsProviderApiExceptionAfterExhaustingRetriesOn409Conflict() {
    for (int i = 0; i < 3; i++) {
      server
          .expect(requestTo(BASE_URL + "/api/v1/provider-offices/" + OFFICE_CODE))
          .andRespond(withStatus(HttpStatus.CONFLICT));
    }

    assertThrows(ProviderApiException.class, () -> service.getProviderEmail(OFFICE_CODE));
    server.verify();
  }

  @Test
  void getProviderEmailThrowsProviderApiExceptionAfterExhaustingRetriesOn5xxServerError() {
    for (int i = 0; i < 3; i++) {
      server
          .expect(requestTo(BASE_URL + "/api/v1/provider-offices/" + OFFICE_CODE))
          .andRespond(withServerError());
    }

    assertThrows(ProviderApiException.class, () -> service.getProviderEmail(OFFICE_CODE));
    server.verify();
  }

  @Test
  void getProviderEmailRetriesAndThrowsProviderApiExceptionOnNetworkError() {
    for (int i = 0; i < 3; i++) {
      server
          .expect(requestTo(BASE_URL + "/api/v1/provider-offices/" + OFFICE_CODE))
          .andRespond(
              request -> {
                throw new IOException("Connection refused");
              });
    }

    assertThrows(ProviderApiException.class, () -> service.getProviderEmail(OFFICE_CODE));
    server.verify();
  }

  @Configuration
  @EnableRetry
  static class RetryTestConfig {

    @Bean
    ProviderDetailsProperties providerDetailsProperties() {
      return new ProviderDetailsProperties(
          BASE_URL, "test-api-key", Duration.ofSeconds(3), Duration.ofSeconds(5), 3, 1, 1.0);
    }

    @Bean
    RestClient.Builder providerDetailsRestClientBuilder(ProviderDetailsProperties properties) {
      return RestClient.builder()
          .baseUrl(properties.baseUrl())
          .requestInterceptor(new ProviderDetailsAuthInterceptor(properties));
    }

    @Bean
    MockRestServiceServer mockRestServiceServer(
        RestClient.Builder providerDetailsRestClientBuilder) {
      return MockRestServiceServer.bindTo(providerDetailsRestClientBuilder).build();
    }

    @Bean
    RestClient providerDetailsRestClient(
        RestClient.Builder providerDetailsRestClientBuilder,
        MockRestServiceServer mockRestServiceServer) {
      return providerDetailsRestClientBuilder.build();
    }

    @Bean
    ProviderDetailsService providerDetailsService(RestClient providerDetailsRestClient) {
      return new ProviderDetailsService(providerDetailsRestClient);
    }
  }
}

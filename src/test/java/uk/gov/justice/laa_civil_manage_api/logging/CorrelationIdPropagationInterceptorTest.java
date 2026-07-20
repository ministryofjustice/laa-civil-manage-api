package uk.gov.justice.laa_civil_manage_api.logging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.net.URI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;

class CorrelationIdPropagationInterceptorTest {

  private final CorrelationIdPropagationInterceptor interceptor =
      new CorrelationIdPropagationInterceptor();

  @AfterEach
  void clearMdc() {
    MDC.clear();
  }

  @Test
  void addsCorrelationIdHeaderFromMdc() throws Exception {
    MDC.put(CorrelationId.MDC_KEY, "corr-abc");
    MockClientHttpRequest request =
        new MockClientHttpRequest(HttpMethod.GET, URI.create("http://ads/x"));
    ClientHttpRequestExecution execution =
        (req, body) -> new MockClientHttpResponse(new byte[0], HttpStatus.OK);

    interceptor.intercept(request, new byte[0], execution);

    assertEquals("corr-abc", request.getHeaders().getFirst(CorrelationId.HEADER));
  }

  @Test
  void doesNotAddHeaderWhenNoCorrelationIdInMdc() throws Exception {
    MockClientHttpRequest request =
        new MockClientHttpRequest(HttpMethod.GET, URI.create("http://ads/x"));
    ClientHttpRequestExecution execution =
        (req, body) -> new MockClientHttpResponse(new byte[0], HttpStatus.OK);

    interceptor.intercept(request, new byte[0], execution);

    assertFalse(request.getHeaders().containsHeader(CorrelationId.HEADER));
  }
}

package uk.gov.justice.laa_civil_manage_api.services.accessdatastore;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.net.URI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

class SecondaryAuthorizationHeaderInterceptorTest {

  private final SecondaryAuthorizationHeaderInterceptor interceptor =
      new SecondaryAuthorizationHeaderInterceptor();

  @AfterEach
  void clearRequestContext() {
    RequestContextHolder.resetRequestAttributes();
  }

  @Test
  void addsXAuthorizationHeaderWhenPresentInInboundRequest() throws Exception {
    MockHttpServletRequest inboundRequest = new MockHttpServletRequest();
    inboundRequest.addHeader("X-Authorization", "raw-id-token-123");
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(inboundRequest));

    MockClientHttpRequest outboundRequest =
        new MockClientHttpRequest(HttpMethod.GET, URI.create("http://ads/x"));
    ClientHttpRequestExecution execution =
        (req, body) -> new MockClientHttpResponse(new byte[0], HttpStatus.OK);

    interceptor.intercept(outboundRequest, new byte[0], execution);

    assertEquals("raw-id-token-123", outboundRequest.getHeaders().getFirst("X-Authorization"));
  }

  @Test
  void doesNotAddHeaderWhenNotInboundRequestHeaderIsMissing() throws Exception {
    MockHttpServletRequest inboundRequest = new MockHttpServletRequest();
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(inboundRequest));

    MockClientHttpRequest outboundRequest =
        new MockClientHttpRequest(HttpMethod.GET, URI.create("http://ads/x"));
    ClientHttpRequestExecution execution =
        (req, body) -> new MockClientHttpResponse(new byte[0], HttpStatus.OK);

    interceptor.intercept(outboundRequest, new byte[0], execution);

    assertFalse(outboundRequest.getHeaders().containsHeader("X-Authorization"));
  }

  @Test
  void doesNotThrowExceptionWhenNoActiveRequestContextExists() throws Exception {
    // Deliberately not setting RequestContextHolder to simulate a background thread/async task
    MockClientHttpRequest outboundRequest =
        new MockClientHttpRequest(HttpMethod.GET, URI.create("http://ads/x"));
    ClientHttpRequestExecution execution =
        (req, body) -> new MockClientHttpResponse(new byte[0], HttpStatus.OK);

    interceptor.intercept(outboundRequest, new byte[0], execution);

    assertFalse(outboundRequest.getHeaders().containsHeader("X-Authorization"));
  }
}

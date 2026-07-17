package uk.gov.justice.laa_civil_manage_api.logging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class CorrelationIdFilterTest {

  private final CorrelationIdFilter filter = new CorrelationIdFilter();

  @AfterEach
  void clearMdc() {
    MDC.clear();
  }

  @Test
  void reusesInboundCorrelationIdInMdcAndResponseHeader() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader(CorrelationId.HEADER, "inbound-123");
    MockHttpServletResponse response = new MockHttpServletResponse();

    AtomicReference<String> mdcDuringRequest = new AtomicReference<>();
    MockFilterChain chain =
        new MockFilterChain() {
          @Override
          public void doFilter(
              jakarta.servlet.ServletRequest req, jakarta.servlet.ServletResponse res) {
            mdcDuringRequest.set(MDC.get(CorrelationId.MDC_KEY));
          }
        };

    filter.doFilter(request, response, chain);

    assertEquals("inbound-123", mdcDuringRequest.get());
    assertEquals("inbound-123", response.getHeader(CorrelationId.HEADER));
  }

  @Test
  void generatesCorrelationIdWhenHeaderMissing() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    AtomicReference<String> mdcDuringRequest = new AtomicReference<>();
    MockFilterChain chain =
        new MockFilterChain() {
          @Override
          public void doFilter(
              jakarta.servlet.ServletRequest req, jakarta.servlet.ServletResponse res) {
            mdcDuringRequest.set(MDC.get(CorrelationId.MDC_KEY));
          }
        };

    filter.doFilter(request, response, chain);

    String generated = response.getHeader(CorrelationId.HEADER);
    assertTrue(generated != null && !generated.isBlank());
    assertEquals(generated, mdcDuringRequest.get());
  }

  @Test
  void truncatesOverlyLongInboundCorrelationId() throws Exception {
    String tooLong = "x".repeat(200);
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader(CorrelationId.HEADER, tooLong);
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilter(request, response, new MockFilterChain());

    assertEquals(CorrelationId.MAX_LENGTH, response.getHeader(CorrelationId.HEADER).length());
  }

  @Test
  void clearsMdcAfterRequest() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader(CorrelationId.HEADER, "inbound-123");

    filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

    assertNull(MDC.get(CorrelationId.MDC_KEY));
  }
}

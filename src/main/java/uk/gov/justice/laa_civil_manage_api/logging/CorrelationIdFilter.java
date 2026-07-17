package uk.gov.justice.laa_civil_manage_api.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Establishes a correlation ID for every request so it appears in all structured log lines and can
 * be traced end-to-end across services.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String correlationId = resolveCorrelationId(request.getHeader(CorrelationId.HEADER));
    MDC.put(CorrelationId.MDC_KEY, correlationId);
    response.setHeader(CorrelationId.HEADER, correlationId);
    try {
      filterChain.doFilter(request, response);
    } finally {
      MDC.remove(CorrelationId.MDC_KEY);
    }
  }

  private static String resolveCorrelationId(String inbound) {
    if (!StringUtils.hasText(inbound)) {
      return UUID.randomUUID().toString();
    }
    String trimmed = inbound.strip();
    return trimmed.length() > CorrelationId.MAX_LENGTH
        ? trimmed.substring(0, CorrelationId.MAX_LENGTH)
        : trimmed;
  }
}

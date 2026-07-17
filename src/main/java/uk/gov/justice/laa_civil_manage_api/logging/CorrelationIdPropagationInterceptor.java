package uk.gov.justice.laa_civil_manage_api.logging;

import java.io.IOException;
import org.slf4j.MDC;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StringUtils;

public class CorrelationIdPropagationInterceptor implements ClientHttpRequestInterceptor {

  @Override
  public ClientHttpResponse intercept(
      HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
    String correlationId = MDC.get(CorrelationId.MDC_KEY);
    if (StringUtils.hasText(correlationId)) {
      request.getHeaders().set(CorrelationId.HEADER, correlationId);
    }
    return execution.execute(request, body);
  }
}

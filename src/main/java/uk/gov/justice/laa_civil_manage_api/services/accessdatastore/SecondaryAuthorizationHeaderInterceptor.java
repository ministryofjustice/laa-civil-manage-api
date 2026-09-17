package uk.gov.justice.laa_civil_manage_api.services.accessdatastore;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class SecondaryAuthorizationHeaderInterceptor implements ClientHttpRequestInterceptor {

  private static final String X_AUTHORIZATION_HEADER = "X-Authorization";

  @Override
  public ClientHttpResponse intercept(
      HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {

    ServletRequestAttributes attributes =
        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

    if (attributes != null) {
      HttpServletRequest inboundRequest = attributes.getRequest();
      String idToken = inboundRequest.getHeader(X_AUTHORIZATION_HEADER);

      if (StringUtils.hasText(idToken)) {
        request.getHeaders().add(X_AUTHORIZATION_HEADER, idToken);
      }
    }

    return execution.execute(request, body);
  }
}

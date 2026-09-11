package uk.gov.justice.laa_civil_manage_api.config;

import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

/**
 * TODO: TEMPORARY HACK FOR SPIKE Attaches a hardcoded Bearer token to every outbound Access Data
 * Store request, bypassing the OAuth2 On-Behalf-Of exchange.
 */
@Slf4j
public class HardcodedBearerTokenInterceptor implements ClientHttpRequestInterceptor {

  private final String token;

  public HardcodedBearerTokenInterceptor(String token) {
    this.token = token;
    log.warn(
        "TEMPORARY SPIKE HACK ENABLED: bypassing OAuth2 OBO flow for the Access Data Store "
            + "client and using a hardcoded Bearer token instead. This MUST NOT be used outside "
            + "of local spike testing.");
  }

  @Override
  public ClientHttpResponse intercept(
      HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
    request.getHeaders().setBearerAuth(token);
    return execution.execute(request, body);
  }
}

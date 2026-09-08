package uk.gov.justice.laa_civil_manage_api.services.providerdetails;

import java.io.IOException;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

/**
 * Attaches the Provider Details API key to every outbound request via the {@code X-Authorization}
 * header. The key itself is never logged.
 */
public class ProviderDetailsAuthInterceptor implements ClientHttpRequestInterceptor {

  private static final String API_KEY_HEADER = "X-Authorization";

  private final ProviderDetailsProperties properties;

  public ProviderDetailsAuthInterceptor(ProviderDetailsProperties properties) {
    this.properties = properties;
  }

  @Override
  public ClientHttpResponse intercept(
      HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
    request.getHeaders().set(API_KEY_HEADER, properties.apiKey());
    return execution.execute(request, body);
  }
}

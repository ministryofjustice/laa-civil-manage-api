package uk.gov.justice.laa_civil_manage_api.services.accessdatastore;

import java.io.IOException;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

/** Attaches the configured service name header to outbound HTTP requests. */
public class ServiceNameHeaderInterceptor implements ClientHttpRequestInterceptor {

  private static final String X_SERVICE_NAME_HEADER = "X-Service-Name";
  private final AccessDataStoreProperties properties;

  public ServiceNameHeaderInterceptor(AccessDataStoreProperties properties) {
    this.properties = properties;
  }

  @Override
  public ClientHttpResponse intercept(
      HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {

    request.getHeaders().add(X_SERVICE_NAME_HEADER, properties.serviceName());

    return execution.execute(request, body);
  }
}

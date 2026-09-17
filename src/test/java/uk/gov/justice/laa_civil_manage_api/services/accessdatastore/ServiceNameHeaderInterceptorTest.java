package uk.gov.justice.laa_civil_manage_api.services.accessdatastore;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;

class ServiceNameHeaderInterceptorTest {

  @Test
  void addsServiceNameHeaderFromProperties() throws Exception {
    AccessDataStoreProperties mockProperties = mock(AccessDataStoreProperties.class);
    when(mockProperties.serviceName()).thenReturn("CIVIL_MANAGE_TEST");

    ServiceNameHeaderInterceptor interceptor = new ServiceNameHeaderInterceptor(mockProperties);

    MockClientHttpRequest outboundRequest =
        new MockClientHttpRequest(HttpMethod.GET, URI.create("http://ads/x"));
    ClientHttpRequestExecution execution =
        (req, body) -> new MockClientHttpResponse(new byte[0], HttpStatus.OK);

    interceptor.intercept(outboundRequest, new byte[0], execution);

    assertEquals("CIVIL_MANAGE_TEST", outboundRequest.getHeaders().getFirst("X-Service-Name"));
  }
}

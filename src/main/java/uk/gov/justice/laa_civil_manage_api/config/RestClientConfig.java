package uk.gov.justice.laa_civil_manage_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import uk.gov.justice.laa_civil_manage_api.services.accessdatastore.AccessDataStoreProperties;

@Configuration
public class RestClientConfig {

  @Bean
  public RestClient.Builder restClientBuilder(AccessDataStoreProperties properties) {
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout((int) properties.connectTimeout().toMillis());
    factory.setReadTimeout((int) properties.readTimeout().toMillis());
    return RestClient.builder().requestFactory(factory);
  }
}

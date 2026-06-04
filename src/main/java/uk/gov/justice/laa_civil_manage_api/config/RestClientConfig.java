package uk.gov.justice.laa_civil_manage_api.config;

import java.net.InetSocketAddress;
import java.net.Proxy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.oauth2.client.JwtBearerOAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.endpoint.RestClientJwtBearerTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import uk.gov.justice.laa_civil_manage_api.services.accessdatastore.AccessDataStoreProperties;

@Configuration
public class RestClientConfig {

  private static final String CLIENT_REGISTRATION_ID = "ads-api";

  @Value("${custom.proxy.host:}")
  private String proxyHost;

  @Value("${custom.proxy.port:0}")
  private int proxyPort;

  /**
   * Manages the OAuth2 On-Behalf-Of exchange: it swaps the inbound user token for an Access Data
   * Store token via the jwt-bearer grant
   */
  @Bean
  public OAuth2AuthorizedClientManager authorizedClientManager(
      AccessDataStoreProperties properties,
      ClientRegistrationRepository clientRegistrationRepository,
      OAuth2AuthorizedClientRepository authorizedClientRepository) {

    RestClientJwtBearerTokenResponseClient tokenResponseClient =
        new RestClientJwtBearerTokenResponseClient();

    tokenResponseClient.addParametersConverter(
        request -> {
          LinkedMultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
          parameters.add("requested_token_use", "on_behalf_of");
          return parameters;
        });

    tokenResponseClient.setRestClient(
        RestClient.builder()
            .requestFactory(requestFactory(properties))
            .configureMessageConverters(
                builder -> {
                  builder.registerDefaults();
                  builder.addCustomConverter(
                      new org.springframework.http.converter.FormHttpMessageConverter());
                  builder.addCustomConverter(
                      new org.springframework.security.oauth2.core.http.converter
                          .OAuth2AccessTokenResponseHttpMessageConverter());
                })
            .build());

    JwtBearerOAuth2AuthorizedClientProvider jwtBearerProvider =
        new JwtBearerOAuth2AuthorizedClientProvider();
    jwtBearerProvider.setAccessTokenResponseClient(tokenResponseClient);

    OAuth2AuthorizedClientProvider authorizedClientProvider =
        OAuth2AuthorizedClientProviderBuilder.builder().provider(jwtBearerProvider).build();

    DefaultOAuth2AuthorizedClientManager authorizedClientManager =
        new DefaultOAuth2AuthorizedClientManager(
            clientRegistrationRepository, authorizedClientRepository);
    authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

    return authorizedClientManager;
  }

  /** Attaches the On-Behalf-Of token to every outbound request */
  @Bean
  public RestClient adsRestClient(
      AccessDataStoreProperties properties,
      OAuth2AuthorizedClientManager authorizedClientManager,
      OAuth2AuthorizedClientRepository authorizedClientRepository) {

    OAuth2ClientHttpRequestInterceptor interceptor =
        new OAuth2ClientHttpRequestInterceptor(authorizedClientManager);
    interceptor.setClientRegistrationIdResolver(request -> CLIENT_REGISTRATION_ID);
    interceptor.setAuthorizationFailureHandler(
        OAuth2ClientHttpRequestInterceptor.authorizationFailureHandler(authorizedClientRepository));

    return RestClient.builder()
        .requestFactory(requestFactory(properties))
        .requestInterceptor(interceptor)
        .build();
  }

  private ClientHttpRequestFactory requestFactory(AccessDataStoreProperties properties) {
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout((int) properties.connectTimeout().toMillis());
    factory.setReadTimeout((int) properties.readTimeout().toMillis());
    if (proxyHost != null && !proxyHost.isEmpty() && proxyPort > 0) {
      factory.setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort)));
    }
    return factory;
  }
}

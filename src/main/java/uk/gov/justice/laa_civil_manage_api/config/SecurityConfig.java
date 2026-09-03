package uk.gov.justice.laa_civil_manage_api.config;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
  private String jwkSetUri;

  @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:}")
  private String issuerUri;

  @Value("${custom.proxy.host:}")
  private String proxyHost;

  @Value("${custom.proxy.port:0}")
  private int proxyPort;

  @Value("${laa-civil-manage-api.cors.allowed-origins:}")
  private String allowedOrigins;

  @Bean
  @ConditionalOnProperty(name = "SKIP_AUTH", havingValue = "true")
  public SecurityFilterChain skipAuthFilterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(authz -> authz.anyRequest().permitAll());

    return http.build();
  }

  @Bean
  @ConditionalOnProperty(name = "SKIP_AUTH", havingValue = "false", matchIfMissing = true)
  public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            authz ->
                authz
                    .requestMatchers(
                        "/actuator/health",
                        "/actuator/health/**",
                        "/info",
                        "/error",
                        "/metrics",
                        "/v3/api-docs/**",
                        "/swagger-ui/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {}));

    return http.build();
  }

  @Bean
  public JwtDecoder jwtDecoder(Environment environment) {
    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();

    if (proxyHost != null && !proxyHost.isEmpty() && proxyPort > 0) {
      Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
      requestFactory.setProxy(proxy);
    }

    RestTemplate restTemplate = new RestTemplate(requestFactory);

    NimbusJwtDecoder decoder =
        NimbusJwtDecoder.withJwkSetUri(jwkSetUri).restOperations(restTemplate).build();
    decoder.setJwtValidator(tokenValidator(environment));
    return decoder;
  }

  private OAuth2TokenValidator<Jwt> tokenValidator(Environment environment) {
    List<String> audiences =
        Binder.get(environment)
            .bind(
                "spring.security.oauth2.resourceserver.jwt.audiences",
                Bindable.listOf(String.class))
            .orElse(List.of());
    return jwtValidator(issuerUri, audiences);
  }

  static OAuth2TokenValidator<Jwt> jwtValidator(String issuerUri, List<String> audiences) {
    OAuth2TokenValidator<Jwt> base =
        StringUtils.hasText(issuerUri)
            ? JwtValidators.createDefaultWithIssuer(issuerUri)
            : JwtValidators.createDefault();

    List<OAuth2TokenValidator<Jwt>> validators = new ArrayList<>();
    validators.add(base);
    if (!audiences.isEmpty()) {
      validators.add(
          new JwtClaimValidator<List<String>>(
              JwtClaimNames.AUD, aud -> aud.stream().anyMatch(audiences::contains)));
    }
    return new DelegatingOAuth2TokenValidator<>(validators);
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    List<String> origins =
        Arrays.stream(allowedOrigins.split(","))
            .map(String::trim)
            .filter(StringUtils::hasText)
            .toList();

    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(origins);
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(List.of("*"));
    configuration.setExposedHeaders(List.of("X-Correlation-ID", "Location"));
    configuration.setAllowCredentials(false);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  @Bean
  public WebSecurityCustomizer webSecurityCustomizer() {
    return (web) -> web.ignoring().requestMatchers("/mock-access-data-store/**");
  }
}

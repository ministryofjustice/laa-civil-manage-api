package uk.gov.justice.laa_civil_manage_api.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(DataAccessApiProperties.class)
public class RestClientConfig {

    @Bean
    public RestClient dataAccessApiRestClient(DataAccessApiProperties properties) {
        return RestClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultHeader("X-Service-Name", "CIVIL_APPLY")
                .build();
    }
}

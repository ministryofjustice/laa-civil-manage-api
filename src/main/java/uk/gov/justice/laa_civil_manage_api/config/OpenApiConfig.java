package uk.gov.justice.laa_civil_manage_api.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public GroupedOpenApi publicApi() {
    return GroupedOpenApi.builder()
        .group("laa-civil-manage-api")
        .pathsToExclude("/mock-access-data-store/**")
        .build();
  }

  @Bean
  public GroupedOpenApi mockAccessDataStoreApi() {
    return GroupedOpenApi.builder()
        .group("mock-access-data-store")
        .pathsToMatch("/mock-access-data-store/**")
        .build();
  }
}

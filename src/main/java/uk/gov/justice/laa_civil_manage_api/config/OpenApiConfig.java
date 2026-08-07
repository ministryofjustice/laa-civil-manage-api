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

  /**
   * The mock Access Data Store split per journey, so the Access Data Store team can generate each
   * contract independently rather than from one combined document.
   */
  @Bean
  public GroupedOpenApi mockAccessDataStoreSubmitPaApi() {
    return GroupedOpenApi.builder()
        .group("mock-access-data-store-submit-pa")
        .pathsToMatch("/mock-access-data-store/applications/**")
        .build();
  }

  @Bean
  public GroupedOpenApi mockAccessDataStoreDraftsApi() {
    return GroupedOpenApi.builder()
        .group("mock-access-data-store-drafts")
        .pathsToMatch("/mock-access-data-store/drafts", "/mock-access-data-store/drafts/**")
        .build();
  }
}

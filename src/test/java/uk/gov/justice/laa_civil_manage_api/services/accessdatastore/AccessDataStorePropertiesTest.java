package uk.gov.justice.laa_civil_manage_api.services.accessdatastore;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.validation.BindValidationException;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class AccessDataStorePropertiesTest {

  private final ApplicationContextRunner runner =
      new ApplicationContextRunner().withUserConfiguration(PropertiesConfig.class);

  @Test
  void bindsTheBaseUrl() {
    runner
        .withPropertyValues("laa-civil-manage-api.access-data-store.base-url=https://ads.test")
        .run(
            context ->
                assertThat(context.getBean(AccessDataStoreProperties.class).baseUrl())
                    .isEqualTo("https://ads.test"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "   "})
  void failsToStartWhenTheBaseUrlIsBlank(String baseUrl) {
    runner
        .withPropertyValues("laa-civil-manage-api.access-data-store.base-url=" + baseUrl)
        .run(
            context ->
                assertThat(context)
                    .getFailure()
                    .rootCause()
                    .isInstanceOf(BindValidationException.class)
                    .hasMessageContaining("baseUrl"));
  }

  @Test
  void failsToStartWhenTheBaseUrlIsMissing() {
    runner.run(
        context ->
            assertThat(context)
                .getFailure()
                .rootCause()
                .isInstanceOf(BindValidationException.class)
                .hasMessageContaining("baseUrl"));
  }

  @EnableConfigurationProperties(AccessDataStoreProperties.class)
  static class PropertiesConfig {}
}

package uk.gov.justice.laa_civil_manage_api.services.legalframework;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.validation.BindValidationException;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class LegalFrameworkPropertiesTest {

  private final ApplicationContextRunner runner =
      new ApplicationContextRunner().withUserConfiguration(PropertiesConfig.class);

  @Test
  void bindsTheBaseUrl() {
    runner
        .withPropertyValues("laa-civil-manage-api.legal-framework.base-url=https://lfa.test")
        .run(
            context ->
                assertThat(context.getBean(LegalFrameworkProperties.class).baseUrl())
                    .isEqualTo("https://lfa.test"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "   "})
  void failsToStartWhenTheBaseUrlIsBlank(String baseUrl) {
    runner
        .withPropertyValues("laa-civil-manage-api.legal-framework.base-url=" + baseUrl)
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

  @EnableConfigurationProperties(LegalFrameworkProperties.class)
  static class PropertiesConfig {}
}

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
                assertThat(context.getBean(LegalFrameworkProperties.class))
                    .satisfies(
                        properties -> {
                          assertThat(properties.baseUrl()).isEqualTo("https://lfa.test");
                          assertThat(properties.maxRetries()).isEqualTo(3);
                          assertThat(properties.initialBackoffMs()).isEqualTo(200);
                          assertThat(properties.backoffMultiplier()).isEqualTo(2.0);
                        }));
  }

  @Test
  void bindsCustomRetrySettings() {
    runner
        .withPropertyValues(
            "laa-civil-manage-api.legal-framework.base-url=https://lfa.test",
            "laa-civil-manage-api.legal-framework.max-retries=5",
            "laa-civil-manage-api.legal-framework.initial-backoff-ms=75",
            "laa-civil-manage-api.legal-framework.backoff-multiplier=1.5")
        .run(
            context ->
                assertThat(context.getBean(LegalFrameworkProperties.class))
                    .satisfies(
                        properties -> {
                          assertThat(properties.maxRetries()).isEqualTo(5);
                          assertThat(properties.initialBackoffMs()).isEqualTo(75);
                          assertThat(properties.backoffMultiplier()).isEqualTo(1.5);
                        }));
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

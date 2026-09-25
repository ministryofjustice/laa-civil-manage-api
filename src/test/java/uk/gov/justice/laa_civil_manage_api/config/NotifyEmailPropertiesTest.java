package uk.gov.justice.laa_civil_manage_api.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.validation.BindValidationException;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class NotifyEmailPropertiesTest {

  private static final String API_KEY = "laa-civil-manage-api.notify.api-key=";
  private static final String TEMPLATE_ID =
      "laa-civil-manage-api.notify.prior-authority-submitted-template-id=";

  private final ApplicationContextRunner runner =
      new ApplicationContextRunner().withUserConfiguration(PropertiesConfig.class);

  @Test
  void bindsTheApiKeyAndTemplateIdWithoutABaseUrl() {
    runner
        .withPropertyValues(API_KEY + "test-key", TEMPLATE_ID + "template-id")
        .run(
            context -> {
              NotifyEmailProperties properties = context.getBean(NotifyEmailProperties.class);
              assertThat(properties.apiKey()).isEqualTo("test-key");
              assertThat(properties.priorAuthoritySubmittedTemplateId()).isEqualTo("template-id");
              assertThat(properties.baseUrl()).isNull();
            });
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "   "})
  void failsToStartWhenTheApiKeyIsBlank(String apiKey) {
    runner
        .withPropertyValues(API_KEY + apiKey, TEMPLATE_ID + "template-id")
        .run(
            context ->
                assertThat(context)
                    .getFailure()
                    .rootCause()
                    .isInstanceOf(BindValidationException.class)
                    .hasMessageContaining("apiKey"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "   "})
  void failsToStartWhenTheTemplateIdIsBlank(String templateId) {
    runner
        .withPropertyValues(API_KEY + "test-key", TEMPLATE_ID + templateId)
        .run(
            context ->
                assertThat(context)
                    .getFailure()
                    .rootCause()
                    .isInstanceOf(BindValidationException.class)
                    .hasMessageContaining("priorAuthoritySubmittedTemplateId"));
  }

  @EnableConfigurationProperties(NotifyEmailProperties.class)
  static class PropertiesConfig {}
}

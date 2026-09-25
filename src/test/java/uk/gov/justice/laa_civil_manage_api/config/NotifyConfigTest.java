package uk.gov.justice.laa_civil_manage_api.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import uk.gov.justice.laa.civil.notify.service.GovUkNotifyEmailSender;
import uk.gov.justice.laa.civil.notify.service.NotifyEmailSender;

class NotifyConfigTest {

  private final ApplicationContextRunner runner =
      new ApplicationContextRunner()
          .withConfiguration(AutoConfigurations.of(PropertyPlaceholderAutoConfiguration.class))
          .withUserConfiguration(NotifyConfig.class);

  @Test
  void registersTheGovUkNotifyEmailSender() {
    runner
        .withPropertyValues(
            "laa-civil-manage-api.notify.api-key=test-key",
            "laa-civil-manage-api.notify.prior-authority-submitted-template-id=template-id")
        .run(
            context ->
                assertThat(context)
                    .getBean(NotifyEmailSender.class)
                    .isInstanceOf(GovUkNotifyEmailSender.class));
  }

  @Test
  void failsToStartRatherThanFallingBackToANoOpSenderWhenNotifyIsNotConfigured() {
    runner.run(context -> assertThat(context).hasFailed());
  }
}

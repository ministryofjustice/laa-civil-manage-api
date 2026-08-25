package uk.gov.justice.laa_civil_manage_api.config;

import java.util.concurrent.CompletableFuture;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import uk.gov.justice.laa.civil.notify.NotifyEmailSender;
import uk.gov.justice.laa.civil.notify.NotifySenderConfiguration;

@Configuration
public class NotifyConfig {

  @Configuration
  @Import(NotifySenderConfiguration.class)
  @ConditionalOnProperty(
      prefix = "laa-civil-manage-api.notify",
      name = {"api-key", "prior-authority-submitted-template-id", "recipient-email"})
  static class EnabledNotifyConfiguration {}

  @Bean
  @ConditionalOnMissingBean(NotifyEmailSender.class)
  public NotifyEmailSender noOpNotifyEmailSender() {
    return ignoredRequest -> CompletableFuture.completedFuture(null);
  }
}


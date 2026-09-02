package uk.gov.justice.laa.civil.notify.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;

import uk.gov.justice.laa.civil.notify.service.GovUkNotifyEmailSender;
import uk.gov.justice.laa.civil.notify.service.NotifyEmailSender;
import uk.gov.service.notify.NotificationClient;

@Configuration
@EnableAsync
@EnableRetry
public class NotifySenderConfiguration {


  @Bean
  public NotificationClient notificationClient(
      @Value("${laa-civil-manage-api.notify.api-key}") String apiKey,
      @Value("${laa-civil-manage-api.notify.base-url:}") String baseUrl) {
    return baseUrl == null || baseUrl.isBlank()
        ? new NotificationClient(apiKey)
        : new NotificationClient(apiKey, baseUrl);
  }

  @Bean
  public NotifyEmailSender notifyEmailSender(NotificationClient notificationClient) {
    return new GovUkNotifyEmailSender(notificationClient);
  }
}


package uk.gov.justice.laa.civil.notify.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;

import uk.gov.justice.laa.civil.notify.service.GovUkNotifyEmailSender;
import uk.gov.justice.laa.civil.notify.service.NotifyEmailSender;
import uk.gov.service.notify.NotificationClient;

@Slf4j
@Configuration
@EnableAsync
@EnableRetry
public class NotifySenderConfiguration {


  @Bean
  public NotificationClient notificationClient(
      @Value("${laa-civil-manage-api.notify.api-key}") String apiKey,
      @Value("${laa-civil-manage-api.notify.base-url:}") String baseUrl) {
    log.info("DEBUG: NotificationClient bean creation - apiKey={}, baseUrl={}", 
        apiKey != null && !apiKey.isEmpty() ? "***" : "NOT_SET", 
        baseUrl != null && !baseUrl.isEmpty() ? baseUrl : "EMPTY/NULL");
    
    NotificationClient client = baseUrl == null || baseUrl.isBlank()
        ? new NotificationClient(apiKey)
        : new NotificationClient(apiKey, baseUrl);
    
    log.info("DEBUG: NotificationClient created successfully");
    return client;
  }

  @Bean
  public NotifyEmailSender notifyEmailSender(NotificationClient notificationClient) {
    log.info("DEBUG: Creating GovUkNotifyEmailSender with NotificationClient");
    return new GovUkNotifyEmailSender(notificationClient);
  }
}



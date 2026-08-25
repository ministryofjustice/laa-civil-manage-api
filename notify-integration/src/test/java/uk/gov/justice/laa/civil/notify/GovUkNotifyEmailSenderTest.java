package uk.gov.justice.laa.civil.notify;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;

class GovUkNotifyEmailSenderTest {

  private static AnnotationConfigApplicationContext context;
  private static NotificationClient client;
  private static NotifyEmailSender sender;

  @BeforeAll
  static void initContext() {
    context = new AnnotationConfigApplicationContext();
    context
        .getEnvironment()
        .getSystemProperties()
        .putAll(
            Map.of(
                "notify.email.retry.max-attempts", "6",
                "notify.email.retry.delay-ms", "1",
                "notify.email.retry.multiplier", "1.0",
                "notify.email.retry.max-delay-ms", "1"));
    context.register(NotifyAsyncConfiguration.class, RetryTestConfig.class);
    context.refresh();
    client = context.getBean(NotificationClient.class);
    sender = context.getBean(NotifyEmailSender.class);
  }

  @AfterAll
  static void closeContext() {
    context.close();
  }

  @BeforeEach
  void resetClientMock() {
    reset(client);
  }

  @Test
  void delegatesToNotificationClient() throws NotificationClientException {
    SendEmailRequest request =
        new SendEmailRequest("template-123", "solicitor@example.com", Map.of("name", "Jane Doe"));
    when(client.sendEmail("template-123", "solicitor@example.com", Map.of("name", "Jane Doe"), null))
        .thenReturn(mock(SendEmailResponse.class));

    sender.sendEmail(request).join();

    verify(client).sendEmail("template-123", "solicitor@example.com", Map.of("name", "Jane Doe"), null);
  }

  @Test
  void retriesAndEventuallySucceeds() throws NotificationClientException {
    SendEmailRequest request =
        new SendEmailRequest("template-123", "solicitor@example.com", Map.of("name", "Jane Doe"));
    when(client.sendEmail("template-123", "solicitor@example.com", Map.of("name", "Jane Doe"), null))
        .thenThrow(new NotificationClientException("temporary error 1"))
        .thenThrow(new NotificationClientException("temporary error 2"))
        .thenReturn(mock(SendEmailResponse.class));

    sender.sendEmail(request).join();

    verify(client, times(3))
        .sendEmail("template-123", "solicitor@example.com", Map.of("name", "Jane Doe"), null);
  }

  @Test
  void wrapsNotificationClientExceptionInRuntimeExceptionAfterExhaustingRetries()
      throws NotificationClientException {
    SendEmailRequest request =
        new SendEmailRequest("template-123", "solicitor@example.com", Map.of());
    when(client.sendEmail("template-123", "solicitor@example.com", Map.of(), null))
        .thenThrow(new NotificationClientException("bad api key"));

    assertThrows(ExecutionException.class, () -> sender.sendEmail(request).get(2, TimeUnit.SECONDS));

    verify(client, times(6)).sendEmail("template-123", "solicitor@example.com", Map.of(), null);
  }

  @Configuration
  static class RetryTestConfig {

    @Bean
    NotificationClient notificationClient() {
      return mock(NotificationClient.class);
    }

    @Bean
    NotifyEmailSender notifyEmailSender(NotificationClient notificationClient) {
      return new GovUkNotifyEmailSender(notificationClient);
    }
  }
}

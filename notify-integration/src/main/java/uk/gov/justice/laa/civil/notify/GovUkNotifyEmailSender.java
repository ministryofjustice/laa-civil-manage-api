package uk.gov.justice.laa.civil.notify;

import java.util.concurrent.CompletableFuture;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

public class GovUkNotifyEmailSender implements NotifyEmailSender {

  private final NotificationClient client;

  public GovUkNotifyEmailSender(NotificationClient client) {
    this.client = client;
  }

  @Override
  @Async
  @Retryable(
      retryFor = NotifyEmailSendException.class,
      maxAttemptsExpression = "${notify.email.retry.max-attempts:6}",
      backoff =
          @Backoff(
              delayExpression = "${notify.email.retry.delay-ms:1000}",
              multiplierExpression = "${notify.email.retry.multiplier:2.0}",
              maxDelayExpression = "${notify.email.retry.max-delay-ms:16000}"))
  public CompletableFuture<Void> sendEmail(SendEmailRequest request) {
    try {
      client.sendEmail(request.templateId(), request.emailAddress(), request.personalisation(), null);
      return CompletableFuture.completedFuture(null);
    } catch (NotificationClientException e) {
      throw new NotifyEmailSendException(request, e);
    }
  }

  @Recover
  @SuppressWarnings("unused") // Invoked by Spring Retry via reflection when retries are exhausted.
  public CompletableFuture<Void> recover(RuntimeException exception, SendEmailRequest request) {
    CompletableFuture<Void> failedResult = new CompletableFuture<>();
    failedResult.completeExceptionally(
        new RuntimeException(
            "Failed to send email to %s using template %s after retries"
                .formatted(request.emailAddress(), request.templateId()),
            exception.getCause() == null ? exception : exception.getCause()));
    return failedResult;
  }

  private static class NotifyEmailSendException extends RuntimeException {
    NotifyEmailSendException(SendEmailRequest request, NotificationClientException cause) {
      super(
          "Failed to send email to %s using template %s"
              .formatted(request.emailAddress(), request.templateId()),
          cause);
    }
  }
}

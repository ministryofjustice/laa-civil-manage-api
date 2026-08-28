package uk.gov.justice.laa.civil.notify.service;

import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;

import uk.gov.justice.laa.civil.notify.model.SendEmailRequest;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

@Slf4j
public class GovUkNotifyEmailSender implements NotifyEmailSender {

  private final NotificationClient client;

  public GovUkNotifyEmailSender(NotificationClient client) {
    this.client = client;
    log.info("DEBUG: GovUkNotifyEmailSender initialized with client: {}", client);
  }

  @Override
  @Async
  @Retryable(
      retryFor = NotifyEmailSendException.class,
      maxAttemptsExpression = "${notify.email.retry.max-attempts:4}",
      backoff =
          @Backoff(
              delayExpression = "${notify.email.retry.delay-ms:1000}",
              multiplierExpression = "${notify.email.retry.multiplier:2.0}"
          )
        )
  public CompletableFuture<Void> sendEmail(SendEmailRequest request) {
    log.info("DEBUG: sendEmail called with request: {}", request);
    try {
      log.info("DEBUG: Calling client.sendEmail with templateId={}, emailAddress={}", 
          request.templateId(), request.emailAddress());
      client.sendEmail(request.templateId(), request.emailAddress(), request.personalisation(), null);
      log.info("DEBUG: Email sent successfully");
      return CompletableFuture.completedFuture(null);
    } catch (NotificationClientException e) {
      log.error("DEBUG: NotificationClientException caught: {}", e.getMessage(), e);
      throw new NotifyEmailSendException(request, e);
    }
  }

  @Recover
  @SuppressWarnings("unused") // Invoked by Spring Retry via reflection when retries are exhausted.
  public CompletableFuture<Void> recover(RuntimeException exception, SendEmailRequest request) {
    log.error("DEBUG: Recover called - email send failed for {}", request.emailAddress(), exception);
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

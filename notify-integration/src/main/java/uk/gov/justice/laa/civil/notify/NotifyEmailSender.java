package uk.gov.justice.laa.civil.notify;

import java.util.concurrent.CompletableFuture;

/** Sends emails via GOV.UK Notify. */
public interface NotifyEmailSender {

  /**
   * Sends an email using the given request parameters.
   *
   * @param request the email to send
   * @return a future completed when sending finishes (or exceptionally on failure)
   */
  CompletableFuture<Void> sendEmail(SendEmailRequest request);
}

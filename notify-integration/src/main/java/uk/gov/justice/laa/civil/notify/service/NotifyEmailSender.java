package uk.gov.justice.laa.civil.notify.service;

import java.util.concurrent.CompletableFuture;

import uk.gov.justice.laa.civil.notify.model.SendEmailRequest;

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

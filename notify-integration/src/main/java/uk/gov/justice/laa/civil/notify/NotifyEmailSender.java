package uk.gov.justice.laa.civil.notify;

/** Sends emails via GOV.UK Notify. */
public interface NotifyEmailSender {

  /**
   * Sends an email using the given request parameters.
   *
   * @param request the email to send
   * @throws RuntimeException if the send fails
   */
  void sendEmail(SendEmailRequest request);
}

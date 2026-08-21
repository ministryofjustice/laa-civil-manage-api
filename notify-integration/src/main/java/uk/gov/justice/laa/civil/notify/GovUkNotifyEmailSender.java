package uk.gov.justice.laa.civil.notify;

import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

public class GovUkNotifyEmailSender implements NotifyEmailSender {

  private final NotificationClient client;

  public GovUkNotifyEmailSender(NotificationClient client) {
    this.client = client;
  }

  @Override
  public void sendEmail(SendEmailRequest request) {
    try {
      client.sendEmail(
          request.templateId(), request.emailAddress(), request.personalisation(), null);
    } catch (NotificationClientException e) {
      throw new RuntimeException(
          "Failed to send email to %s using template %s"
              .formatted(request.emailAddress(), request.templateId()),
          e);
    }
  }
}

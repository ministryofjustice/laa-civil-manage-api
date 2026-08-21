package uk.gov.justice.laa.civil.notify;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import org.junit.jupiter.api.Test;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;

class GovUkNotifyEmailSenderTest {

  private final NotificationClient client = mock(NotificationClient.class);
  private final GovUkNotifyEmailSender sender = new GovUkNotifyEmailSender(client);

  @Test
  void delegatesToNotificationClient() throws NotificationClientException {
    SendEmailRequest request =
        new SendEmailRequest("template-123", "solicitor@example.com", Map.of("name", "Jane Doe"));
    when(client.sendEmail("template-123", "solicitor@example.com", Map.of("name", "Jane Doe"), null))
        .thenReturn(mock(SendEmailResponse.class));

    sender.sendEmail(request);

    verify(client).sendEmail("template-123", "solicitor@example.com", Map.of("name", "Jane Doe"), null);
  }

  @Test
  void wrapsNotificationClientExceptionInRuntimeException() throws NotificationClientException {
    SendEmailRequest request =
        new SendEmailRequest("template-123", "solicitor@example.com", Map.of());
    when(client.sendEmail("template-123", "solicitor@example.com", Map.of(), null))
        .thenThrow(new NotificationClientException("bad api key"));

    assertThrows(RuntimeException.class, () -> sender.sendEmail(request));
  }
}

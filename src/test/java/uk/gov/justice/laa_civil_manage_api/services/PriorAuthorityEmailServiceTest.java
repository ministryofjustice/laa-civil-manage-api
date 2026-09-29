package uk.gov.justice.laa_civil_manage_api.services;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.scheduling.annotation.Async;
import uk.gov.justice.laa.civil.notify.model.SendEmailRequest;
import uk.gov.justice.laa.civil.notify.service.NotifyEmailSender;
import uk.gov.justice.laa_civil_manage_api.config.NotifyEmailProperties;
import uk.gov.justice.laa_civil_manage_api.services.providerdetails.ProviderApiException;

class PriorAuthorityEmailServiceTest {

  private static final UUID PRIOR_AUTHORITY_ID = UUID.randomUUID();
  private static final String OFFICE_CODE = "0W839P";
  private static final String OFFICE_EMAIL = "office@example.com";
  private static final Map<String, Object> PERSONALISATION = Map.of("laaReference", "LAA123456");

  private final ProviderDetailsService providerDetailsService = mock(ProviderDetailsService.class);
  private final NotifyEmailSender notifyEmailSender = mock(NotifyEmailSender.class);
  private final NotifyEmailProperties notifyEmailProperties =
      new NotifyEmailProperties(
          "api-key", "https://api.notifications.service.gov.uk", "template-id");
  private final PriorAuthorityEmailService service =
      new PriorAuthorityEmailService(
          providerDetailsService, notifyEmailSender, notifyEmailProperties);

  @Test
  void sendSubmittedEmailRunsAsynchronously() throws NoSuchMethodException {
    assertNotNull(
        PriorAuthorityEmailService.class
            .getMethod("sendSubmittedEmail", UUID.class, String.class, Map.class)
            .getAnnotation(Async.class));
  }

  @Test
  void sendsSubmittedEmailToTheOfficeEmailFromProviderDetails() {
    when(providerDetailsService.getProviderEmail(OFFICE_CODE)).thenReturn(OFFICE_EMAIL);
    when(notifyEmailSender.sendEmail(any(SendEmailRequest.class)))
        .thenReturn(CompletableFuture.completedFuture(null));

    service.sendSubmittedEmail(PRIOR_AUTHORITY_ID, OFFICE_CODE, PERSONALISATION);

    ArgumentCaptor<SendEmailRequest> captor = ArgumentCaptor.forClass(SendEmailRequest.class);
    verify(notifyEmailSender).sendEmail(captor.capture());
    assertEquals(
        new SendEmailRequest("template-id", OFFICE_EMAIL, PERSONALISATION), captor.getValue());
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"  "})
  void doesNotLookUpOrSendWhenOfficeCodeIsMissing(String officeCode) {
    assertDoesNotThrow(
        () -> service.sendSubmittedEmail(PRIOR_AUTHORITY_ID, officeCode, PERSONALISATION));

    verify(providerDetailsService, never()).getProviderEmail(anyString());
    verify(notifyEmailSender, never()).sendEmail(any());
  }

  @Test
  void doesNotSendWhenProviderDetailsHasNoEmailForTheOffice() {
    when(providerDetailsService.getProviderEmail(OFFICE_CODE)).thenReturn(null);

    assertDoesNotThrow(
        () -> service.sendSubmittedEmail(PRIOR_AUTHORITY_ID, OFFICE_CODE, PERSONALISATION));

    verify(notifyEmailSender, never()).sendEmail(any());
  }

  @Test
  void swallowsProviderDetailsFailureAndDoesNotSend() {
    when(providerDetailsService.getProviderEmail(OFFICE_CODE))
        .thenThrow(new ProviderApiException("provider down", new RuntimeException()));

    assertDoesNotThrow(
        () -> service.sendSubmittedEmail(PRIOR_AUTHORITY_ID, OFFICE_CODE, PERSONALISATION));

    verify(notifyEmailSender, never()).sendEmail(any());
  }

  @Test
  void swallowsNotifySenderThrowingSynchronously() {
    when(providerDetailsService.getProviderEmail(OFFICE_CODE)).thenReturn(OFFICE_EMAIL);
    when(notifyEmailSender.sendEmail(any(SendEmailRequest.class)))
        .thenThrow(new RuntimeException("notify rejected"));

    assertDoesNotThrow(
        () -> service.sendSubmittedEmail(PRIOR_AUTHORITY_ID, OFFICE_CODE, PERSONALISATION));
  }

  @Test
  void swallowsNotifyFutureCompletingExceptionally() {
    when(providerDetailsService.getProviderEmail(OFFICE_CODE)).thenReturn(OFFICE_EMAIL);
    when(notifyEmailSender.sendEmail(any(SendEmailRequest.class)))
        .thenReturn(CompletableFuture.failedFuture(new RuntimeException("notify down")));

    assertDoesNotThrow(
        () -> service.sendSubmittedEmail(PRIOR_AUTHORITY_ID, OFFICE_CODE, PERSONALISATION));
  }
}

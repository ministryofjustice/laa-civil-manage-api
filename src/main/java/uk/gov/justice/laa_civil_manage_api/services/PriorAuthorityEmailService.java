package uk.gov.justice.laa_civil_manage_api.services;

import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uk.gov.justice.laa.civil.notify.model.SendEmailRequest;
import uk.gov.justice.laa.civil.notify.service.NotifyEmailSender;
import uk.gov.justice.laa_civil_manage_api.config.NotifyEmailProperties;

@Slf4j
@Service
@RequiredArgsConstructor
public class PriorAuthorityEmailService {

  private final ProviderDetailsService providerDetailsService;
  private final NotifyEmailSender notifyEmailSender;
  private final NotifyEmailProperties notifyEmailProperties;

  @Async
  public void sendSubmittedEmail(
      UUID priorAuthorityId, String officeCode, Map<String, Object> personalisation) {
    if (!StringUtils.hasText(officeCode)) {
      log.warn(
          "No office code for prior authority, skipping submission email: priorAuthorityId={}",
          priorAuthorityId);
      return;
    }

    try {
      String officeEmail = providerDetailsService.getProviderEmail(officeCode);
      if (!StringUtils.hasText(officeEmail)) {
        log.warn(
            "No email address for office, skipping submission email: priorAuthorityId={},"
                + " officeCode={}",
            priorAuthorityId,
            officeCode);
        return;
      }

      notifyEmailSender
          .sendEmail(
              new SendEmailRequest(
                  notifyEmailProperties.priorAuthoritySubmittedTemplateId(),
                  officeEmail,
                  personalisation))
          .exceptionally(
              throwable -> {
                log.error(
                    "Failed to send prior authority submission email: priorAuthorityId={}",
                    priorAuthorityId,
                    throwable);
                return null;
              });
    } catch (RuntimeException ex) {
      log.error(
          "Failed to send prior authority submission email: priorAuthorityId={}, officeCode={}",
          priorAuthorityId,
          officeCode,
          ex);
    }
  }
}

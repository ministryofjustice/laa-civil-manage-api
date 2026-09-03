package uk.gov.justice.laa_civil_manage_api.services;

import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.justice.laa.civil.notify.model.SendEmailRequest;
import uk.gov.justice.laa.civil.notify.service.NotifyEmailSender;
import uk.gov.justice.laa_civil_manage_api.config.NotifyEmailProperties;
import uk.gov.justice.laa_civil_manage_api.models.ExpertCosts;
import uk.gov.justice.laa_civil_manage_api.models.ExpertDetails;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthority;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthorityApplicationResponse;
import uk.gov.justice.laa_civil_manage_api.models.UploadedDocument;
import uk.gov.justice.laa_civil_manage_api.services.accessdatastore.AccessDataStoreClient;
import uk.gov.justice.laa_civil_manage_api.validation.PdfUploadValidator;

@Slf4j
@Service
@RequiredArgsConstructor
public class PriorAuthorityService {
  private static final DateTimeFormatter SUBMITTED_AT_FORMATTER =
      DateTimeFormatter.ofPattern("d MMMM yyyy, h:mm a", Locale.UK);

  private final AccessDataStoreClient accessDataStoreClient;
  private final NotifyEmailSender notifyEmailSender;
  private final NotifyEmailProperties notifyEmailProperties;
  private final PdfUploadValidator pdfUploadValidator;

  public PriorAuthorityApplicationResponse submit(PriorAuthority priorAuthority) {
    int documentCount =
        priorAuthority.uploadedDocuments() == null ? 0 : priorAuthority.uploadedDocuments().size();
    ExpertDetails expertDetails = priorAuthority.expertDetails();
    ExpertCosts expertCosts = expertDetails == null ? null : expertDetails.expertCosts();
    log.info(
        "Submitting prior authority: applicationId={}, priorAuthorityType={}, expertType={}, billingType={}, documentCount={}",
        priorAuthority.applicationId(),
        priorAuthority.priorAuthorityType(),
        expertDetails == null ? null : expertDetails.expertType(),
        expertCosts == null ? null : expertCosts.billingType(),
        documentCount);

    PriorAuthorityApplicationResponse response =
        accessDataStoreClient.submitPriorAuthority(priorAuthority);

    if (notifyEmailProperties.enabled()) {
      triggerSubmittedEmail(priorAuthority, response);
    }

    log.info("Prior authority submitted: applicationId={}", priorAuthority.applicationId());
    return response;
  }

  private void triggerSubmittedEmail(
      PriorAuthority priorAuthority, PriorAuthorityApplicationResponse response) {

    SendEmailRequest emailRequest =
        new SendEmailRequest(
            notifyEmailProperties.priorAuthoritySubmittedTemplateId(),
            notifyEmailProperties.recipientEmail(),
            Map.of(
                "priorAuthorityReference", response.submissionId(),
                "laaReference", priorAuthority.laaReference(),
                "priorAuthorityType", priorAuthority.priorAuthorityType().getDisplayName(),
                "submittedAt", response.submittedAt().format(SUBMITTED_AT_FORMATTER)));

    notifyEmailSender
        .sendEmail(emailRequest)
        .exceptionally(
            throwable -> {
              log.error(
                  "Failed to send prior authority submission email: applicationId={}, submissionId={}",
                  priorAuthority.applicationId(),
                  response.submissionId(),
                  throwable);
              return null;
            });
  }

  public UploadedDocument uploadDocument(MultipartFile file) {
    String sanitizedFilename = pdfUploadValidator.validate(file);

    log.info(
        "Received document upload: filename={}, contentType={}",
        sanitizedFilename,
        file.getContentType());

    return UploadedDocument.builder()
        .fileName(sanitizedFilename)
        .hostedUrl("https://example.com/" + sanitizedFilename)
        .build();
  }
}

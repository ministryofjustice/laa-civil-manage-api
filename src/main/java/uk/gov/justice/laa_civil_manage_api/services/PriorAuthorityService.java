package uk.gov.justice.laa_civil_manage_api.services;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.justice.laa.civil.notify.model.SendEmailRequest;
import uk.gov.justice.laa.civil.notify.service.NotifyEmailSender;
import uk.gov.justice.laa_civil_manage_api.config.NotifyEmailProperties;
import uk.gov.justice.laa_civil_manage_api.models.ExpertCosts;
import uk.gov.justice.laa_civil_manage_api.models.ExpertDetails;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthority;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthorityApplicationResponse;
import uk.gov.justice.laa_civil_manage_api.models.UploadedDocument;
import uk.gov.justice.laa_civil_manage_api.services.accessdatastore.AccessDataStoreClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class PriorAuthorityService {
  private static final List<String> ALLOWED_FILE_EXTENSIONS =
      List.of("pdf", "doc", "docx", "odt", "rtf", "jpeg", "jpg", "png", "bmp", "tiff", "tif");
  private static final DateTimeFormatter SUBMITTED_AT_FORMATTER =
      DateTimeFormatter.ofPattern("d MMMM yyyy, h:mm a", Locale.UK);

  private final AccessDataStoreClient accessDataStoreClient;
  private final NotifyEmailSender notifyEmailSender;
  private final NotifyEmailProperties notifyEmailProperties;

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
            Map.of("priorAuthorityReference", response.submissionId(),
                   "laaReference", priorAuthority.applicationId(),
                   "priorAuthorityType", priorAuthority.priorAuthorityType(),
                   "submittedAt", response.submittedAt().format(SUBMITTED_AT_FORMATTER)
                ));

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
    if (file == null || file.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "file must not be empty");
    }

    String originalFilename = file.getOriginalFilename();
    if (!StringUtils.hasText(originalFilename)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "file name must not be empty");
    }

    // TODO - cleanPath is not adequate protection against path-traversal attacks - we must save the
    // file with a different name to that provided by the user
    String sanitizedFilename = StringUtils.getFilename(StringUtils.cleanPath(originalFilename));
    if (!StringUtils.hasText(sanitizedFilename)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "file name must not be empty");
    }

    String extension = StringUtils.getFilenameExtension(sanitizedFilename);
    String normalizedExtension = extension == null ? "" : extension.toLowerCase(Locale.ROOT);
    if (!ALLOWED_FILE_EXTENSIONS.contains(normalizedExtension)) {
      throw new ResponseStatusException(
          HttpStatus.UNSUPPORTED_MEDIA_TYPE,
          "unsupported file type; allowed: "
              + String.join(", ", ALLOWED_FILE_EXTENSIONS).toUpperCase(Locale.ROOT));
    }

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

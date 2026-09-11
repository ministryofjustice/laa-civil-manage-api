package uk.gov.justice.laa_civil_manage_api.services;

import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.justice.laa.civil.notify.model.SendEmailRequest;
import uk.gov.justice.laa.civil.notify.service.NotifyEmailSender;
import uk.gov.justice.laa_civil_manage_api.config.NotifyEmailProperties;
import uk.gov.justice.laa_civil_manage_api.models.ApplicationSummary;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthorityApplicationResponse;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthorityDocumentType;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthorityDraft;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthorityResponse;
import uk.gov.justice.laa_civil_manage_api.models.UploadedDocument;
import uk.gov.justice.laa_civil_manage_api.services.accessdatastore.AccessDataStoreClient;
import uk.gov.justice.laa_civil_manage_api.services.accessdatastore.CreatePriorAuthorityDraftRequest;
import uk.gov.justice.laa_civil_manage_api.services.accessdatastore.PriorAuthorityIdResponse;
import uk.gov.justice.laa_civil_manage_api.services.accessdatastore.PriorAuthorityRecordResponse;
import uk.gov.justice.laa_civil_manage_api.services.accessdatastore.SavePriorAuthorityDraftRequest;
import uk.gov.justice.laa_civil_manage_api.services.accessdatastore.SubmitPriorAuthorityDraftResponse;
import uk.gov.justice.laa_civil_manage_api.services.accessdatastore.UploadPriorAuthorityDocumentResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class PriorAuthorityService {
  private static final List<String> ALLOWED_FILE_EXTENSIONS = List.of("pdf");
  private static final String EXPECTED_MEDIA_TYPE = "application/pdf";
  private static final byte[] PDF_MAGIC_BYTES = {0x25, 0x50, 0x44, 0x46}; // "%PDF"
  private static final int MAX_FILENAME_LENGTH = 255;
  private static final long MAX_FILE_SIZE_BYTES = 10L * 1024 * 1024;
  private static final Tika TIKA = new Tika();
  private static final DateTimeFormatter SUBMITTED_AT_FORMATTER =
      DateTimeFormatter.ofPattern("d MMMM yyyy, h:mm a", Locale.UK);

  private final AccessDataStoreClient accessDataStoreClient;
  private final NotifyEmailSender notifyEmailSender;
  private final NotifyEmailProperties notifyEmailProperties;

  public UUID createDraft(PriorAuthorityDraft draft) {
    log.info(
        "Creating prior authority draft: applicationId={}, priorAuthorityType={}",
        draft.applicationId(),
        draft.priorAuthorityType());

    PriorAuthorityIdResponse response =
        accessDataStoreClient.createPriorAuthorityDraft(
            CreatePriorAuthorityDraftRequest.from(draft));

    log.info(
        "Created prior authority draft: priorAuthorityId={}, applicationId={}",
        response.priorAuthorityId(),
        draft.applicationId());
    return response.priorAuthorityId();
  }

  public void updateDraft(UUID priorAuthorityId, PriorAuthorityDraft draft) {
    log.info(
        "Updating prior authority draft: priorAuthorityId={}, applicationId={}",
        priorAuthorityId,
        draft.applicationId());
    accessDataStoreClient.updatePriorAuthorityDraft(
        priorAuthorityId, SavePriorAuthorityDraftRequest.from(draft));
  }

  public Optional<PriorAuthorityResponse> get(UUID priorAuthorityId) {
    log.info("Get prior authority: priorAuthorityId={}", priorAuthorityId);
    Optional<PriorAuthorityRecordResponse> record =
        accessDataStoreClient.getPriorAuthority(priorAuthorityId);
    record.ifPresentOrElse(
        r -> log.info("Found prior authority: priorAuthorityId={}", r.priorAuthorityId()),
        () -> log.info("No prior authority found for priorAuthorityId={}", priorAuthorityId));
    return record.map(this::toSummary);
  }

  public PriorAuthorityApplicationResponse submit(UUID priorAuthorityId) {
    log.info("Submitting prior authority: priorAuthorityId={}", priorAuthorityId);

    SubmitPriorAuthorityDraftResponse submitResponse =
        accessDataStoreClient.submitPriorAuthority(priorAuthorityId);

    PriorAuthorityApplicationResponse response =
        PriorAuthorityApplicationResponse.builder()
            .priorAuthorityId(submitResponse.priorAuthorityId())
            .submittedAt(submitResponse.submittedAt())
            .build();

    if (notifyEmailProperties.enabled()) {
      accessDataStoreClient
          .getPriorAuthority(priorAuthorityId)
          .ifPresent(record -> triggerSubmittedEmail(record, response));
    }

    log.info("Prior authority submitted: priorAuthorityId={}", priorAuthorityId);
    return response;
  }

  private void triggerSubmittedEmail(
      PriorAuthorityRecordResponse record, PriorAuthorityApplicationResponse response) {

    ApplicationSummary app = accessDataStoreClient.getApplicationById(record.applicationId());

    SendEmailRequest emailRequest =
        new SendEmailRequest(
            notifyEmailProperties.priorAuthoritySubmittedTemplateId(),
            notifyEmailProperties.recipientEmail(),
            Map.of(
                "priorAuthorityReference", response.priorAuthorityId(),
                "laaReference", app.laaReference(),
                "priorAuthorityType", record.priorAuthorityType().getDisplayName(),
                "submittedAt", response.submittedAt().format(SUBMITTED_AT_FORMATTER)));

    notifyEmailSender
        .sendEmail(emailRequest)
        .exceptionally(
            throwable -> {
              log.error(
                  "Failed to send prior authority submission email: priorAuthorityId={}",
                  record.priorAuthorityId(),
                  throwable);
              return null;
            });
  }

  private PriorAuthorityResponse toSummary(PriorAuthorityRecordResponse record) {
    PriorAuthorityDraft draft =
        PriorAuthorityDraft.builder()
            .applicationId(record.applicationId())
            .priorAuthorityType(record.priorAuthorityType())
            .justification(record.justification())
            .expertDetails(record.expertDetails())
            .counselDetails(record.counselDetails())
            .disbursementDetails(record.disbursementDetails())
            .build();
    return PriorAuthorityResponse.builder()
        .priorAuthorityId(record.priorAuthorityId())
        .status(record.status())
        .draft(draft)
        .build();
  }

  public UploadedDocument uploadDocument(
      UUID priorAuthorityId, PriorAuthorityDocumentType documentType, MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "file must not be empty");
    }

    if (file.getSize() > MAX_FILE_SIZE_BYTES) {
      throw new ResponseStatusException(
          HttpStatus.CONTENT_TOO_LARGE, "file size must not exceed 10MB");
    }

    String originalFilename = file.getOriginalFilename();
    if (!StringUtils.hasText(originalFilename)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "file name must not be empty");
    }

    String nullByteFreeFilename = originalFilename.replace("\0", "").replaceAll("(?i)%00", "");

    String sanitizedFilename = StringUtils.getFilename(StringUtils.cleanPath(nullByteFreeFilename));
    if (!StringUtils.hasText(sanitizedFilename)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "file name must not be empty");
    }

    if (sanitizedFilename.length() > MAX_FILENAME_LENGTH) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "file name must not exceed " + MAX_FILENAME_LENGTH + " characters");
    }

    if (sanitizedFilename.chars().filter(character -> character == '.').count() > 1) {
      throw new ResponseStatusException(
          HttpStatus.UNSUPPORTED_MEDIA_TYPE, "file name must contain a single extension only");
    }

    String extension = StringUtils.getFilenameExtension(sanitizedFilename);
    String normalizedExtension = extension == null ? "" : extension.toLowerCase(Locale.ROOT);
    if (!ALLOWED_FILE_EXTENSIONS.contains(normalizedExtension)) {
      throw new ResponseStatusException(
          HttpStatus.UNSUPPORTED_MEDIA_TYPE,
          "unsupported file type; allowed: "
              + String.join(", ", ALLOWED_FILE_EXTENSIONS).toUpperCase(Locale.ROOT));
    }

    byte[] header = readHeaderBytes(file);
    if (!Arrays.equals(header, PDF_MAGIC_BYTES)) {
      throw new ResponseStatusException(
          HttpStatus.UNSUPPORTED_MEDIA_TYPE, "file content does not match the PDF file signature");
    }

    if (!EXPECTED_MEDIA_TYPE.equals(detectMediaType(file, sanitizedFilename))) {
      throw new ResponseStatusException(
          HttpStatus.UNSUPPORTED_MEDIA_TYPE, "file content does not match a valid PDF media type");
    }

    log.info(
        "Uploading document for prior authority: priorAuthorityId={}, filename={}, contentType={}",
        priorAuthorityId,
        sanitizedFilename,
        file.getContentType());

    UploadPriorAuthorityDocumentResponse response =
        accessDataStoreClient.uploadPriorAuthorityDocument(priorAuthorityId, documentType, file);

    return UploadedDocument.builder()
        .documentId(response.documentId())
        .documentType(response.documentType())
        .fileName(response.fileName() != null ? response.fileName() : sanitizedFilename)
        .fileType(response.fileType())
        .contentType(response.contentType())
        .size(response.size() != null ? response.size() : file.getSize())
        .uploadedAt(response.uploadedAt() != null ? response.uploadedAt() : OffsetDateTime.now())
        .sourceService(response.sourceService())
        .checksum(response.checksum())
        .build();
  }

  private byte[] readHeaderBytes(MultipartFile file) {
    try (InputStream inputStream = file.getInputStream()) {
      return inputStream.readNBytes(PDF_MAGIC_BYTES.length);
    } catch (IOException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "unable to read uploaded file", e);
    }
  }

  private String detectMediaType(MultipartFile file, String filename) {
    try (InputStream inputStream = file.getInputStream()) {
      return TIKA.detect(inputStream, filename);
    } catch (IOException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "unable to read uploaded file", e);
    }
  }
}

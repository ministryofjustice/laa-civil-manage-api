package uk.gov.justice.laa_civil_manage_api.services;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthorityApplicationResponse;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthorityDocumentType;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthorityDocumentTypeUpdateResponse;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthorityDraft;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthorityResponse;
import uk.gov.justice.laa_civil_manage_api.models.UploadedDocument;
import uk.gov.justice.laa_civil_manage_api.services.accessdatastore.AccessDataStoreApplication;
import uk.gov.justice.laa_civil_manage_api.services.accessdatastore.AccessDataStoreClient;
import uk.gov.justice.laa_civil_manage_api.services.accessdatastore.CreatePriorAuthorityDraftRequest;
import uk.gov.justice.laa_civil_manage_api.services.accessdatastore.DocumentTypeUpdateResponse;
import uk.gov.justice.laa_civil_manage_api.services.accessdatastore.PriorAuthorityIdResponse;
import uk.gov.justice.laa_civil_manage_api.services.accessdatastore.PriorAuthorityRecordResponse;
import uk.gov.justice.laa_civil_manage_api.services.accessdatastore.SavePriorAuthorityDraftRequest;
import uk.gov.justice.laa_civil_manage_api.services.accessdatastore.SubmitPriorAuthorityDraftResponse;
import uk.gov.justice.laa_civil_manage_api.services.accessdatastore.UploadPriorAuthorityDocumentResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class PriorAuthorityService {
  private static final DateTimeFormatter SUBMITTED_AT_FORMATTER =
      DateTimeFormatter.ofPattern("d MMMM yyyy, h:mm a", Locale.UK);

  private final AccessDataStoreClient accessDataStoreClient;
  private final DocumentValidationService documentValidationService;
  private final PriorAuthorityEmailService priorAuthorityEmailService;

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

    triggerSubmittedEmail(priorAuthorityId, response);

    log.info("Prior authority submitted: priorAuthorityId={}", priorAuthorityId);
    return response;
  }

  private void triggerSubmittedEmail(
      UUID priorAuthorityId, PriorAuthorityApplicationResponse response) {
    try {
      Optional<PriorAuthorityRecordResponse> record =
          accessDataStoreClient.getPriorAuthority(priorAuthorityId);
      if (record.isEmpty()) {
        log.warn(
            "Prior authority not found, skipping submission email: priorAuthorityId={}",
            priorAuthorityId);
        return;
      }

      AccessDataStoreApplication app =
          accessDataStoreClient.getApplicationById(record.get().applicationId());
      if (app == null) {
        log.warn(
            "Application not found, skipping submission email: priorAuthorityId={},"
                + " applicationId={}",
            priorAuthorityId,
            record.get().applicationId());
        return;
      }

      String officeCode = app.provider() == null ? null : app.provider().officeCode();
      priorAuthorityEmailService.sendSubmittedEmail(
          priorAuthorityId,
          officeCode,
          Map.of(
              "priorAuthorityReference", response.priorAuthorityId(),
              "laaReference", app.laaReference(),
              "priorAuthorityType", record.get().priorAuthorityType().getDisplayName(),
              "submittedAt", response.submittedAt().format(SUBMITTED_AT_FORMATTER)));
    } catch (RuntimeException ex) {
      log.error(
          "Failed to trigger prior authority submission email: priorAuthorityId={}",
          priorAuthorityId,
          ex);
    }
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
        .uploadedDocuments(
            record.uploadedDocuments() != null ? record.uploadedDocuments() : List.of())
        .build();
  }

  public UploadedDocument uploadDocument(UUID priorAuthorityId, MultipartFile file) {
    String sanitizedFilename = documentValidationService.validateAndSanitize(file);

    log.info(
        "Uploading document for prior authority: priorAuthorityId={}, filename={}, contentType={}",
        priorAuthorityId,
        sanitizedFilename,
        file.getContentType());

    UploadPriorAuthorityDocumentResponse response =
        accessDataStoreClient.uploadPriorAuthorityDocument(priorAuthorityId, file);

    return UploadedDocument.builder()
        .documentId(response.documentId())
        .fileName(response.fileName() != null ? response.fileName() : sanitizedFilename)
        .fileType(response.fileType())
        .mediaType(response.contentType())
        .size(response.size() != null ? response.size() : file.getSize())
        .uploadedAt(response.uploadedAt() != null ? response.uploadedAt() : OffsetDateTime.now())
        .sourceService(response.sourceService())
        .checksum(response.checksum())
        .build();
  }

  public PriorAuthorityDocumentTypeUpdateResponse updateDocumentType(
      UUID priorAuthorityId, UUID documentId, PriorAuthorityDocumentType documentType) {
    log.info(
        "Updating document category: priorAuthorityId={}, documentId={}, documentType={}",
        priorAuthorityId,
        documentId,
        documentType);

    DocumentTypeUpdateResponse response =
        accessDataStoreClient.updatePriorAuthorityDocumentType(
            priorAuthorityId, documentId, documentType);

    return PriorAuthorityDocumentTypeUpdateResponse.builder()
        .documentId(response.documentId())
        .updatedAt(response.updatedAt())
        .build();
  }
}

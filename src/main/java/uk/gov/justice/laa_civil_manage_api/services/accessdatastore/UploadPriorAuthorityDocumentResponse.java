package uk.gov.justice.laa_civil_manage_api.services.accessdatastore;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UploadPriorAuthorityDocumentResponse(
    UUID documentId,
    String fileName,
    String fileType,
    String contentType,
    Long size,
    OffsetDateTime uploadedAt,
    String sourceService,
    String checksum) {}

package uk.gov.justice.laa_civil_manage_api.services.accessdatastore;

import java.time.OffsetDateTime;
import java.util.UUID;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthorityDocumentType;

/**
 * Response body returned by the Access Data Store when a supporting document is uploaded against a
 * prior authority.
 *
 * @param documentId the identifier assigned to the uploaded document
 */
public record UploadPriorAuthorityDocumentResponse(
    UUID documentId,
    PriorAuthorityDocumentType documentType,
    String fileName,
    String fileType,
    String contentType,
    Long size,
    OffsetDateTime uploadedAt,
    String sourceService,
    String checksum) {}

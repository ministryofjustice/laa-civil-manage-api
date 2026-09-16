package uk.gov.justice.laa_civil_manage_api.models;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Builder;

@Schema(description = "Confirmation returned after a document's category has been updated.")
@Builder
public record PriorAuthorityDocumentTypeUpdateResponse(
    @Schema(
            description = "Identifier of the document that was updated.",
            example = "c3b07e24-d92b-410a-9d95-88f117a12b43")
        UUID documentId,
    @Schema(
            description = "Timestamp the category update was recorded by the Access Data Store.",
            example = "2026-05-22T10:00:00Z")
        OffsetDateTime updatedAt) {}

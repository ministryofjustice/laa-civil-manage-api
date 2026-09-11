package uk.gov.justice.laa_civil_manage_api.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Builder;

@Schema(description = "A document uploaded as supporting evidence for a prior-authority request.")
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UploadedDocument(
    @Schema(
            description = "Identifier assigned to the uploaded document by the Access Data Store.",
            example = "c3b07e24-d92b-410a-9d95-88f117a12b43")
        UUID documentId,
    @Schema(description = "Type of document that was uploaded.", example = "GATEWAY_EVIDENCE")
        PriorAuthorityDocumentType documentType,
    @Schema(
            description = "Filename of the uploaded document.",
            example = "abc123.pdf",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String fileName,
    @Schema(description = "File extension/type of the uploaded document.", example = "pdf")
        String fileType,
    @Schema(
            description = "MIME content type of the uploaded document.",
            example = "application/pdf")
        String contentType,
    String hostedUrl,
    @Schema(description = "Size of the uploaded document, in bytes.", example = "10240") Long size,
    @Schema(description = "Timestamp the document was uploaded.", example = "2026-05-22T10:00:00Z")
        OffsetDateTime uploadedAt,
    @Schema(
            description = "The service that uploaded the document to the Access Data Store.",
            example = "CIVIL_APPLY")
        String sourceService,
    @Schema(description = "Checksum of the uploaded document contents.") String checksum) {}

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
    @Schema(
            description = "Filename of the uploaded document.",
            example = "abc123.pdf",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String fileName,
    String hostedUrl,
    @Schema(description = "Size of the uploaded document, in bytes.", example = "10240") Long size,
    @Schema(description = "Timestamp the document was uploaded.", example = "2026-05-22T10:00:00Z")
        OffsetDateTime uploadedAt) {}

package uk.gov.justice.laa_civil_manage_api.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Schema(description = "A document uploaded as supporting evidence for a prior-authority request.")
@Builder
public record UploadedDocument(
    @Schema(
            description = "Filename of the uploaded document.",
            example = "abc123.pdf",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String fileName,
    @NotBlank Long fileSize,
    @NotBlank String hostedUrl) {}

package uk.gov.justice.laa_civil_manage_api.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(
    description =
        "Request body used to assign a category to a previously uploaded prior-authority "
            + "document.")
public record UpdatePriorAuthorityDocumentTypeRequest(
    @Schema(
            description = "Type of document being categorised.",
            example = "GATEWAY_EVIDENCE",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        PriorAuthorityDocumentType documentType) {}

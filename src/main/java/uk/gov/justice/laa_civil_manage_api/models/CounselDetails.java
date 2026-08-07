package uk.gov.justice.laa_civil_manage_api.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Schema(description = "The counsel requested. Required when priorAuthorityType is COUNSEL.")
@Builder
public record CounselDetails(
    @Schema(
            description = "Type of counsel being applied for.",
            example = "KINGS_COUNSEL_ALONE",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        CounselType counselType) {}

package uk.gov.justice.laa_civil_manage_api.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Schema(description = "The amount of the expert's time being requested.")
@Builder
public record TimeRequested(
    @Schema(
            description = "Whole hours requested.",
            example = "2",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        @Min(0)
        Integer hours,
    @Schema(
            description = "Additional minutes requested.",
            example = "30",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        @Min(0)
        @Max(59)
        Integer minutes) {}

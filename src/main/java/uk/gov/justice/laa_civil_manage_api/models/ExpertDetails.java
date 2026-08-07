package uk.gov.justice.laa_civil_manage_api.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Schema(description = "The expert and what they will cost.")
@Builder
public record ExpertDetails(
    @Schema(
            description = "The expert type.",
            example = "Psychologist",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String expertType,
    @Schema(
            description = "Full name of the expert the prior authority is for.",
            example = "Dr John Doe",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String expertFullName,
    @Schema(
            description = "Primary business postcode of the expert, for regional rate mapping.",
            example = "SW1H 9AJ",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String expertPostcode,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) @NotNull @Valid ExpertCosts expertCosts) {}

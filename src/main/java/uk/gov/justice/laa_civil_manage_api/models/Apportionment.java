package uk.gov.justice.laa_civil_manage_api.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.Builder;

@Schema(
    description =
        "How the expert's costs are shared with other parties. Present when "
            + "costsSharedWithOtherParties is true; omitted when it is false.")
@Builder
public record Apportionment(
    @Schema(
            description = "Total number of parties sharing the costs, including the client.",
            example = "4",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        @Min(2)
        Integer partiesSharingCosts,
    @Schema(
            description = "The client's share of the costs, in GBP.",
            example = "31.25",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        @Positive
        BigDecimal clientShareAmount) {}

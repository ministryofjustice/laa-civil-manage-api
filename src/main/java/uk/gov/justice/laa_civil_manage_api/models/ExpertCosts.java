package uk.gov.justice.laa_civil_manage_api.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.Builder;

@Schema(
    description =
        "What the expert will cost, how they will be billed, and how the costs are shared.")
@Builder
public record ExpertCosts(
    @Schema(
            description = "Whether the expert is billed hourly or at a flat rate.",
            example = "HOURLY",
            requiredMode = Schema.RequiredMode.REQUIRED)
        BillingType billingType,
    @Schema(
            description = "Hourly rate in GBP. Required when billingType is HOURLY.",
            example = "50.00")
        @Positive
        BigDecimal hourlyRate,
    @Schema(description = "Time requested. Required when billingType is HOURLY.") @Valid
        TimeRequested timeRequested,
    @Schema(
            description =
                "Amount requested in GBP. For HOURLY billing this is hourlyRate x (hours + minutes / 60); "
                    + "for FIXED_RATE it is the flat fee entered by the provider.",
            example = "125.00",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @Positive
        BigDecimal totalAmount,
    @Schema(
            description = "Whether the expert's costs are shared with other parties.",
            example = "true",
            requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean costsSharedWithOtherParties,
    @Schema(
            description =
                "How the costs are split. Required when costsSharedWithOtherParties is true; omitted when false.")
        @Valid
        Apportionment apportionment) {}

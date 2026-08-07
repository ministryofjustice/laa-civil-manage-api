package uk.gov.justice.laa_civil_manage_api.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.Builder;

@Schema(
    description = "The disbursement requested. Required when priorAuthorityType is DISBURSEMENT.")
@Builder
public record DisbursementDetails(
    @Schema(
            description = "What the disbursement is for.",
            example = "Travel",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String disbursementPurpose,
    @Schema(
            description = "Cost of the disbursement in GBP, exclusive of VAT.",
            example = "125.50",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        @Positive
        BigDecimal disbursementAmount) {}

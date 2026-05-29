package uk.gov.justice.laa_civil_manage_api.models;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

@Schema(description = "A prior-authority request submitted by a legal aid provider against an existing application.")
@Builder
public record PriorAuthority(

        @Schema(
                description = "ID of the application this prior-authority request is associated with.",
                example = "5f1b2c3d-1111-2222-3333-444455556666",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull UUID applicationId,

        @Schema(
                description = "The category of prior authority being requested.",
                example = "EXPERT",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull PriorAuthorityType type,

        @Schema(
                description = "The expert type.",
                example = "Psychologist"
        )
        String expertType,

        @Schema(
                description = "Full name of the expert the prior authority is for.",
                example = "John Doe",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank String expertFullName,

        @Schema(
                description = "Boolean flag to indicate whether the expert is based inside (true) or outside (false) London",
                example = "true",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull Boolean isInLondon,

        @Schema(description = "Supporting documents uploaded with the request.")
        @Valid List<UploadedDocument> uploadedDocuments,

        @Schema(
                description = "True if the requested rates exceed the published LAA guideline rates.",
                example = "false"
        )
        boolean guidelineRatesExceeded,

        @Schema(
                description = "How the work will be billed.",
                example = "HOURLY",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull BillingType billingType,

        @Schema(
                description = "Hourly rate in GBP. Required when billingType is HOURLY.",
                example = "50.00"
        )
        @Positive BigDecimal hourlyRate,

        @Schema(description = "Estimated time the work will take. Required when billingType is HOURLY.")
        @Valid EstimatedTime estimatedTime,

        @Schema(
                description = "Total amount in GBP for hourly billing (hourlyRate * estimatedTime). Required when billingType is HOURLY.",
                example = "125.00"
        )
        @Positive BigDecimal totalAmount,

        @Schema(
                description = "Total flat-rate amount in GBP. Required when billingType is FLAT_RATE.",
                example = "249.99"
        )
        @Positive BigDecimal flatRateTotalAmount
) {

    @AssertTrue(message = "hourlyRate, estimatedTime, and totalAmount are required when billingType is HOURLY")
    @Schema(hidden = true)
    public boolean isHourlyFieldsConsistent() {
        if (billingType != BillingType.HOURLY) {
            return true;
        }
        return hourlyRate != null
                && estimatedTime != null
                && totalAmount != null;
    }

    @AssertTrue(message = "flatRateTotalAmount is required when billingType is FLAT_RATE")
    @Schema(hidden = true)
    public boolean isFlatRateFieldsConsistent() {
        if (billingType != BillingType.FLAT_RATE) {
            return true;
        }
        return flatRateTotalAmount != null;
    }

    @AssertTrue(message = "expertType is required when type is EXPERT")
    @Schema(hidden = true)
    public boolean isExpertTypePresentForExpert() {
        if (type != PriorAuthorityType.EXPERT) {
            return true;
        }
        return expertType != null && !expertType.isBlank();
    }
}
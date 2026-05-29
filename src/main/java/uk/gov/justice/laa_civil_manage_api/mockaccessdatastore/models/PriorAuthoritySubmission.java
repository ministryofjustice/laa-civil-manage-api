package uk.gov.justice.laa_civil_manage_api.mockaccessdatastore.models;

import java.math.BigDecimal;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import uk.gov.justice.laa_civil_manage_api.models.BillingType;
import uk.gov.justice.laa_civil_manage_api.models.EstimatedTime;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthority;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthorityType;
import uk.gov.justice.laa_civil_manage_api.models.UploadedDocument;

@Schema(
        description = "Prior-authority request body as submitted to the Access Data Store. "
                + "The applicationId is taken from the URL path; it does not appear in the body."
)
@Builder
public record PriorAuthoritySubmission(

        @Schema(description = "The category of prior authority being requested.",
                example = "EXPERT",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull PriorAuthorityType type,

        @Schema(description = "The expert type.",
                example = "Psychologist")
        String expertType,

        @Schema(description = "Full name of the expert the prior authority is for.",
                example = "John Doe",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String expertFullName,

        @Schema(
                description = "Boolean flag to indicate whether the expert is based inside (true) or outside (false) London",
                example = "true",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull Boolean expertBasedInLondon,

        @Schema(description = "Supporting documents uploaded with the request.")
        @Valid List<UploadedDocument> uploadedDocuments,

        @Schema(description = "True if the requested rates exceed the published LAA guideline rates.",
                example = "false")
        boolean guidelineRatesExceeded,

        @Schema(description = "How the work will be billed.",
                example = "HOURLY",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull BillingType billingType,

        @Schema(description = "Hourly rate in GBP. Required when billingType is HOURLY.",
                example = "50.00")
        @Positive BigDecimal hourlyRate,

        @Schema(description = "Estimated time the work will take. Required when billingType is HOURLY.")
        @Valid EstimatedTime estimatedTime,

        @Schema(description = "Total amount in GBP for hourly billing. Required when billingType is HOURLY.",
                example = "125.00")
        @Positive BigDecimal totalAmount,

        @Schema(description = "Total flat-rate amount in GBP. Required when billingType is FLAT_RATE.",
                example = "249.99")
        @Positive BigDecimal flatRateTotalAmount
) {

    public static PriorAuthoritySubmission from(PriorAuthority priorAuthority) {
        return PriorAuthoritySubmission.builder()
                .type(priorAuthority.type())
                .expertType(priorAuthority.expertType())
                .expertFullName(priorAuthority.expertFullName())
                .expertBasedInLondon(priorAuthority.expertBasedInLondon())
                .uploadedDocuments(priorAuthority.uploadedDocuments())
                .guidelineRatesExceeded(priorAuthority.guidelineRatesExceeded())
                .billingType(priorAuthority.billingType())
                .hourlyRate(priorAuthority.hourlyRate())
                .estimatedTime(priorAuthority.estimatedTime())
                .totalAmount(priorAuthority.totalAmount())
                .flatRateTotalAmount(priorAuthority.flatRateTotalAmount())
                .build();
    }
}
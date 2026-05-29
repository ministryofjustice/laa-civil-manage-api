package uk.gov.justice.laa_civil_manage_api.models;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Schema(
        description = "An in-progress prior-authority form saved as a draft."
)
@Builder
public record PriorAuthorityDraft(

        @Schema(
                description = "ID of the application this draft is associated with.",
                example = "2a28f60d-fe15-43fe-92c3-5530595d5f51",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull UUID applicationId,

        @Schema(description = "The type of prior authority being requested.", example = "EXPERT", nullable = true)
        PriorAuthorityType type,

        @Schema(description = "The expert type.", example = "Child psychologist", nullable = true)
        String expertType,

        @Schema(description = "Full name of the expert the prior authority is for.", example = "Dr Joe Bloggs", nullable = true)
        String expertFullName,

        @Schema(description = "Boolean flag to indicate whether the expert is based inside (true) or outside (false) London", example = "true", nullable = true)
        Boolean expertBasedInLondon,

        @Schema(description = "Supporting documents uploaded so far.", nullable = true)
        List<UploadedDocument> uploadedDocuments,

        @Schema(description = "Whether the requested rates exceed published LAA guideline rates.", nullable = true)
        Boolean guidelineRatesExceeded,

        @Schema(description = "How the work will be billed.", example = "HOURLY", nullable = true)
        BillingType billingType,

        @Schema(description = "Hourly rate in GBP.", example = "45.00", nullable = true)
        BigDecimal hourlyRate,

        @Schema(description = "Estimated time the work will take.", nullable = true)
        EstimatedTime estimatedTime,

        @Schema(description = "Total amount in GBP for hourly billing.", example = "135.00", nullable = true)
        BigDecimal totalAmount,

        @Schema(description = "Total flat-rate amount in GBP.", example = "249.99", nullable = true)
        BigDecimal flatRateTotalAmount
) {
}

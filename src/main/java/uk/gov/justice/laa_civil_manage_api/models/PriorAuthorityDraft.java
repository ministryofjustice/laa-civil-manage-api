package uk.gov.justice.laa_civil_manage_api.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Schema(description = "An in-progress prior-authority form saved as a draft.")
@Builder
public record PriorAuthorityDraft(
    @Schema(
            description = "ID of the application this draft is associated with.",
            example = "2a28f60d-fe15-43fe-92c3-5530595d5f51",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        UUID applicationId,
    @Schema(
            description = "The type of prior authority being requested.",
            example = "EXPERT",
            nullable = true)
        PriorAuthorityType priorAuthorityType,
    @Schema(
            description = "The counsel type requested.",
            example = "KINGS_COUNSEL_ALONE",
            nullable = true)
        CounselType counselType,
    @Schema(description = "The expert type.", example = "Child psychologist", nullable = true)
        String expertType,
    @Schema(
            description = "Full name of the expert the prior authority is for.",
            example = "Dr Joe Bloggs",
            nullable = true)
        String expertFullName,
    @Schema(
            description = "Primary business postcode of the expert.",
            example = "SW1H 9AJ",
            nullable = true)
        String expertPostcode,
    @Schema(description = "Supporting documents uploaded so far.", nullable = true)
        List<UploadedDocument> uploadedDocuments,
    @Schema(description = "How the work will be billed.", example = "HOURLY", nullable = true)
        BillingType billingType,
    @Schema(description = "Hourly rate in GBP.", example = "45.00", nullable = true)
        BigDecimal hourlyRate,
    @Schema(description = "Estimated whole hours.", example = "2", nullable = true)
        Integer timeHours,
    @Schema(description = "Estimated additional minutes.", example = "30", nullable = true)
        @Min(0)
        @Max(59)
        Integer timeMinutes,
    @Schema(description = "Total amount in GBP.", example = "135.00", nullable = true)
        BigDecimal totalAmount,
    @Schema(
            description = "Detailed rationale explaining why funding is necessary.",
            example = "Expert evidence is needed to establish causation.",
            nullable = true)
        String justification) {}

package uk.gov.justice.laa_civil_manage_api.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Schema(
    description =
        "A prior-authority request submitted by a legal aid provider against an existing application.")
@Builder
public record PriorAuthority(
    @Schema(
            description = "ID of the application this prior-authority request is associated with.",
            example = "5f1b2c3d-1111-2222-3333-444455556666",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        UUID applicationId,
    @Schema(
            description = "The category of prior authority being requested.",
            example = "EXPERT",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        PriorAuthorityType priorAuthorityType,
    @Schema(
            description =
                "The counsel type requested. Required when priorAuthorityType is COUNSEL.",
            example = "KINGS_COUNSEL_ALONE")
        CounselType counselType,
    @Schema(description = "The expert type.", example = "Psychologist") String expertType,
    @Schema(
            description = "Full name of the expert the prior authority is for.",
            example = "John Doe")
        String expertFullName,
    @Schema(description = "Primary business postcode of the expert.", example = "SW1H 9AJ")
        String expertPostcode,
    @Schema(
            description =
                "Boolean flag to indicate whether the expert is based inside (true) or outside (false) London",
            example = "true")
        Boolean expertBasedInLondon,
    @Schema(description = "Supporting documents uploaded with the request.") @Valid
        List<UploadedDocument> uploadedDocuments,
    @Schema(
            description = "How the work will be billed.",
            example = "HOURLY",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        BillingType billingType,
    @Schema(
            description = "Hourly rate in GBP. Required when billingType is HOURLY.",
            example = "50.00")
        BigDecimal hourlyRate,
    @Schema(
            description = "Estimated whole hours. Required when billingType is HOURLY.",
            example = "2")
        Integer timeHours,
    @Schema(
            description = "Estimated additional minutes. Required when billingType is HOURLY.",
            example = "30")
        @Min(0)
        @Max(59)
        Integer timeMinutes,
    @Schema(
            description = "Total amount in GBP.",
            example = "125.00",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        BigDecimal totalAmount,
    @Schema(
            description = "Detailed rationale explaining why funding is necessary.",
            example = "The case requires specialist expert evidence to proceed.",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        String justification) {

  @AssertTrue(message = "counselType must be provided when priorAuthorityType is COUNSEL")
  public boolean isCounselTypeValid() {
    return priorAuthorityType != PriorAuthorityType.COUNSEL || counselType != null;
  }
}

package uk.gov.justice.laa_civil_manage_api.mockaccessdatastore.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import uk.gov.justice.laa_civil_manage_api.models.BillingType;
import uk.gov.justice.laa_civil_manage_api.models.CounselType;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthority;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthorityType;
import uk.gov.justice.laa_civil_manage_api.models.UploadedDocument;

@Schema(
    description =
        "Prior-authority request body as submitted to the Access Data Store. "
            + "The applicationId is taken from the URL path; it does not appear in the body.")
@Builder
public record PriorAuthoritySubmission(
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
    @Schema(description = "Supporting documents uploaded with the request.")
        List<@Valid UploadedDocument> uploadedDocuments,
    @Schema(
            description =
                "How the work will be billed. Required when priorAuthorityType is EXPERT.",
            example = "HOURLY")
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
    @Schema(description = "Total amount in GBP.", example = "125.00") BigDecimal totalAmount,
    @Schema(
            description = "Detailed rationale explaining why funding is necessary.",
            example = "Expert evidence is needed to support the claim.",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        String justification) {

  @Schema(hidden = true)
  @JsonIgnore
  @AssertTrue(message = "counselType must be provided when priorAuthorityType is COUNSEL")
  public boolean isCounselTypeValid() {
    return priorAuthorityType != PriorAuthorityType.COUNSEL || counselType != null;
  }

  @Schema(hidden = true)
  @JsonIgnore
  @AssertTrue(message = "billingType must be provided when priorAuthorityType is EXPERT")
  public boolean isBillingTypeValid() {
    return priorAuthorityType != PriorAuthorityType.EXPERT || billingType != null;
  }

  public static PriorAuthoritySubmission from(PriorAuthority priorAuthority) {
    return PriorAuthoritySubmission.builder()
        .priorAuthorityType(priorAuthority.priorAuthorityType())
        .counselType(
            priorAuthority.priorAuthorityType() == PriorAuthorityType.COUNSEL
                ? priorAuthority.counselType()
                : null)
        .expertType(priorAuthority.expertType())
        .expertFullName(priorAuthority.expertFullName())
        .expertPostcode(priorAuthority.expertPostcode())
        .expertBasedInLondon(priorAuthority.expertBasedInLondon())
        .uploadedDocuments(priorAuthority.uploadedDocuments())
        .billingType(priorAuthority.billingType())
        .hourlyRate(priorAuthority.hourlyRate())
        .timeHours(priorAuthority.timeHours())
        .timeMinutes(priorAuthority.timeMinutes())
        .totalAmount(priorAuthority.totalAmount())
        .justification(priorAuthority.justification())
        .build();
  }
}

package uk.gov.justice.laa_civil_manage_api.mockaccessdatastore.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Builder;
import uk.gov.justice.laa_civil_manage_api.models.CounselDetails;
import uk.gov.justice.laa_civil_manage_api.models.DisbursementDetails;
import uk.gov.justice.laa_civil_manage_api.models.ExpertDetails;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthority;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthorityType;
import uk.gov.justice.laa_civil_manage_api.models.UploadedDocument;

@Schema(
    description =
        "Prior-authority request body as submitted to the Access Data Store. "
            + "The applicationId is taken from the URL path; it does not appear in the body.")
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PriorAuthoritySubmission(
    @Schema(
            description = "The category of prior authority being requested.",
            example = "EXPERT",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        PriorAuthorityType priorAuthorityType,
    @Schema(
            description = "Detailed rationale explaining why funding is necessary.",
            example = "Expert evidence is needed to support the claim.",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String justification,
    @Schema(description = "Supporting documents uploaded with the request. Optional for all types.")
        List<@Valid UploadedDocument> uploadedDocuments,
    @Schema(description = "Present when priorAuthorityType is EXPERT.") @Valid
        ExpertDetails expertDetails,
    @Schema(description = "Present when priorAuthorityType is COUNSEL.") @Valid
        CounselDetails counselDetails,
    @Schema(description = "Present when priorAuthorityType is DISBURSEMENT.") @Valid
        DisbursementDetails disbursementDetails) {

  @Schema(hidden = true)
  @JsonIgnore
  @AssertTrue(
      message =
          "expertDetails is required when priorAuthorityType is EXPERT, and must be omitted otherwise")
  public boolean isExpertDetailsConsistent() {
    return priorAuthorityType == null
        || (priorAuthorityType == PriorAuthorityType.EXPERT) == (expertDetails != null);
  }

  @Schema(hidden = true)
  @JsonIgnore
  @AssertTrue(
      message =
          "counselDetails is required when priorAuthorityType is COUNSEL, and must be omitted otherwise")
  public boolean isCounselDetailsConsistent() {
    return priorAuthorityType == null
        || (priorAuthorityType == PriorAuthorityType.COUNSEL) == (counselDetails != null);
  }

  @Schema(hidden = true)
  @JsonIgnore
  @AssertTrue(
      message =
          "disbursementDetails is required when priorAuthorityType is DISBURSEMENT, and must be omitted otherwise")
  public boolean isDisbursementDetailsConsistent() {
    return priorAuthorityType == null
        || (priorAuthorityType == PriorAuthorityType.DISBURSEMENT) == (disbursementDetails != null);
  }

  /** The nested blocks carry across unchanged, so this is a straight copy minus applicationId. */
  public static PriorAuthoritySubmission from(PriorAuthority priorAuthority) {
    return PriorAuthoritySubmission.builder()
        .priorAuthorityType(priorAuthority.priorAuthorityType())
        .justification(priorAuthority.justification())
        .uploadedDocuments(priorAuthority.uploadedDocuments())
        .expertDetails(priorAuthority.expertDetails())
        .counselDetails(priorAuthority.counselDetails())
        .disbursementDetails(priorAuthority.disbursementDetails())
        .build();
  }
}

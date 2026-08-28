package uk.gov.justice.laa_civil_manage_api.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Schema(
    description =
        "A prior-authority request submitted by a legal aid provider against an existing application. "
            + "Exactly one of expertDetails, counselDetails or disbursementDetails is present, "
            + "matching priorAuthorityType.")
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PriorAuthority(
    @Schema(
            description = "ID of the application this prior-authority request is associated with.",
            example = "5f1b2c3d-1111-2222-3333-444455556666",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        UUID applicationId,
    @Schema(
            description =
                "LAA reference of the application this prior-authority request is associated with.",
            example = "LAA123456",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String laaReference,
    @Schema(
            description = "The category of prior authority being requested.",
            example = "EXPERT",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        PriorAuthorityType priorAuthorityType,
    @Schema(
            description = "Detailed rationale explaining why funding is necessary.",
            example = "The case requires specialist expert evidence to proceed.",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String justification,
    @Schema(description = "Supporting documents uploaded with the request. Optional for all types.")
        List<@Valid UploadedDocument> uploadedDocuments,
    @Schema(description = "Required when priorAuthorityType is EXPERT; omitted otherwise.") @Valid
        ExpertDetails expertDetails,
    @Schema(description = "Required when priorAuthorityType is COUNSEL; omitted otherwise.") @Valid
        CounselDetails counselDetails,
    @Schema(description = "Required when priorAuthorityType is DISBURSEMENT; omitted otherwise.")
        @Valid
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
}

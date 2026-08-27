package uk.gov.justice.laa_civil_manage_api.services.accessdatastore;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import uk.gov.justice.laa_civil_manage_api.models.CounselDetails;
import uk.gov.justice.laa_civil_manage_api.models.DisbursementDetails;
import uk.gov.justice.laa_civil_manage_api.models.ExpertDetails;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthority;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthorityType;

@Schema(
    description =
        "Prior-authority request body as submitted to the Access Data Store. "
            + "The applicationId is taken from the URL path; it does not appear in the body.")
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CreatePriorAuthorityRequest(
    @Schema(
            description = "The category of prior authority being requested.",
            example = "EXPERT",
            requiredMode = Schema.RequiredMode.REQUIRED)
        PriorAuthorityType priorAuthorityType,
    @Schema(
            description = "Detailed rationale explaining why funding is necessary.",
            example = "Expert evidence is needed to support the claim.",
            requiredMode = Schema.RequiredMode.REQUIRED)
        String justification,
    @Schema(description = "Present when priorAuthorityType is EXPERT.") ExpertDetails expertDetails,
    @Schema(description = "Present when priorAuthorityType is COUNSEL.")
        CounselDetails counselDetails,
    @Schema(description = "Present when priorAuthorityType is DISBURSEMENT.")
        DisbursementDetails disbursementDetails) {

  /**
   * The nested blocks carry across unchanged, so this is a straight copy minus applicationId and
   * uploadedDocuments (Data Store doesn't accept documents yet).
   */
  public static CreatePriorAuthorityRequest from(PriorAuthority priorAuthority) {
    return CreatePriorAuthorityRequest.builder()
        .priorAuthorityType(priorAuthority.priorAuthorityType())
        .justification(priorAuthority.justification())
        .expertDetails(priorAuthority.expertDetails())
        .counselDetails(priorAuthority.counselDetails())
        .disbursementDetails(priorAuthority.disbursementDetails())
        .build();
  }
}

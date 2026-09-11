package uk.gov.justice.laa_civil_manage_api.services.accessdatastore;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.Builder;
import uk.gov.justice.laa_civil_manage_api.models.CounselDetails;
import uk.gov.justice.laa_civil_manage_api.models.DisbursementDetails;
import uk.gov.justice.laa_civil_manage_api.models.ExpertDetails;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthorityDraft;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthorityType;

@Schema(
    description =
        "Request body used to create a new prior-authority draft in the Access Data Store.")
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CreatePriorAuthorityDraftRequest(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID applicationId,
    PriorAuthorityType priorAuthorityType,
    String justification,
    ExpertDetails expertDetails,
    CounselDetails counselDetails,
    DisbursementDetails disbursementDetails) {

  public static CreatePriorAuthorityDraftRequest from(PriorAuthorityDraft draft) {
    return CreatePriorAuthorityDraftRequest.builder()
        .applicationId(draft.applicationId())
        .priorAuthorityType(draft.priorAuthorityType())
        .justification(draft.justification())
        .expertDetails(draft.expertDetails())
        .counselDetails(draft.counselDetails())
        .disbursementDetails(draft.disbursementDetails())
        .build();
  }
}

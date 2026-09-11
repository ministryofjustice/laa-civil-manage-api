package uk.gov.justice.laa_civil_manage_api.services.accessdatastore;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import uk.gov.justice.laa_civil_manage_api.models.CounselDetails;
import uk.gov.justice.laa_civil_manage_api.models.DisbursementDetails;
import uk.gov.justice.laa_civil_manage_api.models.ExpertDetails;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthorityDraft;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthorityType;

@Schema(
    description =
        "Request body used to update an existing prior-authority draft in the Access Data Store. "
            + "The applicationId is fixed at creation time and is not included here.")
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SavePriorAuthorityDraftRequest(
    PriorAuthorityType priorAuthorityType,
    String justification,
    ExpertDetails expertDetails,
    CounselDetails counselDetails,
    DisbursementDetails disbursementDetails) {

  public static SavePriorAuthorityDraftRequest from(PriorAuthorityDraft draft) {
    return SavePriorAuthorityDraftRequest.builder()
        .priorAuthorityType(draft.priorAuthorityType())
        .justification(draft.justification())
        .expertDetails(draft.expertDetails())
        .counselDetails(draft.counselDetails())
        .disbursementDetails(draft.disbursementDetails())
        .build();
  }
}

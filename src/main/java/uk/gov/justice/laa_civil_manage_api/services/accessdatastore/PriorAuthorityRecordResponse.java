package uk.gov.justice.laa_civil_manage_api.services.accessdatastore;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.Builder;
import uk.gov.justice.laa_civil_manage_api.models.CounselDetails;
import uk.gov.justice.laa_civil_manage_api.models.DisbursementDetails;
import uk.gov.justice.laa_civil_manage_api.models.ExpertDetails;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthorityType;

@Schema(
    description =
        "A prior-authority record as returned by the Access Data Store. status is null while "
            + "the request is still a draft, and PENDING (or later) once it has been submitted.")
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PriorAuthorityRecordResponse(
    UUID priorAuthorityId,
    UUID applicationId,
    String status,
    PriorAuthorityType priorAuthorityType,
    String justification,
    ExpertDetails expertDetails,
    CounselDetails counselDetails,
    DisbursementDetails disbursementDetails) {}

package uk.gov.justice.laa_civil_manage_api.models;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.Builder;

@Schema(
    description =
        "A prior-authority record as returned by the Access Data Store. status is null while "
            + "the request is still a draft, and PENDING (or later) once it has been submitted.")
@Builder
public record PriorAuthorityResponse(
    @Schema(
            description =
                "Identifier of the prior-authority request, assigned when it was created.",
            example = "c3b07e24-d92b-410a-9d95-88f117a12b43")
        UUID priorAuthorityId,
    @Schema(
            description = "Lifecycle status. null while a draft; e.g. PENDING once submitted.",
            example = "PENDING",
            nullable = true)
        String status,
    @Schema(description = "The saved prior-authority form.") PriorAuthorityDraft draft) {}

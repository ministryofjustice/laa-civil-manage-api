package uk.gov.justice.laa_civil_manage_api.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Builder;

@Schema(
    description =
        "A prior-authority form, saved and updated incrementally through the create/update draft "
            + "lifecycle before being submitted. At most one of expertDetails, counselDetails or "
            + "disbursementDetails should be populated, matching priorAuthorityType.")
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PriorAuthorityDraft(
    @Schema(
            description = "ID of the application this prior-authority request is associated with.",
            example = "2a28f60d-fe15-43fe-92c3-5530595d5f51",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        UUID applicationId,
    @Schema(
            description = "The category of prior authority being requested.",
            example = "EXPERT",
            nullable = true)
        PriorAuthorityType priorAuthorityType,
    @Schema(
            description = "Detailed rationale explaining why funding is necessary.",
            example = "Expert evidence is needed to establish causation.",
            nullable = true)
        String justification,
    @Schema(description = "Present when priorAuthorityType is EXPERT.", nullable = true) @Valid
        ExpertDetails expertDetails,
    @Schema(description = "Present when priorAuthorityType is COUNSEL.", nullable = true) @Valid
        CounselDetails counselDetails,
    @Schema(description = "Present when priorAuthorityType is DISBURSEMENT.", nullable = true)
        @Valid
        DisbursementDetails disbursementDetails) {}

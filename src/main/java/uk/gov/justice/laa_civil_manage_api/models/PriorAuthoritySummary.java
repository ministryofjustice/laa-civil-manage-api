package uk.gov.justice.laa_civil_manage_api.models;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Builder;

@Schema(description = "Summary of a prior-authority request attached to an application.")
@Builder
public record PriorAuthoritySummary(
    @Schema(
            description = "Identifier of the prior-authority request.",
            example = "c3b07e24-d92b-410a-9d95-88f117a12b43")
        UUID priorAuthorityId,
    @Schema(description = "The category of prior authority requested.", example = "EXPERT")
        PriorAuthorityType priorAuthorityType,
    @Schema(
            description = "Lifecycle status of the prior-authority request.",
            example = "SUBMITTED",
            allowableValues = {"DRAFT", "SUBMITTED", "DECIDED"})
        String status,
    @Schema(
            description = "Decision on the request, if decided.",
            example = "GRANTED",
            allowableValues = {"GRANTED", "REFUSED"},
            nullable = true)
        String decision,
    @Schema(
            description = "The date and time the prior-authority request was created (in UTC).",
            example = "2026-09-24T15:00:15.141805Z")
        OffsetDateTime createdAt) {}

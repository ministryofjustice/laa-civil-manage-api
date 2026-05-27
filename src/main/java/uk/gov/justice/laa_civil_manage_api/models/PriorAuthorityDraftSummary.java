package uk.gov.justice.laa_civil_manage_api.models;

import java.time.OffsetDateTime;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "A prior-authority draft returned in a list lookup.")
@Builder
public record PriorAuthorityDraftSummary(

        @Schema(
                description = "Identifier of the draft, assigned when it was created.",
                example = "c3b07e24-d92b-410a-9d95-88f117a12b43"
        )
        UUID draftId,

        @Schema(
                description = "Timestamp the draft was last updated.",
                example = "2026-05-19T12:00:00Z"
        )
        OffsetDateTime timestamp,

        @Schema(description = "The saved prior-authority form.")
        PriorAuthorityDraft draft
) {
}

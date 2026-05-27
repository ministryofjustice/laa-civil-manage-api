package uk.gov.justice.laa_civil_manage_api.models;

import java.time.OffsetDateTime;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "Confirmation returned after a prior-authority application has been submitted.")
@Builder
public record PriorAuthorityApplicationResponse(

        @Schema(
                description = "Identifier assigned to the prior-authority application by the Access Data Store.",
                example = "11111111-2222-3333-4444-555555555555"
        )
        UUID submissionId,

        @Schema(
                description = "Outcome of the submission.",
                example = "ACCEPTED"
        )
        SubmissionStatus status,

        @Schema(
                description = "Timestamp the application was accepted by the Access Data Store.",
                example = "2026-05-22T10:00:00Z"
        )
        OffsetDateTime submittedAt
) {
}

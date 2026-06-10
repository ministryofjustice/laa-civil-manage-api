package uk.gov.justice.laa_civil_manage_api.models;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.Builder;

@Schema(description = "Confirmation returned after a draft has been created.")
@Builder
public record DraftCreatedResponse(
    @Schema(
            description = "Identifier assigned to the draft by the Access Data Store.",
            example = "c3b07e24-d92b-410a-9d95-88f117a12b43")
        UUID draftId) {}

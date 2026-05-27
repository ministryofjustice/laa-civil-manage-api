package uk.gov.justice.laa_civil_manage_api.models;

import java.util.Map;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Schema(
        description = "An in-progress draft that the user wants to save and return to later. "
                + "The draftBody is an unstructured JSON payload owned by the calling system."
)
@Builder
public record Draft(

        @Schema(
                description = "Identifies the system creating the draft (e.g., laa-civil-manage).",
                example = "laa-civil-manage",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank String sourceSystem,

        @Schema(
                description = "Identifies the flow the draft belongs to (e.g., PRIOR_AUTHORITY). ",
                example = "PRIOR_AUTHORITY",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank String draftType,

        @Schema(
                description = "ID of the application the draft is associated with.",
                example = "2a28f60d-fe15-43fe-92c3-5530595d5f51",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull UUID applicationId,

        @Schema(
                description = "Entra identity of the user who owns the draft.",
                example = "entra-id-uuid",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank String userId,

        @Schema(
                description = "JSON payload representing the in-progress form. Not validated.",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull Map<String, Object> draftBody
) {
}

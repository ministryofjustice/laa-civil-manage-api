package uk.gov.justice.laa_civil_manage_api.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Builder;

@Schema(description = "Summary of a civil application containing key details.")
@Builder
public record ApplicationSummary(
    @Schema(
            description = "The unique reference used to identify the application.",
            example = "11111111-2222-3333-4444-555555555555")
        UUID applicationId,
    @Schema(description = "The LAA reference number of the application.", example = "LAA-123456")
        String laaReference,
    @Schema(
            description = "The current overall status of the application decision.",
            example = "APPLICATION_SUBMITTED")
        String status,
    @Schema(
            description = "The date and time the application was submitted (in UTC).",
            example = "2026-07-22T10:00:00Z")
        @JsonProperty("submittedAt")
        OffsetDateTime startDate,
    @Schema(description = "The client first name.", example = "John") String clientFirstName,
    @Schema(description = "The client last name.", example = "Doe") String clientLastName,
    @Schema(description = "The matter type of the application.", example = "SPECIAL_CHILDREN_ACT")
        String matterType) {}

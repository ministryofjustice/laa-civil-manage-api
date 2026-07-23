package uk.gov.justice.laa_civil_manage_api.models;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;

@Schema(description = "Response containing a list of application summaries.")
@Builder
public record ApplicationSummaryResponse(
    @Schema(description = "Pagination metadata for the response.") Paging paging,
    @Schema(description = "List of application summaries matching the requested criteria.")
        List<ApplicationSummary> applications) {}

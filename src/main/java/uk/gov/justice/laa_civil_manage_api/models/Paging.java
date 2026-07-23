package uk.gov.justice.laa_civil_manage_api.models;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "Pagination metadata for a paginated response.")
@Builder
public record Paging(
    @Schema(description = "The current page number (0-based).", example = "0") int page,
    @Schema(description = "The number of items per page.", example = "10") int pageSize,
    @Schema(description = "The number of items returned in this page.", example = "10")
        int itemsReturned,
    @Schema(description = "The total number of records available.", example = "100")
        int totalRecords) {}

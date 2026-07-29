package uk.gov.justice.laa_civil_manage_api.models;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;

@Schema(description = "Response containing a list of individuals (clients) for an application.")
@Builder
public record IndividualsResponse(
    @Schema(description = "Pagination metadata for the response.") Paging paging,
    @Schema(description = "List of individuals matching the requested criteria.")
        List<Client> individuals) {}

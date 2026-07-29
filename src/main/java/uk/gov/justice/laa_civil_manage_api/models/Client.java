package uk.gov.justice.laa_civil_manage_api.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "Summary of an individual (client) associated with an application.")
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record Client(
    @Schema(description = "The client's first name.", example = "John") String firstName,
    @Schema(description = "The client's last name.", example = "Doe") String lastName) {}

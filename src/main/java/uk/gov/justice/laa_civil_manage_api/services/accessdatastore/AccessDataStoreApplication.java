package uk.gov.justice.laa_civil_manage_api.services.accessdatastore;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import uk.gov.justice.laa_civil_manage_api.models.ApplicationSummary;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthoritySummary;

public record AccessDataStoreApplication(
    UUID applicationId,
    String laaReference,
    String status,
    @JsonProperty("submittedAt") OffsetDateTime startDate,
    String clientFirstName,
    String clientLastName,
    String matterType,
    AccessDataStoreProvider provider,
    List<PriorAuthoritySummary> priorAuthorities) {

  public ApplicationSummary toApplicationSummary() {
    return ApplicationSummary.builder()
        .applicationId(applicationId)
        .laaReference(laaReference)
        .status(status)
        .startDate(startDate)
        .clientFirstName(clientFirstName)
        .clientLastName(clientLastName)
        .matterType(matterType)
        .officeCode(provider == null ? null : provider.officeCode())
        .priorAuthorities(priorAuthorities == null ? List.of() : List.copyOf(priorAuthorities))
        .build();
  }
}

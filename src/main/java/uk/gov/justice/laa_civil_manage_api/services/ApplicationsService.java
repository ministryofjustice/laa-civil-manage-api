package uk.gov.justice.laa_civil_manage_api.services;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.justice.laa_civil_manage_api.models.ApplicationStatus;
import uk.gov.justice.laa_civil_manage_api.models.ApplicationSummary;
import uk.gov.justice.laa_civil_manage_api.models.ApplicationSummaryResponse;
import uk.gov.justice.laa_civil_manage_api.models.Client;
import uk.gov.justice.laa_civil_manage_api.models.IndividualsResponse;
import uk.gov.justice.laa_civil_manage_api.models.Paging;
import uk.gov.justice.laa_civil_manage_api.services.accessdatastore.AccessDataStoreClient;

@Service
@RequiredArgsConstructor
public class ApplicationsService {

  private final AccessDataStoreClient accessDataStoreClient;

  public ApplicationSummaryResponse getApplicationsData(
      int page, int pageSize, ApplicationStatus status) {
    ApplicationSummaryResponse response =
        accessDataStoreClient.getApplications(page, pageSize, status);

    if (response != null) {
      return response;
    }

    return ApplicationSummaryResponse.builder()
        .paging(
            Paging.builder().page(page).pageSize(pageSize).itemsReturned(0).totalRecords(0).build())
        .applications(Collections.emptyList())
        .build();
  }

  public ApplicationSummary getApplicationById(String applicationId) {
    UUID id = UUID.fromString(applicationId);
    ApplicationSummary summary = accessDataStoreClient.getApplicationById(id);

    if (summary == null) {
      return null;
    }

    IndividualsResponse individualsResponse = accessDataStoreClient.getIndividuals(id);
    List<Client> individuals =
        individualsResponse == null ? null : individualsResponse.individuals();

    if (individuals == null || individuals.isEmpty()) {
      return summary;
    }

    Client client = individuals.getFirst();
    return summary.toBuilder()
        .clientFirstName(client.firstName())
        .clientLastName(client.lastName())
        .build();
  }
}

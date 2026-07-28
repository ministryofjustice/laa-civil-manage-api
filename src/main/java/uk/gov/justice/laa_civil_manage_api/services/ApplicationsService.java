package uk.gov.justice.laa_civil_manage_api.services;

import java.util.Collections;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.justice.laa_civil_manage_api.models.ApplicationSummary;
import uk.gov.justice.laa_civil_manage_api.models.ApplicationSummaryResponse;
import uk.gov.justice.laa_civil_manage_api.models.Paging;
import uk.gov.justice.laa_civil_manage_api.services.accessdatastore.AccessDataStoreClient;

@Service
@RequiredArgsConstructor
public class ApplicationsService {

  private final AccessDataStoreClient accessDataStoreClient;

  public ApplicationSummaryResponse getApplicationsData(int page, int pageSize) {
    ApplicationSummaryResponse response = accessDataStoreClient.getApplications(page, pageSize);

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
    return accessDataStoreClient.getApplicationById(UUID.fromString(applicationId));
  }
}

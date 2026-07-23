package uk.gov.justice.laa_civil_manage_api.services;

import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.justice.laa_civil_manage_api.models.ApplicationSummaryResponse;
import uk.gov.justice.laa_civil_manage_api.models.Paging;
import uk.gov.justice.laa_civil_manage_api.services.accessdatastore.AccessDataStoreClient;

@Service
@Slf4j
@RequiredArgsConstructor
public class ApplicationsService {

  private final AccessDataStoreClient accessDataStoreClient;

  public ApplicationSummaryResponse getApplicationsData(int page) {
    log.info("Fetching applications from Data Store via OBO token exchange...");
    ApplicationSummaryResponse response = accessDataStoreClient.getApplications(page);

    if (response != null) {
      return response;
    }

    return ApplicationSummaryResponse.builder()
        .paging(Paging.builder().page(page).pageSize(10).itemsReturned(0).totalRecords(0).build())
        .applications(Collections.emptyList())
        .build();
  }
}

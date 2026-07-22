package uk.gov.justice.laa_civil_manage_api.services;

import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.justice.laa_civil_manage_api.models.ApplicationSummary;
import uk.gov.justice.laa_civil_manage_api.models.ApplicationSummaryResponse;
import uk.gov.justice.laa_civil_manage_api.services.accessdatastore.AccessDataStoreClient;

@Service
@Slf4j
@RequiredArgsConstructor
public class ApplicationsService {

  private final AccessDataStoreClient accessDataStoreClient;

  public List<ApplicationSummary> getApplicationsData() {
    log.info("Fetching applications from Data Store via OBO token exchange...");
    ApplicationSummaryResponse response = accessDataStoreClient.getApplications();

    if (response != null && response.applications() != null) {
      return response.applications();
    }

    return Collections.emptyList();
  }
}

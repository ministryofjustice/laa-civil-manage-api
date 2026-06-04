package uk.gov.justice.laa_civil_manage_api.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.justice.laa_civil_manage_api.services.accessdatastore.AccessDataStoreClient;

@Service
@Slf4j
@RequiredArgsConstructor
public class ApplicationsService {

  private final AccessDataStoreClient accessDataStoreClient;

  public String getApplicationsData() {
    log.info("Fetching applications from Data Store via OBO token exchange...");
    return accessDataStoreClient.getApplications();
  }
}

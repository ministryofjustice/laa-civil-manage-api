package uk.gov.justice.laa_civil_manage_api.services;

import java.util.Collections;
import java.util.Optional;
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

  private static final int FETCH_PAGE_SIZE = 100;

  public ApplicationSummary getApplicationById(String applicationId) {
    UUID id = UUID.fromString(applicationId);

    ApplicationSummaryResponse firstPage =
        accessDataStoreClient.getApplications(1, FETCH_PAGE_SIZE);
    if (firstPage == null || firstPage.applications() == null) {
      return null;
    }

    Optional<ApplicationSummary> found =
        firstPage.applications().stream().filter(app -> id.equals(app.applicationId())).findFirst();
    if (found.isPresent()) {
      return found.get();
    }

    int totalRecords = firstPage.paging() != null ? firstPage.paging().totalRecords() : 0;
    int totalPages = (int) Math.ceil((double) totalRecords / FETCH_PAGE_SIZE);

    for (int page = 2; page <= totalPages; page++) {
      ApplicationSummaryResponse nextPage =
          accessDataStoreClient.getApplications(page, FETCH_PAGE_SIZE);
      if (nextPage == null || nextPage.applications() == null) {
        break;
      }
      found =
          nextPage.applications().stream()
              .filter(app -> id.equals(app.applicationId()))
              .findFirst();
      if (found.isPresent()) {
        return found.get();
      }
    }

    return null;
  }
}

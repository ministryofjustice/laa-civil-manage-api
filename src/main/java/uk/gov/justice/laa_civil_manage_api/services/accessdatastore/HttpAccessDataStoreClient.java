package uk.gov.justice.laa_civil_manage_api.services.accessdatastore;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.justice.laa_civil_manage_api.mockaccessdatastore.models.PriorAuthoritySubmission;
import uk.gov.justice.laa_civil_manage_api.models.*;

@Component
public class HttpAccessDataStoreClient implements AccessDataStoreClient {

  private static final ParameterizedTypeReference<List<DraftSummary>> DRAFT_LIST_TYPE =
      new ParameterizedTypeReference<>() {};

  private final RestClient restClient;
  private final AccessDataStoreProperties properties;

  public HttpAccessDataStoreClient(RestClient adsRestClient, AccessDataStoreProperties properties) {
    this.restClient = adsRestClient;
    this.properties = properties;
  }

  @Override
  public PriorAuthorityApplicationResponse submitPriorAuthority(PriorAuthority priorAuthority) {
    String baseUrl = properties.urlFor(AccessDataStoreOperations.SUBMIT_PRIOR_AUTHORITY);
    return restClient
        .post()
        .uri(
            baseUrl + "/applications/{applicationId}/prior-authority",
            priorAuthority.applicationId())
        .contentType(MediaType.APPLICATION_JSON)
        .body(PriorAuthoritySubmission.from(priorAuthority))
        .retrieve()
        .body(PriorAuthorityApplicationResponse.class);
  }

  @Override
  public DraftCreatedResponse createDraft(Draft draft) {
    String baseUrl = properties.urlFor(AccessDataStoreOperations.CREATE_DRAFT);
    return restClient
        .post()
        .uri(baseUrl + "/drafts")
        .contentType(MediaType.APPLICATION_JSON)
        .body(draft)
        .retrieve()
        .body(DraftCreatedResponse.class);
  }

  @Override
  public void updateDraft(UUID draftId, Draft draft) {
    String baseUrl = properties.urlFor(AccessDataStoreOperations.UPDATE_DRAFT);
    restClient
        .put()
        .uri(baseUrl + "/drafts/{draftId}", draftId)
        .contentType(MediaType.APPLICATION_JSON)
        .body(draft)
        .retrieve()
        .toBodilessEntity();
  }

  @Override
  public Optional<DraftSummary> getDraft(UUID draftId) {
    String baseUrl = properties.urlFor(AccessDataStoreOperations.GET_DRAFT);
    return Optional.ofNullable(
        restClient
            .get()
            .uri(baseUrl + "/drafts/{draftId}", draftId)
            .retrieve()
            .onStatus(status -> status.value() == 404, (_, _) -> {})
            .body(DraftSummary.class));
  }

  @Override
  public List<DraftSummary> getDrafts(
      String sourceSystem, String userId, String draftType, UUID applicationId) {
    String baseUrl = properties.urlFor(AccessDataStoreOperations.GET_DRAFTS);
    UriComponentsBuilder uri =
        UriComponentsBuilder.fromUriString(baseUrl + "/drafts")
            .queryParam("sourceSystem", sourceSystem)
            .queryParam("userId", userId);
    if (draftType != null) {
      uri.queryParam("draftType", draftType);
    }
    if (applicationId != null) {
      uri.queryParam("applicationId", applicationId);
    }
    return restClient.get().uri(uri.build().toUri()).retrieve().body(DRAFT_LIST_TYPE);
  }

  @Override
  public void deleteDraft(UUID draftId) {
    String baseUrl = properties.urlFor(AccessDataStoreOperations.DELETE_DRAFT);
    restClient.delete().uri(baseUrl + "/drafts/{draftId}", draftId).retrieve().toBodilessEntity();
  }

  @Override
  public ApplicationSummaryResponse getApplications(
      int page, int pageSize, ApplicationStatus status) {
    String baseUrl = properties.urlFor(AccessDataStoreOperations.GET_APPLICATIONS);

    return restClient
        .get()
        .uri(
            baseUrl + "/api/v0/applications?page={page}&pageSize={pageSize}&status={status}",
            page,
            pageSize,
            status)
        .header("X-Service-Name", "CIVIL_APPLY")
        .retrieve()
        .body(ApplicationSummaryResponse.class);
  }

  @Override
  public ApplicationSummary getApplicationById(UUID applicationId) {
    String baseUrl = properties.urlFor(AccessDataStoreOperations.GET_APPLICATION_BY_ID);

    return restClient
        .get()
        .uri(baseUrl + "/api/v0/applications/" + applicationId)
        .header("X-Service-Name", "CIVIL_APPLY")
        .retrieve()
        .body(ApplicationSummary.class);
  }
}

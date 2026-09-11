package uk.gov.justice.laa_civil_manage_api.services.accessdatastore;

import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.justice.laa_civil_manage_api.models.*;

@Component
@RequiredArgsConstructor
public class HttpAccessDataStoreClient implements AccessDataStoreClient {

  private static final String SERVICE_NAME_HEADER = "X-Service-Name";
  private static final String SERVICE_NAME = "CIVIL_APPLY";
  private static final String PRIOR_AUTHORITIES_PATH = "/api/v0/prior-authorities";

  private static final String DEFAULT_MATTER_TYPE = "SPECIAL_CHILDREN_ACT";
  private static final String DEFAULT_SORT_BY = "SUBMITTED_DATE";
  private static final String DEFAULT_ORDER_BY = "DESC";

  private final RestClient adsRestClient;
  private final AccessDataStoreProperties properties;

  @Override
  public PriorAuthorityIdResponse createPriorAuthorityDraft(
      CreatePriorAuthorityDraftRequest request) {
    String baseUrl = properties.baseUrl();
    return adsRestClient
        .post()
        .uri(baseUrl + PRIOR_AUTHORITIES_PATH)
        .header(SERVICE_NAME_HEADER, SERVICE_NAME)
        .contentType(MediaType.APPLICATION_JSON)
        .body(request)
        .retrieve()
        .body(PriorAuthorityIdResponse.class);
  }

  @Override
  public void updatePriorAuthorityDraft(
      UUID priorAuthorityId, SavePriorAuthorityDraftRequest request) {
    String baseUrl = properties.baseUrl();
    adsRestClient
        .put()
        .uri(baseUrl + PRIOR_AUTHORITIES_PATH + "/{id}", priorAuthorityId)
        .header(SERVICE_NAME_HEADER, SERVICE_NAME)
        .contentType(MediaType.APPLICATION_JSON)
        .body(request)
        .retrieve()
        .toBodilessEntity();
  }

  @Override
  public Optional<PriorAuthorityRecordResponse> getPriorAuthority(UUID priorAuthorityId) {
    String baseUrl = properties.baseUrl();
    return Optional.ofNullable(
        adsRestClient
            .get()
            .uri(baseUrl + PRIOR_AUTHORITIES_PATH + "/{id}", priorAuthorityId)
            .header(SERVICE_NAME_HEADER, SERVICE_NAME)
            .retrieve()
            .onStatus(status -> status.value() == 404, (_, _) -> {})
            .body(PriorAuthorityRecordResponse.class));
  }

  @Override
  public SubmitPriorAuthorityDraftResponse submitPriorAuthority(UUID priorAuthorityId) {
    String baseUrl = properties.baseUrl();
    return adsRestClient
        .post()
        .uri(baseUrl + PRIOR_AUTHORITIES_PATH + "/{id}/submit", priorAuthorityId)
        .header(SERVICE_NAME_HEADER, SERVICE_NAME)
        .retrieve()
        .body(SubmitPriorAuthorityDraftResponse.class);
  }

  @Override
  public UploadPriorAuthorityDocumentResponse uploadPriorAuthorityDocument(
      UUID priorAuthorityId, MultipartFile file) {
    String baseUrl = properties.baseUrl();
    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("file", file.getResource());

    return adsRestClient
        .post()
        .uri(baseUrl + PRIOR_AUTHORITIES_PATH + "/{id}/documents", priorAuthorityId)
        .header(SERVICE_NAME_HEADER, SERVICE_NAME)
        .contentType(MediaType.MULTIPART_FORM_DATA)
        .body(body)
        .retrieve()
        .body(UploadPriorAuthorityDocumentResponse.class);
  }

  @Override
  public ApplicationSummaryResponse getApplications(
      int page,
      int pageSize,
      ApplicationStatus status,
      String laaReference,
      String clientFirstName,
      String clientLastName) {
    String baseUrl = properties.baseUrl();

    UriComponentsBuilder uriBuilder =
        UriComponentsBuilder.fromUriString(baseUrl + "/api/v0/applications")
            .queryParam("page", page)
            .queryParam("pageSize", pageSize)
            .queryParam("status", status)
            .queryParam("matterType", DEFAULT_MATTER_TYPE)
            .queryParam("sortBy", DEFAULT_SORT_BY)
            .queryParam("orderBy", DEFAULT_ORDER_BY);
    if (laaReference != null) {
      uriBuilder.queryParam("laaReference", laaReference);
    }
    if (clientFirstName != null) {
      uriBuilder.queryParam("clientFirstName", clientFirstName);
    }
    if (clientLastName != null) {
      uriBuilder.queryParam("clientLastName", clientLastName);
    }

    return adsRestClient
        .get()
        .uri(uriBuilder.build().toUri())
        .header(SERVICE_NAME_HEADER, SERVICE_NAME)
        .retrieve()
        .body(ApplicationSummaryResponse.class);
  }

  @Override
  public ApplicationSummary getApplicationById(UUID applicationId) {
    String baseUrl = properties.baseUrl();

    return adsRestClient
        .get()
        .uri(baseUrl + "/api/v0/applications/" + applicationId)
        .header(SERVICE_NAME_HEADER, SERVICE_NAME)
        .retrieve()
        .body(ApplicationSummary.class);
  }

  @Override
  public IndividualsResponse getIndividuals(UUID applicationId) {
    String baseUrl = properties.baseUrl();

    return adsRestClient
        .get()
        .uri(baseUrl + "/api/v0/individuals?applicationId={applicationId}", applicationId)
        .header(SERVICE_NAME_HEADER, SERVICE_NAME)
        .retrieve()
        .body(IndividualsResponse.class);
  }
}

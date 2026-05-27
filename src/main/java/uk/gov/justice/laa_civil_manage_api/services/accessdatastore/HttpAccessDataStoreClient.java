package uk.gov.justice.laa_civil_manage_api.services.accessdatastore;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import uk.gov.justice.laa_civil_manage_api.mockaccessdatastore.models.PriorAuthoritySubmission;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthority;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthorityApplicationResponse;

@Component
public class HttpAccessDataStoreClient implements AccessDataStoreClient {

    private final RestClient restClient;
    private final AccessDataStoreProperties properties;

    public HttpAccessDataStoreClient(RestClient.Builder builder, AccessDataStoreProperties properties) {
        this.restClient = builder.build();
        this.properties = properties;
    }

    @Override
    public PriorAuthorityApplicationResponse submitPriorAuthority(PriorAuthority priorAuthority) {
        String baseUrl = properties.urlFor(AccessDataStoreOperations.SUBMIT_PRIOR_AUTHORITY);
        return restClient.post()
                .uri(baseUrl + "/applications/{applicationId}/prior-authorities",
                        priorAuthority.applicationId())
                .contentType(MediaType.APPLICATION_JSON)
                .body(PriorAuthoritySubmission.from(priorAuthority))
                .retrieve()
                .body(PriorAuthorityApplicationResponse.class);
    }
}

package uk.gov.justice.laa_civil_manage_api.clients;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import uk.gov.justice.laa_civil_manage_api.models.Application;
import uk.gov.justice.laa_civil_manage_api.models.ApplicationResponse;

import java.util.List;

@Component
public class DataAccessApiClient {

    private final RestClient restClient;

    public DataAccessApiClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public List<Application> getApplications() {
        ApplicationResponse response = restClient.get()
                .uri("/api/v0/applications")
                .retrieve()
                .body(ApplicationResponse.class);

        return (response != null && response.applications() != null)
                ? response.applications()
                : List.of();
    }
}

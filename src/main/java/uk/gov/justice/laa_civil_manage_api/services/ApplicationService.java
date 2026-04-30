package uk.gov.justice.laa_civil_manage_api.services;

import org.springframework.stereotype.Service;
import uk.gov.justice.laa_civil_manage_api.clients.DataAccessApiClient;
import uk.gov.justice.laa_civil_manage_api.models.Application;

import java.util.List;

@Service
public class ApplicationService {

    private final DataAccessApiClient dataAccessApiClient;

    public ApplicationService(DataAccessApiClient dataAccessApiClient) {
        this.dataAccessApiClient = dataAccessApiClient;
    }

    public List<Application> getApplications() {
        return dataAccessApiClient.getApplications();
    }

    public Application getApplicationById(String id) {
        return getApplications().stream()
                .filter(app -> app.applicationId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + id));
    }
}

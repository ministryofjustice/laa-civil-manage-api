package uk.gov.justice.laa_civil_manage_api.services;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import uk.gov.justice.laa_civil_manage_api.models.Application;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final DataStoreClient dataStoreClient;

    public Application getApplicationById(String id) {

        return dataStoreClient.fetchApplicationById(id);
    }

    public List<Application> sgetAllApplications() {
        return dataStoreClient.fetchApplications();
    }
}

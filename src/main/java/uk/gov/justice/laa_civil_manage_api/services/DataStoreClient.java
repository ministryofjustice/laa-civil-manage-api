package uk.gov.justice.laa_civil_manage_api.services;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.justice.laa_civil_manage_api.config.LaaCivilManageApiApplicationConfig;
import uk.gov.justice.laa_civil_manage_api.models.Application;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataStoreClient {

    private final LaaCivilManageApiApplicationConfig config;
    private final RestTemplate restTemplate;

    public List<Application> fetchApplications() {
        log.info("Fetching applications from Data Store at {}", config.getDataStoreConfig().getUrl());
        ResponseEntity<List<Application>> response = restTemplate.exchange(
            config.getDataStoreConfig().getUrl() + "/applications", 
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<Application>>() {}
        );

        log.info(response.toString());

        return response.getBody();
    }

    public Application fetchApplicationById(String applicationId) {
        log.info("Fetching application by ID from Data Store at {}", config.getDataStoreConfig().getUrl());
        ResponseEntity<Application> response = restTemplate.exchange(
            config.getDataStoreConfig().getUrl() + "/applications/" + applicationId, 
            HttpMethod.GET,
            null,
            Application.class
        );

        return response.getBody();
        
    }

    public void saveApplication(Application application) {
        log.info("Saving application to Data Store at {}", config.getDataStoreConfig().getUrl());
        restTemplate.postForEntity(config.getDataStoreConfig().getUrl() + "/applications", application, Void.class);
    }

}

package uk.gov.justice.laa_civil_manage_api.controllers;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.justice.laa_civil_manage_api.config.LaaCivilManageApiConfig;
import uk.gov.justice.laa_civil_manage_api.models.Application;
import uk.gov.justice.laa_civil_manage_api.services.ApplicationService;

@RestController
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class ApplicationController {
        private final ApplicationService applicationService;
        private final LaaCivilManageApiConfig laaCivilManageApiConfig;
        private final RestTemplate restTemplate;

        @GetMapping("/applications")
        public List<Application> getApplications() {

                ResponseEntity<List<Application>> response = restTemplate.exchange(
                                laaCivilManageApiConfig.getDataStoreUrl() + "/applications",
                                HttpMethod.GET,
                                null,
                                new ParameterizedTypeReference<List<Application>>() {
                                });

                return response.getBody();

        }

        @GetMapping("/applications/{id}")
        public Application getApplicationById(@PathVariable String id) {
                return applicationService.getApplicationById(id);
        }

}

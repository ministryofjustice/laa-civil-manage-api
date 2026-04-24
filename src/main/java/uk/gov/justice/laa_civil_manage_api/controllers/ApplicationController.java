package uk.gov.justice.laa_civil_manage_api.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.justice.laa_civil_manage_api.models.Application;
import uk.gov.justice.laa_civil_manage_api.services.ApplicationService;

@RestController
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class ApplicationController {
    
    private final ApplicationService applicationService;

    @GetMapping("/applications")
    public List<Application> getApplications() {
        log.info("Received request to fetch all applications");

        return applicationService.getAllApplications();
    }
        
    @GetMapping("/applications/{applicationId}")
    public Application getApplicationById(@PathVariable String applicationId) {
        log.info("Received request to fetch application with ID: {}", applicationId);
        return applicationService.getApplicationById(applicationId);
    }

    @PostMapping("/application")
    public void createApplication(@RequestBody Application application) {
        log.info("Received request to create a new application");
        applicationService.createApplication(application);
        }

}

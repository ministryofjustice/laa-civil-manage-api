package uk.gov.justice.laa_civil_manage_api.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
        
    @GetMapping("/applications/{id}")
    public Application getApplicationById(@PathVariable String id) {
        log.info("Received request to fetch application with ID: {}", id);
        return applicationService.getApplicationById(id);
    }

}

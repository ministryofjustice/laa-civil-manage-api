package uk.gov.justice.laa_civil_manage_api.controllers;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.laa_civil_manage_api.models.Application;
import uk.gov.justice.laa_civil_manage_api.services.ApplicationService;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping("/applications")
    public List<Application> getApplications() {
        return applicationService.getApplications();
    }

    @GetMapping("/applications/{id}")
    public Application getApplicationById(@PathVariable String id) {
        return applicationService.getApplicationById(id);
    }
}

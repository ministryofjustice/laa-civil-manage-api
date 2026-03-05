package uk.gov.justice.laa_civil_manage_api.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import uk.gov.justice.laa_civil_manage_api.models.Application;
import uk.gov.justice.laa_civil_manage_api.services.ApplicationService;

@RestController
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ApplicationController {
    private final ApplicationService applicationService;

    @GetMapping("/applications")
    public List<Application> getApplications() {
        Application app1 = Application.builder()
                .applicationId("1")
                .clientFirstName("Ali")
                .clientLastName("Fletcher")
                .status("PENDING")
                .build();
        Application app2 = Application.builder()
                .applicationId("2")
                .clientFirstName("Lucas")
                .clientLastName("Morrison")
                .status("PENDING")
                .build();
        Application app3 = Application.builder()
                .applicationId("3")
                .clientFirstName("Denise")
                .clientLastName("Bennett")
                .status("PENDING")
                .build();
        Application app4 = Application.builder()
                .applicationId("4")
                .clientFirstName("Alan")
                .clientLastName("Harrington")
                .status("PENDING")
                .build();
        Application app5 = Application.builder()
                .applicationId("5")
                .clientFirstName("Tom")
                .clientLastName("Caldwell")
                .status("PENDING")
                .build();

        return List.of(app1, app2, app3, app4, app5);
    }

    @GetMapping("/applications/{id}")
    public Application getApplicationById(@PathVariable String id) {
        return applicationService.getApplicationById(id);
    }

}

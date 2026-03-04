package uk.gov.justice.laa_civil_manage_api.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import uk.gov.justice.laa_civil_manage_api.models.ApplicationModel;

@RestController
public class ApplicationController {
    @GetMapping("/applications")
    public List<ApplicationModel> getApplications() {
        ApplicationModel app1 = ApplicationModel.builder()
                .applicationId(UUID.randomUUID())
                .clientFirstName("Jane")
                .clientLastName("Smith")
                .status("PENDING")
                .build();

        ApplicationModel app2 = ApplicationModel.builder()
                .applicationId(UUID.randomUUID())
                .clientFirstName("Jane")
                .clientLastName("Smith")
                .status("PENDING")
                .build();

        return List.of(app1, app2);
    }
}

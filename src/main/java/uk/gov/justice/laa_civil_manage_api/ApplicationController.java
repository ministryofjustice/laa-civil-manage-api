package uk.gov.justice.laa_civil_manage_api;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import uk.gov.justice.laa_civil_manage_api.models.Application;

@RestController
public class ApplicationController {
    @GetMapping("/applications")
    public List<Application> getApplications() {
        Application app1 = Application.builder()
                .applicationId(UUID.randomUUID())
                .clientFirstName("Jane")
                .clientLastName("Smith")
                .status("PENDING")
                .build();

        Application app2 = Application.builder()
                .applicationId(UUID.randomUUID())
                .clientFirstName("Jane")
                .clientLastName("Smith")
                .status("PENDING")
                .build();

        return List.of(app1, app2);
    }

}

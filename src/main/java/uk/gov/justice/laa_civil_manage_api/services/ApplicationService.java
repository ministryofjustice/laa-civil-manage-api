package uk.gov.justice.laa_civil_manage_api.services;

import java.util.List;

import org.springframework.stereotype.Service;

import uk.gov.justice.laa_civil_manage_api.models.Application;

@Service
public class ApplicationService {

    public Application getApplicationById(String id) {

        return getAllApplications().stream()
                .filter(app -> app.getApplicationId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException());
    }

    private List<Application> getAllApplications() {
        return List.of(
                Application.builder()
                        .applicationId("1")
                        .clientFirstName("Ali")
                        .clientLastName("Fletcher")
                        .status("PENDING")
                        .build(),
                Application.builder()
                        .applicationId("2")
                        .clientFirstName("Lucas")
                        .clientLastName("Morrison")
                        .status("PENDING")
                        .build(),
                Application.builder()
                        .applicationId("3")
                        .clientFirstName("Denise")
                        .clientLastName("Bennett")
                        .status("PENDING")
                        .build(),
                Application.builder()
                        .applicationId("4")
                        .clientFirstName("Alan")
                        .clientLastName("Harrington")
                        .status("PENDING")
                        .build(),
                Application.builder()
                        .applicationId("5")
                        .clientFirstName("Tom")
                        .clientLastName("Caldwell")
                        .status("PENDING")
                        .build()

        );
    }
}

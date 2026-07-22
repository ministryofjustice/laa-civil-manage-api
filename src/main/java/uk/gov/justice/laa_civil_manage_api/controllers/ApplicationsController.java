package uk.gov.justice.laa_civil_manage_api.controllers;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.laa_civil_manage_api.models.ApplicationSummary;
import uk.gov.justice.laa_civil_manage_api.services.ApplicationsService;

@RestController
@RequestMapping("/applications")
@Slf4j
@RequiredArgsConstructor
public class ApplicationsController {

  private final ApplicationsService applicationsService;

  @GetMapping
  public ResponseEntity<List<ApplicationSummary>> getApplications() {
    log.info("Received request to fetch applications");
    List<ApplicationSummary> data = applicationsService.getApplicationsData();
    return ResponseEntity.ok(data);
  }
}

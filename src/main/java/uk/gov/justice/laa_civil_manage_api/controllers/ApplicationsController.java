package uk.gov.justice.laa_civil_manage_api.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.laa_civil_manage_api.models.ApplicationStatus;
import uk.gov.justice.laa_civil_manage_api.models.ApplicationSummary;
import uk.gov.justice.laa_civil_manage_api.models.ApplicationSummaryResponse;
import uk.gov.justice.laa_civil_manage_api.services.ApplicationsService;

@RestController
@RequestMapping("/applications")
@Slf4j
@RequiredArgsConstructor
public class ApplicationsController {

  private final ApplicationsService applicationsService;

  @GetMapping
  public ResponseEntity<ApplicationSummaryResponse> getApplications(
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int pageSize,
      // TODO Change to GRANTED once the Access Data Store API is updated to support
      // filtering by status
      @RequestParam(defaultValue = "APPLICATION_SUBMITTED") ApplicationStatus status) {
    log.info("Received request to fetch applications page {} with pageSize {}", page, pageSize);
    ApplicationSummaryResponse data =
        applicationsService.getApplicationsData(page, pageSize, status);
    return ResponseEntity.ok(data);
  }

  @GetMapping("/{applicationId}")
  public ResponseEntity<ApplicationSummary> getApplicationById(@PathVariable String applicationId) {
    log.info("Received request to fetch application with ID {}", applicationId);
    ApplicationSummary applicationSummary = applicationsService.getApplicationById(applicationId);
    return ResponseEntity.ok(applicationSummary);
  }
}

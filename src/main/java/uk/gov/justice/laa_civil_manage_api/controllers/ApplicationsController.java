package uk.gov.justice.laa_civil_manage_api.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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
      @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int pageSize) {
    log.info("Received request to fetch applications page {}", page);
    ApplicationSummaryResponse data = applicationsService.getApplicationsData(page, pageSize);
    return ResponseEntity.ok(data);
  }
}

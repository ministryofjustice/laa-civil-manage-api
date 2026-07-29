package uk.gov.justice.laa_civil_manage_api.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/client")
@Slf4j
public class ClientController {

  @GetMapping("/{applicationId}")
  public ResponseEntity<String> getClientByApplicationId(@PathVariable String applicationId) {
    log.info("Received request to fetch client for application with ID {}", applicationId);
    return ResponseEntity.ok("Client endpoint");
  }
}

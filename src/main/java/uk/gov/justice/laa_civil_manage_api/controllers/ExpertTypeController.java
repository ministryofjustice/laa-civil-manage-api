package uk.gov.justice.laa_civil_manage_api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.laa_civil_manage_api.services.ExpertTypeService;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ExpertTypeController {

  static final String DEFAULT_MATTER_TYPE = "KPBLW";

  private final ExpertTypeService expertTypeService;

  @Operation(
      summary = "List the expert types available for a matter type",
      description =
          "Sourced from the Legal Framework API. Returns an empty list when the matter type has no "
              + "associated expert types, including when it is not a recognised matter type code.")
  @GetMapping("/expertTypes")
  public List<String> getExpertTypes(
      @Parameter(description = "Matter type code, e.g. KPBLW", example = DEFAULT_MATTER_TYPE)
          @RequestParam(defaultValue = DEFAULT_MATTER_TYPE)
          String matterType) {
    log.info("Received request to fetch expert types for matter type {}", matterType);
    return expertTypeService.getExpertTypeDescriptions(matterType);
  }
}

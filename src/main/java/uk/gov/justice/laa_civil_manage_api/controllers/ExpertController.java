package uk.gov.justice.laa_civil_manage_api.controllers;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.laa_civil_manage_api.config.LaaCivilManageApiConfig;

@RestController
@RequiredArgsConstructor
public class ExpertController {

  private final LaaCivilManageApiConfig laaCivilManageApiConfig;

  @GetMapping("/expertTypes")
  public List<String> getExpertTypes() {
    return laaCivilManageApiConfig.expertTypes();
  }
}

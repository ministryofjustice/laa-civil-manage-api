package uk.gov.justice.laa_civil_manage_api.services;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uk.gov.justice.laa_civil_manage_api.services.legalframework.ExpertType;
import uk.gov.justice.laa_civil_manage_api.services.legalframework.LegalFrameworkClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExpertTypeService {

  private final LegalFrameworkClient legalFrameworkClient;

  public List<String> getExpertTypeDescriptions(String matterType) {
    return legalFrameworkClient.getExpertTypes(matterType).stream()
        .filter(
            expert -> {
              if (!StringUtils.hasText(expert.description())) {
                log.warn(
                    "LFA returned expert with missing/blank description for matter type {}. Code: '{}'",
                    matterType,
                    expert.code());
                return false;
              }
              return true;
            })
        .map(ExpertType::description)
        .toList();
  }
}

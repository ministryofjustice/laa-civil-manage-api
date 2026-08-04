package uk.gov.justice.laa_civil_manage_api.services;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uk.gov.justice.laa_civil_manage_api.services.legalframework.ExpertType;
import uk.gov.justice.laa_civil_manage_api.services.legalframework.LegalFrameworkClient;

@Service
@RequiredArgsConstructor
public class ExpertService {

  private final LegalFrameworkClient legalFrameworkClient;

  public List<String> getExpertTypeDescriptions(String matterType) {
    return legalFrameworkClient.getExpertTypes(matterType).stream()
        .map(ExpertType::description)
        .filter(StringUtils::hasText)
        .toList();
  }
}

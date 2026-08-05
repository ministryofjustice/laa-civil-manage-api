package uk.gov.justice.laa_civil_manage_api.services.legalframework;

import java.util.List;

public interface LegalFrameworkClient {

  List<ExpertType> getExpertTypes(String matterType);

  LegalFrameworkStatus getStatus();
}

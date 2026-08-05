package uk.gov.justice.laa_civil_manage_api.services.legalframework;

import java.util.List;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class HttpLegalFrameworkClient implements LegalFrameworkClient {

  private static final ParameterizedTypeReference<List<ExpertType>> EXPERT_TYPE_LIST_TYPE =
      new ParameterizedTypeReference<>() {};

  private final RestClient restClient;
  private final LegalFrameworkProperties properties;

  public HttpLegalFrameworkClient(
      RestClient legalFrameworkRestClient, LegalFrameworkProperties properties) {
    this.restClient = legalFrameworkRestClient;
    this.properties = properties;
  }

  @Override
  public List<ExpertType> getExpertTypes(String matterType) {
    List<ExpertType> expertTypes =
        restClient
            .get()
            .uri(properties.requireBaseUrl() + "/expert_types/{matterType}", matterType)
            .retrieve()
            .body(EXPERT_TYPE_LIST_TYPE);
    return expertTypes == null ? List.of() : expertTypes;
  }

  @Override
  public LegalFrameworkStatus getStatus() {
    return restClient
        .get()
        .uri(properties.requireBaseUrl() + "/status")
        .retrieve()
        .body(LegalFrameworkStatus.class);
  }
}

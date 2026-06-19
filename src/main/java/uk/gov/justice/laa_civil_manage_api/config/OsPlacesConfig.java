package uk.gov.justice.laa_civil_manage_api.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "laa-civil-manage-api.os-places")
public record OsPlacesConfig(String baseUrl, String apiKey, List<Integer> londonCustodianCodes) {

  public OsPlacesConfig {
    londonCustodianCodes = londonCustodianCodes == null ? List.of() : List.copyOf(londonCustodianCodes);
  }

}

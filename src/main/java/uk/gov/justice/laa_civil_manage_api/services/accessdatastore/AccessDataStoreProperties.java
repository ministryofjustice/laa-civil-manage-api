package uk.gov.justice.laa_civil_manage_api.services.accessdatastore;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "laa-civil-manage-api.access-data-store")
public record AccessDataStoreProperties(
    String baseUrl, Duration connectTimeout, Duration readTimeout) {
  public AccessDataStoreProperties {
    if (baseUrl == null || baseUrl.isBlank()) {
      throw new IllegalStateException("No Access Data Store URL configured");
    }
  }
}

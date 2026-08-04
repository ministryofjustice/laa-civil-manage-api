package uk.gov.justice.laa_civil_manage_api.services.legalframework;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "laa-civil-manage-api.legal-framework")
public record LegalFrameworkProperties(
    String baseUrl, Duration connectTimeout, Duration readTimeout) {

  public String requireBaseUrl() {
    if (baseUrl == null || baseUrl.isBlank()) {
      throw new IllegalStateException("No Legal Framework API base URL configured");
    }
    return baseUrl;
  }
}

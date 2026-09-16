package uk.gov.justice.laa_civil_manage_api.services.accessdatastore;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component("accessDataStore")
@Slf4j
public class AccessDataStoreHealthIndicator implements HealthIndicator {

  private final RestClient healthRestClient;
  private final AccessDataStoreProperties properties;

  public AccessDataStoreHealthIndicator(
      RestClient.Builder restClientBuilder, AccessDataStoreProperties properties) {
    this.healthRestClient = restClientBuilder.baseUrl(properties.baseUrl()).build();
    this.properties = properties;
  }

  @Override
  public Health health() {
    try {
      AccessDataStoreHealthResponse response =
          healthRestClient
              .get()
              .uri("/actuator/health")
              .retrieve()
              .body(AccessDataStoreHealthResponse.class);

      if (response != null && "UP".equals(response.status())) {
        return Health.up().withDetail("baseUrl", properties.baseUrl()).build();
      }

      String reportedStatus = response == null ? "unknown" : response.status();
      log.warn("Access Data Store health check returned non-UP status: {}", reportedStatus);
      return Health.down()
          .withDetail("baseUrl", properties.baseUrl())
          .withDetail("error", "Access Data Store reported status: " + reportedStatus)
          .build();
    } catch (Exception e) {
      log.warn("Access Data Store health check failed", e);
      return Health.down(e)
          .withDetail("baseUrl", properties.baseUrl())
          .withDetail("error", e.getMessage())
          .build();
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  private record AccessDataStoreHealthResponse(String status) {}
}

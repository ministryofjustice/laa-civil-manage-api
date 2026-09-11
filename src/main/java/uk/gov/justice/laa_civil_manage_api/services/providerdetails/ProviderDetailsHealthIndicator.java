package uk.gov.justice.laa_civil_manage_api.services.providerdetails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Reports the Provider Details API's own {@code /actuator/health} check as a {@code
 * providerDetails} component of {@code /actuator/health}. Deliberately excluded from the liveness
 * and readiness health groups the Kubernetes probes use - a Provider Details outage should be
 * visible to monitoring, not restart our pods or pull them out of service.
 */
@Component("providerDetails")
@Slf4j
public class ProviderDetailsHealthIndicator implements HealthIndicator {

  private final RestClient providerDetailsRestClient;
  private final ProviderDetailsProperties properties;

  public ProviderDetailsHealthIndicator(
      RestClient providerDetailsRestClient, ProviderDetailsProperties properties) {
    this.providerDetailsRestClient = providerDetailsRestClient;
    this.properties = properties;
  }

  @Override
  public Health health() {
    try {
      providerDetailsRestClient.get().uri("/actuator/health").retrieve().toBodilessEntity();
      return Health.up().withDetail("baseUrl", properties.baseUrl()).build();
    } catch (Exception e) {
      log.warn("Provider Details API health check failed", e);
      return Health.down(e).withDetail("baseUrl", properties.baseUrl()).build();
    }
  }
}

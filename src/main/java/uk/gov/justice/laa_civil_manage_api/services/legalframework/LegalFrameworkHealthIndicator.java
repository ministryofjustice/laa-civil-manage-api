package uk.gov.justice.laa_civil_manage_api.services.legalframework;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Reports the Legal Framework API's own {@code /status} check as a {@code legalFramework} component
 * of {@code /actuator/health}. Deliberately excluded from the liveness and readiness health groups
 * the Kubernetes probes use - a Legal Framework outage should be visible to monitoring, not restart
 * our pods or pull them out of service.
 */
@Component("legalFramework")
@Slf4j
@RequiredArgsConstructor
public class LegalFrameworkHealthIndicator implements HealthIndicator {

  private final LegalFrameworkClient legalFrameworkClient;

  @Override
  public Health health() {
    try {
      LegalFrameworkStatus status = legalFrameworkClient.getStatus();
      if (status == null) {
        return Health.down().withDetail("reason", "no status returned").build();
      }

      Health.Builder builder = status.isHealthy() ? Health.up() : Health.down();
      status.checks().forEach(builder::withDetail);
      return builder.build();
    } catch (Exception e) {
      log.warn("Legal Framework API health check failed", e);
      return Health.down(e).build();
    }
  }
}

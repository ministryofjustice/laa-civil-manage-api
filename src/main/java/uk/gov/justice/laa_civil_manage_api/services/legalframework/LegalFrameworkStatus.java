package uk.gov.justice.laa_civil_manage_api.services.legalframework;

import java.util.Map;

public record LegalFrameworkStatus(Map<String, Boolean> checks) {

  public LegalFrameworkStatus {
    checks = checks == null ? Map.of() : Map.copyOf(checks);
  }

  public boolean isHealthy() {
    return !checks.isEmpty() && checks.values().stream().allMatch(Boolean.TRUE::equals);
  }
}

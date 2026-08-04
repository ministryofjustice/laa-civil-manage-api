package uk.gov.justice.laa_civil_manage_api.services.legalframework;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.Status;
import org.springframework.web.client.ResourceAccessException;

class LegalFrameworkHealthIndicatorTest {

  private final LegalFrameworkClient client = mock(LegalFrameworkClient.class);
  private final LegalFrameworkHealthIndicator indicator = new LegalFrameworkHealthIndicator(client);

  @Test
  void reportsUpWithTheUpstreamChecksAsDetailsWhenEveryCheckPasses() {
    when(client.getStatus()).thenReturn(new LegalFrameworkStatus(Map.of("database", true)));

    Health health = indicator.health();

    assertEquals(Status.UP, health.getStatus());
    assertEquals(true, health.getDetails().get("database"));
  }

  @Test
  void reportsDownWhenAnyUpstreamCheckFails() {
    Map<String, Boolean> checks = new LinkedHashMap<>();
    checks.put("database", false);
    when(client.getStatus()).thenReturn(new LegalFrameworkStatus(checks));

    Health health = indicator.health();

    assertEquals(Status.DOWN, health.getStatus());
    assertEquals(false, health.getDetails().get("database"));
  }

  @Test
  void reportsDownWhenTheApiIsUnreachable() {
    when(client.getStatus()).thenReturn(null);
    assertEquals(Status.DOWN, indicator.health().getStatus());

    when(client.getStatus()).thenThrow(new ResourceAccessException("connection refused"));
    assertEquals(Status.DOWN, indicator.health().getStatus());
  }
}

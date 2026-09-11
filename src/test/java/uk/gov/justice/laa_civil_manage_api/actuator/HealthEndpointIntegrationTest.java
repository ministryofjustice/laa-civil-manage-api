package uk.gov.justice.laa_civil_manage_api.actuator;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
    properties = {
      "laa-civil-manage-api.provider-details.base-url=http://localhost:9999",
      "laa-civil-manage-api.legal-framework.base-url=http://localhost:9999"
    })
@AutoConfigureMockMvc
class HealthEndpointIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Test
  void globalHealthReportsDownWhenDownstreamServicesAreUnreachable() throws Exception {
    mockMvc
        .perform(get("/actuator/health"))
        .andExpect(status().isServiceUnavailable())
        .andExpect(jsonPath("$.status").value("DOWN"))
        .andExpect(jsonPath("$.components.providerDetails.status").value("DOWN"))
        .andExpect(jsonPath("$.components.legalFramework.status").value("DOWN"));
  }

  @Test
  void livenessProbeRemainsUpSoKubernetesDoesNotKillThePod() throws Exception {
    mockMvc
        .perform(get("/actuator/health/liveness"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("UP"));
  }

  @Test
  void readinessProbeRemainsUpSoKubernetesKeepsRoutingTraffic() throws Exception {
    mockMvc
        .perform(get("/actuator/health/readiness"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("UP"));
  }
}

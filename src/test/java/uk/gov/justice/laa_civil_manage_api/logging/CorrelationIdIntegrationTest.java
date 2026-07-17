package uk.gov.justice.laa_civil_manage_api.logging;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.justice.laa_civil_manage_api.services.accessdatastore.AccessDataStoreClient;

@SpringBootTest
@AutoConfigureMockMvc
class CorrelationIdIntegrationTest {

  private static final String UUID_PATTERN =
      "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}";

  @Autowired private MockMvc mockMvc;

  @MockitoBean private AccessDataStoreClient accessDataStoreClient;

  @Test
  void echoesInboundCorrelationIdOnResponse() throws Exception {
    mockMvc
        .perform(get("/applications").header(CorrelationId.HEADER, "inbound-xyz").with(jwt()))
        .andExpect(status().isOk())
        .andExpect(header().string(CorrelationId.HEADER, "inbound-xyz"));
  }

  @Test
  void generatesCorrelationIdOnResponseWhenNoneProvided() throws Exception {
    mockMvc
        .perform(get("/applications").with(jwt()))
        .andExpect(status().isOk())
        .andExpect(header().string(CorrelationId.HEADER, matchesPattern(UUID_PATTERN)));
  }
}

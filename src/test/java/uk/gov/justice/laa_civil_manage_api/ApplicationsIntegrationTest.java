package uk.gov.justice.laa_civil_manage_api;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
class ApplicationsIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private AccessDataStoreClient accessDataStoreClient;

  @Test
  void returnsApplicationsFromDataStoreForAuthenticatedRequest() throws Exception {
    when(accessDataStoreClient.getApplications()).thenReturn("[{\"id\":\"APP-1\"}]");

    mockMvc
        .perform(get("/applications").with(jwt()))
        .andExpect(status().isOk())
        .andExpect(content().string("[{\"id\":\"APP-1\"}]"));
  }

  @Test
  void returnsUnauthorizedAndDoesNotCallDataStoreWhenNoTokenPresent() throws Exception {
    mockMvc.perform(get("/applications")).andExpect(status().isUnauthorized());

    verifyNoInteractions(accessDataStoreClient);
  }
}

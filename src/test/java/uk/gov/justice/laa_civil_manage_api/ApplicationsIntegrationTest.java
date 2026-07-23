package uk.gov.justice.laa_civil_manage_api;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.justice.laa_civil_manage_api.models.ApplicationSummary;
import uk.gov.justice.laa_civil_manage_api.models.ApplicationSummaryResponse;
import uk.gov.justice.laa_civil_manage_api.services.accessdatastore.AccessDataStoreClient;

@SpringBootTest
@AutoConfigureMockMvc
class ApplicationsIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private AccessDataStoreClient accessDataStoreClient;

  @Test
  void returnsApplicationsFromDataStoreForAuthenticatedRequest() throws Exception {
    ApplicationSummary application =
        ApplicationSummary.builder()
            .applicationId(UUID.fromString("11111111-2222-3333-4444-555555555555"))
            .laaReference("APP-1")
            .status("APPLICATION_SUBMITTED")
            .startDate(OffsetDateTime.parse("2026-07-22T10:00:00Z"))
            .clientFirstName("John")
            .clientLastName("Doe")
            .build();

    when(accessDataStoreClient.getApplications(1))
        .thenReturn(
            ApplicationSummaryResponse.builder()
                .paging(
                    uk.gov.justice.laa_civil_manage_api.models.Paging.builder()
                        .page(1)
                        .pageSize(10)
                        .itemsReturned(1)
                        .totalRecords(1)
                        .build())
                .applications(List.of(application))
                .build());

    mockMvc
        .perform(get("/applications").with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.paging.page").value(1))
        .andExpect(jsonPath("$.paging.pageSize").value(10))
        .andExpect(jsonPath("$.paging.itemsReturned").value(1))
        .andExpect(jsonPath("$.paging.totalRecords").value(1))
        .andExpect(jsonPath("$.applications.length()").value(1))
        .andExpect(jsonPath("$.applications[0].laaReference").value("APP-1"));
  }

  @Test
  void returnsUnauthorizedAndDoesNotCallDataStoreWhenNoTokenPresent() throws Exception {
    mockMvc.perform(get("/applications")).andExpect(status().isUnauthorized());

    verifyNoInteractions(accessDataStoreClient);
  }
}

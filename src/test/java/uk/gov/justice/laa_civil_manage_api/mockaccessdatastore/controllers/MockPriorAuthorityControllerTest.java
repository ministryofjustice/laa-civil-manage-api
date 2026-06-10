package uk.gov.justice.laa_civil_manage_api.mockaccessdatastore.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class MockPriorAuthorityControllerTest {

  @Autowired private MockMvc mockMvc;

  @Test
  void returns201ForValidSubmission() throws Exception {
    String body =
        """
                {
                  "type": "EXPERT",
                  "expertType": "Psychologist",
                  "expertFullName": "John Doe",
                  "expertBasedInLondon": true,
                  "guidelineRatesExceeded": false,
                  "billingType": "FLAT_RATE",
                  "flatRateTotalAmount": 249.99
                }
                """;

    mockMvc
        .perform(
            post("/mock-access-data-store/applications/{id}/prior-authority", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.submissionId").exists())
        .andExpect(jsonPath("$.status").value("ACCEPTED"));
  }

  @Test
  void repeatedSubmissionsForSameApplicationIdAreIdempotent() throws Exception {
    UUID applicationId = UUID.randomUUID();
    String body =
        """
                {
                  "type": "EXPERT",
                  "expertType": "Psychologist",
                  "expertFullName": "John Doe",
                  "expertBasedInLondon": false,
                  "guidelineRatesExceeded": false,
                  "billingType": "FLAT_RATE",
                  "flatRateTotalAmount": 249.99
                }
                """;

    MvcResult first =
        mockMvc
            .perform(
                post("/mock-access-data-store/applications/{id}/prior-authority", applicationId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
            .andExpect(status().isCreated())
            .andReturn();

    mockMvc
        .perform(
            post("/mock-access-data-store/applications/{id}/prior-authority", applicationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isCreated())
        .andExpect(
            result -> {
              String firstBody = first.getResponse().getContentAsString();
              String secondBody = result.getResponse().getContentAsString();
              if (!firstBody.equals(secondBody)) {
                throw new AssertionError(
                    "Expected idempotent response. First: " + firstBody + " Second: " + secondBody);
              }
            });
  }

  @Test
  void returns400ForInvalidSubmission() throws Exception {
    String body =
        """
                {
                  "guidelineRatesExceeded": false,
                  "billingType": "FLAT_RATE",
                  "flatRateTotalAmount": 100.00
                }
                """;

    mockMvc
        .perform(
            post("/mock-access-data-store/applications/{id}/prior-authority", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isBadRequest());
  }
}

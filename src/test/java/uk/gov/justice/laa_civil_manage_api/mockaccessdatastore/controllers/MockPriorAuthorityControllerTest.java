package uk.gov.justice.laa_civil_manage_api.mockaccessdatastore.controllers;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
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
                  "priorAuthorityType": "EXPERT",
                  "expertType": "Psychologist",
                  "expertFullName": "John Doe",
                  "expertBasedInLondon": true,
                  "billingType": "FIXED_RATE",
                  "totalAmount": 249.99,
                  "justification": "Required expert evidence."
                }
                """;

    mockMvc
        .perform(
            post("/mock-access-data-store/applications/{id}/prior-authority", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .with(jwt()))
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
                  "priorAuthorityType": "EXPERT",
                  "expertType": "Psychologist",
                  "expertFullName": "John Doe",
                  "expertBasedInLondon": false,
                  "billingType": "FIXED_RATE",
                  "totalAmount": 249.99,
                  "justification": "Required expert evidence."
                }
                """;
    MvcResult first =
        mockMvc
            .perform(
                post("/mock-access-data-store/applications/{id}/prior-authority", applicationId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
                    .with(jwt()))
            .andExpect(status().isCreated())
            .andReturn();

    mockMvc
        .perform(
            post("/mock-access-data-store/applications/{id}/prior-authority", applicationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .with(jwt()))
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
                  "priorAuthorityType": "EXPERT",
                  "billingType": "FIXED_RATE",
                  "totalAmount": 100.00
                }
                """;
    mockMvc
        .perform(
            post("/mock-access-data-store/applications/{id}/prior-authority", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .with(jwt()))
        .andExpect(status().isBadRequest());
  }

  @Test
  void returns400WhenTimeMinutesIsGreaterThan59() throws Exception {
    String body =
        """
                {
                  "priorAuthorityType": "EXPERT",
                  "expertType": "Psychologist",
                  "billingType": "HOURLY",
                  "hourlyRate": 50.00,
                  "timeHours": 1,
                  "timeMinutes": 60,
                  "totalAmount": 50.00,
                  "justification": "Specialist evidence is required."
                }
                """;
    mockMvc
        .perform(
            post("/mock-access-data-store/applications/{id}/prior-authority", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .with(jwt()))
        .andExpect(status().isBadRequest());
  }

  @Test
  void returns400WhenCounselTypeMissingForCounselSubmission() throws Exception {
    String body =
        """
                {
                  "priorAuthorityType": "COUNSEL",
                  "billingType": "FIXED_RATE",
                  "totalAmount": 249.99,
                  "justification": "Counsel representation required."
                }
                """;
    mockMvc
        .perform(
            post("/mock-access-data-store/applications/{id}/prior-authority", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .with(jwt()))
        .andExpect(status().isBadRequest());
  }

  @Test
  void returns201WhenCounselTypeProvidedForCounselSubmission() throws Exception {
    String body =
        """
                {
                  "priorAuthorityType": "COUNSEL",
                  "counselType": "KINGS_COUNSEL_AND_TWO_JUNIOR_COUNSEL",
                  "billingType": "FIXED_RATE",
                  "totalAmount": 249.99,
                  "justification": "Counsel representation required."
                }
                """;
    mockMvc
        .perform(
            post("/mock-access-data-store/applications/{id}/prior-authority", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .with(jwt()))
        .andExpect(status().isCreated());
  }
}

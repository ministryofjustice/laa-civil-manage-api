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
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@AutoConfigureMockMvc
class MockPriorAuthorityControllerTest {

  private static final String EXPERT_FIXED_RATE =
      """
      {
        "priorAuthorityType": "EXPERT",
        "justification": "Required expert evidence.",
        "expertDetails": {
          "expertType": "Psychologist",
          "expertFullName": "Dr John Doe",
          "expertPostcode": "SW1H 9AJ",
          "expertCosts": { "billingType": "FIXED_RATE", "totalAmount": 249.99, "costsSharedWithOtherParties": false }
        }
      }
      """;

  @Autowired private MockMvc mockMvc;

  private ResultActions submit(UUID applicationId, String body) throws Exception {
    return mockMvc.perform(
        post("/mock-access-data-store/applications/{id}/prior-authority", applicationId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body)
            .with(jwt()));
  }

  @Test
  void returns201ForValidExpertSubmission() throws Exception {
    submit(UUID.randomUUID(), EXPERT_FIXED_RATE)
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.submissionId").exists())
        .andExpect(jsonPath("$.status").value("ACCEPTED"));
  }

  @Test
  void returns201ForAnHourlyApportionedExpertSubmission() throws Exception {
    String body =
        """
        {
          "priorAuthorityType": "EXPERT",
          "justification": "Costs are shared four ways.",
          "uploadedDocuments": [ { "fileName": "psych_assessment.pdf" } ],
          "expertDetails": {
            "expertType": "Psychologist",
            "expertFullName": "Dr John Doe",
            "expertPostcode": "SW1H 9AJ",
            "expertCosts": {
              "billingType": "HOURLY",
              "hourlyRate": 50.00,
              "timeRequested": { "hours": 2, "minutes": 30 },
              "totalAmount": 125.00,
              "costsSharedWithOtherParties": true,
              "apportionment": { "partiesSharingCosts": 4, "clientShareAmount": 31.25 }
            }
          }
        }
        """;

    submit(UUID.randomUUID(), body).andExpect(status().isCreated());
  }

  @Test
  void returns201ForCounselSubmissionWhichCarriesNoBillingOrAmount() throws Exception {
    String body =
        """
        {
          "priorAuthorityType": "COUNSEL",
          "justification": "Counsel is required to advise on complex points of law.",
          "uploadedDocuments": [ { "fileName": "instructions.pdf" } ],
          "counselDetails": { "counselType": "KINGS_COUNSEL_ALONE" }
        }
        """;

    submit(UUID.randomUUID(), body)
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.submissionId").exists())
        .andExpect(jsonPath("$.status").value("ACCEPTED"));
  }

  @Test
  void returns201ForDisbursementSubmission() throws Exception {
    String body =
        """
        {
          "priorAuthorityType": "DISBURSEMENT",
          "justification": "Train fare for the expert to attend in person.",
          "disbursementDetails": { "disbursementPurpose": "Travel", "disbursementAmount": 125.50 }
        }
        """;

    submit(UUID.randomUUID(), body).andExpect(status().isCreated());
  }

  @Test
  void repeatedSubmissionsForSameApplicationIdAreIdempotent() throws Exception {
    UUID applicationId = UUID.randomUUID();

    MvcResult first =
        submit(applicationId, EXPERT_FIXED_RATE).andExpect(status().isCreated()).andReturn();

    submit(applicationId, EXPERT_FIXED_RATE)
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
  void returns400WhenExpertDetailsMissingForExpertSubmission() throws Exception {
    String body =
        """
        {
          "priorAuthorityType": "EXPERT",
          "justification": "Required expert evidence."
        }
        """;

    submit(UUID.randomUUID(), body).andExpect(status().isBadRequest());
  }

  @Test
  void returns400WhenJustificationMissing() throws Exception {
    String body =
        """
        {
          "priorAuthorityType": "EXPERT",
          "expertDetails": {
            "expertType": "Psychologist",
            "expertFullName": "Dr John Doe",
            "expertPostcode": "SW1H 9AJ",
            "expertCosts": { "billingType": "FIXED_RATE", "totalAmount": 100.00, "costsSharedWithOtherParties": false }
          }
        }
        """;

    submit(UUID.randomUUID(), body).andExpect(status().isBadRequest());
  }

  @Test
  void returns400WhenTimeMinutesIsGreaterThan59() throws Exception {
    String body =
        """
        {
          "priorAuthorityType": "EXPERT",
          "justification": "Specialist evidence is required.",
          "expertDetails": {
            "expertType": "Psychologist",
            "expertFullName": "Dr John Doe",
            "expertPostcode": "SW1H 9AJ",
            "expertCosts": {
              "billingType": "HOURLY",
              "hourlyRate": 50.00,
              "timeRequested": { "hours": 1, "minutes": 60 },
              "totalAmount": 50.00,
              "costsSharedWithOtherParties": false
            }
          }
        }
        """;

    submit(UUID.randomUUID(), body).andExpect(status().isBadRequest());
  }

  @Test
  void returns400WhenCounselDetailsMissingForCounselSubmission() throws Exception {
    String body =
        """
        {
          "priorAuthorityType": "COUNSEL",
          "justification": "Counsel representation required."
        }
        """;

    submit(UUID.randomUUID(), body).andExpect(status().isBadRequest());
  }

  @Test
  void returns201WhenCounselTypeProvidedForCounselSubmission() throws Exception {
    String body =
        """
        {
          "priorAuthorityType": "COUNSEL",
          "justification": "Counsel representation required.",
          "counselDetails": { "counselType": "KINGS_COUNSEL_AND_TWO_JUNIOR_COUNSEL" }
        }
        """;

    submit(UUID.randomUUID(), body).andExpect(status().isCreated());
  }

  @Test
  void returns400WhenABlockForTheWrongTypeIsSupplied() throws Exception {
    String body =
        """
        {
          "priorAuthorityType": "COUNSEL",
          "justification": "Counsel representation required.",
          "counselDetails": { "counselType": "KINGS_COUNSEL_ALONE" },
          "expertDetails": {
            "expertType": "Psychologist",
            "expertFullName": "Dr John Doe",
            "expertPostcode": "SW1H 9AJ",
            "expertCosts": { "billingType": "FIXED_RATE", "totalAmount": 100.00, "costsSharedWithOtherParties": false }
          }
        }
        """;

    submit(UUID.randomUUID(), body).andExpect(status().isBadRequest());
  }
}

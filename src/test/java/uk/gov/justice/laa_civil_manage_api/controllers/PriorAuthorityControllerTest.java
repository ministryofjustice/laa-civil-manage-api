package uk.gov.justice.laa_civil_manage_api.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import uk.gov.justice.laa_civil_manage_api.models.PriorAuthority;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthorityApplicationResponse;
import uk.gov.justice.laa_civil_manage_api.models.SubmissionStatus;
import uk.gov.justice.laa_civil_manage_api.services.PriorAuthorityService;

@WebMvcTest(PriorAuthorityController.class)
class PriorAuthorityControllerTest {

    private static final UUID SUBMISSION_ID = UUID.fromString("11111111-2222-3333-4444-555555555555");
    private static final String APPLICATION_ID = "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PriorAuthorityService priorAuthorityService;

    @Test
    void returns201WithLocationAndBodyOnSuccessfulHourlySubmission() throws Exception {
        when(priorAuthorityService.submit(any(PriorAuthority.class)))
                .thenReturn(PriorAuthorityApplicationResponse.builder()
                        .submissionId(SUBMISSION_ID)
                        .status(SubmissionStatus.ACCEPTED)
                        .submittedAt(OffsetDateTime.parse("2026-05-22T10:00:00Z"))
                        .build());

        String body = """
                {
                  "applicationId": "%s",
                  "type": "EXPERT",
                  "expertType": "Psychologist",
                  "expertFullName": "John Doe",
                  "uploadedDocuments": [
                    { "fileName": "report.pdf" }
                  ],
                  "guidelineRatesExceeded": true,
                  "billingType": "HOURLY",
                  "hourlyRate": 50.00,
                  "estimatedTime": { "hours": 2, "minutes": 30 },
                  "totalAmount": 125.00
                }
                """.formatted(APPLICATION_ID);

        mockMvc.perform(post("/prior-authority-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/prior-authority-requests/" + SUBMISSION_ID))
                .andExpect(jsonPath("$.submissionId").value(SUBMISSION_ID.toString()))
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }

    @Test
    void returns201ForFlatRateSubmission() throws Exception {
        when(priorAuthorityService.submit(any(PriorAuthority.class)))
                .thenReturn(PriorAuthorityApplicationResponse.builder()
                        .submissionId(SUBMISSION_ID)
                        .status(SubmissionStatus.ACCEPTED)
                        .submittedAt(OffsetDateTime.parse("2026-05-22T10:00:00Z"))
                        .build());

        String body = """
                {
                  "applicationId": "%s",
                  "type": "EXPERT",
                  "expertType": "Psychologist",
                  "expertFullName": "John Doe",
                  "guidelineRatesExceeded": false,
                  "billingType": "FLAT_RATE",
                  "flatRateTotalAmount": 249.99
                }
                """.formatted(APPLICATION_ID);

        mockMvc.perform(post("/prior-authority-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    @Test
    void returns400WhenApplicationIdMissing() throws Exception {
        String body = """
                {
                  "type": "EXPERT",
                  "expertType": "Psychologist",
                  "expertFullName": "John Doe",
                  "guidelineRatesExceeded": false,
                  "billingType": "FLAT_RATE",
                  "flatRateTotalAmount": 100.00
                }
                """;

        mockMvc.perform(post("/prior-authority-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returns400WhenTypeMissing() throws Exception {
        String body = """
                {
                  "applicationId": "%s",
                  "expertFullName": "John Doe",
                  "guidelineRatesExceeded": false,
                  "billingType": "FLAT_RATE",
                  "flatRateTotalAmount": 100.00
                }
                """.formatted(APPLICATION_ID);

        mockMvc.perform(post("/prior-authority-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returns400WhenHourlyBillingMissesHourlyFields() throws Exception {
        String body = """
                {
                  "applicationId": "%s",
                  "type": "EXPERT",
                  "expertType": "Psychologist",
                  "expertFullName": "John Doe",
                  "guidelineRatesExceeded": true,
                  "billingType": "HOURLY"
                }
                """.formatted(APPLICATION_ID);

        mockMvc.perform(post("/prior-authority-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returns400WhenExpertTypeIsExpertButExpertTypeMissing() throws Exception {
        String body = """
                {
                  "applicationId": "%s",
                  "type": "EXPERT",
                  "expertFullName": "John Doe",
                  "guidelineRatesExceeded": false,
                  "billingType": "FLAT_RATE",
                  "flatRateTotalAmount": 100.00
                }
                """.formatted(APPLICATION_ID);

        mockMvc.perform(post("/prior-authority-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}

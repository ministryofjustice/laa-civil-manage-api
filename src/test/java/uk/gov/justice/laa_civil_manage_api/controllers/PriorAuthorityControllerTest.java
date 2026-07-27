package uk.gov.justice.laa_civil_manage_api.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.justice.laa_civil_manage_api.config.SecurityConfig;
import uk.gov.justice.laa_civil_manage_api.models.BillingType;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthority;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthorityApplicationResponse;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthorityDraft;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthorityDraftSummary;
import uk.gov.justice.laa_civil_manage_api.models.SubmissionStatus;
import uk.gov.justice.laa_civil_manage_api.models.UploadedDocument;
import uk.gov.justice.laa_civil_manage_api.services.PriorAuthorityDraftService;
import uk.gov.justice.laa_civil_manage_api.services.PriorAuthorityService;

@WebMvcTest(PriorAuthorityController.class)
@Import(SecurityConfig.class)
@AutoConfigureMockMvc(addFilters = false)
class PriorAuthorityControllerTest {

  private static final UUID SUBMISSION_ID = UUID.fromString("11111111-2222-3333-4444-555555555555");
  private static final UUID DRAFT_ID = UUID.fromString("c3b07e24-d92b-410a-9d95-88f117a12b43");
  private static final String APPLICATION_ID = "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee";
  private static final String DRAFT_APPLICATION_ID = "2a28f60d-fe15-43fe-92c3-5530595d5f51";

  @Autowired private MockMvc mockMvc;

  @MockitoBean private PriorAuthorityService priorAuthorityService;

  @MockitoBean private PriorAuthorityDraftService draftService;

  @Test
  void returns201WithLocationAndBodyOnSuccessfulHourlySubmission() throws Exception {
    when(priorAuthorityService.submit(any(PriorAuthority.class)))
        .thenReturn(
            PriorAuthorityApplicationResponse.builder()
                .submissionId(SUBMISSION_ID)
                .status(SubmissionStatus.ACCEPTED)
                .submittedAt(OffsetDateTime.parse("2026-05-22T10:00:00Z"))
                .build());

    String body =
        """
                        {
                          "applicationId": "%s",
                          "priorAuthorityType": "EXPERT",
                          "expertType": "Psychologist",
                          "expertFullName": "John Doe",
                          "expertPostcode": "SW1H 9AJ",
                          "expertBasedInLondon": true,
                          "uploadedDocuments": [
                            { "fileName": "report.pdf" }
                          ],
                          "billingType": "HOURLY",
                          "hourlyRate": 50.00,
                          "timeHours": 2,
                          "timeMinutes": 30,
                          "totalAmount": 125.00,
                          "justification": "Specialist evidence is required."
                        }
                        """
            .formatted(APPLICATION_ID);

    mockMvc
        .perform(post("/prior-authority").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "/prior-authority/" + SUBMISSION_ID))
        .andExpect(jsonPath("$.submissionId").value(SUBMISSION_ID.toString()))
        .andExpect(jsonPath("$.status").value("ACCEPTED"));
  }

  @Test
  void returns201ForFixedRateSubmission() throws Exception {
    when(priorAuthorityService.submit(any(PriorAuthority.class)))
        .thenReturn(
            PriorAuthorityApplicationResponse.builder()
                .submissionId(SUBMISSION_ID)
                .status(SubmissionStatus.ACCEPTED)
                .submittedAt(OffsetDateTime.parse("2026-05-22T10:00:00Z"))
                .build());

    String body =
        """
                        {
                          "applicationId": "%s",
                          "priorAuthorityType": "EXPERT",
                          "expertType": "Psychologist",
                          "expertFullName": "John Doe",
                          "expertBasedInLondon": false,
                          "billingType": "FIXED_RATE",
                          "totalAmount": 249.99,
                          "justification": "A fixed fee is appropriate for this work."
                        }
                        """
            .formatted(APPLICATION_ID);

    mockMvc
        .perform(post("/prior-authority").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isCreated());
  }

  @Test
  void returns400WhenApplicationIdMissing() throws Exception {
    String body =
        """
                        {
                          "priorAuthorityType": "EXPERT",
                          "expertType": "Psychologist",
                          "expertFullName": "John Doe",
                          "expertBasedInLondon": false,
                          "billingType": "FIXED_RATE",
                          "totalAmount": 100.00,
                          "justification": "Required to progress the case."
                        }
                        """;

    mockMvc
        .perform(post("/prior-authority").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isBadRequest());
  }

  @Test
  void returns400WhenTypeMissing() throws Exception {
    String body =
        """
                        {
                          "applicationId": "%s",
                          "expertFullName": "John Doe",
                          "expertBasedInLondon": false,
                          "billingType": "FIXED_RATE",
                          "totalAmount": 100.00,
                          "justification": "Required to progress the case."
                        }
                        """
            .formatted(APPLICATION_ID);

    mockMvc
        .perform(post("/prior-authority").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isBadRequest());
  }

  @Test
  void returns201WhenHourlyBillingMissesTimeFields() throws Exception {
    when(priorAuthorityService.submit(any(PriorAuthority.class)))
        .thenReturn(
            PriorAuthorityApplicationResponse.builder()
                .submissionId(SUBMISSION_ID)
                .status(SubmissionStatus.ACCEPTED)
                .submittedAt(OffsetDateTime.parse("2026-05-22T10:00:00Z"))
                .build());

    String body =
        """
                        {
                          "applicationId": "%s",
                          "priorAuthorityType": "EXPERT",
                          "expertType": "Psychologist",
                          "expertFullName": "John Doe",
                          "expertBasedInLondon": true,
                          "billingType": "HOURLY",
                          "totalAmount": 120.00,
                          "justification": "Interim submission without time breakdown."
                        }
                        """
            .formatted(APPLICATION_ID);

    mockMvc
        .perform(post("/prior-authority").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isCreated());
  }

  @Test
  void returns201WhenPriorAuthorityTypeIsExpertButExpertTypeMissing() throws Exception {
    when(priorAuthorityService.submit(any(PriorAuthority.class)))
        .thenReturn(
            PriorAuthorityApplicationResponse.builder()
                .submissionId(SUBMISSION_ID)
                .status(SubmissionStatus.ACCEPTED)
                .submittedAt(OffsetDateTime.parse("2026-05-22T10:00:00Z"))
                .build());

    String body =
        """
                        {
                          "applicationId": "%s",
                          "priorAuthorityType": "EXPERT",
                          "expertFullName": "John Doe",
                          "expertBasedInLondon": false,
                          "billingType": "FIXED_RATE",
                          "totalAmount": 100.00,
                          "justification": "Expert type to be confirmed later."
                        }
                        """
            .formatted(APPLICATION_ID);

    mockMvc
        .perform(post("/prior-authority").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isCreated());
  }

  @Test
  void returns201WhenExpertBasedInLondonMissing() throws Exception {
    when(priorAuthorityService.submit(any(PriorAuthority.class)))
        .thenReturn(
            PriorAuthorityApplicationResponse.builder()
                .submissionId(SUBMISSION_ID)
                .status(SubmissionStatus.ACCEPTED)
                .submittedAt(OffsetDateTime.parse("2026-05-22T10:00:00Z"))
                .build());

    String body =
        """
                        {
                          "applicationId": "%s",
                          "priorAuthorityType": "EXPERT",
                          "expertType": "Psychologist",
                          "expertFullName": "John Doe",
                          "billingType": "FIXED_RATE",
                          "totalAmount": 100.00,
                          "justification": "Location flag supplied later."
                        }
                        """
            .formatted(APPLICATION_ID);

    mockMvc
        .perform(post("/prior-authority").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isCreated());
  }

  @Test
  void returns201ForCounselSubmissionWhenCounselTypeProvided() throws Exception {
    when(priorAuthorityService.submit(any(PriorAuthority.class)))
        .thenReturn(
            PriorAuthorityApplicationResponse.builder()
                .submissionId(SUBMISSION_ID)
                .status(SubmissionStatus.ACCEPTED)
                .submittedAt(OffsetDateTime.parse("2026-05-22T10:00:00Z"))
                .build());

    String body =
        """
                        {
                          "applicationId": "%s",
                          "priorAuthorityType": "COUNSEL",
                          "counselType": "KINGS_COUNSEL_ALONE",
                          "billingType": "FIXED_RATE",
                          "totalAmount": 500.00,
                          "justification": "Counsel representation is required."
                        }
                        """
            .formatted(APPLICATION_ID);

    mockMvc
        .perform(post("/prior-authority").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isCreated());
  }

  @Test
  void returns400ForCounselSubmissionWhenCounselTypeMissing() throws Exception {
    String body =
        """
                        {
                          "applicationId": "%s",
                          "priorAuthorityType": "COUNSEL",
                          "billingType": "FIXED_RATE",
                          "totalAmount": 500.00,
                          "justification": "Counsel representation is required."
                        }
                        """
            .formatted(APPLICATION_ID);

    mockMvc
        .perform(post("/prior-authority").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isBadRequest());
  }

  @Test
  void returns400WhenTimeMinutesIsGreaterThan59() throws Exception {
    String body =
        """
                        {
                          "applicationId": "%s",
                          "priorAuthorityType": "EXPERT",
                          "expertType": "Psychologist",
                          "billingType": "HOURLY",
                          "hourlyRate": 50.00,
                          "timeHours": 1,
                          "timeMinutes": 60,
                          "totalAmount": 50.00,
                          "justification": "Specialist evidence is required."
                        }
                        """
            .formatted(APPLICATION_ID);

    mockMvc
        .perform(post("/prior-authority").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isBadRequest());
  }

  @Test
  void postDraftReturns201WithLocationAndDraftId() throws Exception {
    when(draftService.create(any(PriorAuthorityDraft.class))).thenReturn(DRAFT_ID);

    String body =
        """
                        {
                          "applicationId": "%s",
                          "expertType": "Child psychologist",
                          "expertFullName": "Dr Joe Bloggs",
                          "billingType": "HOURLY",
                          "hourlyRate": 45.00,
                          "totalAmount": 135.00,
                          "justification": "Draft justification"
                        }
                        """
            .formatted(DRAFT_APPLICATION_ID);

    mockMvc
        .perform(
            post("/prior-authority/drafts").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "/prior-authority/drafts/" + DRAFT_ID))
        .andExpect(jsonPath("$.draftId").value(DRAFT_ID.toString()));
  }

  @Test
  void postDraftAcceptsPartiallyCompletedDraftWithoutCrossFieldValidation() throws Exception {
    when(draftService.create(any(PriorAuthorityDraft.class))).thenReturn(DRAFT_ID);

    // HOURLY billing with no hourly fields — would 400 on /prior-authority, must succeed for
    // drafts.
    String body =
        """
                        {
                          "applicationId": "%s",
                          "billingType": "HOURLY"
                        }
                        """
            .formatted(DRAFT_APPLICATION_ID);

    mockMvc
        .perform(
            post("/prior-authority/drafts").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isCreated());
  }

  @Test
  void postDraftReturns400WhenTimeMinutesIsGreaterThan59() throws Exception {
    String body =
        """
                        {
                          "applicationId": "%s",
                          "billingType": "HOURLY",
                          "timeMinutes": 60
                        }
                        """
            .formatted(DRAFT_APPLICATION_ID);

    mockMvc
        .perform(
            post("/prior-authority/drafts").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isBadRequest());
  }

  @Test
  void postDraftReturns400WhenApplicationIdMissing() throws Exception {
    String body =
        """
                        {
                          "expertFullName": "Dr Joe Bloggs"
                        }
                        """;

    mockMvc
        .perform(
            post("/prior-authority/drafts").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isBadRequest());
  }

  @Test
  void putDraftReturns200() throws Exception {
    String body =
        """
                        {
                          "applicationId": "%s",
                          "totalAmount": 180.00
                        }
                        """
            .formatted(DRAFT_APPLICATION_ID);

    mockMvc
        .perform(
            put("/prior-authority/drafts/{draftId}", DRAFT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isOk());

    verify(draftService).update(eq(DRAFT_ID), any(PriorAuthorityDraft.class));
  }

  @Test
  void getDraftByIdReturns200WithBody() throws Exception {
    PriorAuthorityDraft saved =
        PriorAuthorityDraft.builder()
            .applicationId(UUID.fromString(DRAFT_APPLICATION_ID))
            .expertFullName("Dr Joe Bloggs")
            .billingType(BillingType.FIXED_RATE)
            .totalAmount(new BigDecimal("249.99"))
            .build();
    when(draftService.get(DRAFT_ID))
        .thenReturn(
            Optional.of(
                PriorAuthorityDraftSummary.builder()
                    .draftId(DRAFT_ID)
                    .timestamp(OffsetDateTime.parse("2026-05-19T12:00:00Z"))
                    .draft(saved)
                    .build()));

    mockMvc
        .perform(get("/prior-authority/drafts/{draftId}", DRAFT_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.draftId").value(DRAFT_ID.toString()))
        .andExpect(jsonPath("$.draft.expertFullName").value("Dr Joe Bloggs"))
        .andExpect(jsonPath("$.draft.billingType").value("FIXED_RATE"));
  }

  @Test
  void getDraftByIdReturns404WhenNotFound() throws Exception {
    when(draftService.get(DRAFT_ID)).thenReturn(Optional.empty());

    mockMvc
        .perform(get("/prior-authority/drafts/{draftId}", DRAFT_ID))
        .andExpect(status().isNotFound());
  }

  @Test
  void getDraftsReturnsList() throws Exception {
    UUID applicationId = UUID.fromString(DRAFT_APPLICATION_ID);
    PriorAuthorityDraft saved =
        PriorAuthorityDraft.builder()
            .applicationId(applicationId)
            .expertFullName("Dr Joe Bloggs")
            .billingType(BillingType.FIXED_RATE)
            .totalAmount(new BigDecimal("249.99"))
            .build();
    when(draftService.list(applicationId))
        .thenReturn(
            List.of(
                PriorAuthorityDraftSummary.builder()
                    .draftId(DRAFT_ID)
                    .timestamp(OffsetDateTime.parse("2026-05-19T12:00:00Z"))
                    .draft(saved)
                    .build()));

    mockMvc
        .perform(get("/prior-authority/drafts").param("applicationId", DRAFT_APPLICATION_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].draftId").value(DRAFT_ID.toString()))
        .andExpect(jsonPath("$[0].draft.expertFullName").value("Dr Joe Bloggs"))
        .andExpect(jsonPath("$[0].draft.billingType").value("FIXED_RATE"));
  }

  @Test
  void getDraftsWithoutApplicationIdListsAll() throws Exception {
    when(draftService.list(null)).thenReturn(List.of());

    mockMvc.perform(get("/prior-authority/drafts")).andExpect(status().isOk());

    verify(draftService).list(null);
  }

  @Test
  void deleteDraftReturns204() throws Exception {
    mockMvc
        .perform(delete("/prior-authority/drafts/{draftId}", DRAFT_ID))
        .andExpect(status().isNoContent());

    verify(draftService).delete(DRAFT_ID);
  }

  @Test
  void uploadDocumentReturns200WithFileMetadata() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile("file", "evidence.pdf", "application/pdf", "pdf-content".getBytes());

    when(priorAuthorityService.uploadDocument(any()))
        .thenReturn(UploadedDocument.builder().fileName("evidence.pdf").build());

    mockMvc
        .perform(multipart("/prior-authority/documents").file(file))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.fileName").value("evidence.pdf"));
  }

  @Test
  void uploadDocumentReturns400WhenFileIsEmpty() throws Exception {
    MockMultipartFile emptyFile =
        new MockMultipartFile("file", "empty.pdf", "application/pdf", new byte[0]);

    when(priorAuthorityService.uploadDocument(any()))
        .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "file must not be empty"));

    mockMvc
        .perform(multipart("/prior-authority/documents").file(emptyFile))
        .andExpect(status().isBadRequest());
  }

  @Test
  void uploadDocumentReturns400WhenFilenameIsMissing() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile("file", null, "application/pdf", "pdf-content".getBytes());

    when(priorAuthorityService.uploadDocument(any()))
        .thenThrow(
            new ResponseStatusException(HttpStatus.BAD_REQUEST, "file name must not be empty"));

    mockMvc
        .perform(multipart("/prior-authority/documents").file(file))
        .andExpect(status().isBadRequest());
  }

  @Test
  void uploadDocumentReturns415WhenFileTypeIsNotAllowed() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile("file", "script.js", "application/javascript", "alert(1)".getBytes());

    when(priorAuthorityService.uploadDocument(any()))
        .thenThrow(
            new ResponseStatusException(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "unsupported file type; allowed: DOC, DOCX, RTF, ODT, JPG, BMP, PNG, TIF, PDF"));

    mockMvc
        .perform(multipart("/prior-authority/documents").file(file))
        .andExpect(status().isUnsupportedMediaType());
  }

  @Test
  void uploadDocumentReturns413WhenFileExceeds10Mb() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile(
            "file", "large.pdf", "application/pdf", new byte[(10 * 1024 * 1024) + 1]);

    when(priorAuthorityService.uploadDocument(any()))
        .thenThrow(
            new ResponseStatusException(
                HttpStatus.CONTENT_TOO_LARGE, "file size must not exceed 10MB"));

    mockMvc
        .perform(multipart("/prior-authority/documents").file(file))
        .andExpect(status().isContentTooLarge());
  }
}

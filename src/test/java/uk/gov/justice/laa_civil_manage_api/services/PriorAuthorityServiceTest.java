package uk.gov.justice.laa_civil_manage_api.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.justice.laa_civil_manage_api.models.BillingType;
import uk.gov.justice.laa_civil_manage_api.models.ExpertCosts;
import uk.gov.justice.laa_civil_manage_api.models.ExpertDetails;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthority;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthorityApplicationResponse;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthorityType;
import uk.gov.justice.laa_civil_manage_api.models.SubmissionStatus;
import uk.gov.justice.laa_civil_manage_api.models.UploadedDocument;
import uk.gov.justice.laa_civil_manage_api.services.accessdatastore.AccessDataStoreClient;

class PriorAuthorityServiceTest {

  private final AccessDataStoreClient client = mock(AccessDataStoreClient.class);
  private final PriorAuthorityService service = new PriorAuthorityService(client);

  @Test
  void delegatesToAccessDataStoreClient() {
    PriorAuthority pa =
        PriorAuthority.builder()
            .applicationId(UUID.randomUUID())
            .priorAuthorityType(PriorAuthorityType.EXPERT)
            .expertDetails(
                ExpertDetails.builder()
                    .expertType("Psychologist")
                    .expertFullName("Dr John Doe")
                    .expertPostcode("M1 1AA")
                    .expertCosts(
                        ExpertCosts.builder()
                            .billingType(BillingType.FIXED_RATE)
                            .totalAmount(new BigDecimal("249.99"))
                            .costsSharedWithOtherParties(false)
                            .build())
                    .build())
            .justification("Required expert evidence.")
            .build();
    PriorAuthorityApplicationResponse expected =
        PriorAuthorityApplicationResponse.builder()
            .submissionId(UUID.randomUUID())
            .status(SubmissionStatus.ACCEPTED)
            .submittedAt(OffsetDateTime.parse("2026-05-22T10:00:00Z"))
            .build();
    when(client.submitPriorAuthority(pa)).thenReturn(expected);

    PriorAuthorityApplicationResponse actual = service.submit(pa);

    assertSame(expected, actual);
    verify(client).submitPriorAuthority(pa);
  }

  @Test
  void uploadDocumentReturnsUploadedFilename() {
    MockMultipartFile file =
        new MockMultipartFile("file", "evidence.pdf", "application/pdf", "content".getBytes());

    UploadedDocument uploadedDocument = service.uploadDocument(file);

    assertEquals("evidence.pdf", uploadedDocument.fileName());
  }

  @Test
  void uploadDocumentStripsUnixPathSegmentsFromClientSuppliedFilename() {
    MockMultipartFile file =
        new MockMultipartFile(
            "file", "../../sneaky/evidence.pdf", "application/pdf", "content".getBytes());

    UploadedDocument uploadedDocument = service.uploadDocument(file);

    assertEquals("evidence.pdf", uploadedDocument.fileName());
    assertEquals("https://example.com/evidence.pdf", uploadedDocument.hostedUrl());
  }

  @Test
  void uploadDocumentStripsWindowsPathSegmentsFromClientSuppliedFilename() {
    MockMultipartFile file =
        new MockMultipartFile(
            "file", "..\\..\\temp\\evidence.pdf", "application/pdf", "content".getBytes());

    UploadedDocument uploadedDocument = service.uploadDocument(file);

    assertEquals("evidence.pdf", uploadedDocument.fileName());
    assertEquals("https://example.com/evidence.pdf", uploadedDocument.hostedUrl());
  }

  @Test
  void uploadDocumentThrowsWhenFileIsEmpty() {
    MockMultipartFile file =
        new MockMultipartFile("file", "empty.pdf", "application/pdf", new byte[0]);

    ResponseStatusException ex =
        assertThrows(ResponseStatusException.class, () -> service.uploadDocument(file));

    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
  }

  @Test
  void uploadDocumentThrowsWhenFilenameIsMissing() {
    MockMultipartFile file =
        new MockMultipartFile("file", null, "application/pdf", "content".getBytes());

    ResponseStatusException ex =
        assertThrows(ResponseStatusException.class, () -> service.uploadDocument(file));

    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    assertEquals("file name must not be empty", ex.getReason());
  }

  @Test
  void uploadDocumentThrowsWhenFileTypeIsNotAllowed() {
    MockMultipartFile file =
        new MockMultipartFile("file", "malware.exe", "application/octet-stream", "x".getBytes());

    ResponseStatusException ex =
        assertThrows(ResponseStatusException.class, () -> service.uploadDocument(file));

    assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, ex.getStatusCode());
  }

  @Test
  void uploadDocumentThrowsWhenFileIsNull() {
    ResponseStatusException ex =
        assertThrows(ResponseStatusException.class, () -> service.uploadDocument(null));

    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
  }

  @Test
  void uploadDocumentThrowsWhenFileHasNoExtension() {
    MockMultipartFile file =
        new MockMultipartFile("file", "nodotinfilename", "application/pdf", "content".getBytes());

    ResponseStatusException ex =
        assertThrows(ResponseStatusException.class, () -> service.uploadDocument(file));

    assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, ex.getStatusCode());
  }

  @Test
  void submitLogsCorrectlyWhenUploadedDocumentsIsNonNull() {
    PriorAuthority pa =
        PriorAuthority.builder()
            .applicationId(UUID.randomUUID())
            .priorAuthorityType(PriorAuthorityType.EXPERT)
            .expertDetails(
                ExpertDetails.builder()
                    .expertType("Psychologist")
                    .expertFullName("Dr John Doe")
                    .expertPostcode("SW1H 9AJ")
                    .expertCosts(
                        ExpertCosts.builder()
                            .billingType(BillingType.FIXED_RATE)
                            .totalAmount(new BigDecimal("100.00"))
                            .costsSharedWithOtherParties(false)
                            .build())
                    .build())
            .justification("Test.")
            .uploadedDocuments(List.of())
            .build();
    PriorAuthorityApplicationResponse expected =
        PriorAuthorityApplicationResponse.builder()
            .submissionId(UUID.randomUUID())
            .status(SubmissionStatus.ACCEPTED)
            .submittedAt(OffsetDateTime.parse("2026-05-22T10:00:00Z"))
            .build();
    when(client.submitPriorAuthority(pa)).thenReturn(expected);

    PriorAuthorityApplicationResponse actual = service.submit(pa);

    assertSame(expected, actual);
  }
}

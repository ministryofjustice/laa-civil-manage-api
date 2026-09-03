package uk.gov.justice.laa_civil_manage_api.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.justice.laa.civil.notify.model.SendEmailRequest;
import uk.gov.justice.laa.civil.notify.service.NotifyEmailSender;
import uk.gov.justice.laa_civil_manage_api.config.NotifyEmailProperties;
import uk.gov.justice.laa_civil_manage_api.models.BillingType;
import uk.gov.justice.laa_civil_manage_api.models.ExpertCosts;
import uk.gov.justice.laa_civil_manage_api.models.ExpertDetails;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthority;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthorityApplicationResponse;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthorityType;
import uk.gov.justice.laa_civil_manage_api.models.UploadedDocument;
import uk.gov.justice.laa_civil_manage_api.services.accessdatastore.AccessDataStoreClient;

class PriorAuthorityServiceTest {

  private static final byte[] PDF_CONTENT = "%PDF-1.4\nmock pdf content for testing".getBytes();

  private final AccessDataStoreClient client = mock(AccessDataStoreClient.class);
  private final NotifyEmailSender notifyEmailSender = mock(NotifyEmailSender.class);
  private final NotifyEmailProperties notifyEmailProperties =
      new NotifyEmailProperties(
          "api-key",
          "https://api.notifications.service.gov.uk",
          "template-id",
          "ops@example.com",
          true);
  private final PriorAuthorityService service =
      new PriorAuthorityService(client, notifyEmailSender, notifyEmailProperties);

  @BeforeEach
  void resetMocks() {
    reset(client, notifyEmailSender);
  }

  @Test
  void delegatesToAccessDataStoreClient() {
    PriorAuthority pa =
        PriorAuthority.builder()
            .applicationId(UUID.randomUUID())
            .laaReference("LAA123456")
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
            .submittedAt(OffsetDateTime.parse("2026-05-22T10:00:00Z"))
            .build();
    when(client.submitPriorAuthority(pa)).thenReturn(expected);
    when(notifyEmailSender.sendEmail(any(SendEmailRequest.class)))
        .thenReturn(CompletableFuture.completedFuture(null));

    PriorAuthorityApplicationResponse actual = service.submit(pa);

    assertSame(expected, actual);
    verify(client).submitPriorAuthority(pa);
    assertSubmittedEmailRequest(pa, expected);
  }

  @Test
  void submitReturnsResponseEvenWhenEmailFutureFails() {
    PriorAuthority pa =
        PriorAuthority.builder()
            .applicationId(UUID.randomUUID())
            .laaReference("LAA123456")
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
            .submittedAt(OffsetDateTime.parse("2026-05-22T10:00:00Z"))
            .build();
    when(client.submitPriorAuthority(pa)).thenReturn(expected);
    when(notifyEmailSender.sendEmail(any(SendEmailRequest.class)))
        .thenReturn(CompletableFuture.failedFuture(new RuntimeException("notify down")));

    PriorAuthorityApplicationResponse actual = service.submit(pa);

    assertSame(expected, actual);
    verify(client).submitPriorAuthority(pa);
    assertSubmittedEmailRequest(pa, expected);
  }

  @Test
  void doesNotSendEmailWhenNotifyIsNotConfigured() {
    PriorAuthorityService unconfiguredService =
        new PriorAuthorityService(
            client, notifyEmailSender, new NotifyEmailProperties("", "", "", "", false));
    PriorAuthority pa =
        PriorAuthority.builder()
            .applicationId(UUID.randomUUID())
            .laaReference("LAA123456")
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
            .submittedAt(OffsetDateTime.parse("2026-05-22T10:00:00Z"))
            .build();
    when(client.submitPriorAuthority(pa)).thenReturn(expected);

    PriorAuthorityApplicationResponse actual = unconfiguredService.submit(pa);

    assertSame(expected, actual);
    verify(notifyEmailSender, never()).sendEmail(any(SendEmailRequest.class));
  }

  @Test
  void uploadDocumentReturnsUploadedFilename() {
    MockMultipartFile file =
        new MockMultipartFile("file", "evidence.pdf", "application/pdf", PDF_CONTENT);

    UploadedDocument uploadedDocument = service.uploadDocument(file);

    assertEquals("evidence.pdf", uploadedDocument.fileName());
  }

  @Test
  void uploadDocumentStripsUnixPathSegmentsFromClientSuppliedFilename() {
    MockMultipartFile file =
        new MockMultipartFile("file", "../../sneaky/evidence.pdf", "application/pdf", PDF_CONTENT);

    UploadedDocument uploadedDocument = service.uploadDocument(file);

    assertEquals("evidence.pdf", uploadedDocument.fileName());
    assertEquals("https://example.com/evidence.pdf", uploadedDocument.hostedUrl());
  }

  @Test
  void uploadDocumentStripsWindowsPathSegmentsFromClientSuppliedFilename() {
    MockMultipartFile file =
        new MockMultipartFile("file", "..\\..\\temp\\evidence.pdf", "application/pdf", PDF_CONTENT);

    UploadedDocument uploadedDocument = service.uploadDocument(file);

    assertEquals("evidence.pdf", uploadedDocument.fileName());
    assertEquals("https://example.com/evidence.pdf", uploadedDocument.hostedUrl());
  }

  @Test
  void uploadDocumentSanitisesNullBytesBeforeRejectingDisguisedExtension() {
    MockMultipartFile file =
        new MockMultipartFile("file", "evidence.pdf\0.exe", "application/pdf", PDF_CONTENT);

    ResponseStatusException ex =
        assertThrows(ResponseStatusException.class, () -> service.uploadDocument(file));

    assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, ex.getStatusCode());
  }

  @Test
  void uploadDocumentThrowsWhenFilenameExceedsMaxLength() {
    String longName = "a".repeat(252) + ".pdf";
    MockMultipartFile file =
        new MockMultipartFile("file", longName, "application/pdf", PDF_CONTENT);

    ResponseStatusException ex =
        assertThrows(ResponseStatusException.class, () -> service.uploadDocument(file));

    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
  }

  @Test
  void uploadDocumentThrowsWhenFilenameHasMultipleExtensions() {
    MockMultipartFile file =
        new MockMultipartFile("file", "evidence.docx.pdf", "application/pdf", PDF_CONTENT);

    ResponseStatusException ex =
        assertThrows(ResponseStatusException.class, () -> service.uploadDocument(file));

    assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, ex.getStatusCode());
  }

  @Test
  void uploadDocumentThrowsWhenFileContentDoesNotMatchPdfMagicBytes() {
    MockMultipartFile file =
        new MockMultipartFile("file", "evidence.pdf", "application/pdf", "not a pdf".getBytes());

    ResponseStatusException ex =
        assertThrows(ResponseStatusException.class, () -> service.uploadDocument(file));

    assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, ex.getStatusCode());
  }

  @Test
  void uploadDocumentThrowsWhenDisallowedExtension() {
    MockMultipartFile file =
        new MockMultipartFile(
            "file",
            "evidence.docx",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "content".getBytes());

    ResponseStatusException ex =
        assertThrows(ResponseStatusException.class, () -> service.uploadDocument(file));

    assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, ex.getStatusCode());
  }

  @Test
  void uploadDocumentThrowsWhenFileExceedsMaxSize() {
    byte[] oversized = new byte[(10 * 1024 * 1024) + 1];
    System.arraycopy(PDF_CONTENT, 0, oversized, 0, PDF_CONTENT.length);
    MockMultipartFile file =
        new MockMultipartFile("file", "large.pdf", "application/pdf", oversized);

    ResponseStatusException ex =
        assertThrows(ResponseStatusException.class, () -> service.uploadDocument(file));

    assertEquals(HttpStatus.PAYLOAD_TOO_LARGE, ex.getStatusCode());
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
    MockMultipartFile file = new MockMultipartFile("file", null, "application/pdf", PDF_CONTENT);

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
        new MockMultipartFile("file", "nodotinfilename", "application/pdf", PDF_CONTENT);

    ResponseStatusException ex =
        assertThrows(ResponseStatusException.class, () -> service.uploadDocument(file));

    assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, ex.getStatusCode());
  }

  @Test
  void submitLogsCorrectlyWhenUploadedDocumentsIsNonNull() {
    PriorAuthority pa =
        PriorAuthority.builder()
            .applicationId(UUID.randomUUID())
            .laaReference("LAA123456")
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
            .submittedAt(OffsetDateTime.parse("2026-05-22T10:00:00Z"))
            .build();
    when(client.submitPriorAuthority(pa)).thenReturn(expected);
    when(notifyEmailSender.sendEmail(any(SendEmailRequest.class)))
        .thenReturn(CompletableFuture.completedFuture(null));

    PriorAuthorityApplicationResponse actual = service.submit(pa);

    assertSame(expected, actual);
  }

  private void assertSubmittedEmailRequest(
      PriorAuthority priorAuthority, PriorAuthorityApplicationResponse response) {
    ArgumentCaptor<SendEmailRequest> emailCaptor = ArgumentCaptor.forClass(SendEmailRequest.class);
    verify(notifyEmailSender).sendEmail(emailCaptor.capture());

    SendEmailRequest request = emailCaptor.getValue();
    assertEquals("template-id", request.templateId());
    assertEquals("ops@example.com", request.emailAddress());
    assertEquals(
        Map.of(
            "priorAuthorityReference", response.submissionId(),
            "laaReference", priorAuthority.laaReference(),
            "priorAuthorityType", priorAuthority.priorAuthorityType().getDisplayName(),
            "submittedAt", "22 May 2026, 10:00 am"),
        request.personalisation());
  }
}

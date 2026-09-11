package uk.gov.justice.laa_civil_manage_api.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

class DocumentValidationServiceTest {

  private final DocumentValidationService service = new DocumentValidationService();
  private static final byte[] PDF_MAGIC_BYTES = {0x25, 0x50, 0x44, 0x46};
  private static final byte[] PDF_CONTENT = "%PDF-1.4\nmock pdf content for testing".getBytes();

  @Test
  void acceptsValidPdfAndReturnsSanitizedFilename() {
    MockMultipartFile file =
        new MockMultipartFile("file", "evidence%00.pdf", "application/pdf", PDF_CONTENT);

    String result = service.validateAndSanitize(file);

    assertEquals("evidence.pdf", result); // Verifies null bytes were stripped
  }

  @Test
  void throwsWhenFileIsEmpty() {
    MockMultipartFile file =
        new MockMultipartFile("file", "empty.pdf", "application/pdf", new byte[0]);

    ResponseStatusException ex =
        assertThrows(ResponseStatusException.class, () -> service.validateAndSanitize(file));

    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    assertEquals("file must not be empty", ex.getReason());
  }

  @Test
  void throwsWhenFilenameIsMissing() {
    MockMultipartFile file =
        new MockMultipartFile("file", null, "application/pdf", PDF_MAGIC_BYTES);

    ResponseStatusException ex =
        assertThrows(ResponseStatusException.class, () -> service.validateAndSanitize(file));

    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
  }

  @Test
  void throwsWhenFileTypeIsNotAllowed() {
    MockMultipartFile file =
        new MockMultipartFile("file", "malware.exe", "application/octet-stream", "x".getBytes());

    ResponseStatusException ex =
        assertThrows(ResponseStatusException.class, () -> service.validateAndSanitize(file));

    assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, ex.getStatusCode());
  }

  @Test
  void throwsWhenFileContentDoesNotMatchPdfMagicBytes() {
    MockMultipartFile file =
        new MockMultipartFile("file", "evidence.pdf", "application/pdf", "not a pdf".getBytes());

    ResponseStatusException ex =
        assertThrows(ResponseStatusException.class, () -> service.validateAndSanitize(file));

    assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, ex.getStatusCode());
    assertEquals("file content does not match the PDF file signature", ex.getReason());
  }

  @Test
  void throwsWhenFileExceedsMaxSize() {
    byte[] oversized = new byte[(10 * 1024 * 1024) + 1];
    System.arraycopy(PDF_MAGIC_BYTES, 0, oversized, 0, PDF_MAGIC_BYTES.length);
    MockMultipartFile file =
        new MockMultipartFile("file", "large.pdf", "application/pdf", oversized);

    ResponseStatusException ex =
        assertThrows(ResponseStatusException.class, () -> service.validateAndSanitize(file));

    assertEquals(HttpStatus.CONTENT_TOO_LARGE, ex.getStatusCode());
  }

  @Test
  void throwsWhenFileIsNull() {
    ResponseStatusException ex =
        assertThrows(ResponseStatusException.class, () -> service.validateAndSanitize(null));

    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
  }

  @Test
  void throwsWhenSanitizedFilenameIsEmptyAfterCleaning() {
    MockMultipartFile file = new MockMultipartFile("file", "%00", "application/pdf", PDF_CONTENT);

    ResponseStatusException ex =
        assertThrows(ResponseStatusException.class, () -> service.validateAndSanitize(file));

    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    assertEquals("file name must not be empty", ex.getReason());
  }

  @Test
  void throwsWhenFilenameExceedsMaxLength() {
    String longName = "a".repeat(252) + ".pdf"; // 256 characters, single extension
    MockMultipartFile file =
        new MockMultipartFile("file", longName, "application/pdf", PDF_CONTENT);

    ResponseStatusException ex =
        assertThrows(ResponseStatusException.class, () -> service.validateAndSanitize(file));

    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    assertEquals("file name must not exceed 255 characters", ex.getReason());
  }

  @Test
  void throwsWhenFilenameContainsMultipleExtensions() {
    MockMultipartFile file =
        new MockMultipartFile("file", "evidence.v2.pdf", "application/pdf", PDF_CONTENT);

    ResponseStatusException ex =
        assertThrows(ResponseStatusException.class, () -> service.validateAndSanitize(file));

    assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, ex.getStatusCode());
    assertEquals("file name must contain a single extension only", ex.getReason());
  }

  @Test
  void throwsWhenFilenameHasNoExtension() {
    MockMultipartFile file =
        new MockMultipartFile("file", "evidencepdf", "application/pdf", PDF_CONTENT);

    ResponseStatusException ex =
        assertThrows(ResponseStatusException.class, () -> service.validateAndSanitize(file));

    assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, ex.getStatusCode());
  }

  @Test
  void throwsWhenDetectedMediaTypeDoesNotMatchPdf() throws IOException {
    MultipartFile file = mock(MultipartFile.class);
    when(file.isEmpty()).thenReturn(false);
    when(file.getSize()).thenReturn(100L);
    when(file.getOriginalFilename()).thenReturn("evidence.pdf");
    when(file.getInputStream())
        .thenReturn(new ByteArrayInputStream(PDF_MAGIC_BYTES))
        .thenReturn(new ByteArrayInputStream("plain text content, not a pdf at all".getBytes()));

    ResponseStatusException ex =
        assertThrows(ResponseStatusException.class, () -> service.validateAndSanitize(file));

    assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, ex.getStatusCode());
    assertEquals("file content does not match a valid PDF media type", ex.getReason());
  }

  @Test
  void throwsWhenHeaderCannotBeRead() throws IOException {
    MultipartFile file = mock(MultipartFile.class);
    when(file.isEmpty()).thenReturn(false);
    when(file.getSize()).thenReturn(100L);
    when(file.getOriginalFilename()).thenReturn("evidence.pdf");
    when(file.getInputStream()).thenThrow(new IOException("boom"));

    ResponseStatusException ex =
        assertThrows(ResponseStatusException.class, () -> service.validateAndSanitize(file));

    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    assertEquals("unable to read uploaded file", ex.getReason());
  }

  @Test
  void throwsWhenMediaTypeCannotBeDetectedDueToIoError() throws IOException {
    MultipartFile file = mock(MultipartFile.class);
    when(file.isEmpty()).thenReturn(false);
    when(file.getSize()).thenReturn(100L);
    when(file.getOriginalFilename()).thenReturn("evidence.pdf");
    when(file.getInputStream())
        .thenReturn(new ByteArrayInputStream(PDF_MAGIC_BYTES))
        .thenThrow(new IOException("boom"));

    ResponseStatusException ex =
        assertThrows(ResponseStatusException.class, () -> service.validateAndSanitize(file));

    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    assertEquals("unable to read uploaded file", ex.getReason());
  }
}

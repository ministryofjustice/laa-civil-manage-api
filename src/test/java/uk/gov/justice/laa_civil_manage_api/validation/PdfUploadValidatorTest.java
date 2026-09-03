package uk.gov.justice.laa_civil_manage_api.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

class PdfUploadValidatorTest {

  private static final byte[] VALID_PDF_CONTENT =
      ("%PDF-1.4\n1 0 obj<< /Type /Catalog >>endobj\ntrailer<< /Root 1 0 R >>\n%%EOF")
          .getBytes(StandardCharsets.US_ASCII);

  private final PdfUploadValidator validator = new PdfUploadValidator();

  @Test
  void acceptsValidPdf() {
    MockMultipartFile file =
        new MockMultipartFile("file", "evidence.pdf", "application/pdf", VALID_PDF_CONTENT);

    String sanitizedFileName = validator.validate(file);

    assertEquals("evidence.pdf", sanitizedFileName);
  }

  @Test
  void rejectsFileWithWrongExtension() {
    MockMultipartFile file =
        new MockMultipartFile("file", "evidence.docx", "application/pdf", VALID_PDF_CONTENT);

    ResponseStatusException ex =
        assertThrows(ResponseStatusException.class, () -> validator.validate(file));

    assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, ex.getStatusCode());
    assertEquals(FileUploadRules.MESSAGE_INVALID_EXTENSION, ex.getReason());
  }

  @Test
  void rejectsFileWithMissingExtension() {
    MockMultipartFile file =
        new MockMultipartFile("file", "evidence", "application/pdf", VALID_PDF_CONTENT);

    ResponseStatusException ex =
        assertThrows(ResponseStatusException.class, () -> validator.validate(file));

    assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, ex.getStatusCode());
    assertEquals(FileUploadRules.MESSAGE_INVALID_EXTENSION, ex.getReason());
  }

  @Test
  void rejectsDoubleExtension() {
    MockMultipartFile file =
        new MockMultipartFile("file", "evidence.docx.pdf", "application/pdf", VALID_PDF_CONTENT);

    ResponseStatusException ex =
        assertThrows(ResponseStatusException.class, () -> validator.validate(file));

    assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, ex.getStatusCode());
    assertEquals(FileUploadRules.MESSAGE_INVALID_EXTENSION, ex.getReason());
  }

  @Test
  void rejectsFileNameLongerThan255Characters() {
    String longName = "a".repeat(252) + ".pdf";
    MockMultipartFile file =
        new MockMultipartFile("file", longName, "application/pdf", VALID_PDF_CONTENT);

    ResponseStatusException ex =
        assertThrows(ResponseStatusException.class, () -> validator.validate(file));

    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    assertEquals(FileUploadRules.MESSAGE_FILE_NAME_TOO_LONG, ex.getReason());
  }

  @Test
  void sanitizesNullByteInFileName() {
    MockMultipartFile file =
        new MockMultipartFile("file", "evidence\0.pdf", "application/pdf", VALID_PDF_CONTENT);

    String sanitizedFileName = validator.validate(file);

    assertEquals("evidence.pdf", sanitizedFileName);
  }

  @Test
  void sanitizesEncodedNullByteInFileName() {
    MockMultipartFile file =
        new MockMultipartFile("file", "evidence%00.pdf", "application/pdf", VALID_PDF_CONTENT);

    String sanitizedFileName = validator.validate(file);

    assertEquals("evidence.pdf", sanitizedFileName);
  }

  @Test
  void rejectsTamperedContentWithMismatchedMimeType() {
    // A PNG file signature (magic bytes) renamed to look like a PDF.
    byte[] pngBytes = {
      (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00, 0x00, 0x0D
    };
    MockMultipartFile file =
        new MockMultipartFile("file", "evidence.pdf", "application/pdf", pngBytes);

    ResponseStatusException ex =
        assertThrows(ResponseStatusException.class, () -> validator.validate(file));

    assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, ex.getStatusCode());
    assertEquals(FileUploadRules.MESSAGE_INVALID_MIME_TYPE, ex.getReason());
  }

  @Test
  void rejectsContentWithInvalidMagicBytesEvenIfMimeTypeMatches() {
    // Content that Tika sniffs as text/plain rather than PDF, spoofed Content-Type header.
    byte[] plainTextContent =
        "just some plain text, not a real pdf".getBytes(StandardCharsets.UTF_8);
    MockMultipartFile file =
        new MockMultipartFile("file", "evidence.pdf", "application/pdf", plainTextContent);

    ResponseStatusException ex =
        assertThrows(ResponseStatusException.class, () -> validator.validate(file));

    assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, ex.getStatusCode());
  }

  @Test
  void rejectsFileLargerThan10Mb() {
    byte[] oversizedContent = new byte[(int) FileUploadRules.MAX_FILE_SIZE_BYTES + 1];
    System.arraycopy(VALID_PDF_CONTENT, 0, oversizedContent, 0, VALID_PDF_CONTENT.length);
    MockMultipartFile file =
        new MockMultipartFile("file", "evidence.pdf", "application/pdf", oversizedContent);

    ResponseStatusException ex =
        assertThrows(ResponseStatusException.class, () -> validator.validate(file));

    assertEquals(HttpStatus.PAYLOAD_TOO_LARGE, ex.getStatusCode());
    assertEquals(FileUploadRules.MESSAGE_FILE_TOO_LARGE, ex.getReason());
  }

  @Test
  void rejectsEmptyFile() {
    MockMultipartFile file =
        new MockMultipartFile("file", "evidence.pdf", "application/pdf", new byte[0]);

    ResponseStatusException ex =
        assertThrows(ResponseStatusException.class, () -> validator.validate(file));

    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    assertEquals(FileUploadRules.MESSAGE_FILE_EMPTY, ex.getReason());
  }

  @Test
  void rejectsNullFile() {
    ResponseStatusException ex =
        assertThrows(ResponseStatusException.class, () -> validator.validate(null));

    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    assertEquals(FileUploadRules.MESSAGE_FILE_EMPTY, ex.getReason());
  }

  @Test
  void rejectsMissingFileName() {
    MockMultipartFile file =
        new MockMultipartFile("file", null, "application/pdf", VALID_PDF_CONTENT);

    ResponseStatusException ex =
        assertThrows(ResponseStatusException.class, () -> validator.validate(file));

    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    assertEquals(FileUploadRules.MESSAGE_FILE_NAME_EMPTY, ex.getReason());
  }
}

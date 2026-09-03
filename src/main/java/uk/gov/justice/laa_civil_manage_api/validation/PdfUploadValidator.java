package uk.gov.justice.laa_civil_manage_api.validation;

import static uk.gov.justice.laa_civil_manage_api.validation.FileUploadRules.MAX_FILE_NAME_LENGTH;
import static uk.gov.justice.laa_civil_manage_api.validation.FileUploadRules.MAX_FILE_SIZE_BYTES;
import static uk.gov.justice.laa_civil_manage_api.validation.FileUploadRules.MESSAGE_FILE_EMPTY;
import static uk.gov.justice.laa_civil_manage_api.validation.FileUploadRules.MESSAGE_FILE_NAME_EMPTY;
import static uk.gov.justice.laa_civil_manage_api.validation.FileUploadRules.MESSAGE_FILE_NAME_TOO_LONG;
import static uk.gov.justice.laa_civil_manage_api.validation.FileUploadRules.MESSAGE_FILE_TOO_LARGE;
import static uk.gov.justice.laa_civil_manage_api.validation.FileUploadRules.MESSAGE_INVALID_EXTENSION;
import static uk.gov.justice.laa_civil_manage_api.validation.FileUploadRules.MESSAGE_INVALID_MIME_TYPE;
import static uk.gov.justice.laa_civil_manage_api.validation.FileUploadRules.MESSAGE_INVALID_SIGNATURE;
import static uk.gov.justice.laa_civil_manage_api.validation.FileUploadRules.PDF_EXTENSION;
import static uk.gov.justice.laa_civil_manage_api.validation.FileUploadRules.PDF_MIME_TYPE;
import static uk.gov.justice.laa_civil_manage_api.validation.FileUploadRules.PDF_SIGNATURE;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Locale;
import org.apache.tika.Tika;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

/**
 * Validates uploaded supporting documents against the PDF-only upload rules (see ticket "Document
 * Upload Validate PDFs").
 *
 * <p>Unlike a naive check that trusts the browser-supplied {@code Content-Type} multipart header,
 * the MIME-type and file-signature checks here are derived solely from the file's actual bytes (via
 * Apache Tika content sniffing, plus an explicit magic-byte check), so a file with a spoofed
 * Content-Type header or a renamed non-PDF file will still be rejected.
 */
@Component
public class PdfUploadValidator {

  private final Tika tika = new Tika();

  /**
   * Validates the given file against every PDF upload rule.
   *
   * @return the sanitized (null-byte free) original filename, safe to persist/display.
   * @throws ResponseStatusException if any rule is violated.
   */
  public String validate(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, MESSAGE_FILE_EMPTY);
    }

    String originalFilename = file.getOriginalFilename();
    if (originalFilename == null || originalFilename.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, MESSAGE_FILE_NAME_EMPTY);
    }

    String sanitizedFileName = sanitizeFileName(originalFilename);

    // TODO - cleanPath is not adequate protection against path-traversal attacks - we must save
    // the file with a different name to that provided by the user
    sanitizedFileName = StringUtils.getFilename(StringUtils.cleanPath(sanitizedFileName));
    if (sanitizedFileName == null || sanitizedFileName.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, MESSAGE_FILE_NAME_EMPTY);
    }

    validateExtension(sanitizedFileName);

    if (sanitizedFileName.length() > MAX_FILE_NAME_LENGTH) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, MESSAGE_FILE_NAME_TOO_LONG);
    }

    if (file.getSize() > MAX_FILE_SIZE_BYTES) {
      throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, MESSAGE_FILE_TOO_LARGE);
    }

    validateContent(file);

    return sanitizedFileName;
  }

  /** Strips null bytes (both literal {@code \0} and the encoded {@code %00}) from a filename. */
  public static String sanitizeFileName(String fileName) {
    return fileName.replace("\0", "").replaceAll("(?i)%00", "");
  }

  private void validateExtension(String fileName) {
    int firstDotIndex = fileName.indexOf('.');
    int lastDotIndex = fileName.lastIndexOf('.');
    String extension = fileName.substring(lastDotIndex + 1);

    boolean hasSingleValidExtension =
        lastDotIndex > 0
            && firstDotIndex == lastDotIndex
            && extension.toLowerCase(Locale.ROOT).equals(PDF_EXTENSION);

    if (!hasSingleValidExtension) {
      throw new ResponseStatusException(
          HttpStatus.UNSUPPORTED_MEDIA_TYPE, MESSAGE_INVALID_EXTENSION);
    }
  }

  private void validateContent(MultipartFile file) {
    byte[] content;
    try {
      content = file.getBytes();
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to read uploaded file content", e);
    }

    String detectedMimeType = tika.detect(content);
    if (!PDF_MIME_TYPE.equals(detectedMimeType)) {
      throw new ResponseStatusException(
          HttpStatus.UNSUPPORTED_MEDIA_TYPE, MESSAGE_INVALID_MIME_TYPE);
    }

    if (content.length < PDF_SIGNATURE.length || !matchesSignature(content, PDF_SIGNATURE)) {
      throw new ResponseStatusException(
          HttpStatus.UNSUPPORTED_MEDIA_TYPE, MESSAGE_INVALID_SIGNATURE);
    }
  }

  private boolean matchesSignature(byte[] content, byte[] signature) {
    for (int i = 0; i < signature.length; i++) {
      if (content[i] != signature[i]) {
        return false;
      }
    }
    return true;
  }
}

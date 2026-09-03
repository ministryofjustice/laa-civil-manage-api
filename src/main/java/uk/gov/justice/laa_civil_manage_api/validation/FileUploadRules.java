package uk.gov.justice.laa_civil_manage_api.validation;

import java.nio.charset.StandardCharsets;

/**
 * Centrally-defined rules and messages for server-side PDF upload validation, shared by {@link
 * PdfUploadValidator} and any exception handlers that need to surface the same messages (e.g. for
 * uploads rejected by the servlet container/Spring multipart layer before reaching the validator).
 */
public final class FileUploadRules {

  /** Business-rule size limit, kept in sync with {@code spring.servlet.multipart.max-file-size}. */
  public static final long MAX_FILE_SIZE_BYTES = 10L * 1024 * 1024;

  public static final int MAX_FILE_NAME_LENGTH = 255;

  public static final String PDF_EXTENSION = "pdf";

  public static final String PDF_MIME_TYPE = "application/pdf";

  /** The PDF file signature/magic bytes, expected at the very start of the file content. */
  public static final byte[] PDF_SIGNATURE = "%PDF-".getBytes(StandardCharsets.US_ASCII);

  public static final String MESSAGE_FILE_EMPTY = "file must not be empty";

  public static final String MESSAGE_FILE_NAME_EMPTY = "file name must not be empty";

  public static final String MESSAGE_INVALID_EXTENSION = "The selected file must be a PDF";

  public static final String MESSAGE_FILE_NAME_TOO_LONG =
      "The selected file name must be 255 characters or fewer";

  public static final String MESSAGE_FILE_TOO_LARGE = "The selected file must be 10MB or smaller";

  public static final String MESSAGE_INVALID_MIME_TYPE =
      "The selected file does not have a valid PDF media type";

  public static final String MESSAGE_INVALID_SIGNATURE =
      "The selected file does not contain valid PDF content";

  private FileUploadRules() {}
}

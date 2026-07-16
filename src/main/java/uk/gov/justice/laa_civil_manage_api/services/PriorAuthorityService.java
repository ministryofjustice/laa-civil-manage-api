package uk.gov.justice.laa_civil_manage_api.services;

import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthority;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthorityApplicationResponse;
import uk.gov.justice.laa_civil_manage_api.models.UploadedDocument;
import uk.gov.justice.laa_civil_manage_api.services.accessdatastore.AccessDataStoreClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class PriorAuthorityService {

  private static final long MAX_FILE_SIZE_BYTES = 10L * 1024 * 1024;
  private static final List<String> ALLOWED_FILE_EXTENSIONS =
      List.of("doc", "docx", "rtf", "odt", "jpg", "bmp", "png", "tif", "pdf");

  private final AccessDataStoreClient accessDataStoreClient;

  public PriorAuthorityApplicationResponse submit(PriorAuthority priorAuthority) {
    int documentCount =
        priorAuthority.uploadedDocuments() == null ? 0 : priorAuthority.uploadedDocuments().size();
    log.info(
        "Submitting prior authority: applicationId={}, priorAuthorityType={}, expertType={}, billingType={}, documentCount={}",
        priorAuthority.applicationId(),
        priorAuthority.priorAuthorityType(),
        priorAuthority.expertType(),
        priorAuthority.billingType(),
        documentCount);

    PriorAuthorityApplicationResponse response =
        accessDataStoreClient.submitPriorAuthority(priorAuthority);

    log.info("Prior authority submitted: applicationId={}", priorAuthority.applicationId());
    return response;
  }

  public UploadedDocument uploadDocument(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "file must not be empty");
    }

    long originalFileSize = file.getSize();

    if (originalFileSize > MAX_FILE_SIZE_BYTES) {
      throw new ResponseStatusException(
          HttpStatus.CONTENT_TOO_LARGE, "file size must not exceed 10MB");
    }

    String originalFilename = file.getOriginalFilename();
    if (!StringUtils.hasText(originalFilename)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "file name must not be empty");
    }

    String extension = StringUtils.getFilenameExtension(originalFilename);
    String normalizedExtension = extension == null ? "" : extension.toLowerCase(Locale.ROOT);
    if (!ALLOWED_FILE_EXTENSIONS.contains(normalizedExtension)) {
      throw new ResponseStatusException(
          HttpStatus.UNSUPPORTED_MEDIA_TYPE,
          "unsupported file type; allowed: "
              + String.join(", ", ALLOWED_FILE_EXTENSIONS).toUpperCase(Locale.ROOT));
    }

    log.info(
        "Received document upload: filename={}, sizeBytes={}, contentType={}",
        originalFilename,
        originalFileSize,
        file.getContentType());

    return UploadedDocument.builder()
        .fileName(originalFilename)
        .fileSize(originalFileSize)
        .hostedUrl("https://example.com/" + originalFilename)
        .build();
  }
}

package uk.gov.justice.laa_civil_manage_api.services.accessdatastore;

import java.util.UUID;

/**
 * Response body returned by the Access Data Store when a supporting document is uploaded against a
 * prior authority.
 *
 * @param documentId the identifier assigned to the uploaded document
 */
public record UploadPriorAuthorityDocumentResponse(UUID documentId) {}

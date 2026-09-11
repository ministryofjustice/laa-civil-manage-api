package uk.gov.justice.laa_civil_manage_api.services.accessdatastore;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DocumentTypeUpdateResponse(UUID documentId, OffsetDateTime updatedAt) {}

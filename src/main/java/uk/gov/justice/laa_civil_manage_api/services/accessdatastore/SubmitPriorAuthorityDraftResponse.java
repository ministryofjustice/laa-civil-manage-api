package uk.gov.justice.laa_civil_manage_api.services.accessdatastore;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Response body returned by the Access Data Store when a prior-authority draft is submitted.
 *
 * @param priorAuthorityId the identifier of the prior authority that was submitted
 * @param submittedAt the timestamp the Access Data Store recorded the submission at
 */
public record SubmitPriorAuthorityDraftResponse(
    UUID priorAuthorityId, OffsetDateTime submittedAt) {}

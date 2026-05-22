package uk.gov.justice.laa_civil_manage_api.models;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Outcome of a prior-authority submission. ACCEPTED means the Access Data Store has taken the request; REJECTED means it was refused.")
public enum SubmissionStatus {
    ACCEPTED,
    REJECTED
}

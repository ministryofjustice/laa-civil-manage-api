package uk.gov.justice.laa_civil_manage_api.models;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "How the work is billed")
public enum BillingType {
    HOURLY,
    FLAT_RATE
}

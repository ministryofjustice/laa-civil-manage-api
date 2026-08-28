package uk.gov.justice.laa_civil_manage_api.models;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "The type of prior authority")
public enum PriorAuthorityType {
  EXPERT("Expert type"),
  DISBURSEMENT("Disbursement"),
  COUNSEL("Counsel");

  private final String displayName;

  PriorAuthorityType(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }
}

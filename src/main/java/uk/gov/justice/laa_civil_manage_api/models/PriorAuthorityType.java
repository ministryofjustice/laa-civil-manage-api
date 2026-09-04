package uk.gov.justice.laa_civil_manage_api.models;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "The category of prior authority being requested.")
public enum PriorAuthorityType {
  EXPERT("Expert"),
  COUNSEL("Counsel"),
  DISBURSEMENT("Disbursement");

  private final String displayName;

  PriorAuthorityType(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }
}

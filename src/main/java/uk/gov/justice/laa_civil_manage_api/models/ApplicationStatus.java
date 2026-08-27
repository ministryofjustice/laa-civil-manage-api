package uk.gov.justice.laa_civil_manage_api.models;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "The status of an application")
public enum ApplicationStatus {
  APPLICATION_SUBMITTED,
  APPLICATION_GRANTED,
  APPLICATION_REFUSED
}

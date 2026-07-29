package uk.gov.justice.laa_civil_manage_api.models;

import io.swagger.v3.oas.annotations.media.Schema;

// TODO - enums will need changing once we have a definitive list of statuses from the Access Data
// Store API

@Schema(description = "The status of an application")
public enum ApplicationStatus {
  APPLICATION_SUBMITTED,
  READY,
  APPLICATION_IN_PROGRESS,
  GRANTED,
  REFUSED
}

package uk.gov.justice.laa_civil_manage_api.models;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Type of Counsel being applied for.")
public enum CounselType {
  KINGS_COUNSEL_ALONE,
  TWO_JUNIOR_COUNSEL,
  KINGS_COUNSEL_AND_JUNIOR_COUNSEL,
  KINGS_COUNSEL_AND_TWO_JUNIOR_COUNSEL
}

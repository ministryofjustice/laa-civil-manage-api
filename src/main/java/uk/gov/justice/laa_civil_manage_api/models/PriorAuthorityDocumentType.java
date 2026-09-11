package uk.gov.justice.laa_civil_manage_api.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "The type of document being uploaded as supporting evidence.")
public enum PriorAuthorityDocumentType {
  @JsonProperty("gateway_evidence")
  GATEWAY_EVIDENCE,
  @JsonProperty("merits_report")
  MERITS_REPORT,
  @JsonProperty("statement_of_case")
  STATEMENT_OF_CASE,
  @JsonProperty("court_application")
  COURT_APPLICATION,
  @JsonProperty("court_order")
  COURT_ORDER,
  @JsonProperty("expert_report")
  EXPERT_REPORT,
  @JsonProperty("court_application_or_order")
  COURT_APPLICATION_OR_ORDER,
  @JsonProperty("parental_responsibility")
  PARENTAL_RESPONSIBILITY,
  @JsonProperty("local_authority_assessment")
  LOCAL_AUTHORITY_ASSESSMENT,
  @JsonProperty("grounds_of_appeal")
  GROUNDS_OF_APPEAL,
  @JsonProperty("counsel_opinion")
  COUNSEL_OPINION,
  @JsonProperty("judgement")
  JUDGEMENT,
  @JsonProperty("plf_court_order")
  PLF_COURT_ORDER
}

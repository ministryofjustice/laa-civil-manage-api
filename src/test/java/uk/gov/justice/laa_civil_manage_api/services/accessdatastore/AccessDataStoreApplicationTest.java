package uk.gov.justice.laa_civil_manage_api.services.accessdatastore;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import uk.gov.justice.laa_civil_manage_api.models.ApplicationSummary;

class AccessDataStoreApplicationTest {

  private static final UUID APPLICATION_ID = UUID.randomUUID();
  private static final OffsetDateTime SUBMITTED_AT = OffsetDateTime.parse("2026-07-22T10:00:00Z");

  @Test
  void toApplicationSummaryMapsAllFieldsIncludingOfficeCode() {
    AccessDataStoreApplication application =
        new AccessDataStoreApplication(
            APPLICATION_ID,
            "APP-1",
            "APPLICATION_SUBMITTED",
            SUBMITTED_AT,
            "John",
            "Doe",
            "SPECIAL_CHILDREN_ACT",
            new AccessDataStoreProvider("0W839P"));

    ApplicationSummary summary = application.toApplicationSummary();

    assertEquals(
        ApplicationSummary.builder()
            .applicationId(APPLICATION_ID)
            .laaReference("APP-1")
            .status("APPLICATION_SUBMITTED")
            .startDate(SUBMITTED_AT)
            .clientFirstName("John")
            .clientLastName("Doe")
            .matterType("SPECIAL_CHILDREN_ACT")
            .officeCode("0W839P")
            .build(),
        summary);
  }

  @Test
  void toApplicationSummaryLeavesOfficeCodeNullWhenProviderIsMissing() {
    AccessDataStoreApplication application =
        new AccessDataStoreApplication(
            APPLICATION_ID, "APP-1", "APPLICATION_SUBMITTED", SUBMITTED_AT, null, null, null, null);

    assertNull(application.toApplicationSummary().officeCode());
  }
}

package uk.gov.justice.laa_civil_manage_api.services.accessdatastore;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import uk.gov.justice.laa_civil_manage_api.models.ApplicationSummary;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthoritySummary;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthorityType;

class AccessDataStoreApplicationTest {

  private static final UUID APPLICATION_ID = UUID.randomUUID();
  private static final OffsetDateTime SUBMITTED_AT = OffsetDateTime.parse("2026-07-22T10:00:00Z");

  @Test
  void toApplicationSummaryMapsAllFieldsIncludingOfficeCodeAndPriorAuthorities() {
    PriorAuthoritySummary priorAuthority =
        PriorAuthoritySummary.builder()
            .priorAuthorityId(UUID.randomUUID())
            .priorAuthorityType(PriorAuthorityType.EXPERT)
            .status("DECIDED")
            .decision("GRANTED")
            .createdAt(OffsetDateTime.parse("2026-09-24T15:00:15.141805Z"))
            .build();

    AccessDataStoreApplication application =
        new AccessDataStoreApplication(
            APPLICATION_ID,
            "APP-1",
            "APPLICATION_SUBMITTED",
            SUBMITTED_AT,
            "John",
            "Doe",
            "SPECIAL_CHILDREN_ACT",
            new AccessDataStoreProvider("0W839P"),
            List.of(priorAuthority));

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
            .priorAuthorities(List.of(priorAuthority))
            .build(),
        summary);
  }

  @Test
  void toApplicationSummaryLeavesOfficeCodeNullWhenProviderIsMissing() {
    AccessDataStoreApplication application =
        new AccessDataStoreApplication(
            APPLICATION_ID,
            "APP-1",
            "APPLICATION_SUBMITTED",
            SUBMITTED_AT,
            null,
            null,
            null,
            null,
            null);

    assertNull(application.toApplicationSummary().officeCode());
  }

  @Test
  void toApplicationSummaryDefaultsPriorAuthoritiesToEmptyListWhenMissing() {
    AccessDataStoreApplication application =
        new AccessDataStoreApplication(
            APPLICATION_ID,
            "APP-1",
            "APPLICATION_SUBMITTED",
            SUBMITTED_AT,
            null,
            null,
            null,
            null,
            null);

    assertEquals(List.of(), application.toApplicationSummary().priorAuthorities());
  }
}

package uk.gov.justice.laa_civil_manage_api.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa_civil_manage_api.models.ApplicationSummary;
import uk.gov.justice.laa_civil_manage_api.models.Client;
import uk.gov.justice.laa_civil_manage_api.models.IndividualsResponse;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthoritySummary;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthorityType;
import uk.gov.justice.laa_civil_manage_api.services.accessdatastore.AccessDataStoreApplication;
import uk.gov.justice.laa_civil_manage_api.services.accessdatastore.AccessDataStoreClient;
import uk.gov.justice.laa_civil_manage_api.services.accessdatastore.AccessDataStoreProvider;

@ExtendWith(MockitoExtension.class)
class ApplicationsServiceTest {

  private static final UUID APPLICATION_ID =
      UUID.fromString("11111111-2222-3333-4444-555555555555");

  private static final PriorAuthoritySummary PRIOR_AUTHORITY =
      PriorAuthoritySummary.builder()
          .priorAuthorityId(UUID.fromString("c3b07e24-d92b-410a-9d95-88f117a12b43"))
          .priorAuthorityType(PriorAuthorityType.COUNSEL)
          .status("DRAFT")
          .createdAt(OffsetDateTime.parse("2026-07-23T09:30:00Z"))
          .build();

  @Mock private AccessDataStoreClient accessDataStoreClient;

  @InjectMocks private ApplicationsService applicationsService;

  @Test
  void getApplicationByIdKeepsPriorAuthoritiesWhenEnrichingWithClientName() {
    when(accessDataStoreClient.getApplicationById(APPLICATION_ID))
        .thenReturn(adsApplication(List.of(PRIOR_AUTHORITY)));
    when(accessDataStoreClient.getIndividuals(APPLICATION_ID))
        .thenReturn(
            IndividualsResponse.builder()
                .individuals(List.of(Client.builder().firstName("John").lastName("Doe").build()))
                .build());

    ApplicationSummary result = applicationsService.getApplicationById(APPLICATION_ID.toString());

    assertEquals("John", result.clientFirstName());
    assertEquals("Doe", result.clientLastName());
    assertEquals(List.of(PRIOR_AUTHORITY), result.priorAuthorities());
  }

  @Test
  void getApplicationByIdReturnsPriorAuthoritiesWhenNoIndividualsFound() {
    when(accessDataStoreClient.getApplicationById(APPLICATION_ID))
        .thenReturn(adsApplication(List.of(PRIOR_AUTHORITY)));
    when(accessDataStoreClient.getIndividuals(APPLICATION_ID)).thenReturn(null);

    ApplicationSummary result = applicationsService.getApplicationById(APPLICATION_ID.toString());

    assertNull(result.clientFirstName());
    assertEquals(List.of(PRIOR_AUTHORITY), result.priorAuthorities());
  }

  @Test
  void getApplicationByIdReturnsEmptyPriorAuthoritiesWhenAdsOmitsThem() {
    when(accessDataStoreClient.getApplicationById(APPLICATION_ID)).thenReturn(adsApplication(null));
    when(accessDataStoreClient.getIndividuals(APPLICATION_ID)).thenReturn(null);

    ApplicationSummary result = applicationsService.getApplicationById(APPLICATION_ID.toString());

    assertEquals(List.of(), result.priorAuthorities());
  }

  @Test
  void getApplicationByIdReturnsNullAndSkipsIndividualsWhenApplicationNotFound() {
    when(accessDataStoreClient.getApplicationById(APPLICATION_ID)).thenReturn(null);

    assertNull(applicationsService.getApplicationById(APPLICATION_ID.toString()));
    verify(accessDataStoreClient, never()).getIndividuals(APPLICATION_ID);
  }

  private static AccessDataStoreApplication adsApplication(
      List<PriorAuthoritySummary> priorAuthorities) {
    return new AccessDataStoreApplication(
        APPLICATION_ID,
        "APP-1",
        "APPLICATION_SUBMITTED",
        OffsetDateTime.parse("2026-07-22T10:00:00Z"),
        null,
        null,
        "SPECIAL_CHILDREN_ACT",
        new AccessDataStoreProvider("0W839P"),
        priorAuthorities);
  }
}

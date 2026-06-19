package uk.gov.justice.laa_civil_manage_api.services.osplaces;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import uk.gov.justice.laa_civil_manage_api.client.OsPlacesClient;
import uk.gov.justice.laa_civil_manage_api.config.OsPlacesConfig;
import uk.gov.justice.laa_civil_manage_api.services.PostcodeLookupService;

class PostcodeLookupServiceTest {

  private final OsPlacesClient osPlacesClient = mock(OsPlacesClient.class);
  private final OsPlacesConfig properties =
      new OsPlacesConfig("https://api.os.uk/search/places/v1", "test-key", List.of(5990, 5010));
  private final PostcodeLookupService service = new PostcodeLookupService(osPlacesClient, properties);

  @Test
  void returnsTrueWhenCustodianCodeIsConfiguredAsLondon() {
    when(osPlacesClient.lookupCustodianCodeForPostcode("SW1A 1AA")).thenReturn(Optional.of(5990));

    assertTrue(service.isInLondon("SW1A 1AA"));
  }

  @Test
  void returnsFalseWhenCustodianCodeIsNotConfiguredAsLondon() {
    when(osPlacesClient.lookupCustodianCodeForPostcode("LS1 1UR")).thenReturn(Optional.of(3000));

    assertFalse(service.isInLondon("LS1 1UR"));
  }

  @Test
  void returnsFalseWhenNoCustodianCodeFound() {
    when(osPlacesClient.lookupCustodianCodeForPostcode("ZZ99 9ZZ")).thenReturn(Optional.empty());

    assertFalse(service.isInLondon("ZZ99 9ZZ"));
  }
}

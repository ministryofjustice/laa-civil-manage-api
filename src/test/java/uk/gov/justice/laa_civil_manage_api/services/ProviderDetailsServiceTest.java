package uk.gov.justice.laa_civil_manage_api.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import uk.gov.justice.laa_civil_manage_api.services.providerdetails.ProviderDetailsClient;

class ProviderDetailsServiceTest {

  private final ProviderDetailsClient client = mock(ProviderDetailsClient.class);
  private final ProviderDetailsService service = new ProviderDetailsService(client);

  @Test
  void delegatesToTheClient() {
    when(client.getProviderEmail("1A234B")).thenReturn("office@example.com");

    assertEquals("office@example.com", service.getProviderEmail("1A234B"));
  }

  @Test
  void returnsNullWhenTheClientHasNoEmailOnRecord() {
    when(client.getProviderEmail("1A234B")).thenReturn(null);

    assertNull(service.getProviderEmail("1A234B"));
  }
}

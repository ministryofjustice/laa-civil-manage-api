package uk.gov.justice.laa_civil_manage_api.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import uk.gov.justice.laa_civil_manage_api.services.legalframework.ExpertType;
import uk.gov.justice.laa_civil_manage_api.services.legalframework.LegalFrameworkClient;

class ExpertServiceTest {

  private final LegalFrameworkClient client = mock(LegalFrameworkClient.class);
  private final ExpertService service = new ExpertService(client);

  @Test
  void returnsDescriptionsInUpstreamOrderAndDiscardsTheCodes() {
    when(client.getExpertTypes("KPBLW"))
        .thenReturn(
            List.of(
                new ExpertType("psychologist", "Psychologist"),
                new ExpertType("child_psychologist", "Child Psychologist")));

    assertEquals(
        List.of("Psychologist", "Child Psychologist"), service.getExpertTypeDescriptions("KPBLW"));
  }

  @Test
  void skipsEntriesWithoutAUsableDescription() {
    when(client.getExpertTypes("KPBLW"))
        .thenReturn(
            Arrays.asList(
                new ExpertType("psychologist", "Psychologist"),
                new ExpertType("blank", "  "),
                new ExpertType("missing", null)));

    assertEquals(List.of("Psychologist"), service.getExpertTypeDescriptions("KPBLW"));
  }

  @Test
  void returnsEmptyListWhenTheMatterTypeHasNoExpertTypes() {
    when(client.getExpertTypes("KPBLW")).thenReturn(List.of());

    assertTrue(service.getExpertTypeDescriptions("KPBLW").isEmpty());
  }
}

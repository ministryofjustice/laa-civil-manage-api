package uk.gov.justice.laa_civil_manage_api.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import uk.gov.justice.laa_civil_manage_api.services.legalframework.ExpertType;
import uk.gov.justice.laa_civil_manage_api.services.legalframework.LegalFrameworkClient;

class ExpertTypeServiceTest {

  private final LegalFrameworkClient client = mock(LegalFrameworkClient.class);
  private final ExpertTypeService service = new ExpertTypeService(client);

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
  @ExtendWith(OutputCaptureExtension.class)
  void skipsEntriesWithoutAUsableDescription(CapturedOutput output) {
    when(client.getExpertTypes("KPBLW"))
        .thenReturn(
            Arrays.asList(
                new ExpertType("psychologist", "Psychologist"),
                new ExpertType("blank", "  "),
                new ExpertType("missing", null)));

    assertEquals(List.of("Psychologist"), service.getExpertTypeDescriptions("KPBLW"));

    assertTrue(
        output
            .getOut()
            .contains(
                "LFA returned expert with missing/blank description for matter type KPBLW. Code: 'blank'"));
    assertTrue(
        output
            .getOut()
            .contains(
                "LFA returned expert with missing/blank description for matter type KPBLW. Code: 'missing'"));
  }

  @Test
  void returnsEmptyListWhenTheMatterTypeHasNoExpertTypes() {
    when(client.getExpertTypes("KPBLW")).thenReturn(List.of());

    assertTrue(service.getExpertTypeDescriptions("KPBLW").isEmpty());
  }
}

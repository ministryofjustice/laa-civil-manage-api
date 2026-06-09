package uk.gov.justice.laa_civil_manage_api.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import uk.gov.justice.laa_civil_manage_api.models.BillingType;
import uk.gov.justice.laa_civil_manage_api.models.Draft;
import uk.gov.justice.laa_civil_manage_api.models.DraftCreatedResponse;
import uk.gov.justice.laa_civil_manage_api.models.DraftSummary;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthorityDraft;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthorityDraftSummary;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthorityType;
import uk.gov.justice.laa_civil_manage_api.services.accessdatastore.AccessDataStoreClient;

class PriorAuthorityDraftServiceTest {

  private final AccessDataStoreClient client = mock(AccessDataStoreClient.class);
  private final PriorAuthorityDraftService service = new PriorAuthorityDraftService(client);

  @Test
  void createWrapsTypedDraftInGenericEnvelopeWithHardcodedSourceSystemAndDraftType() {
    UUID applicationId = UUID.randomUUID();
    UUID assignedDraftId = UUID.randomUUID();
    when(client.createDraft(any(Draft.class)))
        .thenReturn(DraftCreatedResponse.builder().draftId(assignedDraftId).build());

    PriorAuthorityDraft draft =
        PriorAuthorityDraft.builder()
            .applicationId(applicationId)
            .type(PriorAuthorityType.EXPERT)
            .expertType("Child psychologist")
            .expertFullName("Dr Joe Bloggs")
            .billingType(BillingType.HOURLY)
            .hourlyRate(new BigDecimal("45.00"))
            .totalAmount(new BigDecimal("135.00"))
            .build();

    UUID result = service.create(draft);

    assertEquals(assignedDraftId, result);

    ArgumentCaptor<Draft> envelope = ArgumentCaptor.forClass(Draft.class);
    verify(client).createDraft(envelope.capture());
    Draft sent = envelope.getValue();
    assertEquals("laa-civil-manage", sent.sourceSystem());
    assertEquals("PRIOR_AUTHORITY", sent.draftType());
    assertEquals(applicationId, sent.applicationId());
    assertNotNull(sent.userId());
    assertFalse(sent.userId().isBlank());
    assertEquals(applicationId.toString(), sent.draftBody().get("applicationId"));
    assertEquals("Child psychologist", sent.draftBody().get("expertType"));
    assertEquals("Dr Joe Bloggs", sent.draftBody().get("expertFullName"));
    assertEquals("HOURLY", sent.draftBody().get("billingType"));
  }

  @Test
  void createHandlesMinimalDraftWithNullFields() {
    UUID applicationId = UUID.randomUUID();
    UUID assignedDraftId = UUID.randomUUID();
    when(client.createDraft(any(Draft.class)))
        .thenReturn(DraftCreatedResponse.builder().draftId(assignedDraftId).build());

    PriorAuthorityDraft minimalDraft =
        PriorAuthorityDraft.builder().applicationId(applicationId).build();

    UUID result = service.create(minimalDraft);

    assertEquals(assignedDraftId, result);

    ArgumentCaptor<Draft> envelope = ArgumentCaptor.forClass(Draft.class);
    verify(client).createDraft(envelope.capture());
    Draft sent = envelope.getValue();

    assertEquals(applicationId, sent.applicationId());
    assertNull(sent.draftBody().get("expertType"));
    assertNull(sent.draftBody().get("expertFullName"));
    assertNull(sent.draftBody().get("billingType"));
  }

  @Test
  void updateDelegatesAndWrapsEnvelope() {
    UUID draftId = UUID.randomUUID();
    UUID applicationId = UUID.randomUUID();
    PriorAuthorityDraft draft =
        PriorAuthorityDraft.builder()
            .applicationId(applicationId)
            .totalAmount(new BigDecimal("180.00"))
            .build();

    service.update(draftId, draft);

    ArgumentCaptor<Draft> envelope = ArgumentCaptor.forClass(Draft.class);
    verify(client).updateDraft(eq(draftId), envelope.capture());
    Draft sent = envelope.getValue();
    assertEquals(applicationId, sent.applicationId());
    assertEquals(180.00, ((Number) sent.draftBody().get("totalAmount")).doubleValue());
  }

  @Test
  void listConvertsGenericSummariesBackToTypedDrafts() {
    UUID draftId = UUID.randomUUID();
    UUID applicationId = UUID.randomUUID();
    when(client.getDrafts(eq("laa-civil-manage"), any(), eq("PRIOR_AUTHORITY"), eq(applicationId)))
        .thenReturn(
            List.of(
                DraftSummary.builder()
                    .draftId(draftId)
                    .draftType("PRIOR_AUTHORITY")
                    .timestamp(OffsetDateTime.parse("2026-05-19T12:00:00Z"))
                    .draftBody(
                        Map.of(
                            "applicationId",
                            applicationId.toString(),
                            "expertFullName",
                            "Dr Joe Bloggs",
                            "billingType",
                            "FLAT_RATE",
                            "flatRateTotalAmount",
                            249.99))
                    .build()));

    List<PriorAuthorityDraftSummary> result = service.list(applicationId);

    assertEquals(1, result.size());
    PriorAuthorityDraftSummary summary = result.getFirst();
    assertEquals(draftId, summary.draftId());
    assertEquals(OffsetDateTime.parse("2026-05-19T12:00:00Z"), summary.timestamp());
    assertEquals(applicationId, summary.draft().applicationId());
    assertEquals("Dr Joe Bloggs", summary.draft().expertFullName());
    assertEquals(BillingType.FLAT_RATE, summary.draft().billingType());
    assertEquals(0, new BigDecimal("249.99").compareTo(summary.draft().flatRateTotalAmount()));
    assertNull(summary.draft().expertType());
    assertNull(summary.draft().hourlyRate());
  }

  @Test
  void deleteDelegatesToClient() {
    UUID draftId = UUID.randomUUID();
    service.delete(draftId);
    verify(client).deleteDraft(draftId);
  }

  @Test
  void createAndListUseTheSamePlaceholderUserId() {
    UUID applicationId = UUID.randomUUID();
    when(client.createDraft(any(Draft.class)))
        .thenReturn(DraftCreatedResponse.builder().draftId(UUID.randomUUID()).build());
    when(client.getDrafts(any(), any(), any(), any())).thenReturn(List.of());

    service.create(PriorAuthorityDraft.builder().applicationId(applicationId).build());
    service.list(null);

    ArgumentCaptor<Draft> envelope = ArgumentCaptor.forClass(Draft.class);
    verify(client).createDraft(envelope.capture());
    String userIdOnCreate = envelope.getValue().userId();

    ArgumentCaptor<String> userIdCaptor = ArgumentCaptor.forClass(String.class);
    verify(client).getDrafts(any(), userIdCaptor.capture(), any(), any());

    assertEquals(userIdOnCreate, userIdCaptor.getValue());
  }
}

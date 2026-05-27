package uk.gov.justice.laa_civil_manage_api.services;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import uk.gov.justice.laa_civil_manage_api.models.Draft;
import uk.gov.justice.laa_civil_manage_api.models.DraftCreatedResponse;
import uk.gov.justice.laa_civil_manage_api.models.DraftSummary;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthorityDraft;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthorityDraftSummary;
import uk.gov.justice.laa_civil_manage_api.services.accessdatastore.AccessDataStoreClient;

@Service
public class PriorAuthorityDraftService {

    private static final String SOURCE_SYSTEM = "laa-civil-manage";
    private static final String DRAFT_TYPE = "PRIOR_AUTHORITY";

    // TODO: replace with the authenticated user's Entra ID once auth is wired up.
    // For now we use a single placeholder per process so create/list/update/delete stay consistent locally.
    private static final String PLACEHOLDER_USER_ID = UUID.randomUUID().toString();

    private final AccessDataStoreClient accessDataStoreClient;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public PriorAuthorityDraftService(AccessDataStoreClient accessDataStoreClient) {
        this.accessDataStoreClient = accessDataStoreClient;
    }

    public UUID create(PriorAuthorityDraft draft) {
        DraftCreatedResponse response = accessDataStoreClient.createDraft(toEnvelope(draft));
        return response.draftId();
    }

    public void update(UUID draftId, PriorAuthorityDraft draft) {
        accessDataStoreClient.updateDraft(draftId, toEnvelope(draft));
    }

    public List<PriorAuthorityDraftSummary> list(UUID applicationId) {
        List<DraftSummary> summaries = accessDataStoreClient.getDrafts(
                SOURCE_SYSTEM, PLACEHOLDER_USER_ID, DRAFT_TYPE, applicationId);
        return summaries.stream()
                .map(this::toTypedSummary)
                .toList();
    }

    public void delete(UUID draftId) {
        accessDataStoreClient.deleteDraft(draftId);
    }

    private Draft toEnvelope(PriorAuthorityDraft draft) {
        return Draft.builder()
                .sourceSystem(SOURCE_SYSTEM)
                .draftType(DRAFT_TYPE)
                .applicationId(draft.applicationId())
                .userId(PLACEHOLDER_USER_ID)
                .draftBody(objectMapper.convertValue(draft, new com.fasterxml.jackson.core.type.TypeReference<>() {
                }))
                .build();
    }

    private PriorAuthorityDraftSummary toTypedSummary(DraftSummary summary) {
        PriorAuthorityDraft draft = objectMapper.convertValue(summary.draftBody(), PriorAuthorityDraft.class);
        return PriorAuthorityDraftSummary.builder()
                .draftId(summary.draftId())
                .timestamp(summary.timestamp())
                .draft(draft)
                .build();
    }
}

package uk.gov.justice.laa_civil_manage_api.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.justice.laa_civil_manage_api.models.Draft;
import uk.gov.justice.laa_civil_manage_api.models.DraftCreatedResponse;
import uk.gov.justice.laa_civil_manage_api.models.DraftSummary;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthorityDraft;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthorityDraftSummary;
import uk.gov.justice.laa_civil_manage_api.services.accessdatastore.AccessDataStoreClient;

@Slf4j
@Service
public class PriorAuthorityDraftService {

  private static final String SOURCE_SYSTEM = "laa-civil-manage";
  private static final String DRAFT_TYPE = "PRIOR_AUTHORITY";

  // TODO: replace with the authenticated user's Entra ID once auth is wired up.
  // For now we use a single placeholder per process so create/list/update/delete stay consistent
  // locally.
  private static final String PLACEHOLDER_USER_ID = UUID.randomUUID().toString();

  private final AccessDataStoreClient accessDataStoreClient;
  private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

  public PriorAuthorityDraftService(AccessDataStoreClient accessDataStoreClient) {
    this.accessDataStoreClient = accessDataStoreClient;
  }

  public UUID create(PriorAuthorityDraft draft) {
    log.info(
        "Creating prior authority draft: applicationId={}, priorAuthorityType={}, expertType={}, billingType={}",
        draft.applicationId(),
        draft.priorAuthorityType(),
        draft.expertType(),
        draft.billingType());
    DraftCreatedResponse response = accessDataStoreClient.createDraft(toEnvelope(draft));
    log.info(
        "Created prior authority draft: draftId={}, applicationId={}",
        response.draftId(),
        draft.applicationId());
    return response.draftId();
  }

  public void update(UUID draftId, PriorAuthorityDraft draft) {
    log.info(
        "Updating prior authority draft: draftId={}, applicationId={}, priorAuthorityType={}, billingType={}",
        draftId,
        draft.applicationId(),
        draft.priorAuthorityType(),
        draft.billingType());
    accessDataStoreClient.updateDraft(draftId, toEnvelope(draft));
  }

  public Optional<PriorAuthorityDraftSummary> get(UUID draftId) {
    log.info("Get prior authority draft: draftId={}", draftId);

    Optional<DraftSummary> summary = accessDataStoreClient.getDraft(draftId);
    summary.ifPresent(
        s ->
            log.info(
                "Found prior authority draft: draftId={}, applicationId={}",
                s.draftId(),
                s.draftBody().get("applicationId")));
    summary.ifPresentOrElse(
        _ -> {}, () -> log.info("No prior authority draft found for draftId={}", draftId));
    return summary.map(this::toTypedSummary);
  }

  public List<PriorAuthorityDraftSummary> list(UUID applicationId) {
    log.info("Listing prior authority drafts: applicationId={}", applicationId);
    List<DraftSummary> summaries =
        accessDataStoreClient.getDrafts(
            SOURCE_SYSTEM, PLACEHOLDER_USER_ID, DRAFT_TYPE, applicationId);
    log.info(
        "Found {} prior authority draft(s) for applicationId={}", summaries.size(), applicationId);
    return summaries.stream().map(this::toTypedSummary).toList();
  }

  public void delete(UUID draftId) {
    log.info("Deleting prior authority draft: draftId={}", draftId);
    accessDataStoreClient.deleteDraft(draftId);
  }

  private Draft toEnvelope(PriorAuthorityDraft draft) {
    return Draft.builder()
        .sourceSystem(SOURCE_SYSTEM)
        .draftType(DRAFT_TYPE)
        .applicationId(draft.applicationId())
        .userId(PLACEHOLDER_USER_ID)
        .draftBody(
            objectMapper.convertValue(
                draft, new com.fasterxml.jackson.core.type.TypeReference<>() {}))
        .build();
  }

  private PriorAuthorityDraftSummary toTypedSummary(DraftSummary summary) {
    PriorAuthorityDraft draft =
        objectMapper.convertValue(summary.draftBody(), PriorAuthorityDraft.class);
    return PriorAuthorityDraftSummary.builder()
        .draftId(summary.draftId())
        .timestamp(summary.timestamp())
        .draft(draft)
        .build();
  }
}

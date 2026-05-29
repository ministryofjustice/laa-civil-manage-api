package uk.gov.justice.laa_civil_manage_api.mockaccessdatastore.services;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.http.HttpStatus;
import uk.gov.justice.laa_civil_manage_api.models.Draft;
import uk.gov.justice.laa_civil_manage_api.models.DraftCreatedResponse;
import uk.gov.justice.laa_civil_manage_api.models.DraftSummary;

@Slf4j
@Component
public class MockDraftStore {

    private record StoredDraft(UUID draftId, Draft draft, OffsetDateTime timestamp) {
    }

    private final ConcurrentMap<UUID, StoredDraft> draftsById = new ConcurrentHashMap<>();

    public DraftCreatedResponse create(Draft draft) {
        UUID draftId = UUID.randomUUID();
        draftsById.put(draftId, new StoredDraft(draftId, draft, OffsetDateTime.now()));
        return DraftCreatedResponse.builder().draftId(draftId).build();
    }

    public void update(UUID draftId, Draft draft) {
        StoredDraft existing = draftsById.get(draftId);
        if (existing == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Draft " + draftId + " not found");
        }
        draftsById.put(draftId, new StoredDraft(draftId, draft, OffsetDateTime.now()));
    }

    public Optional<DraftSummary> get(UUID draftId) {
        return Optional.ofNullable(draftsById.get(draftId))
                .map(stored -> DraftSummary.builder()
                        .draftId(stored.draftId())
                        .draftType(stored.draft().draftType())
                        .timestamp(stored.timestamp())
                        .draftBody(stored.draft().draftBody())
                        .build());
    }

    public List<DraftSummary> list(String sourceSystem, String userId, String draftType, UUID applicationId) {
        return draftsById.values().stream()
                .filter(stored -> stored.draft().sourceSystem().equals(sourceSystem))
                .filter(stored -> stored.draft().userId().equals(userId))
                .filter(stored -> draftType == null || stored.draft().draftType().equals(draftType))
                .filter(stored -> applicationId == null || stored.draft().applicationId().equals(applicationId))
                .map(stored -> DraftSummary.builder()
                        .draftId(stored.draftId())
                        .draftType(stored.draft().draftType())
                        .timestamp(stored.timestamp())
                        .draftBody(stored.draft().draftBody())
                        .build())
                .toList();
    }

    public void delete(UUID draftId) {
        draftsById.remove(draftId);
    }
}

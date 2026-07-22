package uk.gov.justice.laa_civil_manage_api.services.accessdatastore;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import uk.gov.justice.laa_civil_manage_api.models.*;

public interface AccessDataStoreClient {

  PriorAuthorityApplicationResponse submitPriorAuthority(PriorAuthority priorAuthority);

  DraftCreatedResponse createDraft(Draft draft);

  void updateDraft(UUID draftId, Draft draft);

  Optional<DraftSummary> getDraft(UUID draftId);

  List<DraftSummary> getDrafts(
      String sourceSystem, String userId, String draftType, UUID applicationId);

  void deleteDraft(UUID draftId);

  ApplicationSummaryResponse getApplications();
}

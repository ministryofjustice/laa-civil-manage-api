package uk.gov.justice.laa_civil_manage_api.mockaccessdatastore.services;

import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

import uk.gov.justice.laa_civil_manage_api.mockaccessdatastore.models.PriorAuthoritySubmission;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthorityApplicationResponse;
import uk.gov.justice.laa_civil_manage_api.models.SubmissionStatus;

@Component
public class MockPriorAuthorityStore {

    private final ConcurrentMap<UUID, PriorAuthorityApplicationResponse> responsesByApplicationId =
            new ConcurrentHashMap<>();

    public PriorAuthorityApplicationResponse submit(UUID applicationId, PriorAuthoritySubmission submission) {
        return responsesByApplicationId.computeIfAbsent(applicationId, id ->
                PriorAuthorityApplicationResponse.builder()
                        .submissionId(UUID.randomUUID())
                        .status(SubmissionStatus.ACCEPTED)
                        .submittedAt(OffsetDateTime.now())
                        .build()
        );
    }
}

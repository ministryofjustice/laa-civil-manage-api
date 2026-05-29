package uk.gov.justice.laa_civil_manage_api.services;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import uk.gov.justice.laa_civil_manage_api.models.BillingType;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthority;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthorityApplicationResponse;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthorityType;
import uk.gov.justice.laa_civil_manage_api.models.SubmissionStatus;
import uk.gov.justice.laa_civil_manage_api.services.accessdatastore.AccessDataStoreClient;

class PriorAuthorityServiceTest {

    private final AccessDataStoreClient client = mock(AccessDataStoreClient.class);
    private final PriorAuthorityService service = new PriorAuthorityService(client);

    @Test
    void delegatesToAccessDataStoreClient() {
        PriorAuthority pa = PriorAuthority.builder()
                .applicationId(UUID.randomUUID())
                .type(PriorAuthorityType.EXPERT)
                .expertType("Psychologist")
                .expertFullName("John Doe")
                .isInLondon(false)
                .guidelineRatesExceeded(false)
                .billingType(BillingType.FLAT_RATE)
                .flatRateTotalAmount(new BigDecimal("249.99"))
                .build();
        PriorAuthorityApplicationResponse expected = PriorAuthorityApplicationResponse.builder()
                .submissionId(UUID.randomUUID())
                .status(SubmissionStatus.ACCEPTED)
                .submittedAt(OffsetDateTime.parse("2026-05-22T10:00:00Z"))
                .build();
        when(client.submitPriorAuthority(pa)).thenReturn(expected);

        PriorAuthorityApplicationResponse actual = service.submit(pa);

        assertSame(expected, actual);
        verify(client).submitPriorAuthority(pa);
    }
}

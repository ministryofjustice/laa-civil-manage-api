package uk.gov.justice.laa_civil_manage_api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.server.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import uk.gov.justice.laa_civil_manage_api.controllers.PriorAuthorityController.DraftIdResponse;
import uk.gov.justice.laa_civil_manage_api.models.BillingType;
import uk.gov.justice.laa_civil_manage_api.models.Draft;
import uk.gov.justice.laa_civil_manage_api.models.DraftCreatedResponse;
import uk.gov.justice.laa_civil_manage_api.models.DraftSummary;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthorityDraft;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthorityDraftSummary;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthorityType;
import uk.gov.justice.laa_civil_manage_api.services.accessdatastore.AccessDataStoreClient;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class PriorAuthorityDraftIntegrationTest {

    private static final ParameterizedTypeReference<List<DraftSummary>> DRAFT_LIST_TYPE =
            new ParameterizedTypeReference<>() {
            };
    private static final ParameterizedTypeReference<List<PriorAuthorityDraftSummary>> TYPED_LIST =
            new ParameterizedTypeReference<>() {
            };

    @TestConfiguration
    static class Config {

        @Bean
        @Primary
        AccessDataStoreClient testAccessDataStoreClient(
                ServletWebServerApplicationContext context,
                RestClient.Builder builder
        ) {
            return new AccessDataStoreClient() {
                private String url() {
                    return "http://localhost:" + context.getWebServer().getPort() + "/mock-access-data-store";
                }

                @Override
                public uk.gov.justice.laa_civil_manage_api.models.PriorAuthorityApplicationResponse submitPriorAuthority(
                        uk.gov.justice.laa_civil_manage_api.models.PriorAuthority pa
                ) {
                    throw new UnsupportedOperationException("Not used in this test");
                }

                @Override
                public DraftCreatedResponse createDraft(Draft draft) {
                    return builder.build().post()
                            .uri(url() + "/drafts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(draft)
                            .retrieve()
                            .body(DraftCreatedResponse.class);
                }

                @Override
                public void updateDraft(UUID draftId, Draft draft) {
                    builder.build().put()
                            .uri(url() + "/drafts/{id}", draftId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(draft)
                            .retrieve()
                            .toBodilessEntity();
                }

                @Override
                public List<DraftSummary> getDrafts(String sourceSystem, String userId,
                                                    String draftType, UUID applicationId) {
                    UriComponentsBuilder uri = UriComponentsBuilder.fromUriString(url() + "/drafts")
                            .queryParam("sourceSystem", sourceSystem)
                            .queryParam("userId", userId);
                    if (draftType != null) {
                        uri.queryParam("draftType", draftType);
                    }
                    if (applicationId != null) {
                        uri.queryParam("applicationId", applicationId);
                    }
                    return builder.build().get()
                            .uri(uri.build().toUri())
                            .retrieve()
                            .body(DRAFT_LIST_TYPE);
                }

                @Override
                public void deleteDraft(UUID draftId) {
                    builder.build().delete()
                            .uri(url() + "/drafts/{id}", draftId)
                            .retrieve()
                            .toBodilessEntity();
                }
            };
        }
    }

    @LocalServerPort
    private int port;

    @Autowired
    private RestClient.Builder restClientBuilder;

    @Test
    void fullDraftLifecycleFromPublicApiThroughMockAccessDataStore() {
        RestClient restClient = restClientBuilder.build();
        UUID applicationId = UUID.randomUUID();

        PriorAuthorityDraft draft = PriorAuthorityDraft.builder()
                .applicationId(applicationId)
                .type(PriorAuthorityType.EXPERT)
                .expertType("Child psychologist")
                .expertFullName("Dr Joe Bloggs")
                .isInLondon(true)
                .billingType(BillingType.HOURLY)
                .hourlyRate(new BigDecimal("45.00"))
                .totalAmount(new BigDecimal("135.00"))
                .build();

        ResponseEntity<DraftIdResponse> create = restClient.post()
                .uri("http://localhost:" + port + "/prior-authority/drafts")
                .contentType(MediaType.APPLICATION_JSON)
                .body(draft)
                .retrieve()
                .toEntity(DraftIdResponse.class);

        assertEquals(HttpStatus.CREATED, create.getStatusCode());
        assertNotNull(create.getBody());
        UUID draftId = create.getBody().draftId();
        assertNotNull(draftId);

        PriorAuthorityDraft updated = PriorAuthorityDraft.builder()
                .applicationId(applicationId)
                .totalAmount(new BigDecimal("180.00"))
                .build();

        ResponseEntity<Void> update = restClient.put()
                .uri("http://localhost:" + port + "/prior-authority/drafts/{id}", draftId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(updated)
                .retrieve()
                .toBodilessEntity();
        assertEquals(HttpStatus.OK, update.getStatusCode());

        ResponseEntity<List<PriorAuthorityDraftSummary>> list = restClient.get()
                .uri("http://localhost:" + port + "/prior-authority/drafts?applicationId={a}", applicationId)
                .retrieve()
                .toEntity(TYPED_LIST);

        assertEquals(HttpStatus.OK, list.getStatusCode());
        assertNotNull(list.getBody());
        assertEquals(1, list.getBody().size());
        PriorAuthorityDraftSummary summary = list.getBody().getFirst();
        assertEquals(draftId, summary.draftId());
        assertEquals(applicationId, summary.draft().applicationId());
        assertEquals(0, new BigDecimal("180.00").compareTo(summary.draft().totalAmount()));

        ResponseEntity<Void> deleteResp = restClient.delete()
                .uri("http://localhost:" + port + "/prior-authority/drafts/{id}", draftId)
                .retrieve()
                .toBodilessEntity();
        assertEquals(HttpStatus.NO_CONTENT, deleteResp.getStatusCode());

        List<PriorAuthorityDraftSummary> afterDelete = restClient.get()
                .uri("http://localhost:" + port + "/prior-authority/drafts")
                .retrieve()
                .body(TYPED_LIST);
        assertNotNull(afterDelete);
        assertTrue(afterDelete.stream().noneMatch(s -> s.draftId().equals(draftId)));
    }

    @Test
    void partiallyCompletedDraftIsAcceptedEvenThoughItWouldFailSubmitValidation() {
        RestClient restClient = restClientBuilder.build();

        PriorAuthorityDraft incomplete = PriorAuthorityDraft.builder()
                .applicationId(UUID.randomUUID())
                .billingType(BillingType.HOURLY) // hourly fields missing
                .build();

        ResponseEntity<DraftIdResponse> create = restClient.post()
                .uri("http://localhost:" + port + "/prior-authority/drafts")
                .contentType(MediaType.APPLICATION_JSON)
                .body(incomplete)
                .retrieve()
                .toEntity(DraftIdResponse.class);

        assertEquals(HttpStatus.CREATED, create.getStatusCode());
    }

    @Test
    void updatingNonExistentDraftReturns404FromAccessDataStoreAsA404() {
        RestClient restClient = restClientBuilder.build();
        UUID unknownDraftId = UUID.randomUUID();

        PriorAuthorityDraft update = PriorAuthorityDraft.builder()
                .applicationId(UUID.randomUUID())
                .totalAmount(new BigDecimal("180.00"))
                .build();

        HttpStatusCode status = restClient.put()
                .uri("http://localhost:" + port + "/prior-authority/drafts/{id}", unknownDraftId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(update)
                .retrieve()
                .onStatus(s -> true, (req, res) -> { /* swallow so we can inspect status */ })
                .toBodilessEntity()
                .getStatusCode();

        assertEquals(HttpStatus.NOT_FOUND, status);
    }
}
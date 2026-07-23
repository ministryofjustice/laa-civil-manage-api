package uk.gov.justice.laa_civil_manage_api.services.accessdatastore;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import uk.gov.justice.laa_civil_manage_api.models.*;

class HttpAccessDataStoreClientTest {

  private static final String BASE_URL = "http://ads.test";

  private MockRestServiceServer server;
  private HttpAccessDataStoreClient client;

  @BeforeEach
  void setup() {
    RestClient.Builder builder = RestClient.builder();
    server = MockRestServiceServer.bindTo(builder).build();
    AccessDataStoreProperties properties =
        new AccessDataStoreProperties(
            BASE_URL, Map.of(), Duration.ofSeconds(3), Duration.ofSeconds(5));
    client = new HttpAccessDataStoreClient(builder.build(), properties);
  }

  @Test
  void submitPriorAuthorityPostsToAdsWithApplicationIdInPathAndStrippedBody() {
    UUID applicationId = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
    UUID submissionId = UUID.fromString("11111111-2222-3333-4444-555555555555");

    server
        .expect(requestTo(BASE_URL + "/applications/" + applicationId + "/prior-authority"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.applicationId").doesNotExist())
        .andExpect(jsonPath("$.priorAuthorityType").value("EXPERT"))
        .andExpect(jsonPath("$.expertFullName").value("John Doe"))
        .andExpect(jsonPath("$.expertBasedInLondon").value(true))
        .andExpect(jsonPath("$.totalAmount").value(249.99))
        .andExpect(jsonPath("$.justification").value("Required expert evidence."))
        .andRespond(
            withSuccess(
                """
                    {
                      "submissionId": "%s",
                      "status": "ACCEPTED",
                      "submittedAt": "2026-05-22T10:00:00Z"
                    }
                    """
                    .formatted(submissionId),
                MediaType.APPLICATION_JSON));

    PriorAuthority pa =
        PriorAuthority.builder()
            .applicationId(applicationId)
            .priorAuthorityType(PriorAuthorityType.EXPERT)
            .expertType("Psychologist")
            .expertFullName("John Doe")
            .expertBasedInLondon(true)
            .billingType(BillingType.FIXED_RATE)
            .totalAmount(new BigDecimal("249.99"))
            .justification("Required expert evidence.")
            .build();

    PriorAuthorityApplicationResponse response = client.submitPriorAuthority(pa);

    assertEquals(submissionId, response.submissionId());
    assertEquals(SubmissionStatus.ACCEPTED, response.status());
    server.verify();
  }

  @Test
  void usesPerOperationUrlWhenConfigured() {
    UUID applicationId = UUID.randomUUID();
    String operationUrl = "http://ads.per-op.test";

    RestClient.Builder builder = RestClient.builder();
    server = MockRestServiceServer.bindTo(builder).build();
    AccessDataStoreProperties properties =
        new AccessDataStoreProperties(
            BASE_URL,
            Map.of(AccessDataStoreOperations.SUBMIT_PRIOR_AUTHORITY, operationUrl),
            Duration.ofSeconds(3),
            Duration.ofSeconds(5));

    client = new HttpAccessDataStoreClient(builder.build(), properties);

    server
        .expect(requestTo(operationUrl + "/applications/" + applicationId + "/prior-authority"))
        .andExpect(method(HttpMethod.POST))
        .andRespond(
            withSuccess(
                """
                    {
                      "submissionId": "11111111-1111-1111-1111-111111111111",
                      "status": "ACCEPTED",
                      "submittedAt": "2026-05-22T10:00:00Z"
                    }
                    """,
                MediaType.APPLICATION_JSON));

    client.submitPriorAuthority(
        PriorAuthority.builder()
            .applicationId(applicationId)
            .priorAuthorityType(PriorAuthorityType.EXPERT)
            .expertType("Psychologist")
            .expertFullName("John Doe")
            .expertBasedInLondon(false)
            .billingType(BillingType.FIXED_RATE)
            .totalAmount(new BigDecimal("249.99"))
            .justification("Required expert evidence.")
            .build());

    server.verify();
  }

  @Test
  void createDraftPostsToAdsAndReturnsDraftId() {
    UUID draftId = UUID.randomUUID();
    UUID applicationId = UUID.randomUUID();

    server
        .expect(requestTo(BASE_URL + "/drafts"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.sourceSystem").value("laa-civil-manage"))
        .andExpect(jsonPath("$.draftType").value("PRIOR_AUTHORITY"))
        .andExpect(jsonPath("$.draftBody.totalAmount").value(135.00))
        .andRespond(
            withSuccess("{ \"draftId\": \"" + draftId + "\" }", MediaType.APPLICATION_JSON));

    Draft draft =
        Draft.builder()
            .sourceSystem("laa-civil-manage")
            .draftType("PRIOR_AUTHORITY")
            .applicationId(applicationId)
            .userId("entra-id")
            .draftBody(Map.of("totalAmount", 135.00))
            .build();

    DraftCreatedResponse response = client.createDraft(draft);
    assertEquals(draftId, response.draftId());
    server.verify();
  }

  @Test
  void updateDraftPutsToAdsWithDraftIdInPath() {
    UUID draftId = UUID.randomUUID();
    UUID applicationId = UUID.randomUUID();

    server
        .expect(requestTo(BASE_URL + "/drafts/" + draftId))
        .andExpect(method(HttpMethod.PUT))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.draftBody.totalAmount").value(180.00))
        .andRespond(withSuccess());

    Draft draft =
        Draft.builder()
            .sourceSystem("laa-civil-manage")
            .draftType("PRIOR_AUTHORITY")
            .applicationId(applicationId)
            .userId("entra-id")
            .draftBody(Map.of("totalAmount", 180.00))
            .build();

    client.updateDraft(draftId, draft);
    server.verify();
  }

  @Test
  void getDraftsBuildsQueryStringWithRequiredAndOptionalParams() {
    UUID applicationId = UUID.fromString("11111111-1111-1111-1111-111111111111");

    server
        .expect(requestTo(org.hamcrest.Matchers.startsWith(BASE_URL + "/drafts")))
        .andExpect(method(HttpMethod.GET))
        .andExpect(queryParam("sourceSystem", "laa-civil-manage"))
        .andExpect(queryParam("userId", "entra-id"))
        .andExpect(queryParam("draftType", "PRIOR_AUTHORITY"))
        .andExpect(queryParam("applicationId", applicationId.toString()))
        .andRespond(
            withSuccess(
                """
                    [
                      {
                        "draftId": "c3b07e24-d92b-410a-9d95-88f117a12b43",
                        "draftType": "PRIOR_AUTHORITY",
                        "timestamp": "2026-05-19T12:00:00Z",
                        "draftBody": { "totalAmount": 135.00 }
                      }
                    ]
                    """,
                MediaType.APPLICATION_JSON));

    List<DraftSummary> drafts =
        client.getDrafts("laa-civil-manage", "entra-id", "PRIOR_AUTHORITY", applicationId);
    assertEquals(1, drafts.size());
    assertEquals("PRIOR_AUTHORITY", drafts.get(0).draftType());
    server.verify();
  }

  @Test
  void getDraftsOmitsOptionalParamsWhenNull() {
    server
        .expect(requestTo(org.hamcrest.Matchers.containsString("sourceSystem=laa-civil-manage")))
        .andExpect(method(HttpMethod.GET))
        .andExpect(queryParam("sourceSystem", "laa-civil-manage"))
        .andExpect(queryParam("userId", "entra-id"))
        .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

    List<DraftSummary> drafts = client.getDrafts("laa-civil-manage", "entra-id", null, null);
    assertEquals(0, drafts.size());
    server.verify();
  }

  @Test
  void deleteDraftDeletesAtAdsWithDraftIdInPath() {
    UUID draftId = UUID.randomUUID();

    server
        .expect(requestTo(BASE_URL + "/drafts/" + draftId))
        .andExpect(method(HttpMethod.DELETE))
        .andRespond(withNoContent());

    client.deleteDraft(draftId);
    server.verify();
  }

  @Test
  void getApplicationsGetsFromAdsWithServiceNameHeader() {
    server
        .expect(requestTo(BASE_URL + "/api/v0/applications?page=1&pageSize=10"))
        .andExpect(method(HttpMethod.GET))
        .andExpect(header("X-Service-Name", "CIVIL_APPLY"))
        .andRespond(
            withSuccess(
                """
                    {
                      "paging": {
                        "page": 1,
                        "pageSize": 10,
                        "itemsReturned": 1,
                        "totalRecords": 1
                      },
                      "applications": [
                        {
                          "applicationId": "11111111-2222-3333-4444-555555555555",
                          "laaReference": "APP-1",
                          "status": "APPLICATION_SUBMITTED",
                          "submittedAt": "2026-07-22T10:00:00Z",
                          "clientFirstName": "John",
                          "clientLastName": "Doe"
                        }
                      ]
                    }
                    """,
                MediaType.APPLICATION_JSON));

    ApplicationSummaryResponse result = client.getApplications(1, 10);

    assertNotNull(result);
    assertEquals(1, result.applications().size());
    assertEquals("APP-1", result.applications().get(0).laaReference());
    server.verify();
  }
}

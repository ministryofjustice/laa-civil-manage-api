package uk.gov.justice.laa_civil_manage_api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
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
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import uk.gov.justice.laa_civil_manage_api.mockaccessdatastore.models.PriorAuthoritySubmission;
import uk.gov.justice.laa_civil_manage_api.models.BillingType;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthority;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthorityType;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthorityApplicationResponse;
import uk.gov.justice.laa_civil_manage_api.models.SubmissionStatus;
import uk.gov.justice.laa_civil_manage_api.services.accessdatastore.AccessDataStoreClient;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class PriorAuthorityIntegrationTest {

    @TestConfiguration
    static class Config {

        @Bean
        @Primary
        AccessDataStoreClient testAccessDataStoreClient(
                ServletWebServerApplicationContext context,
                RestClient.Builder builder
        ) {
            return priorAuthority -> {
                int port = context.getWebServer().getPort();
                String url = "http://localhost:" + port + "/mock-access-data-store";

                return builder.build().post()
                        .uri(url + "/applications/{applicationId}/prior-authorities", priorAuthority.applicationId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(PriorAuthoritySubmission.from(priorAuthority))
                        .retrieve()
                        .body(PriorAuthorityApplicationResponse.class);
            };
        }
    }

    @LocalServerPort
    private int port;

    @Autowired
    private RestClient.Builder restClientBuilder;

    @Test
    void postingAPriorAuthorityRequestFlowsThroughToTheMockAccessDataStore() {
        RestClient restClient = restClientBuilder.build();

        PriorAuthority body = PriorAuthority.builder()
                .applicationId(UUID.randomUUID())
                .type(PriorAuthorityType.EXPERT)
                .expertType("Psychologist")
                .expertFullName("John Doe")
                .guidelineRatesExceeded(false)
                .billingType(BillingType.FLAT_RATE)
                .flatRateTotalAmount(new BigDecimal("249.99"))
                .build();

        ResponseEntity<PriorAuthorityApplicationResponse> response = restClient.post()
                .uri("http://localhost:" + port + "/prior-authority-requests")
                .body(body)
                .retrieve()
                .toEntity(PriorAuthorityApplicationResponse.class);

        assertEquals(HttpStatusCode.valueOf(201), response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().submissionId());
        assertEquals(SubmissionStatus.ACCEPTED, response.getBody().status());
    }
}
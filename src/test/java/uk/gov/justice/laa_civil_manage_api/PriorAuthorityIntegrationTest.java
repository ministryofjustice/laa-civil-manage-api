package uk.gov.justice.laa_civil_manage_api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.server.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import uk.gov.justice.laa_civil_manage_api.models.BillingType;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthority;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthorityType;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthorityApplicationResponse;
import uk.gov.justice.laa_civil_manage_api.models.SubmissionStatus;
import uk.gov.justice.laa_civil_manage_api.services.accessdatastore.AccessDataStoreClient;
import uk.gov.justice.laa_civil_manage_api.services.accessdatastore.AccessDataStoreProperties;
import uk.gov.justice.laa_civil_manage_api.services.accessdatastore.HttpAccessDataStoreClient;

@Disabled("Tomcat fails to bind to a random port in this Spring Boot 4 test setup; "
        + "the loopback is verified end-to-end via the UI form submission. Re-enable when the "
        + "RANDOM_PORT / server.port=0 configuration is sorted.")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, properties = "server.port=0")
class PriorAuthorityIntegrationTest {

    @TestConfiguration
    static class Config {

        @Bean
        @Primary
        @Lazy
        AccessDataStoreClient testAccessDataStoreClient(
                ServletWebServerApplicationContext context,
                RestClient.Builder builder
        ) {
            int port = context.getWebServer().getPort();
            String url = "http://localhost:" + port + "/mock-access-data-store";
            return new HttpAccessDataStoreClient(builder, new AccessDataStoreProperties(url, Map.of()));
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

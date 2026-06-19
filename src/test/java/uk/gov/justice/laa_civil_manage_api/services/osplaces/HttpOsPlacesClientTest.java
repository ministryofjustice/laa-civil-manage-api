package uk.gov.justice.laa_civil_manage_api.services.osplaces;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import uk.gov.justice.laa_civil_manage_api.client.OsPlacesClient;
import uk.gov.justice.laa_civil_manage_api.config.OsPlacesConfig;

class HttpOsPlacesClientTest {

  private static final String BASE_URL = "https://api.os.uk/search/places/v1";

  private MockRestServiceServer server;
  private OsPlacesClient client;

  @BeforeEach
  void setup() {
    RestClient.Builder builder = RestClient.builder();
    server = MockRestServiceServer.bindTo(builder).build();
    OsPlacesConfig properties =
        new OsPlacesConfig(BASE_URL, "test-key", List.of(5990, 5010));
    client = new OsPlacesClient(builder, properties);
  }

  @Test
  void returnsCustodianCodeFromFirstResult() {
    server
        .expect(requestTo(BASE_URL + "/postcode?postcode=SW1A+1AA&key=test-key"))
        .andExpect(method(HttpMethod.GET))
        .andExpect(queryParam("postcode", "SW1A 1AA"))
        .andExpect(queryParam("key", "test-key"))
        .andRespond(
            withSuccess(
                """
                {
                  "results": [
                    {
                      "DPA": {
                        "LOCAL_CUSTODIAN_CODE": 5990
                      }
                    }
                  ]
                }
                """,
                MediaType.APPLICATION_JSON));

    Optional<Integer> code = client.lookupCustodianCodeForPostcode("SW1A 1AA");

    assertTrue(code.isPresent());
    assertEquals(5990, code.get());
    server.verify();
  }

  @Test
  void returnsEmptyWhenNoResults() {
    server
        .expect(requestTo(BASE_URL + "/postcode?postcode=ZZ99+9ZZ&key=test-key"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess("{\"results\":[]}", MediaType.APPLICATION_JSON));

    Optional<Integer> code = client.lookupCustodianCodeForPostcode("ZZ99 9ZZ");

    assertTrue(code.isEmpty());
    server.verify();
  }
}

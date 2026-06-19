package uk.gov.justice.laa_civil_manage_api.client;

import com.fasterxml.jackson.annotation.JsonProperty;

import uk.gov.justice.laa_civil_manage_api.config.OsPlacesConfig;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class OsPlacesClient {

  private final RestClient restClient;
  private final OsPlacesConfig osPlacesConfig;

  public OsPlacesClient(RestClient.Builder builder, OsPlacesConfig osPlacesConfig) {
    this.restClient = builder.build();
    this.osPlacesConfig = osPlacesConfig;
  }

  public Optional<Integer> lookupCustodianCodeForPostcode(String postcode) {
    OsPlacesPostcodeResponse response =
        restClient
            .get()
            .uri(
                UriComponentsBuilder.fromUriString(osPlacesConfig.baseUrl())
                    .path("/postcode")
                    .queryParam("postcode", postcode)
                    .queryParam("key", osPlacesConfig.apiKey())
                    .build()
                    .toUri())
            .retrieve()
            .body(OsPlacesPostcodeResponse.class);

    if (response == null || response.results() == null || response.results().isEmpty()) {
      return Optional.empty();
    }

    return response.results().stream()
        .map(Result::dpa)
        .filter(dpa -> dpa != null && dpa.localCustodianCode() != null)
        .map(Dpa::localCustodianCode)
        .findFirst();
  }

  record OsPlacesPostcodeResponse(List<Result> results) {}

  record Result(@JsonProperty("DPA") Dpa dpa) {}

  record Dpa(@JsonProperty("LOCAL_CUSTODIAN_CODE") Integer localCustodianCode) {}
}

package uk.gov.justice.laa_civil_manage_api.services;

import lombok.RequiredArgsConstructor;
import uk.gov.justice.laa_civil_manage_api.client.OsPlacesClient;
import uk.gov.justice.laa_civil_manage_api.config.OsPlacesConfig;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostcodeLookupService {

  private final OsPlacesClient osPlacesClient;
  private final OsPlacesConfig osPlacesConfig;

  public boolean isInLondon(String postcode) {
    return osPlacesClient
        .lookupCustodianCodeForPostcode(postcode)
        .map(this::isLondonCustodianCode)
        .orElse(false);
  }

  private boolean isLondonCustodianCode(Integer custodianCode) {
    return custodianCode != null && osPlacesConfig.londonCustodianCodes().contains(custodianCode);
  }
}

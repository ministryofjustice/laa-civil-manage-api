package uk.gov.justice.laa_civil_manage_api.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.justice.laa_civil_manage_api.services.providerdetails.ProviderDetailsClient;

@Service
@RequiredArgsConstructor
public class ProviderDetailsService {

  private final ProviderDetailsClient providerDetailsClient;

  public String getProviderEmail(String officeCode) {
    return providerDetailsClient.getProviderEmail(officeCode);
  }
}

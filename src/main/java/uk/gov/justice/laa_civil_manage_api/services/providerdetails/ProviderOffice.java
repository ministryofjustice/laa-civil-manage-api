package uk.gov.justice.laa_civil_manage_api.services.providerdetails;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProviderOffice(String officeCode, String emailAddress) {}

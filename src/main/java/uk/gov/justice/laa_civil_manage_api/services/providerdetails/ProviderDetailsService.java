package uk.gov.justice.laa_civil_manage_api.services.providerdetails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Service
@Slf4j
public class ProviderDetailsService {

  private static final String PROVIDER_OFFICE_PATH = "/api/v1/provider-offices/{officeCode}";

  private final RestClient providerDetailsRestClient;

  public ProviderDetailsService(RestClient providerDetailsRestClient) {
    this.providerDetailsRestClient = providerDetailsRestClient;
  }

  /**
   * Looks up the email address configured for the given provider office code.
   *
   * @param officeCode the provider office code to look up
   * @return the office's email address, or {@code null} if the office code does not exist or has no
   *     email address on record
   * @throws ProviderApiException if the Provider Details API returns an unrecoverable error, or
   *     remains unavailable after every retry attempt has been exhausted
   */
  @Retryable(
      retryFor = {
        HttpServerErrorException.class,
        ResourceAccessException.class,
        HttpClientErrorException.Conflict.class
      },
      maxAttemptsExpression = "${laa-civil-manage-api.provider-details.max-retries:3}",
      backoff =
          @Backoff(
              delayExpression = "${laa-civil-manage-api.provider-details.initial-backoff-ms:200}",
              multiplierExpression =
                  "${laa-civil-manage-api.provider-details.backoff-multiplier:2.0}"))
  public String getProviderEmail(String officeCode) {
    ProviderFirmOffice response;
    try {
      response =
          providerDetailsRestClient
              .get()
              .uri(PROVIDER_OFFICE_PATH, officeCode)
              .accept(MediaType.APPLICATION_JSON)
              .retrieve()
              .body(ProviderFirmOffice.class);
    } catch (HttpClientErrorException ex) {
      if (ex.getStatusCode().equals(HttpStatus.CONFLICT)) {
        // Cache still loading upstream - rethrow natively so @Retryable can retry it.
        throw ex;
      }
      log.error(
          "Provider Details API returned non-recoverable status {} for office {}",
          ex.getStatusCode(),
          officeCode);
      throw new ProviderApiException(
          "Provider Details API request failed for office " + officeCode, ex);
    }

    if (response == null
        || response.office() == null
        || !StringUtils.hasText(response.office().emailAddress())) {
      log.warn("Provider Details API returned no email address for office {}", officeCode);
      return null;
    }

    return response.office().emailAddress();
  }

  @Recover
  public String recoverFromApiError(RestClientResponseException ex, String officeCode) {
    log.error(
        "Provider Details API returned status {} for office {}", ex.getStatusCode(), officeCode);
    throw new ProviderApiException(
        "Provider Details API request failed for office " + officeCode, ex);
  }

  @Recover
  public String recoverFromNetworkError(ResourceAccessException ex, String officeCode) {
    log.error("Provider Details API network failure for office {}", officeCode, ex);
    throw new ProviderApiException(
        "Provider Details API network failure for office " + officeCode, ex);
  }

  @Recover
  public String recover(RuntimeException ex, String officeCode) {
    throw ex;
  }
}

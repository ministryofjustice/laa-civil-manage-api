package uk.gov.justice.laa_civil_manage_api.services.providerdetails;

import jakarta.validation.constraints.NotBlank;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "laa-civil-manage-api.provider-details")
@Validated
public record ProviderDetailsProperties(
    @NotBlank String baseUrl,
    @NotBlank String apiKey,
    Duration connectTimeout,
    Duration readTimeout,
    int maxRetries,
    long initialBackoffMs,
    double backoffMultiplier) {

  private static final int DEFAULT_MAX_RETRIES = 3;
  private static final long DEFAULT_INITIAL_BACKOFF_MS = 200;
  private static final double DEFAULT_BACKOFF_MULTIPLIER = 2.0;

  public ProviderDetailsProperties {
    if (maxRetries <= 0) {
      maxRetries = DEFAULT_MAX_RETRIES;
    }
    if (initialBackoffMs <= 0) {
      initialBackoffMs = DEFAULT_INITIAL_BACKOFF_MS;
    }
    if (backoffMultiplier <= 0) {
      backoffMultiplier = DEFAULT_BACKOFF_MULTIPLIER;
    }
  }
}

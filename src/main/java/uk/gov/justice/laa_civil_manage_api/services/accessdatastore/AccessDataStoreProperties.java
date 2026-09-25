package uk.gov.justice.laa_civil_manage_api.services.accessdatastore;

import jakarta.validation.constraints.NotBlank;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "laa-civil-manage-api.access-data-store")
@Validated
public record AccessDataStoreProperties(
    @NotBlank String baseUrl, Duration connectTimeout, Duration readTimeout, String serviceName) {}

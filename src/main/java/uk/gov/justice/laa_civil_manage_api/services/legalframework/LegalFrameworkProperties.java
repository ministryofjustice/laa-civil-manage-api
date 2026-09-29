package uk.gov.justice.laa_civil_manage_api.services.legalframework;

import jakarta.validation.constraints.NotBlank;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "laa-civil-manage-api.legal-framework")
@Validated
public record LegalFrameworkProperties(
    @NotBlank String baseUrl, Duration connectTimeout, Duration readTimeout) {}

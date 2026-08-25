package uk.gov.justice.laa_civil_manage_api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;

@Validated
@ConfigurationProperties(prefix = "laa-civil-manage-api.notify")
public record NotifyEmailProperties(
    @NotBlank String apiKey, @NotBlank String priorAuthoritySubmittedTemplateId, @NotBlank String recipientEmail, boolean enabled) {

}

package uk.gov.justice.laa_civil_manage_api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "laa-civil-manage-api.notify")
public record NotifyEmailProperties(
    String apiKey,
    String priorAuthoritySubmittedTemplateId,
    String recipientEmail,
    boolean enabled) {}

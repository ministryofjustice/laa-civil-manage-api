package uk.gov.justice.laa_civil_manage_api.services;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "laa-civil-manage-api.notify")
public record NotifyEmailProperties(
    String apiKey, String priorAuthoritySubmittedTemplateId, String recipientEmail) {

  public boolean isConfigured() {
    return hasText(apiKey) && hasText(priorAuthoritySubmittedTemplateId) && hasText(recipientEmail);
  }

  private boolean hasText(String value) {
    return value != null && !value.isBlank();
  }
}


package uk.gov.justice.laa_civil_manage_api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "data-access-api")
public record DataAccessApiProperties(String baseUrl) {}

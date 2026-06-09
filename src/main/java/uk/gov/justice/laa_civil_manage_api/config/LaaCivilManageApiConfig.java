package uk.gov.justice.laa_civil_manage_api.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "laa-civil-manage-api")
public record LaaCivilManageApiConfig(List<String> expertTypes) {}

package uk.gov.justice.laa_civil_manage_api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@ConfigurationProperties(prefix = "laa-civil-manage-api")
@Data
public class LaaCivilManageApiConfig {
    private String dataStoreUrl;
}
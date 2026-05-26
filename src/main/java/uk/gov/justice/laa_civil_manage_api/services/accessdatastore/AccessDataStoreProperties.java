package uk.gov.justice.laa_civil_manage_api.services.accessdatastore;

import java.time.Duration;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "laa-civil-manage-api.access-data-store")
public record AccessDataStoreProperties(
        String defaultBaseUrl,
        Map<String, String> operationBaseUrls,
        Duration connectTimeout,
        Duration readTimeout
) {
    public AccessDataStoreProperties {
        operationBaseUrls = operationBaseUrls == null ? Map.of() : Map.copyOf(operationBaseUrls);
    }

    public String urlFor(String operation) {
        String url = operationBaseUrls.getOrDefault(operation, defaultBaseUrl);
        if (url == null || url.isBlank()) {
            throw new IllegalStateException("No Access Data Store URL configured for operation '" + operation + "'");
        }
        return url;
    }
}
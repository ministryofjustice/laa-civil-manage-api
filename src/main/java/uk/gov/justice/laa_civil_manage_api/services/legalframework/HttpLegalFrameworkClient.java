package uk.gov.justice.laa_civil_manage_api.services.legalframework;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.retry.RetryContext;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.support.RetrySynchronizationManager;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@Component
@Slf4j
public class HttpLegalFrameworkClient implements LegalFrameworkClient {

  private static final ParameterizedTypeReference<List<ExpertType>> EXPERT_TYPE_LIST_TYPE =
      new ParameterizedTypeReference<>() {};

  private final RestClient restClient;
  private final LegalFrameworkProperties properties;

  public HttpLegalFrameworkClient(
      RestClient legalFrameworkRestClient, LegalFrameworkProperties properties) {
    this.restClient = legalFrameworkRestClient;
    this.properties = properties;
  }

  @Override
  @Retryable(
      retryFor = {HttpServerErrorException.class, ResourceAccessException.class},
      maxAttemptsExpression = "${laa-civil-manage-api.legal-framework.max-retries:3}",
      backoff =
          @Backoff(
              delayExpression = "${laa-civil-manage-api.legal-framework.initial-backoff-ms:200}",
              multiplierExpression =
                  "${laa-civil-manage-api.legal-framework.backoff-multiplier:2.0}"))
  public List<ExpertType> getExpertTypes(String matterType) {
    List<ExpertType> expertTypes =
        restClient
            .get()
            .uri(properties.baseUrl() + "/expert_types/{matterType}", matterType)
            .retrieve()
            .body(EXPERT_TYPE_LIST_TYPE);
    return expertTypes == null ? List.of() : expertTypes;
  }

  @Override
  @Retryable(
      retryFor = {HttpServerErrorException.class, ResourceAccessException.class},
      maxAttemptsExpression = "${laa-civil-manage-api.legal-framework.max-retries:3}",
      backoff =
          @Backoff(
              delayExpression = "${laa-civil-manage-api.legal-framework.initial-backoff-ms:200}",
              multiplierExpression =
                  "${laa-civil-manage-api.legal-framework.backoff-multiplier:2.0}"))
  public LegalFrameworkStatus getStatus() {
    return restClient
        .get()
        .uri(properties.baseUrl() + "/status")
        .retrieve()
        .body(LegalFrameworkStatus.class);
  }

  @Recover
  public List<ExpertType> recoverGetExpertTypes(RuntimeException e, String matterType) {
    throw logIfRetriesExhausted("getExpertTypes", e);
  }

  @Recover
  public LegalFrameworkStatus recoverGetStatus(RuntimeException e) {
    throw logIfRetriesExhausted("getStatus", e);
  }

  private static RuntimeException logIfRetriesExhausted(String operation, RuntimeException e) {
    if (e instanceof HttpServerErrorException || e instanceof ResourceAccessException) {
      RetryContext context = RetrySynchronizationManager.getContext();
      int attempts = context == null ? 1 : context.getRetryCount();
      log.error(
          "Legal Framework API call {} failed after {} attempts: {}",
          operation,
          attempts,
          e.toString());
    }
    return e;
  }
}

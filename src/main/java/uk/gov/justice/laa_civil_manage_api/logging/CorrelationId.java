package uk.gov.justice.laa_civil_manage_api.logging;

public final class CorrelationId {

  public static final String HEADER = "X-Correlation-ID";

  public static final String MDC_KEY = "correlationId";

  public static final int MAX_LENGTH = 50;

  private CorrelationId() {}
}

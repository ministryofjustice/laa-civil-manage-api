package uk.gov.justice.laa_civil_manage_api.services.accessdatastore.pact;

public abstract class AbstractPactTest {

  public static final String CONSUMER = "laa-civil-manage-api";
  public static final String PROVIDER = "laa-data-access-api";
  protected static final String SERVICE_NAME = "CIVIL_MANAGE";
  protected static final String UUID_REGEX =
      "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}";
  protected static final String ISO_DATETIME_REGEX =
      "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?(Z|[+-]\\d{2}:\\d{2})";
}

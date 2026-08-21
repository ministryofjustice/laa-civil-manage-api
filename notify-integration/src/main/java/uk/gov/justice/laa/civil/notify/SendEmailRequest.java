package uk.gov.justice.laa.civil.notify;

import java.util.Map;

/**
 * Parameters for sending an email via GOV.UK Notify.
 *
 * @param templateId the Notify template ID
 * @param emailAddress recipient email address
 * @param personalisation key/value pairs merged into the template
 */
public record SendEmailRequest(
    String templateId, String emailAddress, Map<String, Object> personalisation) {}

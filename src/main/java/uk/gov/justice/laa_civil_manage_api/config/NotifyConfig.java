package uk.gov.justice.laa_civil_manage_api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import uk.gov.justice.laa.civil.notify.config.NotifySenderConfiguration;

@Configuration
@Import(NotifySenderConfiguration.class)
public class NotifyConfig {}

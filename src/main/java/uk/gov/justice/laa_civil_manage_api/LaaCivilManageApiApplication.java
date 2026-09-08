package uk.gov.justice.laa_civil_manage_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;

@ConfigurationPropertiesScan
@SpringBootApplication
@EnableAsync
@EnableRetry
public class LaaCivilManageApiApplication {

  public static void main(String[] args) {
    SpringApplication.run(LaaCivilManageApiApplication.class, args);
  }
}

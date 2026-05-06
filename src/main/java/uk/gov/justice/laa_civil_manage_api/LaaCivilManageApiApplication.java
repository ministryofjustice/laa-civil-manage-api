package uk.gov.justice.laa_civil_manage_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties
@SpringBootApplication
public class LaaCivilManageApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(LaaCivilManageApiApplication.class, args);
	}
}


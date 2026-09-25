package uk.gov.justice.laa_civil_manage_api.config;

import java.util.Map;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;

@Configuration
public class AsyncConfig {

  // Applied to the @Async executor so correlation IDs survive the thread hop.
  @Bean
  public TaskDecorator mdcTaskDecorator() {
    return runnable -> {
      Map<String, String> submitterContext = MDC.getCopyOfContextMap();
      return () -> {
        Map<String, String> workerContext = MDC.getCopyOfContextMap();
        setContext(submitterContext);
        try {
          runnable.run();
        } finally {
          setContext(workerContext);
        }
      };
    };
  }

  private static void setContext(Map<String, String> context) {
    if (context == null) {
      MDC.clear();
    } else {
      MDC.setContextMap(context);
    }
  }
}

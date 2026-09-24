package uk.gov.justice.laa_civil_manage_api.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.TaskDecorator;
import uk.gov.justice.laa_civil_manage_api.logging.CorrelationId;

class AsyncConfigTest {

  private final TaskDecorator decorator = new AsyncConfig().mdcTaskDecorator();
  private Map<String, String> captured;

  @AfterEach
  void clearMdc() {
    MDC.clear();
  }

  @Test
  void copiesSubmittingThreadMdcIntoTheTask() {
    MDC.put(CorrelationId.MDC_KEY, "corr-123");
    Runnable decorated = decorator.decorate(() -> capture());

    MDC.clear();
    decorated.run();

    assertThat(captured).containsEntry(CorrelationId.MDC_KEY, "corr-123");
  }

  @Test
  void restoresTheWorkerThreadMdcAfterTheTaskRuns() {
    MDC.put(CorrelationId.MDC_KEY, "corr-123");
    Runnable decorated = decorator.decorate(() -> {});

    MDC.setContextMap(Map.of("worker", "state"));
    decorated.run();

    assertThat(MDC.getCopyOfContextMap()).isEqualTo(Map.of("worker", "state"));
  }

  @Test
  void clearsStaleWorkerMdcWhenSubmitterHadNone() {
    Runnable decorated = decorator.decorate(() -> capture());

    MDC.put(CorrelationId.MDC_KEY, "stale");
    decorated.run();

    assertThat(captured).isEmpty();
    assertThat(MDC.get(CorrelationId.MDC_KEY)).isEqualTo("stale");
  }

  @Test
  void springBootAppliesTheDecoratorToTheApplicationTaskExecutor() {
    new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(TaskExecutionAutoConfiguration.class))
        .withUserConfiguration(AsyncConfig.class)
        .run(
            context -> {
              AsyncTaskExecutor executor =
                  context.getBean("applicationTaskExecutor", AsyncTaskExecutor.class);
              MDC.put(CorrelationId.MDC_KEY, "corr-456");

              String propagated =
                  CompletableFuture.supplyAsync(() -> MDC.get(CorrelationId.MDC_KEY), executor)
                      .get(5, TimeUnit.SECONDS);

              assertThat(propagated).isEqualTo("corr-456");
            });
  }

  private void capture() {
    Map<String, String> context = MDC.getCopyOfContextMap();
    captured = context == null ? new HashMap<>() : context;
  }
}

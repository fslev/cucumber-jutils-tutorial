package com.cucumber.tutorial.context.services;

import com.cucumber.tutorial.context.BaseScenario;
import com.cucumber.tutorial.util.StringUtils;
import io.jtest.utils.matcher.ObjectMatcher;
import io.jtest.utils.matcher.condition.MatchCondition;
import me.tongfei.progressbar.DelegatingProgressBarConsumer;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;

import java.time.Duration;
import java.util.function.Supplier;

public class BaseService extends BaseScenario {

    protected <T> T executeAndMatch(String expected, Supplier<T> supplier, Integer pollingTimeoutSeconds,
                                    Long pollingIntervalInMillis, Double exponentialBackoff, MatchCondition... matchConditions) {
        var wrapper = new Object() {
            T result;
        };
        try {
            if (pollingTimeoutSeconds == null || pollingTimeoutSeconds == 0) {
                wrapper.result = supplier.get();
                scenarioVars.putAll(ObjectMatcher.match(null, expected, wrapper.result, matchConditions));
            } else {
                try (ProgressBar pb = new ProgressBarBuilder().setTaskName("Polling |" + pollingIntervalInMillis + "ms / " + pollingTimeoutSeconds + "s| backoff " + exponentialBackoff + " |")
                        .setInitialMax(Math.round((double) pollingTimeoutSeconds * 1000 / (pollingIntervalInMillis != null ? pollingIntervalInMillis : 3000)))
                        .setConsumer(new DelegatingProgressBarConsumer(c -> {
                            if (System.getProperty("hidePollingProgress") == null) {
                                System.err.println("\r" + c + StringUtils.toOnelineReducedString(wrapper.result, 80));
                            }
                        })).build()) {
                    scenarioVars.putAll(ObjectMatcher.match(null, expected, () -> {
                        wrapper.result = supplier.get();
                        pb.stepTo(Math.round((float) pb.getElapsedAfterStart().toMillis() / (pollingIntervalInMillis != null ? pollingIntervalInMillis : 3000)));
                        return wrapper.result;
                    }, Duration.ofSeconds(pollingTimeoutSeconds), pollingIntervalInMillis, exponentialBackoff, matchConditions));
                }
            }
            return wrapper.result;
        } finally {
            logActual(wrapper.result);
        }
    }

    private void logActual(Object actual) {
        scenarioUtils.log("\n----------------------- ACTUAL -----------------------\n{}", actual);
    }
}

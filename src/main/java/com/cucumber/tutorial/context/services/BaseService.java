package com.cucumber.tutorial.context.services;

import com.cucumber.tutorial.context.BaseScenario;
import com.cucumber.tutorial.util.StringUtils;
import io.jtest.utils.matcher.ObjectMatcher;
import io.jtest.utils.matcher.condition.MatchCondition;
import me.tongfei.progressbar.DelegatingProgressBarConsumer;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import org.awaitility.core.ConditionTimeoutException;
import org.awaitility.pollinterval.FixedPollInterval;
import org.awaitility.pollinterval.PollInterval;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.awaitility.Awaitility.await;

public class BaseService extends BaseScenario {

    protected <T> T executeAndMatch(String expected, Supplier<T> supplier, Integer pollingTimeoutSeconds,
                                    PollInterval pollInterval, MatchCondition... matchConditions) {
        var wrapper = new Object() {
            T result;
        };
        try {
            if (pollingTimeoutSeconds == null || pollingTimeoutSeconds == 0) {
                wrapper.result = supplier.get();
                scenarioVars.putAll(ObjectMatcher.match(null, expected, wrapper.result, matchConditions));
            } else {
                try (ProgressBar pb = new ProgressBarBuilder().setTaskName("Polling")
                        .setInitialMax(pollingTimeoutSeconds)
                        .setConsumer(new DelegatingProgressBarConsumer(c -> {
                            if (System.getProperty("hidePollingProgress") == null) {
                                System.err.println("\r" + c + StringUtils.toOnelineReducedString(wrapper.result, 80));
                            }
                        })).build()) {
                    try {
                        await("Polling response").pollDelay(Duration.ofSeconds(-1)).pollInSameThread()
                                .pollInterval(pollInterval != null ? pollInterval : FixedPollInterval.fixed(Duration.ofSeconds(3)))
                                .atMost(pollingTimeoutSeconds, TimeUnit.SECONDS)
                                .untilAsserted(() -> {
                                    wrapper.result = supplier.get();
                                    pb.stepTo(pb.getElapsedAfterStart().toSeconds());
                                    scenarioVars.putAll(ObjectMatcher.match(null, expected, wrapper.result, matchConditions));
                                });
                    } catch (ConditionTimeoutException e) {
                        pb.stepTo(pb.getElapsedAfterStart().toSeconds());
                        if (e.getCause() instanceof AssertionError) {
                            throw (AssertionError) e.getCause();
                        } else {
                            throw e;
                        }
                    }
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

package com.cucumber.tutorial.context.services;

import com.cucumber.tutorial.context.BaseScenario;
import io.jtest.utils.matcher.ObjectMatcher;
import io.jtest.utils.matcher.condition.MatchCondition;

import java.time.Duration;
import java.util.function.Supplier;

public class BaseService extends BaseScenario {

    protected <T> T executeAndMatch(Supplier<T> supplier, Integer pollingTimeoutSeconds,
                                    Long pollingIntervalInMillis, Double exponentialBackoff, String expected, MatchCondition... matchConditions) {
        var wrapper = new Object() {
            T result;
        };
        try {
            if (pollingTimeoutSeconds == null || pollingTimeoutSeconds == 0) {
                wrapper.result = supplier.get();
                scenarioVars.putAll(ObjectMatcher.match(null, expected, wrapper.result, matchConditions));
            } else {
                scenarioVars.putAll(ObjectMatcher.match(null, expected, () -> {
                    wrapper.result = supplier.get();
                    return wrapper.result;
                }, Duration.ofSeconds(pollingTimeoutSeconds), pollingIntervalInMillis, exponentialBackoff, matchConditions));
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

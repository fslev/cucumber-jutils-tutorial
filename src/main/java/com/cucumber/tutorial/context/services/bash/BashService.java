package com.cucumber.tutorial.context.services.bash;

import com.cucumber.tutorial.client.ShellClient;
import com.cucumber.tutorial.context.BaseScenario;
import com.cucumber.tutorial.util.DateUtils;
import io.cucumber.guice.ScenarioScoped;
import io.jtest.utils.matcher.ObjectMatcher;
import io.jtest.utils.matcher.condition.MatchCondition;

import java.time.Duration;

@ScenarioScoped
public class BashService extends BaseScenario {

    private final ProcessBuilder processBuilder = new ProcessBuilder();
    private final ShellClient shellClient = new ShellClient(processBuilder);

    public String executeAndMatch(String cmd, String expected, MatchCondition... matchConditions) {
        return executeAndMatch(cmd, null, null, expected, matchConditions);
    }

    public String executeAndMatch(String cmd, Integer pollingTimeoutSeconds, String expected, MatchCondition... matchConditions) {
        return executeAndMatch(cmd, pollingTimeoutSeconds, null, expected, matchConditions);
    }

    public String executeAndMatch(String cmd, Integer pollingTimeoutSeconds, Double exponentialBackoff, String expected, MatchCondition... matchConditions) {
        scenarioUtils.log("[{}] BASH CMD: -----------------\n\n{}\n\n---------------------- EXPECTED ----------------------\n{}\n",
                DateUtils.currentDateTime(), cmd, expected);
        var wrapper = new Object() {
            String output;
        };
        try {
            if (pollingTimeoutSeconds == null || pollingTimeoutSeconds == 0) {
                wrapper.output = shellClient.execute("bash", "-c", cmd);
                scenarioVars.putAll(ObjectMatcher.match(null, expected, wrapper.output, matchConditions));
            } else {
                scenarioVars.putAll(ObjectMatcher.match(null, expected, () -> {
                    wrapper.output = shellClient.execute("bash", "-c", cmd);
                    return wrapper.output;
                }, Duration.ofSeconds(pollingTimeoutSeconds), null, exponentialBackoff, matchConditions));
            }
            return wrapper.output;
        } finally {
            scenarioUtils.log("\n----------------------- ACTUAL -----------------------\n{}", wrapper.output);
        }
    }
}

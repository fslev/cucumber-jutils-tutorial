package com.cucumber.tutorial.context.services.bash;

import com.cucumber.tutorial.client.ShellClient;
import com.cucumber.tutorial.context.services.BaseService;
import com.cucumber.tutorial.util.DateUtils;
import io.cucumber.guice.ScenarioScoped;
import io.jtest.utils.matcher.condition.MatchCondition;
import org.awaitility.pollinterval.PollInterval;

@ScenarioScoped
public class BashService extends BaseService {

    private final ProcessBuilder processBuilder = new ProcessBuilder();
    private final ShellClient shellClient = new ShellClient(processBuilder);

    public String executeAndMatch(String cmd, String expected, MatchCondition... matchConditions) {
        return executeAndMatch(cmd, null, null, expected, matchConditions);
    }

    public String executeAndMatch(String cmd, Integer pollingTimeoutSeconds, String expected, MatchCondition... matchConditions) {
        return executeAndMatch(cmd, pollingTimeoutSeconds, null, expected, matchConditions);
    }

    public String executeAndMatch(String cmd, Integer pollingTimeoutSeconds, PollInterval pollInterval, String expected, MatchCondition... matchConditions) {
        scenarioUtils.log("[{}] BASH CMD: -----------------\n\n{}\n\n---------------------- EXPECTED ----------------------\n{}\n",
                DateUtils.currentDateTime(), cmd, expected);
        return executeAndMatch(expected, () -> shellClient.execute("bash", "-c", cmd),
                pollingTimeoutSeconds, pollInterval, matchConditions);
    }
}

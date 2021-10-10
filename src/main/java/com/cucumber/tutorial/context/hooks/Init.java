package com.cucumber.tutorial.context.hooks;

import com.cucumber.tutorial.config.Config;
import com.cucumber.tutorial.context.BaseScenario;
import io.cucumber.guice.ScenarioScoped;
import io.cucumber.java.Before;
import io.cucumber.java.PendingException;

import java.util.Map;

@ScenarioScoped
public class Init extends BaseScenario {

    @Before(order = 0)
    public void safetyCheck() {
        if (Config.isProdEnv() && !scenarioUtils.getScenario().getSourceTagNames().contains("@prod")) {
            throw new PendingException("Safety exception: I am not running scenarios on LIVE environment without '@prod' tag");
        }
    }

    @Before(order = 1)
    public void fillScenarioProps() {
        scenarioProps.putAll((Map) Config.PROPS);
        scenarioProps.putAll(Map.of("token", Config.TOKEN));
    }
}

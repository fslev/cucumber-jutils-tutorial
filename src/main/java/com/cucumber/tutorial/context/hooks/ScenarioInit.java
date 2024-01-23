package com.cucumber.tutorial.context.hooks;

import com.cucumber.tutorial.config.Config;
import com.cucumber.tutorial.context.BaseScenario;
import io.cucumber.guice.ScenarioScoped;
import io.cucumber.java.Before;

import java.util.Map;

import static com.cucumber.tutorial.config.Config.PROPS;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@ScenarioScoped
public class ScenarioInit extends BaseScenario {

    @Before(order = 0)
    public void safetyCheck() {
        if (Config.isProdEnv()) {
            assumeTrue(scenarioUtils.getScenario().getSourceTagNames().contains("@prod"),
                    "LIVE safety: I am not running scenarios on LIVE environment without '@prod' tag");
        }
    }

    @Before(order = 1)
    public void initScenarioVars() {
        scenarioVars.putAll((Map) PROPS);
    }

}

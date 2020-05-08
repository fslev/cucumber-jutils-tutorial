package com.cucumber.tutorial.context.hooks;

import com.cucumber.tutorial.config.Config;
import com.cucumber.tutorial.context.BaseScenario;
import io.cucumber.guice.ScenarioScoped;
import io.cucumber.java.Before;

import java.util.Map;

@ScenarioScoped
public class ScenarioDecorator extends BaseScenario {

    @Before(order = Integer.MAX_VALUE)
    public void fillScenarioProps() {
        scenarioProps.putAll((Map) Config.properties);
        scenarioProps.putAll(Map.of("token", Config.TOKEN));
    }

}

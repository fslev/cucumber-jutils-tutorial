package com.cucumber.tutorial.context.steps.bash;

import com.cucumber.tutorial.context.BaseScenario;
import com.cucumber.tutorial.context.services.bash.BashService;
import com.google.inject.Inject;
import io.cucumber.guice.ScenarioScoped;
import io.cucumber.java.en.Then;

@ScenarioScoped
public class BashSteps extends BaseScenario {

    @Inject
    private BashService bashService;

    @Then("[bash] Execute {} and check response={}")
    public void executeCmdAndMatch(String cmd, String expected) {
        bashService.executeAndMatch(cmd, expected);
    }
}

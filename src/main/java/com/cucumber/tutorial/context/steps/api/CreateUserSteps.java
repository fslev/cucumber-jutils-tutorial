package com.cucumber.tutorial.context.steps.api;

import com.cucumber.tutorial.context.BaseScenario;
import com.cucumber.tutorial.context.services.http.mock.UserService;
import com.google.inject.Inject;
import io.cucumber.guice.ScenarioScoped;
import io.cucumber.java.en.Then;
import io.jtest.utils.matcher.condition.MatchCondition;

@ScenarioScoped
public class CreateUserSteps extends BaseScenario {
    @Inject
    private UserService userService;

    @Then("Create user with name={}, job={} and check response={}")
    public void createUserAndCompare(String name, String job, String expected) {
        userService.buildCreate(name, job, scenarioProps.getAsString("token")).executeAndMatch(expected);
    }

    @Then("Create user with name={}, job={} and check response!={}")
    public void createUserAndCompareNegative(String name, String job, String expected) {
        userService.buildCreate(name, job, scenarioProps.getAsString("token")).executeAndMatch(expected, MatchCondition.DO_NOT_MATCH_HTTP_RESPONSE_BY_BODY);
    }

    @Then("Create user with request={} and check response={}")
    public void createUserAndCompare(String request, String expected) {
        userService.buildCreate(request, scenarioProps.getAsString("token")).executeAndMatch(expected);
    }
}
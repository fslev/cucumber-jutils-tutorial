package com.cucumber.tutorial.context.steps.api;

import com.cucumber.tutorial.context.RestScenario;
import com.cucumber.tutorial.services.http.mock.UserService;
import com.cucumber.utils.context.utils.Cucumbers;
import com.google.inject.Inject;
import io.cucumber.guice.ScenarioScoped;
import io.cucumber.java.en.Then;

@ScenarioScoped
public class CreateUserSteps extends RestScenario {
    private UserService userService = new UserService();

    @Inject
    public CreateUserSteps(Cucumbers cucumbers) {
        cucumbers.loadScenarioPropsFromFile("templates/users/create.yaml");
    }

    @Then("Create user with name={}, job={} and check response={}")
    public void createUserAndCompare(String name, String job, String expected) {
        executeAndCompare(userService.prepareCreate(
                scenarioProps.getAsString("reqresin.address"), name, job, scenarioProps.getAsString("token")), expected);
    }

    @Then("Create user with name={}, job={} and check response!={}")
    public void createUserAndCompareNegative(String name, String job, String expected) {
        executeAndNegativeCompare(userService.prepareCreate(
                scenarioProps.getAsString("reqresin.address"), name, job, scenarioProps.getAsString("token")), expected);
    }

    @Then("Create user with request={} and check response={}")
    public void createUserAndCompare(String request, String expected) {
        executeAndCompare(userService.prepareCreate(
                scenarioProps.getAsString("reqresin.address"), request, scenarioProps.getAsString("token")), expected);
    }
}
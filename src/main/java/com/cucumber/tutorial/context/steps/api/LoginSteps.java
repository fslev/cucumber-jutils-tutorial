package com.cucumber.tutorial.context.steps.api;

import com.cucumber.tutorial.context.RestScenario;
import com.cucumber.tutorial.context.services.http.mock.LoginService;
import com.google.inject.Inject;
import io.cucumber.guice.ScenarioScoped;
import io.cucumber.java.en.Then;

@ScenarioScoped
public class LoginSteps extends RestScenario {
    @Inject
    private LoginService loginService;

    @Then("Login with requestBody={} and check response={}")
    public void login(String requestBody, String expected) {
        executeAndCompare(loginService.buildLogin(requestBody), expected);
    }

    @Then("Login with email={}, password={} and check response={}")
    public void login(String email, String password, String expected) {
        executeAndCompare(loginService.buildLogin(email, password), expected);
    }
}
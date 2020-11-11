package com.cucumber.tutorial.context.steps.api;

import com.cucumber.tutorial.context.BaseScenario;
import com.cucumber.tutorial.context.services.http.mock.LoginService;
import com.google.inject.Inject;
import io.cucumber.guice.ScenarioScoped;
import io.cucumber.java.en.Then;

@ScenarioScoped
public class LoginSteps extends BaseScenario {
    @Inject
    private LoginService loginService;

    @Then("Login with requestBody={} and check response={}")
    public void login(String requestBody, String expected) {
        loginService.buildLogin(requestBody).executeAndMatch(expected);
    }

    @Then("Login with email={}, password={} and check response={}")
    public void login(String email, String password, String expected) {
        loginService.buildLogin(email, password).executeAndMatch(expected);
    }
}
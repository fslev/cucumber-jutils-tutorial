package com.cucumber.tutorial.context.hooks;

import com.cucumber.tutorial.config.Config;
import com.cucumber.tutorial.context.BaseScenario;
import com.cucumber.tutorial.context.services.http.mock.LoginService;
import io.cucumber.guice.ScenarioScoped;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.PendingException;

import java.util.Map;

import static com.cucumber.tutorial.config.Config.AUTH_TOKEN;
import static com.cucumber.tutorial.config.Config.PROPS;

@ScenarioScoped
public class Init extends BaseScenario {

    @BeforeAll
    public static void setAuthenticationToken() {
        AUTH_TOKEN = LoginService.loginAndGetToken(
                PROPS.getProperty("reqresin.address"), "eve.holt@reqres.in", "cityslicka");
        LOG.info("Got AUTH_TOKEN: {}", AUTH_TOKEN);
    }

    @Before(order = 0)
    public void safetyCheck() {
        if (Config.isProdEnv() && !scenarioUtils.getScenario().getSourceTagNames().contains("@prod")) {
            throw new PendingException("Safety exception: I am not running scenarios on LIVE environment without '@prod' tag");
        }
    }

    @Before(order = 1)
    public void fillScenarioProps() {
        scenarioProps.putAll((Map) PROPS);
        scenarioProps.putAll(Map.of("token", AUTH_TOKEN));
    }

}

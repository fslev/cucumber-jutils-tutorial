package com.cucumber.tutorial.context.hooks;

import com.cucumber.tutorial.context.services.http.mock.LoginService;
import io.cucumber.java.BeforeAll;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.cucumber.tutorial.config.Config.AUTH_TOKEN;
import static com.cucumber.tutorial.config.Config.PROPS;

public class Genesis {
    protected static final Logger LOG = LogManager.getLogger();

    @BeforeAll
    public static void setAuthenticationToken() {
        AUTH_TOKEN = LoginService.loginAndGetToken(
                PROPS.getProperty("reqresin.address"), "eve.holt@reqres.in", "cityslicka");
        LOG.info("Got AUTH_TOKEN: {}", AUTH_TOKEN);
    }
}

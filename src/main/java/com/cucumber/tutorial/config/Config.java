package com.cucumber.tutorial.config;

import com.cucumber.tutorial.context.services.http.mock.LoginService;
import io.jtest.utils.common.ResourceUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Properties;

public class Config {
    private static final Logger LOG = LogManager.getLogger();

    public static final Properties PROPS = loadConfig();
    public static final String ENV = PROPS.getProperty("env");

    public static boolean isProdEnv() {
        return ENV.equals("prod");
    }

    public static final String TOKEN =
            LoginService.loginAndGetToken(PROPS.getProperty("reqresin.address"), "eve.holt@reqres.in", "cityslicka");

    private static Properties loadConfig() {
        LOG.info("Get config properties...");
        try {
            return ResourceUtils.readProps("env.properties");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
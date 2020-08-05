package com.cucumber.tutorial.config;

import com.cucumber.tutorial.services.http.mock.LoginService;
import com.cucumber.utils.helper.ResourceUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Properties;

public class Config {
    private static final Logger LOG = LogManager.getLogger();
    public static Properties properties = loadConfig();
    public static final String TOKEN = new LoginService()
            .loginAndGetToken(properties.getProperty("reqresin.address"), "eve.holt@reqres.in", "cityslicka");

    private static Properties loadConfig() {
        LOG.info("Get config");
        return ResourceUtils.readProps("env.properties");
    }
}



package com.cucumber.tutorial.config;

import com.cucumber.tutorial.context.services.http.mock.LoginService;
import io.jtest.utils.common.ResourceUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Properties;

public class Config {
    private static final Logger LOG = LogManager.getLogger();
    public static Properties properties = loadConfig();
    public static final String TOKEN = new LoginService() {
        @Override
        protected String address() {
            return properties.getProperty("reqresin.address");
        }
    }.loginAndGetToken("eve.holt@reqres.in", "cityslicka");

    private static Properties loadConfig() {
        LOG.info("Get config");
        return ResourceUtils.readProps("env.properties");
    }
}



package com.cucumber.tutorial.context.services.http.mock;

import com.cucumber.tutorial.context.services.http.RestService;
import io.cucumber.guice.ScenarioScoped;
import io.jtest.utils.clients.http.HttpClient;
import io.jtest.utils.clients.http.Method;
import io.jtest.utils.common.ResourceUtils;
import io.jtest.utils.common.StringFormat;

import java.io.IOException;
import java.util.Map;

@ScenarioScoped
public class UserService extends RestService {

    public static final String USERS_PATH = "/api/users";
    public static String REQUEST_BODY_TEMPLATE;

    static {
        try {
            REQUEST_BODY_TEMPLATE = ResourceUtils.read("templates/user_api/requestBodyTemplate.json");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public HttpClient buildCreate(String name, String job, String token) {
        return buildCreate(StringFormat.replaceProps(REQUEST_BODY_TEMPLATE, Map.of("name", name, "job", job)), token);
    }

    public HttpClient buildCreate(String requestBody, String token) {
        return getBuilder(address()).path(USERS_PATH).method(Method.POST).addHeader("Authorization", token).entity(requestBody).build();
    }

    private String address() {
        return scenarioProps.getAsString("reqresin.address");
    }
}

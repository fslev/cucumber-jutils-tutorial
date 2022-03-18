package com.cucumber.tutorial.context.services.http.mock;

import com.cucumber.tutorial.context.services.http.HttpService;
import io.cucumber.guice.ScenarioScoped;
import io.jtest.utils.clients.http.Method;
import io.jtest.utils.common.ResourceUtils;
import io.jtest.utils.common.StringFormat;

import java.io.IOException;
import java.util.Map;
import java.util.function.BiFunction;

@ScenarioScoped
public class UserService extends HttpService {

    public static final String USERS_PATH = "/api/users";
    public static BiFunction<String, String, String> REQUEST_BODY_TEMPLATE;

    static {
        REQUEST_BODY_TEMPLATE = (name, job) -> {
            try {
                return StringFormat.replaceProps(ResourceUtils.read("templates/user_api/requestBodyTemplate.json"),
                        Map.of("name", name, "job", job));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public HttpService buildCreate(String name, String job, String token) {
        return buildCreate(REQUEST_BODY_TEMPLATE.apply(name, job), token);
    }

    public HttpService buildCreate(String requestBody, String token) {
        this.client = getBuilder().path(USERS_PATH).method(Method.POST).header("Authorization", token).entity(requestBody).build();
        return this;
    }

    @Override
    public String address() {
        return scenarioVars.getAsString("reqresin.address");
    }
}

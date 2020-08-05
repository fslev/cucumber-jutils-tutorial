package com.cucumber.tutorial.services.http.mock;

import com.cucumber.tutorial.services.http.RestService;
import com.cucumber.utils.clients.http.HttpClient;
import com.cucumber.utils.clients.http.Method;
import com.cucumber.utils.helper.ResourceUtils;
import com.cucumber.utils.helper.StringFormat;

import java.io.IOException;
import java.util.Map;

/**
 * Decouple HTTP Service description from Cucumber context
 * That way, it can be reused by different frameworks
 */
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

    public HttpClient.Builder prepareCreate(String address, String name, String job, String token) {
        return prepareCreate(address, StringFormat.replaceProps(REQUEST_BODY_TEMPLATE,
                Map.of("name", name, "job", job)), token);
    }

    public HttpClient.Builder prepareCreate(String address, String requestBody, String token) {
        return getDefaultClientBuilder().address(address).path(USERS_PATH)
                .method(Method.POST)
                .addHeader("Authorization", token)
                .entity(requestBody);
    }
}

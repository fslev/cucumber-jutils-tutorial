package com.cucumber.tutorial.context.services.http.mock;

import com.cucumber.tutorial.context.services.http.RestService;
import io.cucumber.guice.ScenarioScoped;
import io.jtest.utils.clients.http.HttpClient;
import io.jtest.utils.clients.http.Method;
import io.jtest.utils.common.JsonUtils;
import io.jtest.utils.common.StringFormat;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Map;
import java.util.function.BiFunction;

@ScenarioScoped
public class LoginService extends RestService {

    public static final String PATH = "/api/login";

    public static BiFunction<String, String, String> REQUEST_BODY_TEMPLATE = (email, pwd) -> StringFormat.replaceProps(
            "{\"email\": \"#[email]\", \"password\": \"#[pwd]\"}", Map.of("email", email, "password", pwd));

    public RestService buildLogin(String email, String pwd) {
        return buildLogin(REQUEST_BODY_TEMPLATE.apply(email, pwd));
    }

    public RestService buildLogin(String requestBody) {
        this.client = getBuilder().path(PATH).method(Method.POST).entity(requestBody).build();
        return this;
    }

    // Called outside Cucumber context
    public static String loginAndGetToken(String address, String email, String pwd) {
        HttpEntity entity = null;
        try (CloseableHttpResponse response = new HttpClient.Builder().address(address).path(PATH).method(Method.POST)
                .headers(defaultHeaders()).entity(REQUEST_BODY_TEMPLATE.apply(email, pwd)).build().execute()) {
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new RuntimeException(response.getStatusLine().toString());
            }
            entity = response.getEntity();
            return JsonUtils.toJson(EntityUtils.toString(response.getEntity())).get("token").asText();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (entity != null) {
                try {
                    EntityUtils.consume(entity);
                } catch (IOException e) {
                    LOG.error(e);
                }
            }
        }
    }

    @Override
    protected String address() {
        return scenarioProps.getAsString("reqresin.address");
    }
}

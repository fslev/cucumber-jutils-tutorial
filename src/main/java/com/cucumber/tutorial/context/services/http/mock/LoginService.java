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

@ScenarioScoped
public class LoginService extends RestService {

    public static final String PATH = "/api/login";
    public static String REQUEST_BODY_TEMPLATE = "{\"email\": \"#[email]\", \"password\": \"#[password]\"}";

    public HttpClient buildLogin(String email, String pwd) {
        return buildLogin(StringFormat.replaceProps(REQUEST_BODY_TEMPLATE, Map.of("email", email, "password", pwd)));
    }

    public HttpClient buildLogin(String requestBody) {
        return getBuilder(address()).path(PATH).method(Method.POST).entity(requestBody).build();
    }

    public String loginAndGetToken(String email, String pwd) {
        HttpEntity entity = null;
        try (CloseableHttpResponse response = buildLogin(email, pwd).execute()) {
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
                    throw new RuntimeException(e);
                }
            }
        }
    }

    protected String address() {
        return scenarioProps.getAsString("reqresin.address");
    }
}

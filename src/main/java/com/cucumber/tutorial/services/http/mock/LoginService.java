package com.cucumber.tutorial.services.http.mock;

import com.cucumber.tutorial.services.http.RestService;
import com.cucumber.utils.clients.http.HttpClient;
import com.cucumber.utils.clients.http.Method;
import com.cucumber.utils.engineering.utils.JsonUtils;
import com.cucumber.utils.engineering.utils.ResourceUtils;
import com.cucumber.utils.engineering.utils.StringFormat;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Map;

public class LoginService extends RestService {

    public static final String PATH = "/api/login";
    public static String REQUEST_BODY_TEMPLATE;

    static {
        try {
            REQUEST_BODY_TEMPLATE = ResourceUtils.readYaml("templates/login/login.yaml").get("loginRequestTemplate").toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public HttpClient.Builder prepare(String address, String email, String pwd) {
        return prepare(address, StringFormat.replaceProps(REQUEST_BODY_TEMPLATE, Map.of("email", email, "password", pwd)));
    }

    public HttpClient.Builder prepare(String address, String requestBody) {
        return getDefaultClientBuilder().address(address).path(PATH)
                .method(Method.POST)
                .entity(requestBody);
    }

    public String loginAndGetToken(String address, String email, String pwd) {
        HttpEntity entity = null;
        try (CloseableHttpResponse response = prepare(address, email, pwd).build().execute()) {
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
}

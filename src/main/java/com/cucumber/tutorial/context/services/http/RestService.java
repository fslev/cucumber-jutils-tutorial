package com.cucumber.tutorial.context.services.http;

import com.cucumber.tutorial.context.BaseScenario;
import io.jtest.utils.clients.http.HttpClient;

import java.util.Map;

public abstract class RestService extends BaseScenario {

    protected HttpClient.Builder getBuilder(String address) {
        return new HttpClient.Builder().address(address)
                .setHeaders(Map.of("Content-Type", "application/json", "Accept", "application/json"));
    }
}

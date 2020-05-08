package com.cucumber.tutorial.services.http;

import com.cucumber.utils.clients.http.HttpClient;

import java.util.Map;

/**
 * Decouple HTTP Service description from Cucumber context
 * That way, it can be reused by different frameworks
 */
public class RestService {

    protected HttpClient.Builder getDefaultClientBuilder() {
        return new HttpClient.Builder().setHeaders(Map.of(
                "Content-Type", "application/json",
                "Accept", "application/json")
        );
    }
}

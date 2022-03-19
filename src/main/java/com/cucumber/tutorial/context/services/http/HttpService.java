package com.cucumber.tutorial.context.services.http;

import com.cucumber.tutorial.context.BaseScenario;
import com.cucumber.tutorial.util.DateUtils;
import io.jtest.utils.clients.http.HttpClient;
import io.jtest.utils.clients.http.wrappers.HttpResponseWrapper;
import io.jtest.utils.common.JsonUtils;
import io.jtest.utils.matcher.ObjectMatcher;
import io.jtest.utils.matcher.condition.MatchCondition;
import org.apache.http.client.methods.CloseableHttpResponse;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Consumer;

public abstract class HttpService extends BaseScenario {

    private boolean logDetails = true;
    protected HttpClient client;

    protected abstract String address();

    protected HttpClient.Builder getBuilder() {
        return new HttpClient.Builder().address(address()).headers(defaultHeaders());
    }

    protected static Map<String, String> defaultHeaders() {
        return Map.of("Content-Type", "application/json", "Accept", "application/json");
    }

    public HttpService logDetails(boolean value) {
        this.logDetails = value;
        return this;
    }

    public CloseableHttpResponse execute() {
        try {
            return client.execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public HttpResponseWrapper executeAndMatch(String expected, MatchCondition... matchConditions) {
        return executeAndMatch(expected, null, null, matchConditions);
    }

    public HttpResponseWrapper executeAndMatch(String expected, Consumer<CloseableHttpResponse> consumer, MatchCondition... matchConditions) {
        return executeAndMatch(expected, consumer, null, matchConditions);
    }

    public HttpResponseWrapper executeAndMatch(String expected, Integer pollingTimeoutSeconds, MatchCondition... matchConditions) {
        return executeAndMatch(expected, pollingTimeoutSeconds, 3000, matchConditions);
    }

    public HttpResponseWrapper executeAndMatch(String expected, Consumer<CloseableHttpResponse> consumer, Integer pollingTimeoutSeconds, MatchCondition... matchConditions) {
        return executeAndMatch(expected, consumer, pollingTimeoutSeconds, 3000, matchConditions);
    }

    public HttpResponseWrapper executeAndMatch(String expected, Integer pollingTimeoutSeconds,
                                               long retryIntervalMillis, MatchCondition... matchConditions) {
        return executeAndMatch(expected, null, pollingTimeoutSeconds, retryIntervalMillis, null, matchConditions);
    }

    public HttpResponseWrapper executeAndMatch(String expected, Consumer<CloseableHttpResponse> consumer, Integer pollingTimeoutSeconds,
                                               long retryIntervalMillis, MatchCondition... matchConditions) {
        return executeAndMatch(expected, consumer, pollingTimeoutSeconds, retryIntervalMillis, null, matchConditions);
    }

    public HttpResponseWrapper executeAndMatch(String expected, Consumer<CloseableHttpResponse> consumer, Integer pollingDurationSeconds, long retryIntervalMillis,
                                               Double exponentialBackOff, MatchCondition... matchConditions) {
        logRequest(client);
        logExpected(expected);
        final HttpResponseReference responseRef = new HttpResponseReference();
        HttpResponseWrapper responseWrapper;
        try {
            if (pollingDurationSeconds == null || pollingDurationSeconds == 0) {
                responseRef.set(client.execute());
                scenarioVars.putAll(ObjectMatcher.matchHttpResponse(null, expected, responseRef.get(), matchConditions));
            } else {
                scenarioVars.putAll(ObjectMatcher.matchHttpResponse(null, expected, () -> {
                    try {
                        responseRef.set(client.execute());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    return responseRef.get();
                }, pollingDurationSeconds, retryIntervalMillis, exponentialBackOff, matchConditions));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                responseWrapper = new HttpResponseWrapper(responseRef.get());
                logActual(responseWrapper);
                if (consumer != null) {
                    consumer.accept(responseRef.get());
                } else {
                    responseRef.get().close();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return responseWrapper;
    }

    private void logRequest(HttpClient client) {
        try {
            scenarioUtils.log("------- API REQUEST ({}) -------\n{}\nHEADERS: {}\nBODY: {}\n\n",
                    DateUtils.currentDateTime(), client.getMethod() + " " + URLDecoder.decode(client.getUri(), StandardCharsets.UTF_8.name()),
                    client.getHeaders(), client.getRequestEntity() != null ? "\n" + client.getRequestEntity() : "N/A");
        } catch (UnsupportedEncodingException e) {
            scenarioUtils.log("Error logging request:\n{}", e);
            LOG.error(e);
        }
        if (client.getProxyHost() != null) {
            scenarioUtils.log("via PROXY HOST: {}", client.getProxyHost());
        }
    }

    private void logExpected(String expected) {
        if (logDetails) {
            scenarioUtils.log("----------------- EXPECTED RESPONSE -----------------\n{}\n\n", expected);
        }
    }

    private void logActual(HttpResponseWrapper response) {
        if (logDetails) {
            scenarioUtils.log("------------------ ACTUAL RESPONSE ------------------\nSTATUS: {} {}\nBODY: \n{}\nHEADERS:\n{}\n",
                    response.getStatus(), response.getReasonPhrase(),
                    (response.getEntity() != null) ? JsonUtils.prettyPrint(response.getEntity().toString()) : "Empty data <∅>",
                    response.getHeaders());
        }
    }

    private static class HttpResponseReference {
        private CloseableHttpResponse response;

        public void set(CloseableHttpResponse response) {
            if (this.response != null) {
                try {
                    this.response.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            this.response = response;
        }

        public CloseableHttpResponse get() {
            return response;
        }
    }
}

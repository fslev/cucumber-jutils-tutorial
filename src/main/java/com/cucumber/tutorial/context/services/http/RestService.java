package com.cucumber.tutorial.context.services.http;

import com.cucumber.tutorial.context.BaseScenario;
import io.jtest.utils.clients.http.HttpClient;
import io.jtest.utils.common.JsonUtils;
import io.jtest.utils.matcher.ObjectMatcher;
import io.jtest.utils.matcher.condition.MatchCondition;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

public abstract class RestService extends BaseScenario {

    protected HttpClient client;

    protected abstract String address();

    protected HttpClient.Builder getBuilder() {
        return new HttpClient.Builder().address(address()).headers(defaultHeaders());
    }

    protected static Map<String, String> defaultHeaders() {
        return Map.of("Content-Type", "application/json", "Accept", "application/json");
    }

    public CloseableHttpResponse execute() {
        return client.execute();
    }

    public String executeAndMatch(String expected, MatchCondition... matchConditions) {
        return executeAndMatch(expected, null, matchConditions);
    }

    public String executeAndMatch(String expected, Integer pollingTimeoutSeconds, MatchCondition... matchConditions) {
        return executeAndMatch(expected, pollingTimeoutSeconds, 3000, matchConditions);
    }

    public String executeAndMatch(String expected, Integer pollingTimeoutSeconds,
                                  long retryIntervalMillis, MatchCondition... matchConditions) {
        return executeAndMatch(expected, pollingTimeoutSeconds, retryIntervalMillis, null, matchConditions);
    }

    public String executeAndMatch(String expected, Integer pollingTimeoutSeconds,
                                  Long retryIntervalMillis, Double exponentialBackOff, MatchCondition... matchConditions) {
        logRequest(client);
        final HttpResponseReference responseRef = new HttpResponseReference();
        String responseBody;
        try {
            if (pollingTimeoutSeconds == null || pollingTimeoutSeconds == 0) {
                responseRef.set(client.execute());
                scenarioProps.putAll(ObjectMatcher.matchHttpResponse(null, expected, responseRef.get(), matchConditions));
            } else {
                scenarioProps.putAll(ObjectMatcher.matchHttpResponse(null, expected, () -> {
                    responseRef.set(client.execute());
                    return responseRef.get();
                }, pollingTimeoutSeconds, retryIntervalMillis, exponentialBackOff, matchConditions));
            }
        } finally {
            scenarioUtils.log("----------- EXPECTED RESPONSE -----------\n{}\n\n", expected);
            responseBody = logAndGetResponse(responseRef.get());
        }
        return responseBody;
    }

    private void logRequest(HttpClient client) {
        scenarioUtils.log("-------------- API REQUEST --------------\n{}\nHEADERS: {}\nBODY: {}\n\n",
                client.getMethod() + " " + client.getUri(), client.getHeaders(),
                client.getRequestEntity() != null ? "\n" + client.getRequestEntity() : "N/A");
    }

    private String logAndGetResponse(CloseableHttpResponse actual) {
        HttpEntity entity = null;
        String responseBody = null;
        try {
            if (actual != null) {
                entity = actual.getEntity();
                responseBody = (entity != null) ? EntityUtils.toString(entity) : null;
                scenarioUtils.log("------------ ACTUAL RESPONSE ------------\nSTATUS: {} {}\nBODY: \n{}\nHEADERS:\n{}\n",
                        actual.getStatusLine().getStatusCode(), actual.getStatusLine().getReasonPhrase(),
                        (responseBody != null) ? JsonUtils.prettyPrint(responseBody) : "Empty data <∅>",
                        Arrays.asList(actual.getAllHeaders()).toString());
            }
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
            try {
                if (actual != null) {
                    actual.close();
                }
            } catch (IOException e) {
                LOG.error(e);
            }
        }
        return responseBody;
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

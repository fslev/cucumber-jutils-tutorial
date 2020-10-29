package com.cucumber.tutorial.context.services.http;

import com.cucumber.tutorial.context.BaseScenario;
import com.cucumber.utils.context.Cucumbers;
import com.google.inject.Inject;
import io.jtest.utils.clients.http.HttpClient;
import io.jtest.utils.common.JsonUtils;
import io.jtest.utils.matcher.condition.MatchCondition;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public abstract class RestService extends BaseScenario {

    @Inject
    protected Cucumbers cucumbers;

    protected HttpClient client;

    protected abstract String address();

    protected HttpClient.Builder getBuilder() {
        return new HttpClient.Builder().address(address())
                .setHeaders(Map.of("Content-Type", "application/json", "Accept", "application/json"));
    }

    public CloseableHttpResponse execute() {
        return client.execute();
    }

    public String executeAndCompare(String expected, MatchCondition... matchConditions) {
        return executeAndCompare(expected, null, matchConditions);
    }

    public String executeAndCompare(String expected, Integer pollDurationInSeconds, MatchCondition... matchConditions) {
        return executeAndCompare(expected, pollDurationInSeconds, 3000, matchConditions);
    }

    public String executeAndCompare(String expected, Integer pollDurationInSeconds,
                                    long retryIntervalMillis, MatchCondition... matchConditions) {
        return executeAndCompare(expected, pollDurationInSeconds, retryIntervalMillis, null, matchConditions);
    }

    public String executeAndCompare(String expected, Integer pollDurationInSeconds,
                                    Long retryIntervalMillis, Double exponentialBackOff, MatchCondition... matchConditions) {
        logRequest(client);
        final AtomicReference<CloseableHttpResponse> responseWrapper = new AtomicReference<>();
        String responseBody;
        try {
            if (pollDurationInSeconds == null || pollDurationInSeconds == 0) {
                responseWrapper.set(client.execute());
                cucumbers.compareHttpResponse(null, expected, responseWrapper.get(), matchConditions);
            } else {
                cucumbers.pollAndCompareHttpResponse(null, expected, pollDurationInSeconds, retryIntervalMillis, exponentialBackOff,
                        () -> {
                            responseWrapper.set(client.execute());
                            return responseWrapper.get();
                        }, matchConditions);
            }
        } catch (Exception e) {
            throw (e);
        } finally {
            scenarioUtils.log("----------- EXPECTED RESPONSE -----------\n{}", expected);
            responseBody = logAndGetResponse(responseWrapper.get());
        }
        return responseBody;
    }

    private void logRequest(HttpClient client) {
        scenarioUtils.log("-------------- API REQUEST --------------\n{}\nHEADERS: {}\nBODY: {}\n",
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
}

package com.cucumber.tutorial.context;

import com.cucumber.utils.clients.http.HttpClient;
import com.cucumber.utils.engineering.utils.JsonUtils;
import io.cucumber.guice.ScenarioScoped;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

@ScenarioScoped
public class RestScenario extends BaseScenario {
    protected Logger log = LogManager.getLogger();

    public String executeAndCompare(HttpClient.Builder builder, String expected) {
        return executeAndCompare(builder, expected, null);
    }

    public String executeAndCompare(HttpClient.Builder builder, String expected, Integer pollDurationInSeconds) {
        return executeAndCompare(builder, expected, pollDurationInSeconds, 3000);
    }

    public String executeAndCompare(HttpClient.Builder builder, String expected, Integer pollDurationInSeconds, long retryIntervalMillis) {
        return executeAndCompare(builder, expected, pollDurationInSeconds, retryIntervalMillis, 1.0);
    }

    public String executeAndCompare(HttpClient.Builder builder, String expected, Integer pollDurationInSeconds, long retryIntervalMillis, double exponentialBackOff) {
        HttpClient client = builder.build();
        logRequest(client);
        final AtomicReference<CloseableHttpResponse> responseWrapper = new AtomicReference<>();
        String responseBody;
        try {
            if (pollDurationInSeconds == null || pollDurationInSeconds == 0) {
                responseWrapper.set(client.execute());
                cucumbers.compareHttpResponse(null, expected, responseWrapper.get(), false, false, false);
            } else {
                cucumbers.pollAndCompareHttpResponse(null, expected, pollDurationInSeconds, retryIntervalMillis, exponentialBackOff,
                        () -> {
                            responseWrapper.set(client.execute());
                            return responseWrapper.get();
                        }, false, false, false);
            }
        } catch (Exception e) {
            throw (e);
        } finally {
            scenarioUtils.log("----------- Comparison -----------");
            scenarioUtils.log("EXPECTED Response:\n{}", expected);
            scenarioUtils.log("--------------- vs ---------------");
            responseBody = logAndGetResponse(responseWrapper.get());
        }
        return responseBody;
    }

    public String executeAndNegativeCompare(HttpClient.Builder builder, String expected) {
        return executeAndNegativeCompare(builder, expected, null, null, null,true,false,false);
    }

    public String executeAndNegativeCompare(HttpClient.Builder builder, String expected, Integer pollDurationInSeconds, Long retryIntervalMillis, Double exponentialBackOff,
                                            boolean byBody,boolean byStatus, boolean byHeaders) {
        HttpClient client = builder.build();
        logRequest(client);
        String responseBody;
        final AtomicReference<CloseableHttpResponse> responseWrapper = new AtomicReference<>();
        try {
            if (pollDurationInSeconds == null || pollDurationInSeconds == 0) {
                responseWrapper.set(client.execute());
                cucumbers.negativeCompareHttpResponse(null, expected, responseWrapper.get(), byBody, byStatus, byHeaders, false, false, false, false);
            } else {
                cucumbers.negativePollAndCompareHttpResponse(null, expected, pollDurationInSeconds, retryIntervalMillis, exponentialBackOff,
                        byBody, byStatus, byHeaders, false,
                        () -> {
                            responseWrapper.set(client.execute());
                            return responseWrapper.get();
                        }, false, false, false);
            }
        } catch (Exception e) {
            throw (e);
        } finally {
            scenarioUtils.log("----------- NEGATIVE Comparison by HTTP Response Body -----------");
            scenarioUtils.log("EXPECTED Response:\n{}", expected);
            scenarioUtils.log("--------------- vs ---------------");
            responseBody = logAndGetResponse(responseWrapper.get());
        }
        return responseBody;
    }

    private void logRequest(HttpClient client) {
        scenarioUtils.log("--------- API call details ---------");
        scenarioUtils.log("REQUEST: {}", client.getMethod() + " " + client.getUri());
        scenarioUtils.log("REQUEST HEADERS: {}", client.getHeaders());
        if (client.getRequestEntity() != null) {
            scenarioUtils.log("REQUEST BODY:\n{}", client.getRequestEntity());
        }
    }

    private String logAndGetResponse(CloseableHttpResponse actual) {
        HttpEntity entity = null;
        String responseBody = null;
        try {
            if (actual != null) {
                scenarioUtils.log("ACTUAL Response status: {}", actual.getStatusLine().getStatusCode());
                entity = actual.getEntity();
                responseBody = (entity != null) ? EntityUtils.toString(entity) : null;
                scenarioUtils.log("ACTUAL Response body:\n{}", (responseBody != null) ? JsonUtils.prettyPrint(responseBody) : "Empty data <∅>");
                scenarioUtils.log("ACTUAL Response headers: {}", Arrays.asList(actual.getAllHeaders()).toString());
            }
        } catch (IOException e) {
            log.error(e);
        } finally {
            if (entity != null) {
                try {
                    EntityUtils.consume(entity);
                } catch (IOException e) {
                    log.error(e);
                }
            }
            try {
                if (actual != null) {
                    actual.close();
                }
            } catch (IOException e) {
                log.error(e);
            }
        }
        return responseBody;
    }
}

package com.cucumber.tutorial.context.services.api;

import com.cucumber.tutorial.context.BaseScenario;
import com.cucumber.tutorial.util.DateUtils;
import io.json.compare.util.JsonUtils;
import io.jtest.utils.common.StringFormat;
import io.jtest.utils.matcher.ObjectMatcher;
import io.jtest.utils.matcher.condition.MatchCondition;
import io.jtest.utils.matcher.http.PlainHttpResponse;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.Method;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.net.URIBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.cucumber.tutorial.util.PlainHttpResponseUtils.from;

public abstract class HttpService extends BaseScenario {

    private final static CloseableHttpClient client = HttpClients.custom()
            .setConnectionManager(new PoolingHttpClientConnectionManager()).build();

    protected HttpUriRequestBase request;

    protected abstract String address();

    protected HttpUriRequestBase getDefaultRequest(Method method, URI uri) {
        HttpUriRequestBase requestBase = new HttpUriRequestBase(method.toString(), uri);
        requestBase.setHeaders(headers(defaultHeaders()));
        return requestBase;
    }

    protected static Map<String, String> defaultHeaders() {
        return Map.of("Content-Type", "application/json", "Accept", "application/json");
    }

    public CloseableHttpResponse execute() {
        try {
            return client.execute(request);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public PlainHttpResponse executeAndMatch(String expected, MatchCondition... matchConditions) {
        return executeAndMatch(expected, null, null, matchConditions);
    }

    public PlainHttpResponse executeAndMatch(String expected, Consumer<CloseableHttpResponse> consumer, MatchCondition... matchConditions) {
        return executeAndMatch(expected, consumer, null, matchConditions);
    }

    public PlainHttpResponse executeAndMatch(String expected, Integer pollingTimeoutSeconds, MatchCondition... matchConditions) {
        return executeAndMatch(expected, pollingTimeoutSeconds, 3000, matchConditions);
    }

    public PlainHttpResponse executeAndMatch(String expected, Consumer<CloseableHttpResponse> consumer, Integer pollingTimeoutSeconds, MatchCondition... matchConditions) {
        return executeAndMatch(expected, consumer, pollingTimeoutSeconds, 3000, matchConditions);
    }

    public PlainHttpResponse executeAndMatch(String expected, Integer pollingTimeoutSeconds,
                                             long retryIntervalMillis, MatchCondition... matchConditions) {
        return executeAndMatch(expected, null, pollingTimeoutSeconds, retryIntervalMillis, null, matchConditions);
    }

    public PlainHttpResponse executeAndMatch(String expected, Consumer<CloseableHttpResponse> consumer, Integer pollingTimeoutSeconds,
                                             long retryIntervalMillis, MatchCondition... matchConditions) {
        return executeAndMatch(expected, consumer, pollingTimeoutSeconds, retryIntervalMillis, null, matchConditions);
    }

    public PlainHttpResponse executeAndMatch(String expected, Consumer<CloseableHttpResponse> consumer, Integer pollingDurationSeconds, long retryIntervalMillis,
                                             Double exponentialBackOff, MatchCondition... matchConditions) {
        logRequest();
        logExpected(expected);
        final HttpResponseReference responseRef = new HttpResponseReference();
        PlainHttpResponse plainResponse = null;
        try {
            if (pollingDurationSeconds == null || pollingDurationSeconds == 0) {
                responseRef.set(client.execute(request));
                scenarioVars.putAll(ObjectMatcher.matchHttpResponse(null, from(expected), from(responseRef.get()), matchConditions));
            } else {
                scenarioVars.putAll(ObjectMatcher.matchHttpResponse(null, from(expected), () -> {
                    try {
                        responseRef.set(client.execute(request));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    return from(responseRef.get());
                }, Duration.ofSeconds(pollingDurationSeconds), retryIntervalMillis, exponentialBackOff, matchConditions));
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not execute HTTP request", e);
        } finally {
            if (responseRef.get() != null) {
                try {
                    plainResponse = from(responseRef.get());
                    logResponse(plainResponse);
                    if (consumer != null) {
                        consumer.accept(responseRef.get());
                    } else {
                        EntityUtils.consumeQuietly(responseRef.get().getEntity());
                        responseRef.get().close();
                    }
                } catch (Exception e) {
                    scenarioUtils.log(e);
                    LOG.error(e);
                }
            }
        }
        return plainResponse;
    }

    private void logRequest() {
        try {
            scenarioUtils.log("------- API REQUEST ({}) -------\n{}\nHEADERS: {}\nBODY: {}\n\n",
                    DateUtils.currentDateTime(), request.getMethod() + " " +
                            URLDecoder.decode(request.getUri().toString(), StandardCharsets.UTF_8),
                    request.getHeaders(), request.getEntity() != null ? "\n" + EntityUtils.toString(request.getEntity()) : "N/A");
            if (request.getConfig() != null && request.getConfig().getProxy() != null) {
                scenarioUtils.log("via PROXY HOST: {}", request.getConfig().getProxy());
            }
        } catch (URISyntaxException | IOException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private void logExpected(String expected) {
        scenarioUtils.log("----------------- EXPECTED RESPONSE -----------------\n{}\n\n", expected);
    }

    private void logResponse(PlainHttpResponse response) {
        scenarioUtils.log("------------------ ACTUAL RESPONSE ------------------\nSTATUS: {} {}\nBODY: \n{}\nHEADERS:\n{}\n",
                response.getStatus(), response.getReasonPhrase(),
                (response.getEntity() != null) ? prettyPrint(response.getEntity().toString()) : "Empty data <∅>",
                response.getHeaders());
    }

    protected static URI uri(String address, String path) {
        return uri(address, path, null, null);
    }

    protected static URI uri(String address, String path, Map<String, String> pathParams) {
        return uri(address, path, pathParams, null);
    }

    protected static URI uri(String address, String path, Map<String, String> pathParams, Map<String, String> queryParams) {
        URIBuilder uriBuilder;
        try {
            uriBuilder = new URIBuilder(address + (path != null ? StringFormat.replaceProps(path, pathParams) : ""));
            if (queryParams != null) {
                uriBuilder.addParameters(queryParams.entrySet().stream().map(param ->
                        new BasicNameValuePair(param.getKey(), param.getValue())).collect(Collectors.toList()));
            }
            return uriBuilder.build();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    protected static Header[] headers(Map<String, String> headers) {
        return headers.entrySet().stream()
                .map(h -> new BasicHeader(h.getKey(), h.getValue())).collect(Collectors.toList())
                .toArray(Header[]::new);
    }


    private static String prettyPrint(String content) {
        try {
            return JsonUtils.prettyPrint(content);
        } catch (IOException e) {
            return content;
        }
    }

    private static class HttpResponseReference {
        private CloseableHttpResponse response;

        public void set(CloseableHttpResponse response) {
            if (this.response != null) {
                try {
                    EntityUtils.consumeQuietly(this.response.getEntity());
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

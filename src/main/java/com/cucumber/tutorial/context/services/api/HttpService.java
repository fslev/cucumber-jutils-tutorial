package com.cucumber.tutorial.context.services.api;

import com.cucumber.tutorial.context.services.BaseService;
import com.cucumber.tutorial.util.DateUtils;
import com.cucumber.tutorial.util.PlainHttpResponseUtils;
import io.json.compare.util.JsonUtils;
import io.jtest.utils.common.StringFormat;
import io.jtest.utils.matcher.ObjectMatcher;
import io.jtest.utils.matcher.condition.MatchCondition;
import io.jtest.utils.matcher.http.PlainHttpResponse;
import me.tongfei.progressbar.DelegatingProgressBarConsumer;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.Method;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.net.URIBuilder;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.cucumber.tutorial.util.PlainHttpResponseUtils.from;
import static org.awaitility.Awaitility.await;

public abstract class HttpService extends BaseService {

    private static final CloseableHttpClient CLIENT = defaultHttpClientBuilder().build();

    protected HttpUriRequestBase request;

    protected static HttpClientBuilder defaultHttpClientBuilder() {
        try {
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(new KeyManager[0], new TrustManager[]{new DefaultTrustManager()}, new SecureRandom());
            return HttpClients.custom()
                    .setConnectionManager(PoolingHttpClientConnectionManagerBuilder.create()
                            .setSSLSocketFactory(new SSLConnectionSocketFactory(ctx, new NoopHostnameVerifier())).build());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract String address();

    protected HttpUriRequestBase getDefaultRequest(Method method, URI uri) {
        HttpUriRequestBase requestBase = new HttpUriRequestBase(method.toString(), uri);
        addHeaders(requestBase, defaultHeaders());
        return requestBase;
    }

    protected URI uri(String path) {
        return uri(path, null);
    }

    protected URI uri(String path, Map<String, Object> pathParams) {
        return uri(path, pathParams, null);
    }

    protected URI uri(String path, Map<String, Object> pathParams, Map<String, String> nonEmptyQueryParams) {
        return uri(path, pathParams, nonEmptyQueryParams, null);
    }

    protected URI uri(String path, Map<String, Object> pathParams, Map<String, String> nonEmptyQueryParams, Map<String, String> rawQueryParams) {
        return uri(address(), path, pathParams, nonEmptyQueryParams, rawQueryParams);
    }

    public static URI uri(String address, String path, Map<String, Object> pathParams, Map<String, String> nonEmptyQueryParams, Map<String, String> rawQueryParams) {
        URIBuilder uriBuilder;
        try {
            uriBuilder = new URIBuilder(address);
            if (path != null) {
                uriBuilder.appendPath(StringFormat.replaceProps(path, pathParams));
            }
            if (nonEmptyQueryParams != null) {
                uriBuilder.addParameters(nonEmptyQueryParams.entrySet().stream()
                        .filter(e -> e.getValue() != null && !e.getValue().isEmpty())
                        .map(param -> new BasicNameValuePair(param.getKey(), param.getValue())).collect(Collectors.toList()));
            }
            if (rawQueryParams != null) {
                uriBuilder.addParameters(rawQueryParams.entrySet().stream()
                        .map(param -> new BasicNameValuePair(param.getKey(), param.getValue())).collect(Collectors.toList()));
            }
            return uriBuilder.build();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    protected static Map<String, String> defaultHeaders() {
        return Map.of("Content-Type", "application/json", "Accept", "application/json");
    }

    public static void addHeaders(HttpUriRequestBase requestBase, Map<String, String> headers) {
        headers.forEach(requestBase::addHeader);
    }

    public PlainHttpResponse execute() {
        try {
            return CLIENT.execute(request, PlainHttpResponseUtils::from);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public PlainHttpResponse executeAndMatch(String expected, MatchCondition... matchConditions) {
        return executeAndMatch(expected, null, null, matchConditions);
    }

    public PlainHttpResponse executeAndMatch(String expected, Consumer<PlainHttpResponse> consumer, MatchCondition... matchConditions) {
        return executeAndMatch(expected, consumer, null, matchConditions);
    }

    public PlainHttpResponse executeAndMatch(String expected, Integer pollingTimeoutSeconds, MatchCondition... matchConditions) {
        return executeAndMatch(expected, pollingTimeoutSeconds, 3000, matchConditions);
    }

    public PlainHttpResponse executeAndMatch(String expected, Consumer<PlainHttpResponse> consumer, Integer pollingTimeoutSeconds, MatchCondition... matchConditions) {
        return executeAndMatch(expected, consumer, pollingTimeoutSeconds, 3000, matchConditions);
    }

    public PlainHttpResponse executeAndMatch(String expected, Integer pollingTimeoutSeconds,
                                             long retryIntervalMillis, MatchCondition... matchConditions) {
        return executeAndMatch(expected, null, pollingTimeoutSeconds, retryIntervalMillis, null, matchConditions);
    }

    public PlainHttpResponse executeAndMatch(String expected, Consumer<PlainHttpResponse> consumer, Integer pollingTimeoutSeconds,
                                             long retryIntervalMillis, MatchCondition... matchConditions) {
        return executeAndMatch(expected, consumer, pollingTimeoutSeconds, retryIntervalMillis, null, matchConditions);
    }

    public PlainHttpResponse executeAndMatch(String expected, Consumer<PlainHttpResponse> consumer, Integer pollingDurationSeconds, long retryIntervalMillis,
                                             Double exponentialBackOff, MatchCondition... matchConditions) {
        logRequestAndExpectedResult(expected);
        final AtomicReference<PlainHttpResponse> responseRef = new AtomicReference<>();
        try {
            if (pollingDurationSeconds == null || pollingDurationSeconds == 0) {
                responseRef.set(CLIENT.execute(request, PlainHttpResponseUtils::from));
                scenarioVars.putAll(ObjectMatcher.matchHttpResponse(null, from(expected), responseRef.get(), matchConditions));
            } else {
                try (ProgressBar pb = new ProgressBarBuilder().setTaskName("Polling |" + retryIntervalMillis + "ms/" + pollingDurationSeconds + "s| backoff " + exponentialBackOff + " |")
                        .setInitialMax(Math.round(pollingDurationSeconds * 1000 / (double) retryIntervalMillis))
                        .setConsumer(new DelegatingProgressBarConsumer(c -> {
                            if (System.getProperty("hidePollingProgress") == null) {
                                System.err.println("\r" + c + PlainHttpResponseUtils.toOnelineReducedString(responseRef.get()));
                            }
                        })).build()) {
                    await("Polling HTTP response").pollDelay(Duration.ZERO).pollInterval(Duration.ofMillis(retryIntervalMillis))
                            .atMost(pollingDurationSeconds, TimeUnit.SECONDS)
                            .untilAsserted(() -> {
                                responseRef.set(CLIENT.execute(request, PlainHttpResponseUtils::from));
                                pb.stepTo(Math.round(pb.getElapsedAfterStart().toMillis() / (float) retryIntervalMillis));
                                scenarioVars.putAll(ObjectMatcher.matchHttpResponse(null, from(expected), responseRef.get()));
                            });
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not execute HTTP request", e);
        } finally {
            if (responseRef.get() != null) {
                logResponse(responseRef.get());
                if (consumer != null) {
                    consumer.accept(responseRef.get());
                }
            }
        }
        return responseRef.get();
    }

    private void logRequestAndExpectedResult(String expected) {
        try {
            scenarioUtils.log("------- API REQUEST ({}) -------\n\n{}\nHEADERS: {}\n" +
                            "BODY: {}\n\n----------------- EXPECTED RESPONSE -----------------\n{}\n\n",
                    DateUtils.currentDateTime(), request.getMethod() + " " +
                            URLDecoder.decode(request.getUri().toString(), StandardCharsets.UTF_8),
                    request.getHeaders(), request.getEntity() != null ? "\n" + EntityUtils.toString(request.getEntity()) : "N/A",
                    expected);
        } catch (URISyntaxException | IOException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private void logResponse(PlainHttpResponse response) {
        scenarioUtils.log("------------------ ACTUAL RESPONSE ------------------\nSTATUS: {} {}\nBODY: \n{}\nHEADERS:\n{}\n",
                response.getStatus(), response.getReasonPhrase(),
                (response.getEntity() != null) ? prettyPrint(response.getEntity().toString()) : "Empty data <∅>",
                response.getHeaders());
    }

    private static String prettyPrint(String content) {
        try {
            return JsonUtils.prettyPrint(content);
        } catch (IOException e) {
            return content;
        }
    }

    private static class DefaultTrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] arg0, String arg1) {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] arg0, String arg1) {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }
}

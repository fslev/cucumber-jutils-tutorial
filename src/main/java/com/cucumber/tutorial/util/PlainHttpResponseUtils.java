package com.cucumber.tutorial.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import io.jtest.utils.matcher.http.PlainHttpResponse;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PlainHttpResponseUtils {

    public static PlainHttpResponse from(String content) {
        ObjectMapper mapper = new ObjectMapper().setNodeFactory(JsonNodeFactory.withExactBigDecimals(true))
                .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
                .configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true)
                .configure(DeserializationFeature.ACCEPT_FLOAT_AS_INT, false);
        try {
            return mapper.readValue(content, PlainHttpResponse.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static PlainHttpResponse from(ClassicHttpResponse response) {
        Integer status = response.getCode();
        String reasonPhrase = response.getReasonPhrase();
        List<Map.Entry<String, String>> headers = extractHeaders(response);
        String content = null;
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            try {
                content = EntityUtils.toString(entity, StandardCharsets.UTF_8);
            } catch (Exception e) {
                throw new RuntimeException("Cannot extract entity from HTTP Response", e);
            } finally {
                if (content != null) {
                    response.setEntity(new StringEntity(content, StandardCharsets.UTF_8));
                }
            }
        }
        return PlainHttpResponse.Builder.create()
                .status(status)
                .reasonPhrase(reasonPhrase)
                .entity(content)
                .headers(headers).build();
    }

    public static String toOnelineReducedString(PlainHttpResponse response) {
        return response != null ? response.getStatus() + " | "
                + (response.getEntity() != null ? StringUtils.toOnelineReducedString(response.getEntity().toString(), 70) : "N/A")
                : "";
    }

    private static List<Map.Entry<String, String>> extractHeaders(HttpResponse response) {
        List<Map.Entry<String, String>> headers = new ArrayList<>();
        Arrays.stream(response.getHeaders()).forEach(h ->
                headers.add(new AbstractMap.SimpleEntry<>(h.getName(), h.getValue())));
        return headers;
    }
}


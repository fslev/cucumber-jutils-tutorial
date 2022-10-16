package com.cucumber.tutorial.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jtest.utils.matcher.http.PlainHttpResponse;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class PlainHttpResponseUtils {

    public static PlainHttpResponse from(String content) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
        try {
            return mapper.readValue(content, PlainHttpResponse.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static PlainHttpResponse from(CloseableHttpResponse response) {
        String status = String.valueOf(response.getCode());
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
                EntityUtils.consumeQuietly(entity);
                if (content != null) {
                    response.setEntity(new StringEntity(content, StandardCharsets.UTF_8));
                }
            }
        }
        return new PlainHttpResponse(status, reasonPhrase, content, headers);
    }

    private static List<Map.Entry<String, String>> extractHeaders(HttpResponse response) {
        List<Map.Entry<String, String>> headers = new ArrayList<>();
        Arrays.stream(response.getHeaders()).forEach(h ->
                headers.add(new AbstractMap.SimpleEntry<>(h.getName(), h.getValue())));
        return headers;
    }
}


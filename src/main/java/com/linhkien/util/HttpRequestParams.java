package com.linhkien.util;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Tiện ích phân tích tham số từ URL query string và request body (application/x-www-form-urlencoded).
 */
public final class HttpRequestParams {
    private final Map<String, String> params;

    public HttpRequestParams(Map<String, String> params) {
        this.params = params;
    }

    public static HttpRequestParams fromExchange(HttpExchange exchange) throws IOException {
        Map<String, String> map = new HashMap<>();
        String query = exchange.getRequestURI().getRawQuery();
        if (query != null && !query.isBlank()) {
            parseFormEncoded(query, map);
        }
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        if (!body.isBlank()) {
            parseFormEncoded(body, map);
        }
        return new HttpRequestParams(map);
    }

    public static void parseFormEncoded(String raw, Map<String, String> target) {
        if (raw == null || raw.isBlank()) {
            return;
        }
        for (String pair : raw.split("&")) {
            if (pair.isEmpty()) {
                continue;
            }
            String[] parts = pair.split("=", 2);
            String key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
            String value = parts.length == 2 ? URLDecoder.decode(parts[1], StandardCharsets.UTF_8) : "";
            target.put(key, value);
        }
    }

    public String get(String key) {
        return params.get(key);
    }

    public String getOrDefault(String key, String defaultValue) {
        return params.getOrDefault(key, defaultValue);
    }

    public boolean has(String key) {
        return params.containsKey(key) && params.get(key) != null && !params.get(key).isBlank();
    }

    public int getIntOrDefault(String key, int defaultValue) {
        String value = params.get(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Tham số '" + key + "' phải là số nguyên hợp lệ");
        }
    }

    public int requirePositiveInt(String key, String errorMessage) {
        int val = getIntOrDefault(key, 0);
        if (val <= 0) {
            throw new IllegalArgumentException(errorMessage);
        }
        return val;
    }

    public String requireString(String key, String errorMessage) {
        String val = params.get(key);
        if (val == null || val.trim().isEmpty()) {
            throw new IllegalArgumentException(errorMessage);
        }
        return val.trim();
    }
}

package com.linhkien.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Bộ tuần tự hóa JSON đơn giản cho các đối tượng chuẩn, Map, Iterable và Model trong com.linhkien.model.
 */
public final class JsonSerializer {

    private JsonSerializer() {
    }

    public static String toJson(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        }
        if (value instanceof Map<?, ?> map) {
            List<String> entries = new ArrayList<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                entries.add(toJson(String.valueOf(entry.getKey())) + ":" + toJson(entry.getValue()));
            }
            return "{" + String.join(",", entries) + "}";
        }
        if (value instanceof Iterable<?> iterable) {
            List<String> values = new ArrayList<>();
            for (Object item : iterable) {
                values.add(toJson(item));
            }
            return "[" + String.join(",", values) + "]";
        }
        Package pkg = value.getClass().getPackage();
        if (pkg != null && pkg.getName().startsWith("com.linhkien.model")) {
            Map<String, Object> fields = new TreeMap<>();
            for (Method method : value.getClass().getMethods()) {
                if (method.getParameterCount() != 0 || method.getName().equals("getClass")) {
                    continue;
                }
                String name = method.getName();
                String fieldName = null;
                if (name.startsWith("get") && name.length() > 3) {
                    String raw = name.substring(3);
                    fieldName = Character.toLowerCase(raw.charAt(0)) + raw.substring(1);
                } else if (name.startsWith("is") && name.length() > 2
                        && (method.getReturnType() == boolean.class || method.getReturnType() == Boolean.class)) {
                    String raw = name.substring(2);
                    fieldName = Character.toLowerCase(raw.charAt(0)) + raw.substring(1);
                }
                if (fieldName != null) {
                    try {
                        fields.put(fieldName, method.invoke(value));
                    } catch (Exception e) {
                        throw new IllegalStateException("Không thể đọc thuộc tính JSON: " + name, e);
                    }
                }
            }
            return toJson(fields);
        }

        String text = value.toString();
        StringBuilder sb = new StringBuilder("\"");
        for (char c : text.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 32) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.append('"').toString();
    }
}

package com.linhkien.api;

import com.linhkien.util.HttpRequestParams;

/**
 * Hợp đồng chung cho các controller xử lý HTTP API trong prototype server.
 */
public interface ApiController {
    Object UNHANDLED = new Object();

    Object handle(String route, String method, HttpRequestParams params);
}

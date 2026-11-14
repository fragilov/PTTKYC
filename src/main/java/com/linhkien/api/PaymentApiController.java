package com.linhkien.api;

import com.linhkien.model.ThanhToan;
import com.linhkien.service.ThanhToanService;
import com.linhkien.util.HttpRequestParams;

import java.util.Map;

/**
 * Controller API quản lý Giao dịch thanh toán & hoàn tiền (UC20).
 */
public class PaymentApiController implements ApiController {
    private final ThanhToanService paymentService;

    public PaymentApiController(ThanhToanService paymentService) {
        this.paymentService = paymentService;
    }

    @Override
    public Object handle(String route, String method, HttpRequestParams p) {
        if (route.equals("/payments") && method.equals("GET")) {
            int orderId = p.getIntOrDefault("orderId", p.getIntOrDefault("id", 0));
            if (orderId > 0) {
                return paymentService.layGiaoDichTheoDon(orderId);
            }
            return paymentService.layTatCaGiaoDich(p.getIntOrDefault("staffId", 4));
        }
        if (route.equals("/payments/confirm") && method.equals("POST")) {
            int orderId = p.has("orderId")
                ? p.requirePositiveInt("orderId", "Mã đơn hàng không hợp lệ")
                : p.requirePositiveInt("id", "Mã đơn hàng không hợp lệ");
            String reference = p.getOrDefault("reference", "");
            int staffId = p.getIntOrDefault("staffId", 4);
            ThanhToan tt = paymentService.xacNhanThanhToan(orderId, reference, staffId);
            return Map.of("ok", true, "payment", tt);
        }
        if (route.equals("/payments/refund") && method.equals("POST")) {
            int orderId = p.has("orderId")
                ? p.requirePositiveInt("orderId", "Mã đơn hàng không hợp lệ")
                : p.requirePositiveInt("id", "Mã đơn hàng không hợp lệ");
            String reference = p.getOrDefault("reference", "");
            int staffId = p.getIntOrDefault("staffId", 4);
            ThanhToan tt = paymentService.xacNhanHoanTien(orderId, reference, staffId);
            return Map.of("ok", true, "payment", tt);
        }
        return UNHANDLED;
    }
}

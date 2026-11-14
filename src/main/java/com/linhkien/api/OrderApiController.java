package com.linhkien.api;

import com.linhkien.model.DonHang;
import com.linhkien.service.DonHangService;
import com.linhkien.service.ThanhToanService;
import com.linhkien.util.HttpRequestParams;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Controller API quản lý Đơn hàng, Theo dõi giao hàng và Hủy đơn (UC08, UC10, UC14, UC19).
 */
public class OrderApiController implements ApiController {
    private final DonHangService orderService;
    private final ThanhToanService paymentService;

    public OrderApiController(DonHangService orderService, ThanhToanService paymentService) {
        this.orderService = orderService;
        this.paymentService = paymentService;
    }

    @Override
    public Object handle(String route, String method, HttpRequestParams p) {
        if (route.equals("/orders") && method.equals("GET")) {
            if ("true".equals(p.get("admin"))) {
                return orderService.layTatCaDon(p.getIntOrDefault("staffId", 4), p.get("status"));
            }
            return orderService.layDonCuaKhach(p.getIntOrDefault("userId", 1));
        }
        if (route.equals("/orders") && method.equals("POST")) {
            int userId = p.getIntOrDefault("userId", 1);
            return orderService.taoDon(
                userId,
                p.get("name"),
                p.get("phone"),
                p.get("address"),
                p.get("payment"),
                p.get("requestId"),
                p.getOrDefault("note", ""),
                p.getOrDefault("shipping", "standard"),
                p.get("couponCode")
            );
        }
        if (route.equals("/tracking") && method.equals("GET")) {
            int id = p.requirePositiveInt("id", "Mã đơn hàng không hợp lệ");
            int viewerId = "true".equals(p.get("admin"))
                ? p.getIntOrDefault("staffId", 4)
                : p.getIntOrDefault("userId", 1);
            DonHang order = orderService.layChiTietDon(id, viewerId);
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("order", order);
            data.put("history", orderService.layLichSuTrangThai(id));
            data.put("shipping", orderService.layVanChuyen(id));
            data.put("payments", paymentService.layGiaoDichTheoDon(id));
            return data;
        }
        if (route.equals("/status") && method.equals("POST")) {
            int id = p.requirePositiveInt("id", "Mã đơn hàng không hợp lệ");
            orderService.capNhatTrangThai(
                id,
                p.get("status"),
                p.getIntOrDefault("staffId", 4),
                p.getOrDefault("note", "Cập nhật từ prototype UI")
            );
            return Map.of("ok", true);
        }
        if (route.equals("/shipping") && method.equals("POST")) {
            int id = p.requirePositiveInt("id", "Mã đơn hàng không hợp lệ");
            return orderService.banGiaoVanChuyen(
                id,
                p.get("carrier"),
                p.get("tracking"),
                p.getIntOrDefault("staffId", 4)
            );
        }
        if (route.equals("/cancel") && method.equals("POST")) {
            int id = p.requirePositiveInt("id", "Mã đơn hàng không hợp lệ");
            int actorId = "true".equals(p.get("admin"))
                ? p.getIntOrDefault("staffId", 4)
                : p.getIntOrDefault("userId", 1);
            orderService.huyDon(id, actorId, p.getOrDefault("reason", ""));
            DonHang updated = orderService.layChiTietDon(id, actorId);
            return Map.of("ok", true, "order", updated);
        }
        return UNHANDLED;
    }
}

package com.linhkien.api;

import com.linhkien.service.GioHangService;
import com.linhkien.util.HttpRequestParams;

/**
 * Controller API quản lý Giỏ hàng (UC06, UC07).
 */
public class CartApiController implements ApiController {
    private final GioHangService cartService;

    public CartApiController(GioHangService cartService) {
        this.cartService = cartService;
    }

    @Override
    public Object handle(String route, String method, HttpRequestParams p) {
        int userId = p.getIntOrDefault("userId", 1);
        if (route.equals("/cart") && method.equals("GET")) {
            return cartService.layGioHang(userId);
        }
        if (route.equals("/cart") && method.equals("POST")) {
            String action = p.getOrDefault("action", "");
            if ("clear".equals(action)) {
                cartService.lamTrongGio(userId);
                return cartService.layGioHang(userId);
            }
            int productId = p.requirePositiveInt("id", "Mã linh kiện không hợp lệ");
            int qty = p.getIntOrDefault("quantity", 0);
            if ("add".equals(action)) {
                cartService.themVaoGio(userId, productId, qty);
            } else if (qty == 0) {
                cartService.xoaKhoiGio(userId, productId);
            } else {
                cartService.capNhatSoLuong(userId, productId, qty);
            }
            return cartService.layGioHang(userId);
        }
        return UNHANDLED;
    }
}

package com.linhkien.api;

import com.linhkien.model.MaGiamGia;
import com.linhkien.service.GioHangService;
import com.linhkien.service.MaGiamGiaService;
import com.linhkien.util.HttpRequestParams;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Controller API quản lý và kiểm tra mã giảm giá (UC09, UC17).
 */
public class CouponApiController implements ApiController {
    private final MaGiamGiaService couponService;
    private final GioHangService cartService;

    public CouponApiController(MaGiamGiaService couponService, GioHangService cartService) {
        this.couponService = couponService;
        this.cartService = cartService;
    }

    @Override
    public Object handle(String route, String method, HttpRequestParams p) {
        if (route.equals("/coupons") && method.equals("GET")) {
            return couponService.layTatCa();
        }
        if (route.equals("/coupons") && method.equals("POST")) {
            return saveCoupon(p);
        }
        if (route.equals("/coupons") && method.equals("DELETE")) {
            int id = p.requirePositiveInt("id", "Mã giảm giá không hợp lệ");
            int staffId = p.getIntOrDefault("staffId", 4);
            return Map.of("deleted", couponService.xoaMaGiamGia(id, staffId));
        }
        if (route.equals("/coupons/validate") && (method.equals("POST") || method.equals("GET"))) {
            String code = p.requireString("code", "Vui lòng nhập mã giảm giá");
            BigDecimal subtotal;
            if (p.has("subtotal")) {
                subtotal = parseBigDecimal(p.get("subtotal"), "Giá trị đơn hàng không hợp lệ");
            } else {
                int userId = p.getIntOrDefault("userId", 1);
                subtotal = cartService.tinhTongTien(userId);
            }
            MaGiamGia mg = couponService.kiemTraMaGiamGia(code, subtotal);
            BigDecimal discount = mg.tinhSoTienGiam(subtotal);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("valid", true);
            result.put("coupon", mg);
            result.put("discountAmount", discount);
            result.put("finalSubtotal", subtotal.subtract(discount).max(BigDecimal.ZERO));
            return result;
        }
        return UNHANDLED;
    }

    private MaGiamGia saveCoupon(HttpRequestParams p) {
        int id = p.getIntOrDefault("id", 0);
        int staffId = p.getIntOrDefault("staffId", 4);
        MaGiamGia mg = id > 0 ? couponService.layTheoMa(id) : new MaGiamGia();
        if (mg == null) {
            throw new IllegalArgumentException("Không tìm thấy mã giảm giá ID: " + id);
        }
        mg.setMaCode(p.requireString("code", "Mã code giảm giá không được để trống"));
        mg.setLoaiGiam(p.getOrDefault("type", MaGiamGia.SO_TIEN));
        mg.setGiaTriGiam(parseBigDecimal(p.requireString("value", "Giá trị giảm không được để trống"), "Giá trị giảm phải là số hợp lệ"));
        mg.setDonHangToiThieu(p.has("minOrder")
            ? parseBigDecimal(p.get("minOrder"), "Giá trị đơn tối thiểu không hợp lệ")
            : BigDecimal.ZERO);
        if (p.has("maxUses")) {
            int max = p.getIntOrDefault("maxUses", 0);
            mg.setSoLuotToiDa(max > 0 ? max : null);
        } else {
            mg.setSoLuotToiDa(null);
        }
        mg.setNgayBatDau(parseDateTime(p.get("startDate"), false, LocalDateTime.now().minusDays(1)));
        mg.setNgayKetThuc(parseDateTime(p.get("endDate"), true, LocalDateTime.now().plusDays(30)));
        return id == 0 ? couponService.themMaGiamGia(mg, staffId) : couponService.capNhatMaGiamGia(mg, staffId);
    }

    private static BigDecimal parseBigDecimal(String raw, String message) {
        try {
            return new BigDecimal(raw.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(message);
        }
    }

    private static LocalDateTime parseDateTime(String raw, boolean endOfDay, LocalDateTime fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        String trimmed = raw.trim();
        try {
            if (trimmed.length() == 10) {
                LocalDate d = LocalDate.parse(trimmed);
                return endOfDay ? d.atTime(23, 59, 59) : d.atStartOfDay();
            }
            return LocalDateTime.parse(trimmed.replace(' ', 'T'));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Định dạng ngày giờ không hợp lệ: " + raw);
        }
    }
}

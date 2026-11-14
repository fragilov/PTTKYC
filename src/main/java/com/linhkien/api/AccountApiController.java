package com.linhkien.api;

import com.linhkien.model.DiaChiNhanHang;
import com.linhkien.service.DiaChiService;
import com.linhkien.util.HttpRequestParams;

import java.util.Map;

/**
 * Controller API quản lý Sổ địa chỉ nhận hàng và bảo mật tài khoản (FR06).
 */
public class AccountApiController implements ApiController {
    private final DiaChiService diaChiService;

    public AccountApiController(DiaChiService diaChiService) {
        this.diaChiService = diaChiService;
    }

    @Override
    public Object handle(String route, String method, HttpRequestParams p) {
        int userId = p.getIntOrDefault("userId", 1);
        if (route.equals("/addresses") && method.equals("GET")) {
            return diaChiService.layDanhSachDiaChi(userId);
        }
        if (route.equals("/addresses") && method.equals("POST")) {
            String action = p.getOrDefault("action", "");
            if ("setDefault".equals(action)) {
                int id = p.requirePositiveInt("id", "Mã địa chỉ không hợp lệ");
                diaChiService.datDiaChiMacDinh(userId, id);
                return diaChiService.layDanhSachDiaChi(userId);
            }
            DiaChiNhanHang dc = new DiaChiNhanHang();
            dc.setMaDiaChi(p.getIntOrDefault("id", 0));
            dc.setMaNguoiDung(userId);
            dc.setTenNguoiNhan(p.get("name"));
            dc.setSoDienThoaiNhan(p.get("phone"));
            dc.setDiaChiChiTiet(p.get("street"));
            dc.setPhuongXa(p.get("ward"));
            dc.setTinhThanh(p.get("city"));
            dc.setLaMacDinh("true".equals(p.getOrDefault("isDefault", "true")));
            return diaChiService.luuDiaChi(dc);
        }
        if (route.equals("/addresses") && method.equals("DELETE")) {
            int id = p.requirePositiveInt("id", "Mã địa chỉ không hợp lệ");
            return Map.of("deleted", diaChiService.xoaDiaChi(userId, id));
        }
        if (route.equals("/account/password") && method.equals("POST")) {
            diaChiService.doiMatKhau(userId, p.get("currentPassword"), p.get("newPassword"));
            return Map.of("ok", true, "message", "Cập nhật mật khẩu thành công");
        }
        return UNHANDLED;
    }
}

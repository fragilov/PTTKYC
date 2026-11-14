package com.linhkien.api;

import com.linhkien.dao.DanhMucDAO;
import com.linhkien.model.LinhKien;
import com.linhkien.model.ThongSoKyThuat;
import com.linhkien.service.LinhKienService;
import com.linhkien.util.HttpRequestParams;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Controller API quản lý Danh mục, Linh kiện, Thông số kỹ thuật và Tồn kho.
 */
public class ProductApiController implements ApiController {
    private final LinhKienService productService;
    private final DanhMucDAO danhMucDAO;

    public ProductApiController(LinhKienService productService, DanhMucDAO danhMucDAO) {
        this.productService = productService;
        this.danhMucDAO = danhMucDAO;
    }

    @Override
    public Object handle(String route, String method, HttpRequestParams p) {
        if (route.equals("/categories") && method.equals("GET")) {
            return danhMucDAO.findAll();
        }
        if (route.equals("/products") && method.equals("GET")) {
            int id = p.getIntOrDefault("id", 0);
            if (id > 0) {
                LinhKien lk = productService.timTheoMa(id);
                if (lk == null) {
                    throw new IllegalArgumentException("Không tìm thấy linh kiện có mã: " + id);
                }
                return lk;
            }
            if (p.has("q")) {
                return productService.timKiem(p.get("q"));
            }
            return productService.layTatCaLinhKien();
        }
        if (route.equals("/products") && method.equals("POST")) {
            return saveProduct(p);
        }
        if (route.equals("/products") && method.equals("DELETE")) {
            int id = p.requirePositiveInt("id", "Mã linh kiện không hợp lệ");
            return Map.of("deleted", productService.xoaLinhKien(id));
        }
        if (route.equals("/products/low-stock") && method.equals("GET")) {
            return productService.layLinhKienTonThap();
        }
        if (route.equals("/products/specs") && method.equals("GET")) {
            int id = p.requirePositiveInt("id", "Mã linh kiện không hợp lệ");
            return productService.layThongSoKyThuat(id);
        }
        if (route.equals("/products/specs") && method.equals("POST")) {
            int id = p.requirePositiveInt("id", "Mã linh kiện không hợp lệ");
            List<ThongSoKyThuat> specs = parseSpecs(p.getOrDefault("specs", ""));
            return productService.capNhatThongSoKyThuat(id, specs);
        }
        if (route.equals("/inventory/adjust") && method.equals("POST")) {
            int id = p.requirePositiveInt("id", "Mã linh kiện không hợp lệ");
            int delta = p.getIntOrDefault("delta", 0);
            String reason = p.getOrDefault("reason", "");
            int staffId = p.getIntOrDefault("staffId", 4);
            productService.dieuChinhTonKho(id, delta, reason, staffId);
            return productService.timTheoMa(id);
        }
        if (route.equals("/inventory/history") && method.equals("GET")) {
            int id = p.getIntOrDefault("id", 0);
            return productService.layLichSuTonKho(id > 0 ? id : null);
        }
        return UNHANDLED;
    }

    private LinhKien saveProduct(HttpRequestParams p) {
        int id = p.getIntOrDefault("id", 0);
        LinhKien lk = id == 0 ? new LinhKien() : productService.timTheoMa(id);
        if (lk == null) {
            throw new IllegalArgumentException("Không tìm thấy linh kiện");
        }
        lk.setMaSKU(p.get("sku"));
        lk.setTenLinhKien(p.get("name"));
        lk.setMaDanhMuc(p.requirePositiveInt("category", "Danh mục không hợp lệ"));
        try {
            lk.setGiaBan(new BigDecimal(p.requireString("price", "Giá bán không được để trống").trim()));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Giá bán phải là số hợp lệ");
        }
        lk.setSoLuongTon(p.getIntOrDefault("stock", 0));
        lk.setNhaSanXuat(p.getOrDefault("maker", ""));
        if (p.has("packageType")) {
            lk.setKieuDongGoi(p.get("packageType"));
        } else if (lk.getKieuDongGoi() == null || lk.getKieuDongGoi().isBlank()) {
            lk.setKieuDongGoi("MODULE");
        }
        if (p.has("minStock")) {
            lk.setNguongTonToiThieu(p.getIntOrDefault("minStock", 10));
        }
        if (p.has("description")) {
            lk.setMoTa(p.get("description"));
        }
        if (p.has("voltage")) {
            lk.setDienApHoatDong(p.get("voltage"));
        }
        if (p.has("interfaceType")) {
            lk.setGiaoTiep(p.get("interfaceType"));
        }
        if (p.has("datasheet")) {
            lk.setDuongDanDatasheet(p.get("datasheet"));
        }
        if (p.has("image")) {
            lk.setHinhAnh(p.get("image"));
        }
        if (p.has("unit")) {
            lk.setDonViTinh(p.get("unit"));
        }
        if (p.has("status")) {
            lk.setTrangThaiBan(p.get("status"));
        }

        if (id == 0) {
            productService.themLinhKien(lk);
        } else {
            productService.capNhatLinhKien(lk);
        }
        return lk;
    }

    private List<ThongSoKyThuat> parseSpecs(String raw) {
        List<ThongSoKyThuat> list = new ArrayList<>();
        if (raw == null || raw.isBlank()) {
            return list;
        }
        for (String line : raw.split("\\r?\\n")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            String[] parts = trimmed.split("\\|", 3);
            if (parts.length < 2 || parts[0].isBlank() || parts[1].isBlank()) {
                continue;
            }
            String unit = parts.length == 3 ? parts[2].trim() : null;
            list.add(new ThongSoKyThuat(0, parts[0].trim(), parts[1].trim(), unit));
        }
        return list;
    }
}

package com.linhkien.service;

import com.linhkien.dao.DanhMucDAO;
import com.linhkien.dao.DonHangDAO;
import com.linhkien.dao.LinhKienDAO;
import com.linhkien.model.DanhMucLinhKien;
import com.linhkien.model.LichSuTonKho;
import com.linhkien.model.LinhKien;
import com.linhkien.model.ThongSoKyThuat;
import com.linhkien.util.JdbcHelper;

import java.math.BigDecimal;
import java.util.List;

/**
 * Lớp dịch vụ xử lý nghiệp vụ Quản lý Linh kiện điện tử (UC03–UC05, UC12, UC13).
 */
public class LinhKienService {
    private final LinhKienDAO linhKienDAO;
    private final DanhMucDAO danhMucDAO;
    private final DonHangDAO donHangDAO;

    public LinhKienService() {
        this.linhKienDAO = new LinhKienDAO();
        this.danhMucDAO = new DanhMucDAO();
        this.donHangDAO = new DonHangDAO();
    }

    public LinhKienService(LinhKienDAO linhKienDAO, DanhMucDAO danhMucDAO) {
        this.linhKienDAO = linhKienDAO;
        this.danhMucDAO = danhMucDAO;
        this.donHangDAO = new DonHangDAO();
    }

    public LinhKienService(LinhKienDAO linhKienDAO, DanhMucDAO danhMucDAO, DonHangDAO donHangDAO) {
        this.linhKienDAO = linhKienDAO;
        this.danhMucDAO = danhMucDAO;
        this.donHangDAO = donHangDAO;
    }

    /**
     * Lấy toàn bộ danh sách linh kiện.
     */
    public List<LinhKien> layTatCaLinhKien() {
        return linhKienDAO.findAll();
    }

    /**
     * Lấy danh sách linh kiện đang bán.
     */
    public List<LinhKien> layLinhKienDangBan() {
        return linhKienDAO.findAllActive();
    }

    /**
     * Tìm linh kiện theo mã số ID.
     */
    public LinhKien timTheoMa(int maLinhKien) {
        if (maLinhKien <= 0) {
            throw new IllegalArgumentException("Mã linh kiện phải là số nguyên dương");
        }
        return linhKienDAO.findById(maLinhKien);
    }

    /**
     * Tìm linh kiện theo mã SKU.
     */
    public LinhKien timTheoSKU(String maSKU) {
        if (maSKU == null || maSKU.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã SKU không được để trống");
        }
        return linhKienDAO.findBySKU(maSKU.trim());
    }

    /**
     * Tìm kiếm linh kiện theo mã (ID/SKU) hoặc tên linh kiện.
     * @param keyword Từ khóa tìm kiếm
     */
    public List<LinhKien> timKiem(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return layTatCaLinhKien();
        }
        return linhKienDAO.searchByMaOrTen(keyword.trim());
    }

    /**
     * Thêm mới một linh kiện điện tử vào hệ thống (UC12).
     * Kiểm tra các ràng buộc toàn vẹn theo Section 2.6.4:
     * - Mã SKU không rỗng và không bị trùng
     * - Tên linh kiện không để trống
     * - Danh mục phải tồn tại
     * - Giá bán không âm (>= 0)
     * - Số lượng tồn không âm (>= 0)
     */
    public void themLinhKien(LinhKien lk) {
        validateLinhKien(lk);

        // Kiểm tra trùng SKU
        LinhKien existingSKU = linhKienDAO.findBySKU(lk.getMaSKU());
        if (existingSKU != null) {
            throw new IllegalArgumentException(
                String.format("Mã SKU '%s' đã tồn tại trong hệ thống (gán cho linh kiện ID: %d)",
                    lk.getMaSKU(), existingSKU.getMaLinhKien()));
        }

        // Kiểm tra danh mục
        DanhMucLinhKien dm = danhMucDAO.findById(lk.getMaDanhMuc());
        if (dm == null) {
            throw new IllegalArgumentException("Mã danh mục " + lk.getMaDanhMuc() + " không tồn tại");
        }

        boolean success = linhKienDAO.insert(lk);
        if (!success) {
            throw new RuntimeException("Không thể lưu linh kiện vào cơ sở dữ liệu");
        }
    }

    /**
     * Cập nhật thông tin linh kiện điện tử (UC12).
     */
    public void capNhatLinhKien(LinhKien lk) {
        if (lk.getMaLinhKien() <= 0) {
            throw new IllegalArgumentException("Mã linh kiện không hợp lệ để cập nhật");
        }

        LinhKien current = linhKienDAO.findById(lk.getMaLinhKien());
        if (current == null) {
            throw new IllegalArgumentException("Không tìm thấy linh kiện có mã ID: " + lk.getMaLinhKien());
        }

        validateLinhKien(lk);

        // Kiểm tra nếu đổi SKU thì SKU mới không được trùng với linh kiện khác
        if (!current.getMaSKU().equalsIgnoreCase(lk.getMaSKU())) {
            LinhKien duplicateSKU = linhKienDAO.findBySKU(lk.getMaSKU());
            if (duplicateSKU != null && duplicateSKU.getMaLinhKien() != lk.getMaLinhKien()) {
                throw new IllegalArgumentException("Mã SKU '" + lk.getMaSKU() + "' đã thuộc về linh kiện khác");
            }
        }

        // Kiểm tra danh mục
        DanhMucLinhKien dm = danhMucDAO.findById(lk.getMaDanhMuc());
        if (dm == null) {
            throw new IllegalArgumentException("Mã danh mục " + lk.getMaDanhMuc() + " không tồn tại");
        }

        boolean success = linhKienDAO.update(lk);
        if (!success) {
            throw new RuntimeException("Cập nhật linh kiện thất bại trong CSDL");
        }
    }

    /**
     * Xóa linh kiện điện tử (UC12).
     * Theo tài liệu Section 2.6.4: Linh kiện đã bán hoặc phát sinh dữ liệu không xóa vật lý
     * mà chuyển trạng thái sang 'NGUNG_BAN'.
     */
    public boolean xoaLinhKien(int maLinhKien) {
        LinhKien lk = linhKienDAO.findById(maLinhKien);
        if (lk == null) {
            throw new IllegalArgumentException("Không tìm thấy linh kiện có mã ID: " + maLinhKien);
        }
        return linhKienDAO.delete(maLinhKien);
    }

    /**
     * Điều chỉnh tồn kho linh kiện (UC13 - Kiểm kê/Nhập hàng) và ghi nhận nhật ký biến động.
     */
    public void dieuChinhTonKho(int maLinhKien, int mucDieuChinh, String lyDo) {
        dieuChinhTonKho(maLinhKien, mucDieuChinh, lyDo, null);
    }

    public void dieuChinhTonKho(int maLinhKien, int mucDieuChinh, String lyDo, Integer maNguoiThucHien) {
        if (mucDieuChinh == 0) {
            throw new IllegalArgumentException("Mức điều chỉnh tồn kho phải khác 0");
        }
        JdbcHelper.giaoDich(() -> {
            LinhKien lk = linhKienDAO.findById(maLinhKien);
            if (lk == null) {
                throw new IllegalArgumentException("Không tìm thấy linh kiện có mã: " + maLinhKien);
            }
            int tonTruoc = lk.getSoLuongTon();
            lk.dieuChinhTon(mucDieuChinh, lyDo);
            int tonSau = lk.getSoLuongTon();

            boolean ok = linhKienDAO.updateSoLuongTon(maLinhKien, tonSau);
            if (!ok) {
                throw new RuntimeException("Cập nhật số lượng tồn kho trong CSDL thất bại");
            }
            String loai = mucDieuChinh > 0 ? LichSuTonKho.NHAP : LichSuTonKho.DIEU_CHINH;
            String ghiChu = (lyDo != null && !lyDo.isBlank())
                ? lyDo.trim()
                : (mucDieuChinh > 0 ? "Nhập bổ sung tồn kho" : "Điều chỉnh giảm tồn kho");
            donHangDAO.ghiLichSuTonKho(maLinhKien, loai, mucDieuChinh, tonTruoc, tonSau, null, maNguoiThucHien, ghiChu);
            return null;
        });
    }

    /**
     * Lấy nhật ký biến động tồn kho (NFR03). Truyền null để lấy toàn bộ.
     */
    public List<LichSuTonKho> layLichSuTonKho(Integer maLinhKien) {
        return JdbcHelper.chay(() -> linhKienDAO.findLichSuTonKho(maLinhKien));
    }

    /**
     * Lấy danh sách thông số kỹ thuật mở rộng của linh kiện.
     */
    public List<ThongSoKyThuat> layThongSoKyThuat(int maLinhKien) {
        if (maLinhKien <= 0) {
            throw new IllegalArgumentException("Mã linh kiện phải là số nguyên dương");
        }
        return linhKienDAO.findThongSoByLinhKien(maLinhKien);
    }

    /**
     * Cập nhật toàn bộ danh sách thông số kỹ thuật mở rộng cho linh kiện.
     */
    public List<ThongSoKyThuat> capNhatThongSoKyThuat(int maLinhKien, List<ThongSoKyThuat> danhSach) {
        LinhKien lk = linhKienDAO.findById(maLinhKien);
        if (lk == null) {
            throw new IllegalArgumentException("Không tìm thấy linh kiện có mã: " + maLinhKien);
        }
        return JdbcHelper.giaoDich(() -> {
            linhKienDAO.replaceThongSo(maLinhKien, danhSach);
            return linhKienDAO.findThongSoByLinhKien(maLinhKien);
        });
    }

    /**
     * Lấy danh sách linh kiện sắp hết hàng (tồn kho <= ngưỡng tối thiểu).
     */
    public List<LinhKien> layLinhKienTonThap() {
        return linhKienDAO.findLowStock();
    }

    private void validateLinhKien(LinhKien lk) {
        if (lk == null) {
            throw new IllegalArgumentException("Dữ liệu linh kiện không được để trống");
        }
        if (lk.getMaSKU() == null || lk.getMaSKU().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã SKU không được để trống");
        }
        if (lk.getTenLinhKien() == null || lk.getTenLinhKien().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên linh kiện không được để trống");
        }
        if (lk.getGiaBan() == null || lk.getGiaBan().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Giá bán không được âm (>= 0)");
        }
        if (lk.getSoLuongTon() < 0) {
            throw new IllegalArgumentException("Số lượng tồn kho không được âm (>= 0)");
        }
        if (lk.getNguongTonToiThieu() < 0) {
            throw new IllegalArgumentException("Ngưỡng tồn tối thiểu không được âm (>= 0)");
        }
    }
}

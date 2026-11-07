package com.linhkien.service;

import com.linhkien.dao.GioHangDAO;
import com.linhkien.dao.LinhKienDAO;
import com.linhkien.model.ChiTietGioHang;
import com.linhkien.model.GioHang;
import com.linhkien.model.LinhKien;

import java.math.BigDecimal;

/**
 * Lớp dịch vụ xử lý nghiệp vụ Giỏ hàng (UC06, UC06.1, UC06.2).
 */
public class GioHangService {
    private final GioHangDAO gioHangDAO;
    private final LinhKienDAO linhKienDAO;

    public GioHangService() {
        this.gioHangDAO = new GioHangDAO();
        this.linhKienDAO = new LinhKienDAO();
    }

    public GioHangService(GioHangDAO gioHangDAO, LinhKienDAO linhKienDAO) {
        this.gioHangDAO = gioHangDAO;
        this.linhKienDAO = linhKienDAO;
    }

    /**
     * Lấy thông tin giỏ hàng hiện tại của người dùng.
     * @param maNguoiDung Mã khách hàng
     * @return Đối tượng GioHang chứa đầy đủ các mặt hàng đã chọn
     */
    public GioHang layGioHang(int maNguoiDung) {
        if (maNguoiDung <= 0) {
            throw new IllegalArgumentException("Mã người dùng không hợp lệ");
        }
        return gioHangDAO.getOrCreateGioHangByNguoiDung(maNguoiDung);
    }

    /**
     * Thêm linh kiện vào giỏ hàng (UC06.1 - CV07.1 step 2).
     * Kiểm tra các điều kiện nghiệp vụ:
     * - Số lượng thêm phải > 0
     * - Linh kiện phải tồn tại và đang mở bán ('DANG_BAN')
     * - Tổng số lượng trong giỏ không vượt quá số lượng tồn kho hiện tại
     * @param maNguoiDung Mã người dùng
     * @param maLinhKien Mã linh kiện cần mua
     * @param soLuong Số lượng muốn thêm
     */
    public void themVaoGio(int maNguoiDung, int maLinhKien, int soLuong) {
        if (soLuong <= 0) {
            throw new IllegalArgumentException("Số lượng thêm vào giỏ phải lớn hơn 0");
        }

        // 1. Kiểm tra linh kiện
        LinhKien lk = linhKienDAO.findById(maLinhKien);
        if (lk == null) {
            throw new IllegalArgumentException("Không tìm thấy linh kiện với mã ID: " + maLinhKien);
        }

        if (!"DANG_BAN".equalsIgnoreCase(lk.getTrangThaiBan())) {
            throw new IllegalStateException(
                String.format("Linh kiện '%s' hiện đang ngừng bán, không thể thêm vào giỏ hàng", lk.getTenLinhKien()));
        }

        if (lk.getSoLuongTon() <= 0) {
            throw new IllegalStateException(
                String.format("Linh kiện '%s' hiện đã hết hàng trong kho", lk.getTenLinhKien()));
        }

        // 2. Lấy giỏ hàng
        GioHang gioHang = layGioHang(maNguoiDung);

        // 3. Kiểm tra số lượng hiện có trong giỏ
        ChiTietGioHang existingLine = gioHang.timDongTheoMa(maLinhKien);
        int currentInCart = existingLine != null ? existingLine.getSoLuong() : 0;
        int totalRequested = currentInCart + soLuong;

        if (totalRequested > lk.getSoLuongTon()) {
            throw new IllegalStateException(
                String.format("Không đủ số lượng tồn kho cho linh kiện '%s'. Trong kho còn: %d, trong giỏ đã có: %d, bạn vừa chọn thêm: %d (Tổng: %d)",
                    lk.getTenLinhKien(), lk.getSoLuongTon(), currentInCart, soLuong, totalRequested));
        }

        // 4. Lưu vào CSDL
        boolean ok = gioHangDAO.themVaoGio(gioHang.getMaGioHang(), maLinhKien, soLuong);
        if (!ok) {
            throw new RuntimeException("Lưu sản phẩm vào giỏ hàng thất bại");
        }
    }

    /**
     * Cập nhật số lượng của một linh kiện trong giỏ hàng (UC06.2).
     * @param maNguoiDung Mã người dùng
     * @param maLinhKien Mã linh kiện cần đổi số lượng
     * @param soLuongMoi Số lượng mới (nếu <= 0 sẽ tự động xóa khỏi giỏ)
     */
    public void capNhatSoLuong(int maNguoiDung, int maLinhKien, int soLuongMoi) {
        GioHang gioHang = layGioHang(maNguoiDung);

        ChiTietGioHang existingLine = gioHang.timDongTheoMa(maLinhKien);
        if (existingLine == null) {
            throw new IllegalArgumentException("Sản phẩm mã " + maLinhKien + " không có trong giỏ hàng");
        }

        if (soLuongMoi <= 0) {
            // Xóa khỏi giỏ nếu số lượng <= 0
            xoaKhoiGio(maNguoiDung, maLinhKien);
            return;
        }

        LinhKien lk = linhKienDAO.findById(maLinhKien);
        if (lk != null && soLuongMoi > lk.getSoLuongTon()) {
            throw new IllegalStateException(
                String.format("Số lượng cập nhật (%d) vượt quá tồn kho hiện có (%d) của '%s'",
                    soLuongMoi, lk.getSoLuongTon(), lk.getTenLinhKien()));
        }

        boolean ok = gioHangDAO.capNhatSoLuong(gioHang.getMaGioHang(), maLinhKien, soLuongMoi);
        if (!ok) {
            throw new RuntimeException("Cập nhật số lượng trong giỏ hàng thất bại");
        }
    }

    /**
     * Xóa một linh kiện khỏi giỏ hàng.
     * @param maNguoiDung Mã người dùng
     * @param maLinhKien Mã linh kiện cần xóa
     */
    public void xoaKhoiGio(int maNguoiDung, int maLinhKien) {
        GioHang gioHang = layGioHang(maNguoiDung);
        boolean ok = gioHangDAO.xoaKhoiGio(gioHang.getMaGioHang(), maLinhKien);
        if (!ok) {
            throw new RuntimeException("Xóa linh kiện khỏi giỏ hàng thất bại");
        }
    }

    /**
     * Làm trống toàn bộ giỏ hàng.
     * @param maNguoiDung Mã người dùng
     */
    public void lamTrongGio(int maNguoiDung) {
        GioHang gioHang = layGioHang(maNguoiDung);
        boolean ok = gioHangDAO.xoaToanBoGio(gioHang.getMaGioHang());
        if (!ok) {
            throw new RuntimeException("Làm trống giỏ hàng thất bại");
        }
    }

    /**
     * Tính tổng tiền giỏ hàng (tổng số tiền tạm tính của tất cả sản phẩm).
     * @param maNguoiDung Mã người dùng
     * @return Số tiền tổng (BigDecimal)
     */
    public BigDecimal tinhTongTien(int maNguoiDung) {
        GioHang gioHang = layGioHang(maNguoiDung);
        return gioHang.tinhTamTinh();
    }
}

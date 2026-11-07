package com.linhkien.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp đại diện cho Giỏ hàng theo Class Diagram và bảng GioHang trong CSDL.
 */
public class GioHang {
    private int maGioHang;
    private int maNguoiDung;
    private LocalDateTime ngayTao;
    private LocalDateTime ngayCapNhat;

    // Quan hệ Composition: GioHang ◆ ChiTietGioHang (1 - 0..*)
    private List<ChiTietGioHang> danhSachChiTiet = new ArrayList<>();

    public GioHang() {
        this.ngayTao = LocalDateTime.now();
        this.ngayCapNhat = LocalDateTime.now();
    }

    public GioHang(int maGioHang, int maNguoiDung) {
        this();
        this.maGioHang = maGioHang;
        this.maNguoiDung = maNguoiDung;
    }

    // ==========================================
    // CÁC HÀM NGHIỆP VỤ (Theo Class Diagram)
    // ==========================================

    /**
     * Thêm linh kiện vào giỏ hàng (UC06.1).
     * Nếu linh kiện đã có trong giỏ thì cộng dồn số lượng.
     * @param maLinhKien Mã linh kiện
     * @param soLuong Số lượng thêm
     */
    public void themVaoGio(int maLinhKien, int soLuong) {
        if (soLuong <= 0) {
            throw new IllegalArgumentException("Số lượng thêm vào giỏ phải lớn hơn 0");
        }
        ChiTietGioHang line = timDongTheoMa(maLinhKien);
        if (line != null) {
            line.setSoLuong(line.getSoLuong() + soLuong);
        } else {
            ChiTietGioHang newLine = new ChiTietGioHang(this.maGioHang, maLinhKien, soLuong);
            this.danhSachChiTiet.add(newLine);
        }
        this.ngayCapNhat = LocalDateTime.now();
    }

    /**
     * Thêm linh kiện (có đầy đủ đối tượng LinhKien) vào giỏ hàng.
     * @param linhKien Đối tượng LinhKien
     * @param soLuong Số lượng thêm
     */
    public void themVaoGio(LinhKien linhKien, int soLuong) {
        if (linhKien == null) {
            throw new IllegalArgumentException("Linh kiện không thể là null");
        }
        if (soLuong <= 0) {
            throw new IllegalArgumentException("Số lượng thêm vào giỏ phải lớn hơn 0");
        }
        ChiTietGioHang line = timDongTheoMa(linhKien.getMaLinhKien());
        if (line != null) {
            line.setSoLuong(line.getSoLuong() + soLuong);
            line.setLinhKien(linhKien);
        } else {
            ChiTietGioHang newLine = new ChiTietGioHang(this.maGioHang, linhKien, soLuong);
            this.danhSachChiTiet.add(newLine);
        }
        this.ngayCapNhat = LocalDateTime.now();
    }

    /**
     * Cập nhật số lượng của một dòng trong giỏ (UC06.2).
     * @param maLinhKien Mã linh kiện cần cập nhật
     * @param soLuong Số lượng mới (nếu <= 0 sẽ tự động xóa dòng)
     */
    public void capNhatDongGio(int maLinhKien, int soLuong) {
        if (soLuong <= 0) {
            xoaDongGio(maLinhKien);
            return;
        }
        ChiTietGioHang line = timDongTheoMa(maLinhKien);
        if (line != null) {
            line.setSoLuong(soLuong);
            this.ngayCapNhat = LocalDateTime.now();
        } else {
            throw new IllegalArgumentException("Không tìm thấy linh kiện mã " + maLinhKien + " trong giỏ hàng");
        }
    }

    /**
     * Xóa một dòng linh kiện khỏi giỏ hàng.
     * @param maLinhKien Mã linh kiện cần xóa
     */
    public void xoaDongGio(int maLinhKien) {
        boolean removed = this.danhSachChiTiet.removeIf(item -> item.getMaLinhKien() == maLinhKien);
        if (removed) {
            this.ngayCapNhat = LocalDateTime.now();
        }
    }

    /**
     * Tính tạm tính tổng tiền giỏ hàng (tổng tiền các món trước phí ship và giảm giá).
     * @return Tổng số tiền giỏ hàng (BigDecimal)
     */
    public BigDecimal tinhTamTinh() {
        BigDecimal tong = BigDecimal.ZERO;
        for (ChiTietGioHang line : this.danhSachChiTiet) {
            tong = tong.add(line.tinhThanhTien());
        }
        return tong;
    }

    /**
     * Làm trống toàn bộ giỏ hàng (sau khi thanh toán hoặc khi người dùng dọn sạch giỏ).
     */
    public void lamTrong() {
        this.danhSachChiTiet.clear();
        this.ngayCapNhat = LocalDateTime.now();
    }

    /**
     * Tìm dòng chi tiết trong giỏ theo mã linh kiện.
     */
    public ChiTietGioHang timDongTheoMa(int maLinhKien) {
        for (ChiTietGioHang line : this.danhSachChiTiet) {
            if (line.getMaLinhKien() == maLinhKien) {
                return line;
            }
        }
        return null;
    }

    /**
     * Lấy tổng số lượng sản phẩm trong giỏ hàng.
     */
    public int getTongSoLuong() {
        int total = 0;
        for (ChiTietGioHang line : this.danhSachChiTiet) {
            total += line.getSoLuong();
        }
        return total;
    }

    // ==========================================
    // GETTERS VÀ SETTERS
    // ==========================================

    public int getMaGioHang() {
        return maGioHang;
    }

    public void setMaGioHang(int maGioHang) {
        this.maGioHang = maGioHang;
    }

    public int getMaNguoiDung() {
        return maNguoiDung;
    }

    public void setMaNguoiDung(int maNguoiDung) {
        this.maNguoiDung = maNguoiDung;
    }

    public LocalDateTime getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(LocalDateTime ngayTao) {
        this.ngayTao = ngayTao;
    }

    public LocalDateTime getNgayCapNhat() {
        return ngayCapNhat;
    }

    public void setNgayCapNhat(LocalDateTime ngayCapNhat) {
        this.ngayCapNhat = ngayCapNhat;
    }

    public List<ChiTietGioHang> getDanhSachChiTiet() {
        return danhSachChiTiet;
    }

    public void setDanhSachChiTiet(List<ChiTietGioHang> danhSachChiTiet) {
        this.danhSachChiTiet = danhSachChiTiet != null ? danhSachChiTiet : new ArrayList<>();
    }
}

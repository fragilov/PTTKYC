package com.linhkien.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Lớp đại diện cho một dòng chi tiết trong giỏ hàng theo Class Diagram và bảng ChiTietGioHang.
 */
public class ChiTietGioHang {
    private int maGioHang;
    private int maLinhKien;
    private int soLuong;
    private LocalDateTime ngayThem;

    // Thuộc tính liên kết đối tượng để truy xuất thông tin linh kiện thuận tiện
    private LinhKien linhKien;

    public ChiTietGioHang() {
        this.soLuong = 1;
        this.ngayThem = LocalDateTime.now();
    }

    public ChiTietGioHang(int maGioHang, int maLinhKien, int soLuong) {
        this.maGioHang = maGioHang;
        this.maLinhKien = maLinhKien;
        this.soLuong = soLuong;
        this.ngayThem = LocalDateTime.now();
    }

    public ChiTietGioHang(int maGioHang, LinhKien linhKien, int soLuong) {
        this.maGioHang = maGioHang;
        if (linhKien != null) {
            this.maLinhKien = linhKien.getMaLinhKien();
            this.linhKien = linhKien;
        }
        this.soLuong = soLuong;
        this.ngayThem = LocalDateTime.now();
    }

    // ==========================================
    // NGHIỆP VỤ (Theo Class Diagram)
    // ==========================================

    /**
     * Tính thành tiền của dòng chi tiết giỏ hàng.
     * Thành tiền = Số lượng * Đơn giá linh kiện
     * @return Số tiền thành tiền (BigDecimal)
     */
    public BigDecimal tinhThanhTien() {
        if (linhKien == null || linhKien.getGiaBan() == null) {
            return BigDecimal.ZERO;
        }
        return linhKien.getGiaBan().multiply(BigDecimal.valueOf(this.soLuong));
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

    public int getMaLinhKien() {
        return maLinhKien;
    }

    public void setMaLinhKien(int maLinhKien) {
        this.maLinhKien = maLinhKien;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        if (soLuong <= 0) {
            throw new IllegalArgumentException("Số lượng trong giỏ hàng phải lớn hơn 0");
        }
        this.soLuong = soLuong;
    }

    public LocalDateTime getNgayThem() {
        return ngayThem;
    }

    public void setNgayThem(LocalDateTime ngayThem) {
        this.ngayThem = ngayThem;
    }

    public LinhKien getLinhKien() {
        return linhKien;
    }

    public void setLinhKien(LinhKien linhKien) {
        this.linhKien = linhKien;
        if (linhKien != null) {
            this.maLinhKien = linhKien.getMaLinhKien();
        }
    }

    @Override
    public String toString() {
        String ten = (linhKien != null) ? linhKien.getTenLinhKien() : "Mã LK: " + maLinhKien;
        BigDecimal donGia = (linhKien != null) ? linhKien.getGiaBan() : BigDecimal.ZERO;
        return String.format("%s | Số lượng: %d | Đơn giá: %,d VNĐ | Thành tiền: %,d VNĐ",
            ten, soLuong, donGia.longValue(), tinhThanhTien().longValue());
    }
}

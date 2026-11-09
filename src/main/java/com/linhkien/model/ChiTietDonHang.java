package com.linhkien.model;

import java.math.BigDecimal;

/**
 * Lớp đại diện cho một dòng chi tiết của đơn hàng theo Class Diagram và bảng ChiTietDonHang.
 * Đơn giá được chốt tại thời điểm đặt hàng nên không đổi khi giá linh kiện thay đổi sau này.
 */
public class ChiTietDonHang {
    private int maDonHang;
    private int maLinhKien;
    private int soLuong;
    private BigDecimal donGia;

    // Thuộc tính hiển thị lấy từ bảng LinhKien
    private String maSKU;
    private String tenLinhKien;
    private String donViTinh;

    public ChiTietDonHang() {
        this.donGia = BigDecimal.ZERO;
    }

    public ChiTietDonHang(int maLinhKien, int soLuong, BigDecimal donGia) {
        this.maLinhKien = maLinhKien;
        this.soLuong = soLuong;
        this.donGia = donGia;
    }

    /**
     * Thành tiền của dòng = Số lượng * Đơn giá đã chốt.
     */
    public BigDecimal tinhThanhTien() {
        if (donGia == null) {
            return BigDecimal.ZERO;
        }
        return donGia.multiply(BigDecimal.valueOf(soLuong));
    }

    public int getMaDonHang() {
        return maDonHang;
    }

    public void setMaDonHang(int maDonHang) {
        this.maDonHang = maDonHang;
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
        this.soLuong = soLuong;
    }

    public BigDecimal getDonGia() {
        return donGia;
    }

    public void setDonGia(BigDecimal donGia) {
        this.donGia = donGia;
    }

    public String getMaSKU() {
        return maSKU;
    }

    public void setMaSKU(String maSKU) {
        this.maSKU = maSKU;
    }

    public String getTenLinhKien() {
        return tenLinhKien;
    }

    public void setTenLinhKien(String tenLinhKien) {
        this.tenLinhKien = tenLinhKien;
    }

    public String getDonViTinh() {
        return donViTinh;
    }

    public void setDonViTinh(String donViTinh) {
        this.donViTinh = donViTinh;
    }
}

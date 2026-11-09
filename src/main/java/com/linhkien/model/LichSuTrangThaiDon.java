package com.linhkien.model;

import java.time.LocalDateTime;

/**
 * Dòng nhật ký thay đổi trạng thái đơn hàng (Audit Trail - NFR03).
 */
public class LichSuTrangThaiDon {
    private long maLichSu;
    private int maDonHang;
    private String trangThaiCu;
    private String trangThaiMoi;
    private Integer maNguoiThucHien;
    private String tenNguoiThucHien;
    private LocalDateTime thoiDiem;
    private String ghiChu;

    public long getMaLichSu() {
        return maLichSu;
    }

    public void setMaLichSu(long maLichSu) {
        this.maLichSu = maLichSu;
    }

    public int getMaDonHang() {
        return maDonHang;
    }

    public void setMaDonHang(int maDonHang) {
        this.maDonHang = maDonHang;
    }

    public String getTrangThaiCu() {
        return trangThaiCu;
    }

    public void setTrangThaiCu(String trangThaiCu) {
        this.trangThaiCu = trangThaiCu;
    }

    public String getTrangThaiMoi() {
        return trangThaiMoi;
    }

    public void setTrangThaiMoi(String trangThaiMoi) {
        this.trangThaiMoi = trangThaiMoi;
    }

    public Integer getMaNguoiThucHien() {
        return maNguoiThucHien;
    }

    public void setMaNguoiThucHien(Integer maNguoiThucHien) {
        this.maNguoiThucHien = maNguoiThucHien;
    }

    public String getTenNguoiThucHien() {
        return tenNguoiThucHien;
    }

    public void setTenNguoiThucHien(String tenNguoiThucHien) {
        this.tenNguoiThucHien = tenNguoiThucHien;
    }

    public LocalDateTime getThoiDiem() {
        return thoiDiem;
    }

    public void setThoiDiem(LocalDateTime thoiDiem) {
        this.thoiDiem = thoiDiem;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
}

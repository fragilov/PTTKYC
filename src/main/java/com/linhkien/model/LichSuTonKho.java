package com.linhkien.model;

import java.time.LocalDateTime;

/**
 * Dòng nhật ký biến động tồn kho (Audit Trail - NFR03, UC13 - bảng LichSuTonKho).
 * Loại biến động: NHAP, BAN, HOAN_HUY, DIEU_CHINH.
 */
public class LichSuTonKho {
    public static final String NHAP = "NHAP";
    public static final String BAN = "BAN";
    public static final String HOAN_HUY = "HOAN_HUY";
    public static final String DIEU_CHINH = "DIEU_CHINH";

    private long maLichSu;
    private int maLinhKien;
    private String tenLinhKien;
    private String maSKU;
    private String loaiBienDong;
    private int soLuongThayDoi;
    private int tonTruoc;
    private int tonSau;
    private Integer maDonHang;
    private Integer maNguoiThucHien;
    private String tenNguoiThucHien;
    private String lyDo;
    private LocalDateTime thoiDiem;

    public long getMaLichSu() {
        return maLichSu;
    }

    public void setMaLichSu(long maLichSu) {
        this.maLichSu = maLichSu;
    }

    public int getMaLinhKien() {
        return maLinhKien;
    }

    public void setMaLinhKien(int maLinhKien) {
        this.maLinhKien = maLinhKien;
    }

    public String getTenLinhKien() {
        return tenLinhKien;
    }

    public void setTenLinhKien(String tenLinhKien) {
        this.tenLinhKien = tenLinhKien;
    }

    public String getMaSKU() {
        return maSKU;
    }

    public void setMaSKU(String maSKU) {
        this.maSKU = maSKU;
    }

    public String getLoaiBienDong() {
        return loaiBienDong;
    }

    public void setLoaiBienDong(String loaiBienDong) {
        this.loaiBienDong = loaiBienDong;
    }

    public int getSoLuongThayDoi() {
        return soLuongThayDoi;
    }

    public void setSoLuongThayDoi(int soLuongThayDoi) {
        this.soLuongThayDoi = soLuongThayDoi;
    }

    public int getTonTruoc() {
        return tonTruoc;
    }

    public void setTonTruoc(int tonTruoc) {
        this.tonTruoc = tonTruoc;
    }

    public int getTonSau() {
        return tonSau;
    }

    public void setTonSau(int tonSau) {
        this.tonSau = tonSau;
    }

    public Integer getMaDonHang() {
        return maDonHang;
    }

    public void setMaDonHang(Integer maDonHang) {
        this.maDonHang = maDonHang;
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

    public String getLyDo() {
        return lyDo;
    }

    public void setLyDo(String lyDo) {
        this.lyDo = lyDo;
    }

    public LocalDateTime getThoiDiem() {
        return thoiDiem;
    }

    public void setThoiDiem(LocalDateTime thoiDiem) {
        this.thoiDiem = thoiDiem;
    }
}

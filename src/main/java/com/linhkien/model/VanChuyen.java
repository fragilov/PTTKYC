package com.linhkien.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Lớp đại diện cho thông tin vận chuyển của đơn hàng theo Class Diagram và bảng VanChuyen (UC11, UC14).
 * Trạng thái giao: CHO_LAY_HANG, DANG_GIAO, DA_GIAO, GIAO_THAT_BAI, HOAN_HANG.
 */
public class VanChuyen {
    private int maVanChuyen;
    private int maDonHang;
    private String donViVanChuyen;
    private String maVanDon;
    private String trangThaiGiao;
    private LocalDateTime ngayBanGiao;
    private LocalDate ngayGiaoDuKien;
    private LocalDateTime ngayGiaoThucTe;
    private boolean daThuCOD;
    private String ghiChu;

    public VanChuyen() {
        this.trangThaiGiao = "CHO_LAY_HANG";
    }

    public VanChuyen(int maDonHang, String donViVanChuyen, String maVanDon) {
        this();
        this.maDonHang = maDonHang;
        this.donViVanChuyen = donViVanChuyen;
        this.maVanDon = maVanDon;
    }

    public int getMaVanChuyen() {
        return maVanChuyen;
    }

    public void setMaVanChuyen(int maVanChuyen) {
        this.maVanChuyen = maVanChuyen;
    }

    public int getMaDonHang() {
        return maDonHang;
    }

    public void setMaDonHang(int maDonHang) {
        this.maDonHang = maDonHang;
    }

    public String getDonViVanChuyen() {
        return donViVanChuyen;
    }

    public void setDonViVanChuyen(String donViVanChuyen) {
        this.donViVanChuyen = donViVanChuyen;
    }

    public String getMaVanDon() {
        return maVanDon;
    }

    public void setMaVanDon(String maVanDon) {
        this.maVanDon = maVanDon;
    }

    public String getTrangThaiGiao() {
        return trangThaiGiao;
    }

    public void setTrangThaiGiao(String trangThaiGiao) {
        this.trangThaiGiao = trangThaiGiao;
    }

    public LocalDateTime getNgayBanGiao() {
        return ngayBanGiao;
    }

    public void setNgayBanGiao(LocalDateTime ngayBanGiao) {
        this.ngayBanGiao = ngayBanGiao;
    }

    public LocalDate getNgayGiaoDuKien() {
        return ngayGiaoDuKien;
    }

    public void setNgayGiaoDuKien(LocalDate ngayGiaoDuKien) {
        this.ngayGiaoDuKien = ngayGiaoDuKien;
    }

    public LocalDateTime getNgayGiaoThucTe() {
        return ngayGiaoThucTe;
    }

    public void setNgayGiaoThucTe(LocalDateTime ngayGiaoThucTe) {
        this.ngayGiaoThucTe = ngayGiaoThucTe;
    }

    public boolean isDaThuCOD() {
        return daThuCOD;
    }

    public void setDaThuCOD(boolean daThuCOD) {
        this.daThuCOD = daThuCOD;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
}

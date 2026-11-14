package com.linhkien.model;

/**
 * Thông số kỹ thuật mở rộng dạng cặp tên - giá trị của linh kiện (NFR06 - bảng ThongSoKyThuat).
 */
public class ThongSoKyThuat {
    private int maThongSo;
    private int maLinhKien;
    private String tenThongSo;
    private String giaTri;
    private String donVi;

    public ThongSoKyThuat() {
    }

    public ThongSoKyThuat(int maLinhKien, String tenThongSo, String giaTri, String donVi) {
        this.maLinhKien = maLinhKien;
        this.tenThongSo = tenThongSo;
        this.giaTri = giaTri;
        this.donVi = donVi;
    }

    public int getMaThongSo() {
        return maThongSo;
    }

    public void setMaThongSo(int maThongSo) {
        this.maThongSo = maThongSo;
    }

    public int getMaLinhKien() {
        return maLinhKien;
    }

    public void setMaLinhKien(int maLinhKien) {
        this.maLinhKien = maLinhKien;
    }

    public String getTenThongSo() {
        return tenThongSo;
    }

    public void setTenThongSo(String tenThongSo) {
        this.tenThongSo = tenThongSo;
    }

    public String getGiaTri() {
        return giaTri;
    }

    public void setGiaTri(String giaTri) {
        this.giaTri = giaTri;
    }

    public String getDonVi() {
        return donVi;
    }

    public void setDonVi(String donVi) {
        this.donVi = donVi;
    }
}

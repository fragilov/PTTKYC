package com.linhkien.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Lớp đại diện cho Danh mục linh kiện theo thiết kế Section 2.6.
 * Bảng CSDL: DanhMucLinhKien
 */
public class DanhMucLinhKien {
    private int maDanhMuc;
    private String tenDanhMuc;
    private Integer maDanhMucCha;
    private String moTa;
    private String trangThai; // "HIEN", "AN"

    // Danh sách quan hệ trong mô hình hướng đối tượng
    private List<DanhMucLinhKien> danhMucCon = new ArrayList<>();
    private List<LinhKien> danhSachLinhKien = new ArrayList<>();

    public DanhMucLinhKien() {
        this.trangThai = "HIEN";
    }

    public DanhMucLinhKien(int maDanhMuc, String tenDanhMuc, Integer maDanhMucCha, String moTa, String trangThai) {
        this.maDanhMuc = maDanhMuc;
        this.tenDanhMuc = tenDanhMuc;
        this.maDanhMucCha = maDanhMucCha;
        this.moTa = moTa;
        this.trangThai = trangThai != null ? trangThai : "HIEN";
    }

    public DanhMucLinhKien(String tenDanhMuc, Integer maDanhMucCha, String moTa) {
        this.tenDanhMuc = tenDanhMuc;
        this.maDanhMucCha = maDanhMucCha;
        this.moTa = moTa;
        this.trangThai = "HIEN";
    }

    // Nghiệp vụ theo Class Diagram
    public void themDanhMucCon(DanhMucLinhKien dm) {
        if (dm != null) {
            dm.setMaDanhMucCha(this.maDanhMuc);
            this.danhMucCon.add(dm);
        }
    }

    public List<LinhKien> layDanhSachLinhKien() {
        return this.danhSachLinhKien;
    }

    // Getters and Setters
    public int getMaDanhMuc() {
        return maDanhMuc;
    }

    public void setMaDanhMuc(int maDanhMuc) {
        this.maDanhMuc = maDanhMuc;
    }

    public String getTenDanhMuc() {
        return tenDanhMuc;
    }

    public void setTenDanhMuc(String tenDanhMuc) {
        this.tenDanhMuc = tenDanhMuc;
    }

    public Integer getMaDanhMucCha() {
        return maDanhMucCha;
    }

    public void setMaDanhMucCha(Integer maDanhMucCha) {
        this.maDanhMucCha = maDanhMucCha;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public List<DanhMucLinhKien> getDanhMucCon() {
        return danhMucCon;
    }

    public void setDanhMucCon(List<DanhMucLinhKien> danhMucCon) {
        this.danhMucCon = danhMucCon;
    }

    public List<LinhKien> getDanhSachLinhKien() {
        return danhSachLinhKien;
    }

    public void setDanhSachLinhKien(List<LinhKien> danhSachLinhKien) {
        this.danhSachLinhKien = danhSachLinhKien;
    }

    @Override
    public String toString() {
        return String.format("[%d] %s", maDanhMuc, tenDanhMuc);
    }
}

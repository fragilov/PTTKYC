package com.linhkien.model;

import java.time.LocalDateTime;

/**
 * Lớp trừu tượng đại diện cho Tài khoản người dùng trong hệ thống (Section 2.6).
 * Bảng CSDL: NguoiDung
 */
public class NguoiDung {
    protected int maNguoiDung;
    protected String hoTen;
    protected String email;
    protected String soDienThoai;
    protected String matKhauHash;
    protected String vaiTro; // 'KHACH_HANG', 'NV_BAN_HANG', 'NV_KHO', 'QUAN_TRI'
    protected String trangThai; // 'HOAT_DONG', 'BI_KHOA'
    protected int soLanDangNhapSai;
    protected LocalDateTime khoaDen;
    protected LocalDateTime ngayTao;

    public NguoiDung() {
        this.vaiTro = "KHACH_HANG";
        this.trangThai = "HOAT_DONG";
        this.soLanDangNhapSai = 0;
        this.ngayTao = LocalDateTime.now();
    }

    public NguoiDung(int maNguoiDung, String hoTen, String email, String soDienThoai, String vaiTro) {
        this();
        this.maNguoiDung = maNguoiDung;
        this.hoTen = hoTen;
        this.email = email;
        this.soDienThoai = soDienThoai;
        this.vaiTro = vaiTro != null ? vaiTro : "KHACH_HANG";
    }

    // Các hàm nghiệp vụ theo Class Diagram
    public boolean dangNhap(String email, String matKhau) {
        if (!"HOAT_DONG".equalsIgnoreCase(this.trangThai)) {
            return false;
        }
        return this.email.equalsIgnoreCase(email);
    }

    public void doiMatKhau(String matKhauCu, String matKhauMoi) {
        this.matKhauHash = matKhauMoi;
    }

    public boolean kiemTraQuyen(String chucNang) {
        if ("QUAN_TRI".equalsIgnoreCase(this.vaiTro)) {
            return true;
        }
        if ("NV_BAN_HANG".equalsIgnoreCase(this.vaiTro) && "BAN_HANG".equalsIgnoreCase(chucNang)) {
            return true;
        }
        if ("NV_KHO".equalsIgnoreCase(this.vaiTro) && "KHO".equalsIgnoreCase(chucNang)) {
            return true;
        }
        return "KHACH_HANG".equalsIgnoreCase(this.vaiTro) && "MUA_HANG".equalsIgnoreCase(chucNang);
    }

    // Getters and Setters
    public int getMaNguoiDung() {
        return maNguoiDung;
    }

    public void setMaNguoiDung(int maNguoiDung) {
        this.maNguoiDung = maNguoiDung;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getMatKhauHash() {
        return matKhauHash;
    }

    public void setMatKhauHash(String matKhauHash) {
        this.matKhauHash = matKhauHash;
    }

    public String getVaiTro() {
        return vaiTro;
    }

    public void setVaiTro(String vaiTro) {
        this.vaiTro = vaiTro;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public int getSoLanDangNhapSai() {
        return soLanDangNhapSai;
    }

    public void setSoLanDangNhapSai(int soLanDangNhapSai) {
        this.soLanDangNhapSai = soLanDangNhapSai;
    }

    public LocalDateTime getKhoaDen() {
        return khoaDen;
    }

    public void setKhoaDen(LocalDateTime khoaDen) {
        this.khoaDen = khoaDen;
    }

    public LocalDateTime getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(LocalDateTime ngayTao) {
        this.ngayTao = ngayTao;
    }
}

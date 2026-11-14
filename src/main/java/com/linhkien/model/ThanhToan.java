package com.linhkien.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Lớp đại diện cho Giao dịch thanh toán / hoàn tiền (Module 3 - UC20 - bảng ThanhToan).
 */
public class ThanhToan {
    public static final String LOAI_THU = "THU";
    public static final String LOAI_HOAN = "HOAN";

    public static final String TRANG_THAI_CHO = "CHO_XAC_NHAN";
    public static final String TRANG_THAI_THANH_CONG = "THANH_CONG";
    public static final String TRANG_THAI_THAT_BAI = "THAT_BAI";

    private int maThanhToan;
    private int maDonHang;
    private String loaiGiaoDich = LOAI_THU;
    private String phuongThuc;
    private BigDecimal soTien = BigDecimal.ZERO;
    private String maThamChieu;
    private String trangThai = TRANG_THAI_CHO;
    private Integer maNguoiXacNhan;
    private String tenNguoiXacNhan;
    private LocalDateTime thoiDiem;

    public ThanhToan() {
        this.thoiDiem = LocalDateTime.now();
    }

    public ThanhToan(int maDonHang, String loaiGiaoDich, String phuongThuc, BigDecimal soTien) {
        this(maDonHang, loaiGiaoDich, phuongThuc, soTien, null);
    }

    public ThanhToan(int maDonHang, String loaiGiaoDich, String phuongThuc, BigDecimal soTien, String maThamChieu) {
        this();
        this.maDonHang = maDonHang;
        this.loaiGiaoDich = loaiGiaoDich;
        this.phuongThuc = phuongThuc;
        this.soTien = soTien != null ? soTien : BigDecimal.ZERO;
        this.maThamChieu = maThamChieu;
    }

    public void xacNhan(int maNguoiXacNhan) {
        this.trangThai = TRANG_THAI_THANH_CONG;
        this.maNguoiXacNhan = maNguoiXacNhan;
        this.thoiDiem = LocalDateTime.now();
    }

    public void danhDauThatBai() {
        this.trangThai = TRANG_THAI_THAT_BAI;
        this.thoiDiem = LocalDateTime.now();
    }

    public int getMaThanhToan() {
        return maThanhToan;
    }

    public void setMaThanhToan(int maThanhToan) {
        this.maThanhToan = maThanhToan;
    }

    public int getMaDonHang() {
        return maDonHang;
    }

    public void setMaDonHang(int maDonHang) {
        this.maDonHang = maDonHang;
    }

    public String getLoaiGiaoDich() {
        return loaiGiaoDich;
    }

    public void setLoaiGiaoDich(String loaiGiaoDich) {
        this.loaiGiaoDich = loaiGiaoDich;
    }

    public String getPhuongThuc() {
        return phuongThuc;
    }

    public void setPhuongThuc(String phuongThuc) {
        this.phuongThuc = phuongThuc;
    }

    public BigDecimal getSoTien() {
        return soTien;
    }

    public void setSoTien(BigDecimal soTien) {
        this.soTien = soTien != null ? soTien : BigDecimal.ZERO;
    }

    public String getMaThamChieu() {
        return maThamChieu;
    }

    public void setMaThamChieu(String maThamChieu) {
        this.maThamChieu = maThamChieu;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public Integer getMaNguoiXacNhan() {
        return maNguoiXacNhan;
    }

    public void setMaNguoiXacNhan(Integer maNguoiXacNhan) {
        this.maNguoiXacNhan = maNguoiXacNhan;
    }

    public String getTenNguoiXacNhan() {
        return tenNguoiXacNhan;
    }

    public void setTenNguoiXacNhan(String tenNguoiXacNhan) {
        this.tenNguoiXacNhan = tenNguoiXacNhan;
    }

    public LocalDateTime getThoiDiem() {
        return thoiDiem;
    }

    public void setThoiDiem(LocalDateTime thoiDiem) {
        this.thoiDiem = thoiDiem;
    }
}

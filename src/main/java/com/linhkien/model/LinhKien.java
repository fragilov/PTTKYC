package com.linhkien.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Lớp đại diện cho Linh kiện điện tử theo Class Diagram và bảng LinhKien trong CSDL.
 */
public class LinhKien {
    private int maLinhKien;
    private String maSKU;
    private String tenLinhKien;
    private int maDanhMuc;
    private String tenDanhMuc; // Thuộc tính bổ trợ hiển thị tên danh mục
    private String nhaSanXuat;
    private String moTa;
    private String kieuDongGoi; // 'DIP', 'SMD', 'MODULE', 'KHAC'
    private String dienApHoatDong;
    private String giaoTiep;
    private String duongDanDatasheet;
    private String hinhAnh;
    private String donViTinh;
    private BigDecimal giaBan;
    private int soLuongTon;
    private int nguongTonToiThieu;
    private String trangThaiBan; // 'DANG_BAN', 'NGUNG_BAN'
    private LocalDateTime ngayCapNhat;

    public LinhKien() {
        this.donViTinh = "Cái";
        this.giaBan = BigDecimal.ZERO;
        this.soLuongTon = 0;
        this.nguongTonToiThieu = 10;
        this.trangThaiBan = "DANG_BAN";
        this.ngayCapNhat = LocalDateTime.now();
    }

    public LinhKien(String maSKU, String tenLinhKien, int maDanhMuc, String nhaSanXuat,
                   String kieuDongGoi, String dienApHoatDong, String giaoTiep,
                   BigDecimal giaBan, int soLuongTon, int nguongTonToiThieu) {
        this();
        this.maSKU = maSKU;
        this.tenLinhKien = tenLinhKien;
        this.maDanhMuc = maDanhMuc;
        this.nhaSanXuat = nhaSanXuat;
        this.kieuDongGoi = kieuDongGoi;
        this.dienApHoatDong = dienApHoatDong;
        this.giaoTiep = giaoTiep;
        this.giaBan = giaBan != null ? giaBan : BigDecimal.ZERO;
        this.soLuongTon = soLuongTon;
        this.nguongTonToiThieu = nguongTonToiThieu;
    }

    // ==========================================
    // CÁC HÀM NGHIỆP VỤ (Theo Class Diagram)
    // ==========================================

    /**
     * Kiểm tra xem số lượng tồn có đủ cho yêu cầu hay không.
     * @param soLuong Số lượng cần kiểm tra
     * @return true nếu còn đủ hàng, false nếu không đủ hoặc ngừng bán
     */
    public boolean kiemTraTon(int soLuong) {
        if (!"DANG_BAN".equalsIgnoreCase(this.trangThaiBan)) {
            return false;
        }
        return this.soLuongTon >= soLuong && soLuong > 0;
    }

    /**
     * Trừ số lượng tồn khi xuất kho hoặc thanh toán đơn hàng.
     * @param soLuong Số lượng xuất
     */
    public void truTon(int soLuong) {
        if (soLuong <= 0) {
            throw new IllegalArgumentException("Số lượng trừ phải lớn hơn 0");
        }
        if (this.soLuongTon < soLuong) {
            throw new IllegalStateException(
                String.format("Không đủ tồn kho cho linh kiện '%s' (Mã: %s). Còn lại: %d, yêu cầu: %d",
                    this.tenLinhKien, this.maSKU, this.soLuongTon, soLuong));
        }
        this.soLuongTon -= soLuong;
        this.ngayCapNhat = LocalDateTime.now();
    }

    /**
     * Hoàn lại số lượng tồn khi hủy đơn hàng.
     * @param soLuong Số lượng hoàn lại
     */
    public void hoanTon(int soLuong) {
        if (soLuong <= 0) {
            throw new IllegalArgumentException("Số lượng hoàn phải lớn hơn 0");
        }
        this.soLuongTon += soLuong;
        this.ngayCapNhat = LocalDateTime.now();
    }

    /**
     * Điều chỉnh số lượng tồn kho (kiểm kê kho, nhập kho thêm).
     * @param mucDieuChinh Số lượng thay đổi (dương là tăng, âm là giảm)
     * @param lyDo Lý do điều chỉnh
     */
    public void dieuChinhTon(int mucDieuChinh, String lyDo) {
        int tonMoi = this.soLuongTon + mucDieuChinh;
        if (tonMoi < 0) {
            throw new IllegalStateException("Số lượng tồn sau điều chỉnh không thể âm");
        }
        this.soLuongTon = tonMoi;
        this.ngayCapNhat = LocalDateTime.now();
    }

    /**
     * Kiểm tra xem linh kiện có đang ở mức tồn kho thấp (dưới ngưỡng) không.
     * @return true nếu tồn kho <= ngưỡng tồn tối thiểu
     */
    public boolean laTonThap() {
        return this.soLuongTon <= this.nguongTonToiThieu;
    }

    /**
     * Chuyển trạng thái linh kiện sang ngừng bán.
     */
    public void ngungBan() {
        this.trangThaiBan = "NGUNG_BAN";
        this.ngayCapNhat = LocalDateTime.now();
    }

    // ==========================================
    // GETTERS VÀ SETTERS
    // ==========================================

    public int getMaLinhKien() {
        return maLinhKien;
    }

    public void setMaLinhKien(int maLinhKien) {
        this.maLinhKien = maLinhKien;
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

    public String getNhaSanXuat() {
        return nhaSanXuat;
    }

    public void setNhaSanXuat(String nhaSanXuat) {
        this.nhaSanXuat = nhaSanXuat;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public String getKieuDongGoi() {
        return kieuDongGoi;
    }

    public void setKieuDongGoi(String kieuDongGoi) {
        this.kieuDongGoi = kieuDongGoi;
    }

    public String getDienApHoatDong() {
        return dienApHoatDong;
    }

    public void setDienApHoatDong(String dienApHoatDong) {
        this.dienApHoatDong = dienApHoatDong;
    }

    public String getGiaoTiep() {
        return giaoTiep;
    }

    public void setGiaoTiep(String giaoTiep) {
        this.giaoTiep = giaoTiep;
    }

    public String getDuongDanDatasheet() {
        return duongDanDatasheet;
    }

    public void setDuongDanDatasheet(String duongDanDatasheet) {
        this.duongDanDatasheet = duongDanDatasheet;
    }

    public String getHinhAnh() {
        return hinhAnh;
    }

    public void setHinhAnh(String hinhAnh) {
        this.hinhAnh = hinhAnh;
    }

    public String getDonViTinh() {
        return donViTinh;
    }

    public void setDonViTinh(String donViTinh) {
        this.donViTinh = donViTinh;
    }

    public BigDecimal getGiaBan() {
        return giaBan;
    }

    public void setGiaBan(BigDecimal giaBan) {
        this.giaBan = giaBan != null ? giaBan : BigDecimal.ZERO;
    }

    public int getSoLuongTon() {
        return soLuongTon;
    }

    public void setSoLuongTon(int soLuongTon) {
        this.soLuongTon = soLuongTon;
    }

    public int getNguongTonToiThieu() {
        return nguongTonToiThieu;
    }

    public void setNguongTonToiThieu(int nguongTonToiThieu) {
        this.nguongTonToiThieu = nguongTonToiThieu;
    }

    public String getTrangThaiBan() {
        return trangThaiBan;
    }

    public void setTrangThaiBan(String trangThaiBan) {
        this.trangThaiBan = trangThaiBan;
    }

    public LocalDateTime getNgayCapNhat() {
        return ngayCapNhat;
    }

    public void setNgayCapNhat(LocalDateTime ngayCapNhat) {
        this.ngayCapNhat = ngayCapNhat;
    }

    @Override
    public String toString() {
        return String.format("[%d] %s (%s) - Giá: %,d VNĐ - Tồn: %d %s - Trạng thái: %s",
            maLinhKien, tenLinhKien, maSKU, giaBan.longValue(), soLuongTon, donViTinh, trangThaiBan);
    }
}

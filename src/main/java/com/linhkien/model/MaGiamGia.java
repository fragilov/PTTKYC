package com.linhkien.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Lớp đại diện cho Mã giảm giá (Module 2 - UC09, UC17 - bảng MaGiamGia).
 */
public class MaGiamGia {
    public static final String PHAN_TRAM = "PHAN_TRAM";
    public static final String SO_TIEN = "SO_TIEN";

    private int maGiamGia;
    private String maCode;
    private String loaiGiam;
    private BigDecimal giaTriGiam = BigDecimal.ZERO;
    private BigDecimal donHangToiThieu = BigDecimal.ZERO;
    private Integer soLuotToiDa;
    private int soLuotDaDung;
    private LocalDateTime ngayBatDau;
    private LocalDateTime ngayKetThuc;

    public MaGiamGia() {
    }

    public MaGiamGia(String maCode, String loaiGiam, BigDecimal giaTriGiam, BigDecimal donHangToiThieu,
                     Integer soLuotToiDa, LocalDateTime ngayBatDau, LocalDateTime ngayKetThuc) {
        this.maCode = maCode;
        this.loaiGiam = loaiGiam;
        this.giaTriGiam = giaTriGiam != null ? giaTriGiam : BigDecimal.ZERO;
        this.donHangToiThieu = donHangToiThieu != null ? donHangToiThieu : BigDecimal.ZERO;
        this.soLuotToiDa = soLuotToiDa;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
    }

    /**
     * Kiểm tra điều kiện áp dụng mã theo thời gian hiệu lực, lượt dùng và giá trị đơn hàng tối thiểu.
     */
    public boolean kiemTraMa(BigDecimal tongTien) {
        if (tongTien == null || tongTien.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        if (ngayBatDau != null && now.isBefore(ngayBatDau)) {
            return false;
        }
        if (ngayKetThuc != null && now.isAfter(ngayKetThuc)) {
            return false;
        }
        if (soLuotToiDa != null && soLuotDaDung >= soLuotToiDa) {
            return false;
        }
        BigDecimal toiThieu = donHangToiThieu != null ? donHangToiThieu : BigDecimal.ZERO;
        return tongTien.compareTo(toiThieu) >= 0;
    }

    /**
     * Tính số tiền được giảm (không vượt quá tổng tiền hàng).
     */
    public BigDecimal tinhSoTienGiam(BigDecimal tongTien) {
        if (!kiemTraMa(tongTien)) {
            return BigDecimal.ZERO;
        }
        BigDecimal giam;
        if (PHAN_TRAM.equalsIgnoreCase(loaiGiam)) {
            giam = tongTien.multiply(giaTriGiam).divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
        } else {
            giam = giaTriGiam != null ? giaTriGiam : BigDecimal.ZERO;
        }
        return giam.min(tongTien).max(BigDecimal.ZERO);
    }

    public int getMaGiamGia() {
        return maGiamGia;
    }

    public void setMaGiamGia(int maGiamGia) {
        this.maGiamGia = maGiamGia;
    }

    public String getMaCode() {
        return maCode;
    }

    public void setMaCode(String maCode) {
        this.maCode = maCode;
    }

    public String getLoaiGiam() {
        return loaiGiam;
    }

    public void setLoaiGiam(String loaiGiam) {
        this.loaiGiam = loaiGiam;
    }

    public BigDecimal getGiaTriGiam() {
        return giaTriGiam;
    }

    public void setGiaTriGiam(BigDecimal giaTriGiam) {
        this.giaTriGiam = giaTriGiam != null ? giaTriGiam : BigDecimal.ZERO;
    }

    public BigDecimal getDonHangToiThieu() {
        return donHangToiThieu;
    }

    public void setDonHangToiThieu(BigDecimal donHangToiThieu) {
        this.donHangToiThieu = donHangToiThieu != null ? donHangToiThieu : BigDecimal.ZERO;
    }

    public Integer getSoLuotToiDa() {
        return soLuotToiDa;
    }

    public void setSoLuotToiDa(Integer soLuotToiDa) {
        this.soLuotToiDa = soLuotToiDa;
    }

    public int getSoLuotDaDung() {
        return soLuotDaDung;
    }

    public void setSoLuotDaDung(int soLuotDaDung) {
        this.soLuotDaDung = soLuotDaDung;
    }

    public LocalDateTime getNgayBatDau() {
        return ngayBatDau;
    }

    public void setNgayBatDau(LocalDateTime ngayBatDau) {
        this.ngayBatDau = ngayBatDau;
    }

    public LocalDateTime getNgayKetThuc() {
        return ngayKetThuc;
    }

    public void setNgayKetThuc(LocalDateTime ngayKetThuc) {
        this.ngayKetThuc = ngayKetThuc;
    }
}

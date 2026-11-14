package com.linhkien.model;

/**
 * Lớp đại diện cho Địa chỉ nhận hàng của khách hàng (FR06 - bảng DiaChiNhanHang).
 */
public class DiaChiNhanHang {
    private int maDiaChi;
    private int maNguoiDung;
    private String tenNguoiNhan;
    private String soDienThoaiNhan;
    private String diaChiChiTiet;
    private String phuongXa;
    private String tinhThanh;
    private boolean laMacDinh;

    public DiaChiNhanHang() {
    }

    public DiaChiNhanHang(int maNguoiDung, String tenNguoiNhan, String soDienThoaiNhan,
                          String diaChiChiTiet, String phuongXa, String tinhThanh, boolean laMacDinh) {
        this.maNguoiDung = maNguoiDung;
        this.tenNguoiNhan = tenNguoiNhan;
        this.soDienThoaiNhan = soDienThoaiNhan;
        this.diaChiChiTiet = diaChiChiTiet;
        this.phuongXa = phuongXa;
        this.tinhThanh = tinhThanh;
        this.laMacDinh = laMacDinh;
    }

    public void datMacDinh() {
        this.laMacDinh = true;
    }

    public String dinhDangDayDu() {
        StringBuilder sb = new StringBuilder();
        if (diaChiChiTiet != null && !diaChiChiTiet.isBlank()) {
            sb.append(diaChiChiTiet.trim());
        }
        if (phuongXa != null && !phuongXa.isBlank()) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(phuongXa.trim());
        }
        if (tinhThanh != null && !tinhThanh.isBlank()) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(tinhThanh.trim());
        }
        return sb.toString();
    }

    public int getMaDiaChi() {
        return maDiaChi;
    }

    public void setMaDiaChi(int maDiaChi) {
        this.maDiaChi = maDiaChi;
    }

    public int getMaNguoiDung() {
        return maNguoiDung;
    }

    public void setMaNguoiDung(int maNguoiDung) {
        this.maNguoiDung = maNguoiDung;
    }

    public String getTenNguoiNhan() {
        return tenNguoiNhan;
    }

    public void setTenNguoiNhan(String tenNguoiNhan) {
        this.tenNguoiNhan = tenNguoiNhan;
    }

    public String getSoDienThoaiNhan() {
        return soDienThoaiNhan;
    }

    public void setSoDienThoaiNhan(String soDienThoaiNhan) {
        this.soDienThoaiNhan = soDienThoaiNhan;
    }

    public String getDiaChiChiTiet() {
        return diaChiChiTiet;
    }

    public void setDiaChiChiTiet(String diaChiChiTiet) {
        this.diaChiChiTiet = diaChiChiTiet;
    }

    public String getPhuongXa() {
        return phuongXa;
    }

    public void setPhuongXa(String phuongXa) {
        this.phuongXa = phuongXa;
    }

    public String getTinhThanh() {
        return tinhThanh;
    }

    public void setTinhThanh(String tinhThanh) {
        this.tinhThanh = tinhThanh;
    }

    public boolean isLaMacDinh() {
        return laMacDinh;
    }

    public void setLaMacDinh(boolean laMacDinh) {
        this.laMacDinh = laMacDinh;
    }
}

package com.linhkien.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Lớp đại diện cho Đơn hàng theo Class Diagram và bảng DonHang trong CSDL (UC08, UC10, UC14, UC19).
 * Trạng thái đơn: CHO_XAC_NHAN -> DANG_DONG_GOI -> DANG_GIAO -> DA_GIAO (hoặc GIAO_THAT_BAI / DA_HUY).
 */
public class DonHang {
    public static final String CHO_XAC_NHAN = "CHO_XAC_NHAN";
    public static final String DANG_DONG_GOI = "DANG_DONG_GOI";
    public static final String DANG_GIAO = "DANG_GIAO";
    public static final String DA_GIAO = "DA_GIAO";
    public static final String GIAO_THAT_BAI = "GIAO_THAT_BAI";
    public static final String DA_HUY = "DA_HUY";

    // Các chuyển trạng thái hợp lệ của đơn hàng
    private static final Map<String, Set<String>> CHUYEN_HOP_LE = Map.of(
        CHO_XAC_NHAN, Set.of(DANG_DONG_GOI, DA_HUY),
        DANG_DONG_GOI, Set.of(DANG_GIAO, DA_HUY),
        DANG_GIAO, Set.of(DA_GIAO, GIAO_THAT_BAI),
        DA_GIAO, Set.of(),
        GIAO_THAT_BAI, Set.of(),
        DA_HUY, Set.of()
    );

    private int maDonHang;
    private int maNguoiDung;
    private Integer maGiamGia;
    private String maYeuCau;
    private LocalDateTime ngayDat;
    private String tenNguoiNhan;
    private String soDienThoaiNhan;
    private String diaChiNhan;
    private BigDecimal tongTienHang = BigDecimal.ZERO;
    private BigDecimal phiVanChuyen = BigDecimal.ZERO;
    private BigDecimal soTienGiam = BigDecimal.ZERO;
    private BigDecimal tongThanhToan = BigDecimal.ZERO;
    private String phuongThucThanhToan;
    private String trangThaiThanhToan = "CHO_THANH_TOAN";
    private String trangThaiDon = CHO_XAC_NHAN;
    private boolean daHoanKho;
    private String ghiChu;

    // Thuộc tính hiển thị
    private String tenKhachHang;

    // Quan hệ Composition: DonHang ◆ ChiTietDonHang (1 - 1..*)
    private List<ChiTietDonHang> danhSachChiTiet = new ArrayList<>();

    public DonHang() {
        this.ngayDat = LocalDateTime.now();
    }

    // ==========================================
    // CÁC HÀM NGHIỆP VỤ (Theo Class Diagram)
    // ==========================================

    /**
     * Tính tổng tiền hàng bằng tổng thành tiền các dòng chi tiết.
     */
    public BigDecimal tinhTongTienHang() {
        BigDecimal tong = BigDecimal.ZERO;
        for (ChiTietDonHang line : danhSachChiTiet) {
            tong = tong.add(line.tinhThanhTien());
        }
        return tong;
    }

    /**
     * Tổng thanh toán = Tổng tiền hàng + Phí vận chuyển - Số tiền giảm.
     */
    public BigDecimal tinhTongThanhToan() {
        return tongTienHang.add(phiVanChuyen).subtract(soTienGiam);
    }

    /**
     * Kiểm tra việc chuyển từ trạng thái hiện tại sang trạng thái mới có hợp lệ hay không.
     */
    public boolean coTheChuyenSang(String trangThaiMoi) {
        Set<String> cho = CHUYEN_HOP_LE.get(trangThaiDon);
        return cho != null && cho.contains(trangThaiMoi);
    }

    /**
     * Chỉ được hủy đơn khi đang chờ xác nhận hoặc đang đóng gói (UC19).
     */
    public boolean coTheHuy() {
        return coTheChuyenSang(DA_HUY);
    }

    /**
     * Danh sách các trạng thái mà đơn có thể chuyển tới từ trạng thái hiện tại.
     */
    public Set<String> trangThaiTiepTheo() {
        return CHUYEN_HOP_LE.getOrDefault(trangThaiDon, Set.of());
    }

    public static String nhanTrangThai(String trangThai) {
        if (trangThai == null) {
            return "";
        }
        return switch (trangThai) {
            case CHO_XAC_NHAN -> "Chờ xác nhận";
            case DANG_DONG_GOI -> "Đang đóng gói";
            case "DA_BAN_GIAO" -> "Đã bàn giao";
            case DANG_GIAO -> "Đang giao";
            case DA_GIAO -> "Đã giao";
            case GIAO_THAT_BAI -> "Giao thất bại";
            case DA_HUY -> "Đã hủy";
            default -> trangThai;
        };
    }

    public static String nhanThanhToan(String trangThai) {
        if (trangThai == null) {
            return "";
        }
        return switch (trangThai) {
            case "CHO_THANH_TOAN" -> "Chờ thanh toán";
            case "DA_THANH_TOAN" -> "Đã thanh toán";
            case "DANG_HOAN_TIEN" -> "Đang hoàn tiền";
            case "DA_HOAN_TIEN" -> "Đã hoàn tiền";
            default -> trangThai;
        };
    }

    // ==========================================
    // GETTERS VÀ SETTERS
    // ==========================================

    public int getMaDonHang() {
        return maDonHang;
    }

    public void setMaDonHang(int maDonHang) {
        this.maDonHang = maDonHang;
    }

    public int getMaNguoiDung() {
        return maNguoiDung;
    }

    public void setMaNguoiDung(int maNguoiDung) {
        this.maNguoiDung = maNguoiDung;
    }

    public Integer getMaGiamGia() {
        return maGiamGia;
    }

    public void setMaGiamGia(Integer maGiamGia) {
        this.maGiamGia = maGiamGia;
    }

    public String getMaYeuCau() {
        return maYeuCau;
    }

    public void setMaYeuCau(String maYeuCau) {
        this.maYeuCau = maYeuCau;
    }

    public LocalDateTime getNgayDat() {
        return ngayDat;
    }

    public void setNgayDat(LocalDateTime ngayDat) {
        this.ngayDat = ngayDat;
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

    public String getDiaChiNhan() {
        return diaChiNhan;
    }

    public void setDiaChiNhan(String diaChiNhan) {
        this.diaChiNhan = diaChiNhan;
    }

    public BigDecimal getTongTienHang() {
        return tongTienHang;
    }

    public void setTongTienHang(BigDecimal tongTienHang) {
        this.tongTienHang = tongTienHang;
    }

    public BigDecimal getPhiVanChuyen() {
        return phiVanChuyen;
    }

    public void setPhiVanChuyen(BigDecimal phiVanChuyen) {
        this.phiVanChuyen = phiVanChuyen;
    }

    public BigDecimal getSoTienGiam() {
        return soTienGiam;
    }

    public void setSoTienGiam(BigDecimal soTienGiam) {
        this.soTienGiam = soTienGiam;
    }

    public BigDecimal getTongThanhToan() {
        return tongThanhToan;
    }

    public void setTongThanhToan(BigDecimal tongThanhToan) {
        this.tongThanhToan = tongThanhToan;
    }

    public String getPhuongThucThanhToan() {
        return phuongThucThanhToan;
    }

    public void setPhuongThucThanhToan(String phuongThucThanhToan) {
        this.phuongThucThanhToan = phuongThucThanhToan;
    }

    public String getTrangThaiThanhToan() {
        return trangThaiThanhToan;
    }

    public void setTrangThaiThanhToan(String trangThaiThanhToan) {
        this.trangThaiThanhToan = trangThaiThanhToan;
    }

    public String getTrangThaiDon() {
        return trangThaiDon;
    }

    public void setTrangThaiDon(String trangThaiDon) {
        this.trangThaiDon = trangThaiDon;
    }

    public boolean isDaHoanKho() {
        return daHoanKho;
    }

    public void setDaHoanKho(boolean daHoanKho) {
        this.daHoanKho = daHoanKho;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public String getTenKhachHang() {
        return tenKhachHang;
    }

    public void setTenKhachHang(String tenKhachHang) {
        this.tenKhachHang = tenKhachHang;
    }

    public List<ChiTietDonHang> getDanhSachChiTiet() {
        return danhSachChiTiet;
    }

    public void setDanhSachChiTiet(List<ChiTietDonHang> danhSachChiTiet) {
        this.danhSachChiTiet = danhSachChiTiet != null ? danhSachChiTiet : new ArrayList<>();
    }
}

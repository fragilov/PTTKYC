package com.linhkien.dao;

import com.linhkien.config.DatabaseConnection;
import com.linhkien.model.ChiTietDonHang;
import com.linhkien.model.DonHang;
import com.linhkien.model.LichSuTrangThaiDon;
import com.linhkien.model.VanChuyen;
import com.linhkien.util.JdbcHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp truy xuất dữ liệu Đơn hàng, Chi tiết đơn hàng, Vận chuyển và các bảng nhật ký (DAO).
 * Khác với các DAO trước, các hàm ở đây ném SQLException lên cho tầng Service
 * để Service quản lý Transaction (đặt hàng và hủy đơn thay đổi nhiều bảng cùng lúc).
 */
public class DonHangDAO {

    // ==========================================
    // ĐƠN HÀNG
    // ==========================================

    /**
     * Thêm đơn hàng mới và gán mã đơn sinh tự động vào đối tượng.
     */
    public void insert(DonHang dh) throws SQLException {
        String sql = """
            INSERT INTO DonHang (MaNguoiDung, MaGiamGia, MaYeuCau, NgayDat, TenNguoiNhan, SoDienThoaiNhan,
                                 DiaChiNhan, TongTienHang, PhiVanChuyen, SoTienGiam, TongThanhToan,
                                 PhuongThucThanhToan, TrangThaiThanhToan, TrangThaiDon, DaHoanKho, GhiChu)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, dh.getMaNguoiDung());
            JdbcHelper.setSoNguyenHoacNull(ps, 2, dh.getMaGiamGia());
            ps.setString(3, dh.getMaYeuCau());
            JdbcHelper.setThoiGian(ps, 4, dh.getNgayDat());
            ps.setString(5, dh.getTenNguoiNhan());
            ps.setString(6, dh.getSoDienThoaiNhan());
            ps.setString(7, dh.getDiaChiNhan());
            ps.setBigDecimal(8, dh.getTongTienHang());
            ps.setBigDecimal(9, dh.getPhiVanChuyen());
            ps.setBigDecimal(10, dh.getSoTienGiam());
            ps.setBigDecimal(11, dh.getTongThanhToan());
            ps.setString(12, dh.getPhuongThucThanhToan());
            ps.setString(13, dh.getTrangThaiThanhToan());
            ps.setString(14, dh.getTrangThaiDon());
            ps.setBoolean(15, dh.isDaHoanKho());
            ps.setString(16, dh.getGhiChu());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    dh.setMaDonHang(rs.getInt(1));
                }
            }
        }
    }

    public void insertChiTiet(ChiTietDonHang ct) throws SQLException {
        String sql = "INSERT INTO ChiTietDonHang (MaDonHang, MaLinhKien, SoLuong, DonGia) VALUES (?, ?, ?, ?)";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ct.getMaDonHang());
            ps.setInt(2, ct.getMaLinhKien());
            ps.setInt(3, ct.getSoLuong());
            ps.setBigDecimal(4, ct.getDonGia());
            ps.executeUpdate();
        }
    }

    /**
     * Tìm đơn hàng theo mã yêu cầu (khóa chống trùng đơn - NFR05).
     */
    public DonHang findByMaYeuCau(String maYeuCau) throws SQLException {
        String sql = SELECT_DON + " WHERE dh.MaYeuCau = ?";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maYeuCau);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapDon(rs);
                }
            }
        }
        return null;
    }

    /**
     * Tìm đơn hàng theo mã, kèm đầy đủ các dòng chi tiết.
     */
    public DonHang findById(int maDonHang) throws SQLException {
        String sql = SELECT_DON + " WHERE dh.MaDonHang = ?";
        Connection conn = DatabaseConnection.getConnection();
        DonHang dh = null;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maDonHang);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    dh = mapDon(rs);
                }
            }
        }
        if (dh != null) {
            dh.setDanhSachChiTiet(findChiTiet(maDonHang));
        }
        return dh;
    }

    /**
     * Lịch sử đơn hàng của một khách hàng, đơn mới nhất lên đầu (UC10).
     */
    public List<DonHang> findByNguoiDung(int maNguoiDung) throws SQLException {
        String sql = SELECT_DON + " WHERE dh.MaNguoiDung = ? ORDER BY dh.NgayDat DESC, dh.MaDonHang DESC";
        List<DonHang> list = new ArrayList<>();
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maNguoiDung);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapDon(rs));
                }
            }
        }
        return list;
    }

    /**
     * Toàn bộ đơn hàng của cửa hàng, có thể lọc theo trạng thái (UC14).
     * @param trangThaiDon Trạng thái cần lọc, null hoặc rỗng để lấy tất cả
     */
    public List<DonHang> findAll(String trangThaiDon) throws SQLException {
        boolean loc = trangThaiDon != null && !trangThaiDon.isBlank();
        String sql = SELECT_DON + (loc ? " WHERE dh.TrangThaiDon = ?" : "")
            + " ORDER BY dh.NgayDat DESC, dh.MaDonHang DESC";
        List<DonHang> list = new ArrayList<>();
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (loc) {
                ps.setString(1, trangThaiDon);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapDon(rs));
                }
            }
        }
        return list;
    }

    public List<ChiTietDonHang> findChiTiet(int maDonHang) throws SQLException {
        String sql = """
            SELECT ct.MaDonHang, ct.MaLinhKien, ct.SoLuong, ct.DonGia,
                   lk.MaSKU, lk.TenLinhKien, lk.DonViTinh
            FROM ChiTietDonHang ct
            JOIN LinhKien lk ON ct.MaLinhKien = lk.MaLinhKien
            WHERE ct.MaDonHang = ?
            ORDER BY ct.MaLinhKien ASC
        """;
        List<ChiTietDonHang> list = new ArrayList<>();
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maDonHang);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ChiTietDonHang ct = new ChiTietDonHang();
                    ct.setMaDonHang(rs.getInt("MaDonHang"));
                    ct.setMaLinhKien(rs.getInt("MaLinhKien"));
                    ct.setSoLuong(rs.getInt("SoLuong"));
                    ct.setDonGia(rs.getBigDecimal("DonGia"));
                    ct.setMaSKU(rs.getString("MaSKU"));
                    ct.setTenLinhKien(rs.getString("TenLinhKien"));
                    ct.setDonViTinh(rs.getString("DonViTinh"));
                    list.add(ct);
                }
            }
        }
        return list;
    }

    public void capNhatTrangThaiDon(int maDonHang, String trangThaiMoi) throws SQLException {
        capNhatChuoi("UPDATE DonHang SET TrangThaiDon = ? WHERE MaDonHang = ?", trangThaiMoi, maDonHang);
    }

    public void capNhatTrangThaiThanhToan(int maDonHang, String trangThaiThanhToan) throws SQLException {
        capNhatChuoi("UPDATE DonHang SET TrangThaiThanhToan = ? WHERE MaDonHang = ?", trangThaiThanhToan, maDonHang);
    }

    public void danhDauDaHoanKho(int maDonHang) throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement("UPDATE DonHang SET DaHoanKho = ? WHERE MaDonHang = ?")) {
            ps.setBoolean(1, true);
            ps.setInt(2, maDonHang);
            ps.executeUpdate();
        }
    }

    private void capNhatChuoi(String sql, String giaTri, int maDonHang) throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, giaTri);
            ps.setInt(2, maDonHang);
            ps.executeUpdate();
        }
    }

    // ==========================================
    // TỒN KHO
    // ==========================================

    public int layTonKho(int maLinhKien) throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement("SELECT SoLuongTon FROM LinhKien WHERE MaLinhKien = ?")) {
            ps.setInt(1, maLinhKien);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    /**
     * Trừ tồn kho nguyên tử (NFR04): chỉ trừ khi linh kiện còn đang bán và còn đủ hàng.
     * @return true nếu trừ thành công, false nếu không đủ tồn hoặc đã ngừng bán
     */
    public boolean truTonNguyenTu(int maLinhKien, int soLuong) throws SQLException {
        String sql = "UPDATE LinhKien SET SoLuongTon = SoLuongTon - ? "
            + "WHERE MaLinhKien = ? AND SoLuongTon >= ? AND TrangThaiBan = 'DANG_BAN'";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, soLuong);
            ps.setInt(2, maLinhKien);
            ps.setInt(3, soLuong);
            return ps.executeUpdate() > 0;
        }
    }

    public void congTon(int maLinhKien, int soLuong) throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE LinhKien SET SoLuongTon = SoLuongTon + ? WHERE MaLinhKien = ?")) {
            ps.setInt(1, soLuong);
            ps.setInt(2, maLinhKien);
            ps.executeUpdate();
        }
    }

    /**
     * Ghi nhật ký biến động tồn kho (NFR03). Loại biến động: NHAP, BAN, HOAN_HUY, DIEU_CHINH.
     */
    public void ghiLichSuTonKho(int maLinhKien, String loaiBienDong, int soLuongThayDoi, int tonTruoc,
                                int tonSau, Integer maDonHang, Integer maNguoiThucHien, String lyDo) throws SQLException {
        String sql = """
            INSERT INTO LichSuTonKho (MaLinhKien, LoaiBienDong, SoLuongThayDoi, TonTruoc, TonSau,
                                      MaDonHang, MaNguoiThucHien, LyDo, ThoiDiem)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maLinhKien);
            ps.setString(2, loaiBienDong);
            ps.setInt(3, soLuongThayDoi);
            ps.setInt(4, tonTruoc);
            ps.setInt(5, tonSau);
            JdbcHelper.setSoNguyenHoacNull(ps, 6, maDonHang);
            JdbcHelper.setSoNguyenHoacNull(ps, 7, maNguoiThucHien);
            ps.setString(8, lyDo);
            JdbcHelper.setThoiGian(ps, 9, LocalDateTime.now());
            ps.executeUpdate();
        }
    }

    // ==========================================
    // NHẬT KÝ TRẠNG THÁI ĐƠN
    // ==========================================

    public void ghiLichSuTrangThai(int maDonHang, String trangThaiCu, String trangThaiMoi,
                                   Integer maNguoiThucHien, String ghiChu) throws SQLException {
        String sql = """
            INSERT INTO LichSuTrangThaiDon (MaDonHang, TrangThaiCu, TrangThaiMoi, MaNguoiThucHien, ThoiDiem, GhiChu)
            VALUES (?, ?, ?, ?, ?, ?)
        """;
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maDonHang);
            ps.setString(2, trangThaiCu);
            ps.setString(3, trangThaiMoi);
            JdbcHelper.setSoNguyenHoacNull(ps, 4, maNguoiThucHien);
            JdbcHelper.setThoiGian(ps, 5, LocalDateTime.now());
            ps.setString(6, ghiChu);
            ps.executeUpdate();
        }
    }

    public List<LichSuTrangThaiDon> findLichSuTrangThai(int maDonHang) throws SQLException {
        String sql = """
            SELECT ls.MaLichSu, ls.MaDonHang, ls.TrangThaiCu, ls.TrangThaiMoi, ls.MaNguoiThucHien,
                   nd.HoTen, ls.ThoiDiem, ls.GhiChu
            FROM LichSuTrangThaiDon ls
            LEFT JOIN NguoiDung nd ON ls.MaNguoiThucHien = nd.MaNguoiDung
            WHERE ls.MaDonHang = ?
            ORDER BY ls.MaLichSu ASC
        """;
        List<LichSuTrangThaiDon> list = new ArrayList<>();
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maDonHang);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LichSuTrangThaiDon ls = new LichSuTrangThaiDon();
                    ls.setMaLichSu(rs.getLong("MaLichSu"));
                    ls.setMaDonHang(rs.getInt("MaDonHang"));
                    ls.setTrangThaiCu(rs.getString("TrangThaiCu"));
                    ls.setTrangThaiMoi(rs.getString("TrangThaiMoi"));
                    int nguoi = rs.getInt("MaNguoiThucHien");
                    ls.setMaNguoiThucHien(rs.wasNull() ? null : nguoi);
                    ls.setTenNguoiThucHien(rs.getString("HoTen"));
                    ls.setThoiDiem(JdbcHelper.docThoiGian(rs, "ThoiDiem"));
                    ls.setGhiChu(rs.getString("GhiChu"));
                    list.add(ls);
                }
            }
        }
        return list;
    }

    // ==========================================
    // VẬN CHUYỂN
    // ==========================================

    public void insertVanChuyen(VanChuyen vc) throws SQLException {
        String sql = """
            INSERT INTO VanChuyen (MaDonHang, DonViVanChuyen, MaVanDon, TrangThaiGiao, NgayBanGiao,
                                   NgayGiaoDuKien, NgayGiaoThucTe, DaThuCOD, GhiChu)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, vc.getMaDonHang());
            ps.setString(2, vc.getDonViVanChuyen());
            ps.setString(3, vc.getMaVanDon());
            ps.setString(4, vc.getTrangThaiGiao());
            JdbcHelper.setThoiGian(ps, 5, vc.getNgayBanGiao());
            JdbcHelper.setNgay(ps, 6, vc.getNgayGiaoDuKien());
            JdbcHelper.setThoiGian(ps, 7, vc.getNgayGiaoThucTe());
            ps.setBoolean(8, vc.isDaThuCOD());
            ps.setString(9, vc.getGhiChu());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    vc.setMaVanChuyen(rs.getInt(1));
                }
            }
        }
    }

    public VanChuyen findVanChuyenByDon(int maDonHang) throws SQLException {
        String sql = """
            SELECT MaVanChuyen, MaDonHang, DonViVanChuyen, MaVanDon, TrangThaiGiao, NgayBanGiao,
                   NgayGiaoDuKien, NgayGiaoThucTe, DaThuCOD, GhiChu
            FROM VanChuyen WHERE MaDonHang = ?
        """;
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maDonHang);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    VanChuyen vc = new VanChuyen();
                    vc.setMaVanChuyen(rs.getInt("MaVanChuyen"));
                    vc.setMaDonHang(rs.getInt("MaDonHang"));
                    vc.setDonViVanChuyen(rs.getString("DonViVanChuyen"));
                    vc.setMaVanDon(rs.getString("MaVanDon"));
                    vc.setTrangThaiGiao(rs.getString("TrangThaiGiao"));
                    vc.setNgayBanGiao(JdbcHelper.docThoiGian(rs, "NgayBanGiao"));
                    vc.setNgayGiaoDuKien(JdbcHelper.docNgay(rs, "NgayGiaoDuKien"));
                    vc.setNgayGiaoThucTe(JdbcHelper.docThoiGian(rs, "NgayGiaoThucTe"));
                    vc.setDaThuCOD(rs.getBoolean("DaThuCOD"));
                    vc.setGhiChu(rs.getString("GhiChu"));
                    return vc;
                }
            }
        }
        return null;
    }

    public boolean tonTaiMaVanDon(String maVanDon) throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM VanChuyen WHERE MaVanDon = ?")) {
            ps.setString(1, maVanDon);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Cập nhật kết quả giao hàng: trạng thái giao, thời điểm giao thực tế, đã thu COD hay chưa.
     */
    public void capNhatKetQuaGiao(int maDonHang, String trangThaiGiao, LocalDateTime ngayGiaoThucTe,
                                  boolean daThuCOD, String ghiChu) throws SQLException {
        String sql = "UPDATE VanChuyen SET TrangThaiGiao = ?, NgayGiaoThucTe = ?, DaThuCOD = ?, GhiChu = ? WHERE MaDonHang = ?";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, trangThaiGiao);
            JdbcHelper.setThoiGian(ps, 2, ngayGiaoThucTe);
            ps.setBoolean(3, daThuCOD);
            ps.setString(4, ghiChu);
            ps.setInt(5, maDonHang);
            ps.executeUpdate();
        }
    }

    // ==========================================
    // TIỆN ÍCH ÁNH XẠ
    // ==========================================

    private static final String SELECT_DON = """
        SELECT dh.MaDonHang, dh.MaNguoiDung, dh.MaGiamGia, mg.MaCode AS MaCodeGiamGia,
               dh.MaYeuCau, dh.NgayDat, dh.TenNguoiNhan, dh.SoDienThoaiNhan, dh.DiaChiNhan,
               dh.TongTienHang, dh.PhiVanChuyen, dh.SoTienGiam, dh.TongThanhToan,
               dh.PhuongThucThanhToan, dh.TrangThaiThanhToan, dh.TrangThaiDon,
               dh.DaHoanKho, dh.GhiChu, nd.HoTen AS TenKhachHang
        FROM DonHang dh
        JOIN NguoiDung nd ON dh.MaNguoiDung = nd.MaNguoiDung
        LEFT JOIN MaGiamGia mg ON dh.MaGiamGia = mg.MaGiamGia
    """;

    private DonHang mapDon(ResultSet rs) throws SQLException {
        DonHang dh = new DonHang();
        dh.setMaDonHang(rs.getInt("MaDonHang"));
        dh.setMaNguoiDung(rs.getInt("MaNguoiDung"));
        int giam = rs.getInt("MaGiamGia");
        dh.setMaGiamGia(rs.wasNull() ? null : giam);
        dh.setMaCodeGiamGia(rs.getString("MaCodeGiamGia"));
        dh.setMaYeuCau(rs.getString("MaYeuCau"));
        dh.setNgayDat(JdbcHelper.docThoiGian(rs, "NgayDat"));
        dh.setTenNguoiNhan(rs.getString("TenNguoiNhan"));
        dh.setSoDienThoaiNhan(rs.getString("SoDienThoaiNhan"));
        dh.setDiaChiNhan(rs.getString("DiaChiNhan"));
        dh.setTongTienHang(rs.getBigDecimal("TongTienHang"));
        dh.setPhiVanChuyen(rs.getBigDecimal("PhiVanChuyen"));
        dh.setSoTienGiam(rs.getBigDecimal("SoTienGiam"));
        dh.setTongThanhToan(rs.getBigDecimal("TongThanhToan"));
        dh.setPhuongThucThanhToan(rs.getString("PhuongThucThanhToan"));
        dh.setTrangThaiThanhToan(rs.getString("TrangThaiThanhToan"));
        dh.setTrangThaiDon(rs.getString("TrangThaiDon"));
        dh.setDaHoanKho(rs.getBoolean("DaHoanKho"));
        dh.setGhiChu(rs.getString("GhiChu"));
        dh.setTenKhachHang(rs.getString("TenKhachHang"));
        return dh;
    }
}

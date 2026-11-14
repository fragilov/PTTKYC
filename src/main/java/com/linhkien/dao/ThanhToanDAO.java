package com.linhkien.dao;

import com.linhkien.config.DatabaseConnection;
import com.linhkien.model.ThanhToan;
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
 * Lớp truy xuất dữ liệu giao dịch thanh toán & hoàn tiền (ThanhToan).
 */
public class ThanhToanDAO {

    private static final String SELECT_ALL = """
        SELECT tt.MaThanhToan, tt.MaDonHang, tt.LoaiGiaoDich, tt.PhuongThuc, tt.SoTien,
               tt.MaThamChieu, tt.TrangThai, tt.MaNguoiXacNhan, tt.ThoiDiem,
               nd.HoTen AS TenNguoiXacNhan
        FROM ThanhToan tt
        LEFT JOIN NguoiDung nd ON tt.MaNguoiXacNhan = nd.MaNguoiDung
    """;

    public int insert(ThanhToan tt) throws SQLException {
        String sql = """
            INSERT INTO ThanhToan (MaDonHang, LoaiGiaoDich, PhuongThuc, SoTien,
                                   MaThamChieu, TrangThai, MaNguoiXacNhan, ThoiDiem)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, tt.getMaDonHang());
            ps.setString(2, tt.getLoaiGiaoDich());
            ps.setString(3, tt.getPhuongThuc());
            ps.setBigDecimal(4, tt.getSoTien());
            ps.setString(5, tt.getMaThamChieu());
            ps.setString(6, tt.getTrangThai());
            JdbcHelper.setSoNguyenHoacNull(ps, 7, tt.getMaNguoiXacNhan());
            JdbcHelper.setThoiGian(ps, 8, tt.getThoiDiem() != null ? tt.getThoiDiem() : LocalDateTime.now());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    tt.setMaThanhToan(rs.getInt(1));
                }
            }
        }
        return tt.getMaThanhToan();
    }

    public ThanhToan findById(int maThanhToan) throws SQLException {
        String sql = SELECT_ALL + " WHERE tt.MaThanhToan = ?";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maThanhToan);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public List<ThanhToan> findByDonHang(int maDonHang) throws SQLException {
        String sql = SELECT_ALL + " WHERE tt.MaDonHang = ? ORDER BY tt.MaThanhToan DESC";
        List<ThanhToan> list = new ArrayList<>();
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maDonHang);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public List<ThanhToan> findAll() throws SQLException {
        String sql = SELECT_ALL + " ORDER BY tt.MaThanhToan DESC";
        List<ThanhToan> list = new ArrayList<>();
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public boolean capNhatTrangThai(int maThanhToan, String trangThai, Integer maNguoiXacNhan,
                                    String maThamChieu) throws SQLException {
        String sql = """
            UPDATE ThanhToan
            SET TrangThai = ?, MaNguoiXacNhan = ?, MaThamChieu = COALESCE(?, MaThamChieu), ThoiDiem = ?
            WHERE MaThanhToan = ?
        """;
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, trangThai);
            JdbcHelper.setSoNguyenHoacNull(ps, 2, maNguoiXacNhan);
            ps.setString(3, maThamChieu);
            JdbcHelper.setThoiGian(ps, 4, LocalDateTime.now());
            ps.setInt(5, maThanhToan);
            return ps.executeUpdate() > 0;
        }
    }

    private ThanhToan mapRow(ResultSet rs) throws SQLException {
        ThanhToan tt = new ThanhToan();
        tt.setMaThanhToan(rs.getInt("MaThanhToan"));
        tt.setMaDonHang(rs.getInt("MaDonHang"));
        tt.setLoaiGiaoDich(rs.getString("LoaiGiaoDich"));
        tt.setPhuongThuc(rs.getString("PhuongThuc"));
        tt.setSoTien(rs.getBigDecimal("SoTien"));
        tt.setMaThamChieu(rs.getString("MaThamChieu"));
        tt.setTrangThai(rs.getString("TrangThai"));
        int nguoi = rs.getInt("MaNguoiXacNhan");
        tt.setMaNguoiXacNhan(rs.wasNull() ? null : nguoi);
        tt.setTenNguoiXacNhan(rs.getString("TenNguoiXacNhan"));
        tt.setThoiDiem(JdbcHelper.docThoiGian(rs, "ThoiDiem"));
        return tt;
    }
}

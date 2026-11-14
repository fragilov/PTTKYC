package com.linhkien.dao;

import com.linhkien.config.DatabaseConnection;
import com.linhkien.model.NguoiDung;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Lớp truy xuất dữ liệu NguoiDung (DAO). Dùng để xác định vai trò khi xử lý đơn hàng.
 */
public class NguoiDungDAO {

    /**
     * Tìm người dùng theo mã.
     * @return Đối tượng NguoiDung hoặc null nếu không tồn tại
     */
    public NguoiDung findById(int maNguoiDung) {
        String sql = "SELECT MaNguoiDung, HoTen, Email, SoDienThoai, VaiTro, TrangThai FROM NguoiDung WHERE MaNguoiDung = ?";
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, maNguoiDung);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        NguoiDung nd = new NguoiDung(
                            rs.getInt("MaNguoiDung"),
                            rs.getString("HoTen"),
                            rs.getString("Email"),
                            rs.getString("SoDienThoai"),
                            rs.getString("VaiTro"));
                        nd.setTrangThai(rs.getString("TrangThai"));
                        return nd;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[DAO LỖI] Tìm người dùng theo mã thất bại: " + e.getMessage());
        }
        return null;
    }

    public String findMatKhauHashById(int maNguoiDung) {
        String sql = "SELECT MatKhauHash FROM NguoiDung WHERE MaNguoiDung = ?";
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, maNguoiDung);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("MatKhauHash");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[DAO LỖI] Đọc mật khẩu người dùng thất bại: " + e.getMessage());
        }
        return null;
    }

    public boolean updateMatKhauHash(int maNguoiDung, String matKhauHashMoi) throws SQLException {
        String sql = "UPDATE NguoiDung SET MatKhauHash = ? WHERE MaNguoiDung = ?";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, matKhauHashMoi);
            ps.setInt(2, maNguoiDung);
            return ps.executeUpdate() > 0;
        }
    }
}

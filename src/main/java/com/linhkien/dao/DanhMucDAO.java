package com.linhkien.dao;

import com.linhkien.config.DatabaseConnection;
import com.linhkien.model.DanhMucLinhKien;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp truy xuất dữ liệu DanhMucLinhKien.
 */
public class DanhMucDAO {

    public List<DanhMucLinhKien> findAll() {
        List<DanhMucLinhKien> list = new ArrayList<>();
        String sql = "SELECT MaDanhMuc, TenDanhMuc, MaDanhMucCha, MoTa, TrangThai FROM DanhMucLinhKien ORDER BY MaDanhMuc ASC";

        try {
            Connection conn = DatabaseConnection.getConnection();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    DanhMucLinhKien dm = mapResultSetToDanhMuc(rs);
                    list.add(dm);
                }
            }
        } catch (SQLException e) {
            System.err.println("[DAO LỖI] Lấy danh sách danh mục thất bại: " + e.getMessage());
        }
        return list;
    }

    public DanhMucLinhKien findById(int maDanhMuc) {
        String sql = "SELECT MaDanhMuc, TenDanhMuc, MaDanhMucCha, MoTa, TrangThai FROM DanhMucLinhKien WHERE MaDanhMuc = ?";

        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, maDanhMuc);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToDanhMuc(rs);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[DAO LỖI] Tìm danh mục theo ID thất bại: " + e.getMessage());
        }
        return null;
    }

    private DanhMucLinhKien mapResultSetToDanhMuc(ResultSet rs) throws SQLException {
        int ma = rs.getInt("MaDanhMuc");
        String ten = rs.getString("TenDanhMuc");
        int chaInt = rs.getInt("MaDanhMucCha");
        Integer cha = rs.wasNull() ? null : chaInt;
        String moTa = rs.getString("MoTa");
        String trangThai = rs.getString("TrangThai");

        return new DanhMucLinhKien(ma, ten, cha, moTa, trangThai);
    }
}

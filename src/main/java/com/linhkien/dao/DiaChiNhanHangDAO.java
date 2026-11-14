package com.linhkien.dao;

import com.linhkien.config.DatabaseConnection;
import com.linhkien.model.DiaChiNhanHang;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp truy xuất dữ liệu sổ địa chỉ nhận hàng (DiaChiNhanHang).
 */
public class DiaChiNhanHangDAO {

    private static final String SELECT_ALL = """
        SELECT MaDiaChi, MaNguoiDung, TenNguoiNhan, SoDienThoaiNhan,
               DiaChiChiTiet, PhuongXa, TinhThanh, LaMacDinh
        FROM DiaChiNhanHang
    """;

    public List<DiaChiNhanHang> findByNguoiDung(int maNguoiDung) throws SQLException {
        String sql = SELECT_ALL + " WHERE MaNguoiDung = ? ORDER BY LaMacDinh DESC, MaDiaChi DESC";
        List<DiaChiNhanHang> list = new ArrayList<>();
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maNguoiDung);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public DiaChiNhanHang findById(int maDiaChi) throws SQLException {
        String sql = SELECT_ALL + " WHERE MaDiaChi = ?";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maDiaChi);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public DiaChiNhanHang findMacDinh(int maNguoiDung) throws SQLException {
        String sql = SELECT_ALL + " WHERE MaNguoiDung = ? AND LaMacDinh = 1 ORDER BY MaDiaChi DESC";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maNguoiDung);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public int insert(DiaChiNhanHang dc) throws SQLException {
        String sql = """
            INSERT INTO DiaChiNhanHang (MaNguoiDung, TenNguoiNhan, SoDienThoaiNhan,
                                        DiaChiChiTiet, PhuongXa, TinhThanh, LaMacDinh)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, dc.getMaNguoiDung());
            ps.setString(2, dc.getTenNguoiNhan());
            ps.setString(3, dc.getSoDienThoaiNhan());
            ps.setString(4, dc.getDiaChiChiTiet());
            ps.setString(5, dc.getPhuongXa());
            ps.setString(6, dc.getTinhThanh());
            ps.setBoolean(7, dc.isLaMacDinh());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    dc.setMaDiaChi(rs.getInt(1));
                }
            }
        }
        return dc.getMaDiaChi();
    }

    public boolean update(DiaChiNhanHang dc) throws SQLException {
        String sql = """
            UPDATE DiaChiNhanHang
            SET TenNguoiNhan = ?, SoDienThoaiNhan = ?, DiaChiChiTiet = ?,
                PhuongXa = ?, TinhThanh = ?, LaMacDinh = ?
            WHERE MaDiaChi = ? AND MaNguoiDung = ?
        """;
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dc.getTenNguoiNhan());
            ps.setString(2, dc.getSoDienThoaiNhan());
            ps.setString(3, dc.getDiaChiChiTiet());
            ps.setString(4, dc.getPhuongXa());
            ps.setString(5, dc.getTinhThanh());
            ps.setBoolean(6, dc.isLaMacDinh());
            ps.setInt(7, dc.getMaDiaChi());
            ps.setInt(8, dc.getMaNguoiDung());
            return ps.executeUpdate() > 0;
        }
    }

    public void boMacDinhTatCa(int maNguoiDung) throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE DiaChiNhanHang SET LaMacDinh = 0 WHERE MaNguoiDung = ?")) {
            ps.setInt(1, maNguoiDung);
            ps.executeUpdate();
        }
    }

    public boolean datMacDinh(int maNguoiDung, int maDiaChi) throws SQLException {
        boMacDinhTatCa(maNguoiDung);
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE DiaChiNhanHang SET LaMacDinh = 1 WHERE MaDiaChi = ? AND MaNguoiDung = ?")) {
            ps.setInt(1, maDiaChi);
            ps.setInt(2, maNguoiDung);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int maDiaChi) throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM DiaChiNhanHang WHERE MaDiaChi = ?")) {
            ps.setInt(1, maDiaChi);
            return ps.executeUpdate() > 0;
        }
    }

    private DiaChiNhanHang mapRow(ResultSet rs) throws SQLException {
        DiaChiNhanHang dc = new DiaChiNhanHang();
        dc.setMaDiaChi(rs.getInt("MaDiaChi"));
        dc.setMaNguoiDung(rs.getInt("MaNguoiDung"));
        dc.setTenNguoiNhan(rs.getString("TenNguoiNhan"));
        dc.setSoDienThoaiNhan(rs.getString("SoDienThoaiNhan"));
        dc.setDiaChiChiTiet(rs.getString("DiaChiChiTiet"));
        dc.setPhuongXa(rs.getString("PhuongXa"));
        dc.setTinhThanh(rs.getString("TinhThanh"));
        dc.setLaMacDinh(rs.getBoolean("LaMacDinh"));
        return dc;
    }
}

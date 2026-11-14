package com.linhkien.dao;

import com.linhkien.config.DatabaseConnection;
import com.linhkien.model.MaGiamGia;
import com.linhkien.util.JdbcHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp truy xuất dữ liệu mã giảm giá (MaGiamGia).
 */
public class MaGiamGiaDAO {

    private static final String SELECT_ALL = """
        SELECT MaGiamGia, MaCode, LoaiGiam, GiaTriGiam, DonHangToiThieu,
               SoLuotToiDa, SoLuotDaDung, NgayBatDau, NgayKetThuc
        FROM MaGiamGia
    """;

    public List<MaGiamGia> findAll() throws SQLException {
        String sql = SELECT_ALL + " ORDER BY MaGiamGia DESC";
        List<MaGiamGia> list = new ArrayList<>();
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public MaGiamGia findById(int maGiamGia) throws SQLException {
        String sql = SELECT_ALL + " WHERE MaGiamGia = ?";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maGiamGia);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public MaGiamGia findByCode(String maCode) throws SQLException {
        if (maCode == null || maCode.isBlank()) {
            return null;
        }
        String sql = SELECT_ALL + " WHERE UPPER(MaCode) = UPPER(?)";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maCode.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public int insert(MaGiamGia mg) throws SQLException {
        String sql = """
            INSERT INTO MaGiamGia (MaCode, LoaiGiam, GiaTriGiam, DonHangToiThieu,
                                   SoLuotToiDa, SoLuotDaDung, NgayBatDau, NgayKetThuc)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, mg.getMaCode().trim().toUpperCase());
            ps.setString(2, mg.getLoaiGiam());
            ps.setBigDecimal(3, mg.getGiaTriGiam());
            ps.setBigDecimal(4, mg.getDonHangToiThieu());
            JdbcHelper.setSoNguyenHoacNull(ps, 5, mg.getSoLuotToiDa());
            ps.setInt(6, mg.getSoLuotDaDung());
            JdbcHelper.setThoiGian(ps, 7, mg.getNgayBatDau());
            JdbcHelper.setThoiGian(ps, 8, mg.getNgayKetThuc());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    mg.setMaGiamGia(rs.getInt(1));
                }
            }
        }
        return mg.getMaGiamGia();
    }

    public boolean update(MaGiamGia mg) throws SQLException {
        String sql = """
            UPDATE MaGiamGia
            SET MaCode = ?, LoaiGiam = ?, GiaTriGiam = ?, DonHangToiThieu = ?,
                SoLuotToiDa = ?, NgayBatDau = ?, NgayKetThuc = ?
            WHERE MaGiamGia = ?
        """;
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, mg.getMaCode().trim().toUpperCase());
            ps.setString(2, mg.getLoaiGiam());
            ps.setBigDecimal(3, mg.getGiaTriGiam());
            ps.setBigDecimal(4, mg.getDonHangToiThieu());
            JdbcHelper.setSoNguyenHoacNull(ps, 5, mg.getSoLuotToiDa());
            JdbcHelper.setThoiGian(ps, 6, mg.getNgayBatDau());
            JdbcHelper.setThoiGian(ps, 7, mg.getNgayKetThuc());
            ps.setInt(8, mg.getMaGiamGia());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean tangSoLuotDung(int maGiamGia) throws SQLException {
        String sql = """
            UPDATE MaGiamGia
            SET SoLuotDaDung = SoLuotDaDung + 1
            WHERE MaGiamGia = ? AND (SoLuotToiDa IS NULL OR SoLuotDaDung < SoLuotToiDa)
        """;
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maGiamGia);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean giamSoLuotDung(int maGiamGia) throws SQLException {
        String sql = """
            UPDATE MaGiamGia
            SET SoLuotDaDung = SoLuotDaDung - 1
            WHERE MaGiamGia = ? AND SoLuotDaDung > 0
        """;
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maGiamGia);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int maGiamGia) throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM MaGiamGia WHERE MaGiamGia = ?")) {
            ps.setInt(1, maGiamGia);
            return ps.executeUpdate() > 0;
        }
    }

    private MaGiamGia mapRow(ResultSet rs) throws SQLException {
        MaGiamGia mg = new MaGiamGia();
        mg.setMaGiamGia(rs.getInt("MaGiamGia"));
        mg.setMaCode(rs.getString("MaCode"));
        mg.setLoaiGiam(rs.getString("LoaiGiam"));
        mg.setGiaTriGiam(rs.getBigDecimal("GiaTriGiam"));
        mg.setDonHangToiThieu(rs.getBigDecimal("DonHangToiThieu"));
        int max = rs.getInt("SoLuotToiDa");
        mg.setSoLuotToiDa(rs.wasNull() ? null : max);
        mg.setSoLuotDaDung(rs.getInt("SoLuotDaDung"));
        mg.setNgayBatDau(JdbcHelper.docThoiGian(rs, "NgayBatDau"));
        mg.setNgayKetThuc(JdbcHelper.docThoiGian(rs, "NgayKetThuc"));
        return mg;
    }
}

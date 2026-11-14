package com.linhkien.dao;

import com.linhkien.config.DatabaseConnection;
import com.linhkien.model.LichSuTonKho;
import com.linhkien.model.LinhKien;
import com.linhkien.model.ThongSoKyThuat;
import com.linhkien.util.JdbcHelper;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp truy xuất dữ liệu LinhKien (Data Access Object).
 * Thực hiện các chức năng: Thêm, Sửa, Xóa, Tìm kiếm theo mã hoặc tên linh kiện.
 */
public class LinhKienDAO {

    /**
     * Lấy toàn bộ danh sách linh kiện trong CSDL (kèm tên danh mục).
     */
    public List<LinhKien> findAll() {
        List<LinhKien> list = new ArrayList<>();
        String sql = """
            SELECT lk.MaLinhKien, lk.MaSKU, lk.TenLinhKien, lk.MaDanhMuc, dm.TenDanhMuc,
                   lk.NhaSanXuat, lk.MoTa, lk.KieuDongGoi, lk.DienApHoatDong, lk.GiaoTiep,
                   lk.DuongDanDatasheet, lk.HinhAnh, lk.DonViTinh, lk.GiaBan,
                   lk.SoLuongTon, lk.NguongTonToiThieu, lk.TrangThaiBan, lk.NgayCapNhat
            FROM LinhKien lk
            LEFT JOIN DanhMucLinhKien dm ON lk.MaDanhMuc = dm.MaDanhMuc
            ORDER BY lk.MaLinhKien ASC
        """;

        try {
            Connection conn = DatabaseConnection.getConnection();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    list.add(mapResultSetToLinhKien(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[DAO LỖI] Lấy danh sách linh kiện thất bại: " + e.getMessage());
        }
        return list;
    }

    /**
     * Lấy danh sách linh kiện đang được phép bán (TrangThaiBan = 'DANG_BAN').
     */
    public List<LinhKien> findAllActive() {
        List<LinhKien> list = new ArrayList<>();
        String sql = """
            SELECT lk.MaLinhKien, lk.MaSKU, lk.TenLinhKien, lk.MaDanhMuc, dm.TenDanhMuc,
                   lk.NhaSanXuat, lk.MoTa, lk.KieuDongGoi, lk.DienApHoatDong, lk.GiaoTiep,
                   lk.DuongDanDatasheet, lk.HinhAnh, lk.DonViTinh, lk.GiaBan,
                   lk.SoLuongTon, lk.NguongTonToiThieu, lk.TrangThaiBan, lk.NgayCapNhat
            FROM LinhKien lk
            LEFT JOIN DanhMucLinhKien dm ON lk.MaDanhMuc = dm.MaDanhMuc
            WHERE lk.TrangThaiBan = 'DANG_BAN'
            ORDER BY lk.MaLinhKien ASC
        """;

        try {
            Connection conn = DatabaseConnection.getConnection();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    list.add(mapResultSetToLinhKien(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[DAO LỖI] Lấy danh sách linh kiện đang bán thất bại: " + e.getMessage());
        }
        return list;
    }

    /**
     * Tìm kiếm linh kiện theo mã số ID (MaLinhKien).
     */
    public LinhKien findById(int maLinhKien) {
        String sql = """
            SELECT lk.MaLinhKien, lk.MaSKU, lk.TenLinhKien, lk.MaDanhMuc, dm.TenDanhMuc,
                   lk.NhaSanXuat, lk.MoTa, lk.KieuDongGoi, lk.DienApHoatDong, lk.GiaoTiep,
                   lk.DuongDanDatasheet, lk.HinhAnh, lk.DonViTinh, lk.GiaBan,
                   lk.SoLuongTon, lk.NguongTonToiThieu, lk.TrangThaiBan, lk.NgayCapNhat
            FROM LinhKien lk
            LEFT JOIN DanhMucLinhKien dm ON lk.MaDanhMuc = dm.MaDanhMuc
            WHERE lk.MaLinhKien = ?
        """;

        LinhKien lk = null;
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, maLinhKien);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        lk = mapResultSetToLinhKien(rs);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[DAO LỖI] Tìm linh kiện theo ID thất bại: " + e.getMessage());
            return null;
        }
        if (lk != null) {
            lk.setDanhSachThongSo(findThongSoByLinhKien(maLinhKien));
        }
        return lk;
    }

    /**
     * Tìm kiếm linh kiện theo mã SKU.
     */
    public LinhKien findBySKU(String maSKU) {
        String sql = """
            SELECT lk.MaLinhKien, lk.MaSKU, lk.TenLinhKien, lk.MaDanhMuc, dm.TenDanhMuc,
                   lk.NhaSanXuat, lk.MoTa, lk.KieuDongGoi, lk.DienApHoatDong, lk.GiaoTiep,
                   lk.DuongDanDatasheet, lk.HinhAnh, lk.DonViTinh, lk.GiaBan,
                   lk.SoLuongTon, lk.NguongTonToiThieu, lk.TrangThaiBan, lk.NgayCapNhat
            FROM LinhKien lk
            LEFT JOIN DanhMucLinhKien dm ON lk.MaDanhMuc = dm.MaDanhMuc
            WHERE UPPER(lk.MaSKU) = UPPER(?)
        """;

        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, maSKU.trim());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToLinhKien(rs);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[DAO LỖI] Tìm linh kiện theo SKU thất bại: " + e.getMessage());
        }
        return null;
    }

    /**
     * Tìm kiếm linh kiện theo từ khóa: mã ID, mã SKU hoặc tên linh kiện (khớp gần đúng LIKE).
     * @param keyword Từ khóa tìm kiếm
     */
    public List<LinhKien> searchByMaOrTen(String keyword) {
        List<LinhKien> list = new ArrayList<>();
        if (keyword == null || keyword.trim().isEmpty()) {
            return findAll();
        }

        String search = "%" + keyword.trim() + "%";
        Integer parsedId = null;
        try {
            parsedId = Integer.parseInt(keyword.trim());
        } catch (NumberFormatException ignored) {
        }

        String sql = """
            SELECT lk.MaLinhKien, lk.MaSKU, lk.TenLinhKien, lk.MaDanhMuc, dm.TenDanhMuc,
                   lk.NhaSanXuat, lk.MoTa, lk.KieuDongGoi, lk.DienApHoatDong, lk.GiaoTiep,
                   lk.DuongDanDatasheet, lk.HinhAnh, lk.DonViTinh, lk.GiaBan,
                   lk.SoLuongTon, lk.NguongTonToiThieu, lk.TrangThaiBan, lk.NgayCapNhat
            FROM LinhKien lk
            LEFT JOIN DanhMucLinhKien dm ON lk.MaDanhMuc = dm.MaDanhMuc
            WHERE lk.MaSKU LIKE ? 
               OR lk.TenLinhKien LIKE ?
               OR lk.MaLinhKien = ?
            ORDER BY lk.MaLinhKien ASC
        """;

        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, search);
                ps.setString(2, search);
                if (parsedId != null) {
                    ps.setInt(3, parsedId);
                } else {
                    ps.setInt(3, -1);
                }

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapResultSetToLinhKien(rs));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[DAO LỖI] Tìm kiếm linh kiện thất bại: " + e.getMessage());
        }
        return list;
    }

    /**
     * Thêm mới một linh kiện vào CSDL.
     * @param lk Đối tượng linh kiện cần thêm
     * @return true nếu thêm thành công, false nếu thất bại
     */
    public boolean insert(LinhKien lk) {
        String sql = """
            INSERT INTO LinhKien (
                MaSKU, TenLinhKien, MaDanhMuc, NhaSanXuat, MoTa,
                KieuDongGoi, DienApHoatDong, GiaoTiep, DuongDanDatasheet, HinhAnh,
                DonViTinh, GiaBan, SoLuongTon, NguongTonToiThieu, TrangThaiBan, NgayCapNhat
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, lk.getMaSKU());
                ps.setString(2, lk.getTenLinhKien());
                ps.setInt(3, lk.getMaDanhMuc());
                ps.setString(4, lk.getNhaSanXuat());
                ps.setString(5, lk.getMoTa());
                ps.setString(6, lk.getKieuDongGoi());
                ps.setString(7, lk.getDienApHoatDong());
                ps.setString(8, lk.getGiaoTiep());
                ps.setString(9, lk.getDuongDanDatasheet());
                ps.setString(10, lk.getHinhAnh());
                ps.setString(11, lk.getDonViTinh() != null ? lk.getDonViTinh() : "Cái");
                ps.setBigDecimal(12, lk.getGiaBan() != null ? lk.getGiaBan() : BigDecimal.ZERO);
                ps.setInt(13, lk.getSoLuongTon());
                ps.setInt(14, lk.getNguongTonToiThieu());
                ps.setString(15, lk.getTrangThaiBan() != null ? lk.getTrangThaiBan() : "DANG_BAN");

                String dbType = DatabaseConnection.getActiveDbType();
                if ("sqlite".equalsIgnoreCase(dbType)) {
                    ps.setString(16, LocalDateTime.now().toString());
                } else {
                    ps.setTimestamp(16, Timestamp.valueOf(LocalDateTime.now()));
                }

                int affected = ps.executeUpdate();
                if (affected > 0) {
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            lk.setMaLinhKien(rs.getInt(1));
                        }
                    }
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("[DAO LỖI] Thêm linh kiện thất bại: " + e.getMessage());
        }
        return false;
    }

    /**
     * Cập nhật thông tin linh kiện.
     * @param lk Đối tượng linh kiện đã sửa đổi
     * @return true nếu cập nhật thành công
     */
    public boolean update(LinhKien lk) {
        String sql = """
            UPDATE LinhKien
            SET MaSKU = ?, TenLinhKien = ?, MaDanhMuc = ?, NhaSanXuat = ?, MoTa = ?,
                KieuDongGoi = ?, DienApHoatDong = ?, GiaoTiep = ?, DuongDanDatasheet = ?, HinhAnh = ?,
                DonViTinh = ?, GiaBan = ?, SoLuongTon = ?, NguongTonToiThieu = ?, TrangThaiBan = ?, NgayCapNhat = ?
            WHERE MaLinhKien = ?
        """;

        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, lk.getMaSKU());
                ps.setString(2, lk.getTenLinhKien());
                ps.setInt(3, lk.getMaDanhMuc());
                ps.setString(4, lk.getNhaSanXuat());
                ps.setString(5, lk.getMoTa());
                ps.setString(6, lk.getKieuDongGoi());
                ps.setString(7, lk.getDienApHoatDong());
                ps.setString(8, lk.getGiaoTiep());
                ps.setString(9, lk.getDuongDanDatasheet());
                ps.setString(10, lk.getHinhAnh());
                ps.setString(11, lk.getDonViTinh());
                ps.setBigDecimal(12, lk.getGiaBan());
                ps.setInt(13, lk.getSoLuongTon());
                ps.setInt(14, lk.getNguongTonToiThieu());
                ps.setString(15, lk.getTrangThaiBan());

                String dbType = DatabaseConnection.getActiveDbType();
                if ("sqlite".equalsIgnoreCase(dbType)) {
                    ps.setString(16, LocalDateTime.now().toString());
                } else {
                    ps.setTimestamp(16, Timestamp.valueOf(LocalDateTime.now()));
                }

                ps.setInt(17, lk.getMaLinhKien());

                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("[DAO LỖI] Cập nhật linh kiện thất bại: " + e.getMessage());
        }
        return false;
    }

    /**
     * Xóa linh kiện khỏi hệ thống.
     * Thử xóa vật lý; nếu gặp ràng buộc khóa ngoại (đã có trong giỏ hoặc đơn hàng)
     * thì tự động chuyển sang Xóa mềm (ngừng bán: TrangThaiBan = 'NGUNG_BAN') theo quy tắc Section 2.6.4.
     * @param maLinhKien Mã linh kiện cần xóa
     * @return true nếu xóa hoặc chuyển trạng thái thành công
     */
    public boolean delete(int maLinhKien) {
        // Thử xóa vật lý
        String sqlDelete = "DELETE FROM LinhKien WHERE MaLinhKien = ?";
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sqlDelete)) {
                ps.setInt(1, maLinhKien);
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    return true;
                }
            }
        } catch (SQLException e) {
            // Ràng buộc khóa ngoại hoặc lỗi xóa vật lý -> Áp dụng soft-delete theo thiết kế
            System.out.println("[DAO THÔNG BÁO] Linh kiện có dữ liệu ràng buộc, chuyển sang ngừng bán (soft delete)...");
            return softDelete(maLinhKien);
        }
        return false;
    }

    /**
     * Chuyển trạng thái linh kiện sang 'NGUNG_BAN' (soft delete).
     */
    public boolean softDelete(int maLinhKien) {
        String sql = "UPDATE LinhKien SET TrangThaiBan = 'NGUNG_BAN' WHERE MaLinhKien = ?";
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, maLinhKien);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("[DAO LỖI] Ngừng bán linh kiện thất bại: " + e.getMessage());
        }
        return false;
    }

    /**
     * Cập nhật số lượng tồn kho.
     */
    public boolean updateSoLuongTon(int maLinhKien, int soLuongMoi) {
        String sql = "UPDATE LinhKien SET SoLuongTon = ? WHERE MaLinhKien = ?";
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, soLuongMoi);
                ps.setInt(2, maLinhKien);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("[DAO LỖI] Cập nhật tồn kho thất bại: " + e.getMessage());
        }
        return false;
    }

    /**
     * Cộng hoặc trừ tồn kho nguyên tử (đảm bảo không âm).
     */
    public boolean tangSoLuongTon(int maLinhKien, int delta) throws SQLException {
        String sql = "UPDATE LinhKien SET SoLuongTon = SoLuongTon + ?, NgayCapNhat = ? "
            + "WHERE MaLinhKien = ? AND SoLuongTon + ? >= 0";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, delta);
            JdbcHelper.setThoiGian(ps, 2, LocalDateTime.now());
            ps.setInt(3, maLinhKien);
            ps.setInt(4, delta);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Lấy danh sách thông số kỹ thuật mở rộng của linh kiện.
     */
    public List<ThongSoKyThuat> findThongSoByLinhKien(int maLinhKien) {
        List<ThongSoKyThuat> list = new ArrayList<>();
        String sql = """
            SELECT MaThongSo, MaLinhKien, TenThongSo, GiaTri, DonVi
            FROM ThongSoKyThuat
            WHERE MaLinhKien = ?
            ORDER BY MaThongSo ASC
        """;
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, maLinhKien);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        ThongSoKyThuat ts = new ThongSoKyThuat();
                        ts.setMaThongSo(rs.getInt("MaThongSo"));
                        ts.setMaLinhKien(rs.getInt("MaLinhKien"));
                        ts.setTenThongSo(rs.getString("TenThongSo"));
                        ts.setGiaTri(rs.getString("GiaTri"));
                        ts.setDonVi(rs.getString("DonVi"));
                        list.add(ts);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[DAO LỖI] Lấy thông số kỹ thuật thất bại: " + e.getMessage());
        }
        return list;
    }

    /**
     * Thay thế toàn bộ thông số kỹ thuật mở rộng của một linh kiện.
     */
    public void replaceThongSo(int maLinhKien, List<ThongSoKyThuat> danhSach) throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement del = conn.prepareStatement("DELETE FROM ThongSoKyThuat WHERE MaLinhKien = ?")) {
            del.setInt(1, maLinhKien);
            del.executeUpdate();
        }
        if (danhSach == null || danhSach.isEmpty()) {
            return;
        }
        String insertSql = "INSERT INTO ThongSoKyThuat (MaLinhKien, TenThongSo, GiaTri, DonVi) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ins = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            for (ThongSoKyThuat ts : danhSach) {
                if (ts == null || ts.getTenThongSo() == null || ts.getTenThongSo().isBlank()
                        || ts.getGiaTri() == null || ts.getGiaTri().isBlank()) {
                    continue;
                }
                ts.setMaLinhKien(maLinhKien);
                ins.setInt(1, maLinhKien);
                ins.setString(2, ts.getTenThongSo().trim());
                ins.setString(3, ts.getGiaTri().trim());
                ins.setString(4, ts.getDonVi() != null && !ts.getDonVi().isBlank() ? ts.getDonVi().trim() : null);
                ins.executeUpdate();
                try (ResultSet keys = ins.getGeneratedKeys()) {
                    if (keys.next()) {
                        ts.setMaThongSo(keys.getInt(1));
                    }
                }
            }
        }
    }

    /**
     * Truy vấn lịch sử biến động tồn kho (NFR03), sắp xếp mới nhất trước.
     */
    public List<LichSuTonKho> findLichSuTonKho(Integer maLinhKien) throws SQLException {
        StringBuilder sql = new StringBuilder("""
            SELECT ls.MaLichSu, ls.MaLinhKien, lk.TenLinhKien, lk.MaSKU,
                   ls.LoaiBienDong, ls.SoLuongThayDoi, ls.TonTruoc, ls.TonSau,
                   ls.MaDonHang, ls.MaNguoiThucHien, nd.HoTen AS TenNguoiThucHien,
                   ls.LyDo, ls.ThoiDiem
            FROM LichSuTonKho ls
            JOIN LinhKien lk ON ls.MaLinhKien = lk.MaLinhKien
            LEFT JOIN NguoiDung nd ON ls.MaNguoiThucHien = nd.MaNguoiDung
        """);
        if (maLinhKien != null) {
            sql.append(" WHERE ls.MaLinhKien = ?");
        }
        sql.append(" ORDER BY ls.MaLichSu DESC");

        List<LichSuTonKho> list = new ArrayList<>();
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            if (maLinhKien != null) {
                ps.setInt(1, maLinhKien);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LichSuTonKho item = new LichSuTonKho();
                    item.setMaLichSu(rs.getLong("MaLichSu"));
                    item.setMaLinhKien(rs.getInt("MaLinhKien"));
                    item.setTenLinhKien(rs.getString("TenLinhKien"));
                    item.setMaSKU(rs.getString("MaSKU"));
                    item.setLoaiBienDong(rs.getString("LoaiBienDong"));
                    item.setSoLuongThayDoi(rs.getInt("SoLuongThayDoi"));
                    item.setTonTruoc(rs.getInt("TonTruoc"));
                    item.setTonSau(rs.getInt("TonSau"));
                    int maDon = rs.getInt("MaDonHang");
                    item.setMaDonHang(rs.wasNull() ? null : maDon);
                    int nguoi = rs.getInt("MaNguoiThucHien");
                    item.setMaNguoiThucHien(rs.wasNull() ? null : nguoi);
                    item.setTenNguoiThucHien(rs.getString("TenNguoiThucHien"));
                    item.setLyDo(rs.getString("LyDo"));
                    item.setThoiDiem(JdbcHelper.docThoiGian(rs, "ThoiDiem"));
                    list.add(item);
                }
            }
        }
        return list;
    }

    /**
     * Lấy các linh kiện có mức tồn kho thấp (soLuongTon <= nguongTonToiThieu).
     */
    public List<LinhKien> findLowStock() {
        List<LinhKien> list = new ArrayList<>();
        String sql = """
            SELECT lk.MaLinhKien, lk.MaSKU, lk.TenLinhKien, lk.MaDanhMuc, dm.TenDanhMuc,
                   lk.NhaSanXuat, lk.MoTa, lk.KieuDongGoi, lk.DienApHoatDong, lk.GiaoTiep,
                   lk.DuongDanDatasheet, lk.HinhAnh, lk.DonViTinh, lk.GiaBan,
                   lk.SoLuongTon, lk.NguongTonToiThieu, lk.TrangThaiBan, lk.NgayCapNhat
            FROM LinhKien lk
            LEFT JOIN DanhMucLinhKien dm ON lk.MaDanhMuc = dm.MaDanhMuc
            WHERE lk.SoLuongTon <= lk.NguongTonToiThieu
            ORDER BY lk.SoLuongTon ASC
        """;

        try {
            Connection conn = DatabaseConnection.getConnection();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    list.add(mapResultSetToLinhKien(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[DAO LỖI] Lấy danh sách tồn kho thấp thất bại: " + e.getMessage());
        }
        return list;
    }

    private LinhKien mapResultSetToLinhKien(ResultSet rs) throws SQLException {
        LinhKien lk = new LinhKien();
        lk.setMaLinhKien(rs.getInt("MaLinhKien"));
        lk.setMaSKU(rs.getString("MaSKU"));
        lk.setTenLinhKien(rs.getString("TenLinhKien"));
        lk.setMaDanhMuc(rs.getInt("MaDanhMuc"));
        lk.setTenDanhMuc(rs.getString("TenDanhMuc"));
        lk.setNhaSanXuat(rs.getString("NhaSanXuat"));
        lk.setMoTa(rs.getString("MoTa"));
        lk.setKieuDongGoi(rs.getString("KieuDongGoi"));
        lk.setDienApHoatDong(rs.getString("DienApHoatDong"));
        lk.setGiaoTiep(rs.getString("GiaoTiep"));
        lk.setDuongDanDatasheet(rs.getString("DuongDanDatasheet"));
        lk.setHinhAnh(rs.getString("HinhAnh"));
        lk.setDonViTinh(rs.getString("DonViTinh"));
        lk.setGiaBan(rs.getBigDecimal("GiaBan"));
        lk.setSoLuongTon(rs.getInt("SoLuongTon"));
        lk.setNguongTonToiThieu(rs.getInt("NguongTonToiThieu"));
        lk.setTrangThaiBan(rs.getString("TrangThaiBan"));

        return lk;
    }
}

package com.linhkien.dao;

import com.linhkien.config.DatabaseConnection;
import com.linhkien.model.ChiTietGioHang;
import com.linhkien.model.GioHang;
import com.linhkien.model.LinhKien;

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
 * Lớp truy xuất dữ liệu Giỏ hàng và Chi tiết giỏ hàng (DAO).
 * Thực hiện: Thêm linh kiện vào giỏ, cập nhật số lượng, xóa khỏi giỏ, tính tổng tiền.
 */
public class GioHangDAO {

    /**
     * Lấy hoặc tự động tạo mới giỏ hàng cho người dùng.
     * Mỗi khách hàng có duy nhất 1 giỏ hàng hoạt động (UC06).
     * @param maNguoiDung Mã người dùng
     * @return Đối tượng GioHang chứa đầy đủ danh sách chi tiết
     */
    public GioHang getOrCreateGioHangByNguoiDung(int maNguoiDung) {
        GioHang gioHang = findByMaNguoiDung(maNguoiDung);
        if (gioHang == null) {
            gioHang = createGioHang(maNguoiDung);
        }
        if (gioHang != null) {
            List<ChiTietGioHang> chiTietList = getChiTietGioHang(gioHang.getMaGioHang());
            gioHang.setDanhSachChiTiet(chiTietList);
        }
        return gioHang;
    }

    /**
     * Tìm giỏ hàng theo mã người dùng.
     */
    public GioHang findByMaNguoiDung(int maNguoiDung) {
        String sql = "SELECT MaGioHang, MaNguoiDung, NgayTao, NgayCapNhat FROM GioHang WHERE MaNguoiDung = ?";

        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, maNguoiDung);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        GioHang gh = new GioHang();
                        gh.setMaGioHang(rs.getInt("MaGioHang"));
                        gh.setMaNguoiDung(rs.getInt("MaNguoiDung"));
                        return gh;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[DAO LỖI] Tìm giỏ hàng theo mã người dùng thất bại: " + e.getMessage());
        }
        return null;
    }

    /**
     * Tạo giỏ hàng mới cho người dùng.
     */
    public GioHang createGioHang(int maNguoiDung) {
        String sql = "INSERT INTO GioHang (MaNguoiDung, NgayTao, NgayCapNhat) VALUES (?, ?, ?)";

        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, maNguoiDung);

                String dbType = DatabaseConnection.getActiveDbType();
                if ("sqlite".equalsIgnoreCase(dbType)) {
                    String nowStr = LocalDateTime.now().toString();
                    ps.setString(2, nowStr);
                    ps.setString(3, nowStr);
                } else {
                    Timestamp nowTs = Timestamp.valueOf(LocalDateTime.now());
                    ps.setTimestamp(2, nowTs);
                    ps.setTimestamp(3, nowTs);
                }

                int affected = ps.executeUpdate();
                if (affected > 0) {
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            GioHang gh = new GioHang(rs.getInt(1), maNguoiDung);
                            return gh;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[DAO LỖI] Tạo giỏ hàng thất bại: " + e.getMessage());
        }
        return null;
    }

    /**
     * Lấy toàn bộ danh sách các dòng chi tiết trong giỏ hàng (kèm thông tin LinhKien liên kết).
     */
    public List<ChiTietGioHang> getChiTietGioHang(int maGioHang) {
        List<ChiTietGioHang> list = new ArrayList<>();
        String sql = """
            SELECT ct.MaGioHang, ct.MaLinhKien, ct.SoLuong, ct.NgayThem,
                   lk.MaSKU, lk.TenLinhKien, lk.GiaBan, lk.SoLuongTon, lk.DonViTinh, lk.TrangThaiBan
            FROM ChiTietGioHang ct
            JOIN LinhKien lk ON ct.MaLinhKien = lk.MaLinhKien
            WHERE ct.MaGioHang = ?
            ORDER BY ct.NgayThem ASC
        """;

        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, maGioHang);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        ChiTietGioHang line = new ChiTietGioHang();
                        line.setMaGioHang(rs.getInt("MaGioHang"));
                        line.setMaLinhKien(rs.getInt("MaLinhKien"));
                        line.setSoLuong(rs.getInt("SoLuong"));

                        // Gán thông tin đối tượng LinhKien để hiển thị và tính toán
                        LinhKien lk = new LinhKien();
                        lk.setMaLinhKien(rs.getInt("MaLinhKien"));
                        lk.setMaSKU(rs.getString("MaSKU"));
                        lk.setTenLinhKien(rs.getString("TenLinhKien"));
                        lk.setGiaBan(rs.getBigDecimal("GiaBan"));
                        lk.setSoLuongTon(rs.getInt("SoLuongTon"));
                        lk.setDonViTinh(rs.getString("DonViTinh"));
                        lk.setTrangThaiBan(rs.getString("TrangThaiBan"));

                        line.setLinhKien(lk);
                        list.add(line);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[DAO LỖI] Lấy chi tiết giỏ hàng thất bại: " + e.getMessage());
        }
        return list;
    }

    /**
     * Thêm linh kiện vào giỏ hàng (UC06.1).
     * Nếu linh kiện đã tồn tại trong giỏ thì tăng thêm số lượng (cộng dồn).
     * Ngược lại thì tạo dòng chi tiết mới.
     * @param maGioHang Mã giỏ hàng
     * @param maLinhKien Mã linh kiện
     * @param soLuong Số lượng thêm
     * @return true nếu thêm thành công
     */
    public boolean themVaoGio(int maGioHang, int maLinhKien, int soLuong) {
        if (soLuong <= 0) {
            return false;
        }

        // Kiểm tra xem dòng này đã có trong giỏ chưa
        String checkSql = "SELECT SoLuong FROM ChiTietGioHang WHERE MaGioHang = ? AND MaLinhKien = ?";
        try {
            Connection conn = DatabaseConnection.getConnection();
            Integer currentQuantity = null;

            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setInt(1, maGioHang);
                ps.setInt(2, maLinhKien);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        currentQuantity = rs.getInt("SoLuong");
                    }
                }
            }

            if (currentQuantity != null) {
                // Đã có -> Cập nhật cộng dồn số lượng
                int newQuantity = currentQuantity + soLuong;
                return capNhatSoLuong(maGioHang, maLinhKien, newQuantity);
            } else {
                // Chưa có -> Chèn dòng mới
                String insertSql = "INSERT INTO ChiTietGioHang (MaGioHang, MaLinhKien, SoLuong, NgayThem) VALUES (?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    ps.setInt(1, maGioHang);
                    ps.setInt(2, maLinhKien);
                    ps.setInt(3, soLuong);

                    String dbType = DatabaseConnection.getActiveDbType();
                    if ("sqlite".equalsIgnoreCase(dbType)) {
                        ps.setString(4, LocalDateTime.now().toString());
                    } else {
                        ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                    }

                    int affected = ps.executeUpdate();
                    if (affected > 0) {
                        capNhatNgayGioHang(maGioHang);
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[DAO LỖI] Thêm linh kiện vào giỏ thất bại: " + e.getMessage());
        }
        return false;
    }

    /**
     * Cập nhật số lượng của linh kiện trong giỏ hàng (UC06.2).
     * Nếu số lượng mới <= 0 thì tự động xóa linh kiện đó khỏi giỏ.
     * @param maGioHang Mã giỏ hàng
     * @param maLinhKien Mã linh kiện
     * @param soLuongMoi Số lượng mới
     * @return true nếu cập nhật thành công
     */
    public boolean capNhatSoLuong(int maGioHang, int maLinhKien, int soLuongMoi) {
        if (soLuongMoi <= 0) {
            return xoaKhoiGio(maGioHang, maLinhKien);
        }

        String sql = "UPDATE ChiTietGioHang SET SoLuong = ? WHERE MaGioHang = ? AND MaLinhKien = ?";
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, soLuongMoi);
                ps.setInt(2, maGioHang);
                ps.setInt(3, maLinhKien);
                int affected = ps.executeUpdate();
                if (affected > 0) {
                    capNhatNgayGioHang(maGioHang);
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("[DAO LỖI] Cập nhật số lượng giỏ hàng thất bại: " + e.getMessage());
        }
        return false;
    }

    /**
     * Xóa một linh kiện khỏi giỏ hàng.
     */
    public boolean xoaKhoiGio(int maGioHang, int maLinhKien) {
        String sql = "DELETE FROM ChiTietGioHang WHERE MaGioHang = ? AND MaLinhKien = ?";
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, maGioHang);
                ps.setInt(2, maLinhKien);
                int affected = ps.executeUpdate();
                if (affected > 0) {
                    capNhatNgayGioHang(maGioHang);
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("[DAO LỖI] Xóa linh kiện khỏi giỏ thất bại: " + e.getMessage());
        }
        return false;
    }

    /**
     * Xóa toàn bộ sản phẩm trong giỏ hàng (làm trống giỏ).
     */
    public boolean xoaToanBoGio(int maGioHang) {
        String sql = "DELETE FROM ChiTietGioHang WHERE MaGioHang = ?";
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, maGioHang);
                ps.executeUpdate();
                capNhatNgayGioHang(maGioHang);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[DAO LỖI] Xóa toàn bộ giỏ hàng thất bại: " + e.getMessage());
        }
        return false;
    }

    private void capNhatNgayGioHang(int maGioHang) {
        String sql = "UPDATE GioHang SET NgayCapNhat = ? WHERE MaGioHang = ?";
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                String dbType = DatabaseConnection.getActiveDbType();
                if ("sqlite".equalsIgnoreCase(dbType)) {
                    ps.setString(1, LocalDateTime.now().toString());
                } else {
                    ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                }
                ps.setInt(2, maGioHang);
                ps.executeUpdate();
            }
        } catch (SQLException ignored) {
        }
    }
}

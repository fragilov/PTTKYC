package com.linhkien.util;

import com.linhkien.config.DatabaseConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

/**
 * Tiện ích thao tác JDBC dùng chung giữa các tầng DAO và Service:
 * - Ánh xạ kiểu thời gian/số nguyên tương thích SQLite và Microsoft SQL Server.
 * - Quản lý thực thi và Transaction (commit/rollback an toàn).
 */
public final class JdbcHelper {

    private JdbcHelper() {
    }

    @FunctionalInterface
    public interface SqlAction<T> {
        T run() throws SQLException;
    }

    public static boolean laSqlite() {
        return "sqlite".equalsIgnoreCase(DatabaseConnection.getActiveDbType());
    }

    public static void setSoNguyenHoacNull(PreparedStatement ps, int index, Integer value) throws SQLException {
        if (value != null) {
            ps.setInt(index, value);
        } else {
            ps.setNull(index, Types.INTEGER);
        }
    }

    public static void setThoiGian(PreparedStatement ps, int index, LocalDateTime time) throws SQLException {
        if (time == null) {
            ps.setNull(index, laSqlite() ? Types.VARCHAR : Types.TIMESTAMP);
        } else if (laSqlite()) {
            ps.setString(index, time.toString());
        } else {
            ps.setTimestamp(index, Timestamp.valueOf(time));
        }
    }

    public static void setNgay(PreparedStatement ps, int index, LocalDate date) throws SQLException {
        if (date == null) {
            ps.setNull(index, laSqlite() ? Types.VARCHAR : Types.DATE);
        } else if (laSqlite()) {
            ps.setString(index, date.toString());
        } else {
            ps.setDate(index, Date.valueOf(date));
        }
    }

    public static LocalDateTime docThoiGian(ResultSet rs, String cot) throws SQLException {
        if (laSqlite()) {
            String s = rs.getString(cot);
            if (s == null || s.isBlank()) {
                return null;
            }
            try {
                return LocalDateTime.parse(s.trim().replace(' ', 'T'));
            } catch (DateTimeParseException e) {
                return null;
            }
        }
        Timestamp ts = rs.getTimestamp(cot);
        return ts != null ? ts.toLocalDateTime() : null;
    }

    public static LocalDate docNgay(ResultSet rs, String cot) throws SQLException {
        if (laSqlite()) {
            String s = rs.getString(cot);
            if (s == null || s.length() < 10) {
                return null;
            }
            try {
                return LocalDate.parse(s.substring(0, 10));
            } catch (DateTimeParseException e) {
                return null;
            }
        }
        Date d = rs.getDate(cot);
        return d != null ? d.toLocalDate() : null;
    }

    /**
     * Thực thi thao tác CSDL, chuyển SQLException thành RuntimeException.
     */
    public static <T> T chay(SqlAction<T> hanhDong) {
        try {
            return hanhDong.run();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi CSDL: " + e.getMessage(), e);
        }
    }

    /**
     * Thực thi thao tác trong một JDBC Transaction.
     * Nếu đã ở trong một transaction cha (autoCommit == false), tham gia trực tiếp vào transaction đó.
     */
    public static <T> T giaoDich(SqlAction<T> hanhDong) {
        Connection conn;
        try {
            conn = DatabaseConnection.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Không thể kết nối CSDL: " + e.getMessage(), e);
        }

        boolean autoCommitCu;
        try {
            autoCommitCu = conn.getAutoCommit();
        } catch (SQLException e) {
            throw new RuntimeException("Không thể kiểm tra trạng thái giao dịch CSDL: " + e.getMessage(), e);
        }

        if (!autoCommitCu) {
            try {
                return hanhDong.run();
            } catch (SQLException e) {
                throw new RuntimeException("Lỗi CSDL: " + e.getMessage(), e);
            }
        }

        try {
            conn.setAutoCommit(false);
            T ketQua = hanhDong.run();
            conn.commit();
            return ketQua;
        } catch (RuntimeException | SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("[LỖI] Hoàn tác giao dịch thất bại: " + rollbackEx.getMessage());
            }
            if (e instanceof RuntimeException re) {
                throw re;
            }
            throw new RuntimeException("Lỗi CSDL: " + e.getMessage(), e);
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException ignored) {
            }
        }
    }
}

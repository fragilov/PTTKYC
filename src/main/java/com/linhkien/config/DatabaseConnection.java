package com.linhkien.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Lớp quản lý kết nối CSDL (JDBC Connection).
 * Hỗ trợ cả Microsoft SQL Server (theo thiết kế bài toán) và SQLite (chạy cục bộ độc lập).
 */
public class DatabaseConnection {
    private static Connection connection = null;
    private static String activeDbType = null;

    private DatabaseConnection() {
        // Private constructor for singleton pattern
    }

    /**
     * Lấy đối tượng Connection đang mở, nếu chưa có hoặc đã đóng thì tạo mới.
     */
    public static synchronized Connection getConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            return connection;
        }

        String preferredType = DatabaseConfig.getDbType();

        if ("sqlserver".equalsIgnoreCase(preferredType)) {
            try {
                // Tải driver Microsoft SQL Server
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                String url = DatabaseConfig.getSqlServerUrl();
                String user = DatabaseConfig.getSqlServerUser();
                String pass = DatabaseConfig.getSqlServerPassword();

                connection = DriverManager.getConnection(url, user, pass);
                activeDbType = "sqlserver";
                System.out.println("[CSDL] Kết nối thành công đến Microsoft SQL Server: " + url);
                return connection;
            } catch (Exception ex) {
                System.err.println("[CẢNH BÁO] Không thể kết nối Microsoft SQL Server: " + ex.getMessage());
                System.err.println("[CSDL] Đang tự động chuyển sang CSDL SQLite cục bộ để đảm bảo hệ thống hoạt động...");
                // Chuyển sang sqlite fallback
            }
        }

        // Kết nối SQLite
        try {
            Class.forName("org.sqlite.JDBC");
            String url = DatabaseConfig.getSqliteUrl();
            connection = DriverManager.getConnection(url);
            activeDbType = "sqlite";
            System.out.println("[CSDL] Kết nối thành công đến SQLite: " + url);
            return connection;
        } catch (ClassNotFoundException e) {
            throw new SQLException("Không tìm thấy SQLite JDBC Driver trong Classpath", e);
        }
    }

    /**
     * Lấy loại CSDL đang thực sự kết nối ('sqlserver' hoặc 'sqlite').
     */
    public static String getActiveDbType() {
        if (activeDbType == null) {
            try {
                getConnection();
            } catch (Exception ignored) {
            }
        }
        return activeDbType != null ? activeDbType : DatabaseConfig.getDbType();
    }

    /**
     * Đóng kết nối CSDL.
     */
    public static synchronized void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                    System.out.println("[CSDL] Đã đóng kết nối CSDL.");
                }
            } catch (SQLException e) {
                System.err.println("[LỖI] Lỗi khi đóng kết nối: " + e.getMessage());
            } finally {
                connection = null;
            }
        }
    }
}

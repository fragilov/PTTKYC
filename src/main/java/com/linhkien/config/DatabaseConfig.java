package com.linhkien.config;

import java.io.InputStream;
import java.util.Properties;

/**
 * Lớp đọc và lưu trữ cấu hình kết nối CSDL từ file db.properties.
 */
public class DatabaseConfig {
    private static final Properties properties = new Properties();

    private static String dbType;
    private static String sqliteUrl;

    private static String sqlServerHost;
    private static int sqlServerPort;
    private static String sqlServerDbName;
    private static String sqlServerUser;
    private static String sqlServerPassword;
    private static boolean sqlServerEncrypt;
    private static boolean sqlServerTrustCert;

    private static boolean initSampleData;

    static {
        loadProperties();
    }

    private static void loadProperties() {
        try (InputStream input = DatabaseConfig.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (Exception e) {
            System.err.println("[CẢNH BÁO] Không thể đọc file db.properties, sử dụng cấu hình mặc định: " + e.getMessage());
        }

        dbType = properties.getProperty("db.type", "sqlite").trim().toLowerCase();
        sqliteUrl = properties.getProperty("sqlite.url", "jdbc:sqlite:database/ql_banlinhkien.db").trim();

        sqlServerHost = properties.getProperty("sqlserver.host", "localhost").trim();
        sqlServerPort = Integer.parseInt(properties.getProperty("sqlserver.port", "1433").trim());
        sqlServerDbName = properties.getProperty("sqlserver.databaseName", "QL_BanLinhKien").trim();
        sqlServerUser = properties.getProperty("sqlserver.username", "sa").trim();
        sqlServerPassword = properties.getProperty("sqlserver.password", "123456").trim();
        sqlServerEncrypt = Boolean.parseBoolean(properties.getProperty("sqlserver.encrypt", "false").trim());
        sqlServerTrustCert = Boolean.parseBoolean(properties.getProperty("sqlserver.trustServerCertificate", "true").trim());

        initSampleData = Boolean.parseBoolean(properties.getProperty("db.init.sample_data", "true").trim());
    }

    public static String getDbType() {
        return dbType;
    }

    public static void setDbType(String type) {
        dbType = type;
    }

    public static String getSqliteUrl() {
        return sqliteUrl;
    }

    public static String getSqlServerUrl() {
        return String.format(
            "jdbc:sqlserver://%s:%d;databaseName=%s;encrypt=%b;trustServerCertificate=%b",
            sqlServerHost, sqlServerPort, sqlServerDbName, sqlServerEncrypt, sqlServerTrustCert
        );
    }

    public static String getSqlServerUser() {
        return sqlServerUser;
    }

    public static String getSqlServerPassword() {
        return sqlServerPassword;
    }

    public static boolean isInitSampleData() {
        return initSampleData;
    }
}

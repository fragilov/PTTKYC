package com.linhkien.config;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Lớp khởi tạo cấu trúc bảng và nạp dữ liệu mẫu ban đầu cho CSDL.
 */
public class DatabaseInitializer {

    public static void initializeDatabase() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String dbType = DatabaseConnection.getActiveDbType();
            if ("sqlite".equalsIgnoreCase(dbType)) {
                initSqliteSchema(conn);
                if (DatabaseConfig.isInitSampleData()) {
                    seedSqliteSampleData(conn);
                    seedNhanVienMau(conn);
                    seedDiaChiMau(conn);
                    seedThongSoMau(conn);
                    seedMaGiamGiaMau(conn);
                }
            } else if ("sqlserver".equalsIgnoreCase(dbType)) {
                checkSqlServerSchema(conn);
            }
        } catch (SQLException e) {
            System.err.println("[LỖI] Khởi tạo CSDL thất bại: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void initSqliteSchema(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // 1. DanhMucLinhKien
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS DanhMucLinhKien (
                    MaDanhMuc INTEGER PRIMARY KEY AUTOINCREMENT,
                    TenDanhMuc TEXT NOT NULL UNIQUE,
                    MaDanhMucCha INTEGER NULL,
                    MoTa TEXT NULL,
                    TrangThai TEXT NOT NULL DEFAULT 'HIEN',
                    FOREIGN KEY (MaDanhMucCha) REFERENCES DanhMucLinhKien(MaDanhMuc)
                );
            """);

            // 2. LinhKien
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS LinhKien (
                    MaLinhKien INTEGER PRIMARY KEY AUTOINCREMENT,
                    MaSKU TEXT NOT NULL UNIQUE,
                    TenLinhKien TEXT NOT NULL,
                    MaDanhMuc INTEGER NOT NULL,
                    NhaSanXuat TEXT NULL,
                    MoTa TEXT NULL,
                    KieuDongGoi TEXT NULL,
                    DienApHoatDong TEXT NULL,
                    GiaoTiep TEXT NULL,
                    DuongDanDatasheet TEXT NULL,
                    HinhAnh TEXT NULL,
                    DonViTinh TEXT NOT NULL DEFAULT 'Cái',
                    GiaBan NUMERIC NOT NULL DEFAULT 0,
                    SoLuongTon INTEGER NOT NULL DEFAULT 0,
                    NguongTonToiThieu INTEGER NOT NULL DEFAULT 10,
                    TrangThaiBan TEXT NOT NULL DEFAULT 'DANG_BAN',
                    NgayCapNhat TEXT NOT NULL,
                    FOREIGN KEY (MaDanhMuc) REFERENCES DanhMucLinhKien(MaDanhMuc)
                );
            """);

            // 3. NguoiDung
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS NguoiDung (
                    MaNguoiDung INTEGER PRIMARY KEY AUTOINCREMENT,
                    HoTen TEXT NOT NULL,
                    Email TEXT NOT NULL UNIQUE,
                    SoDienThoai TEXT NULL,
                    MatKhauHash TEXT NOT NULL,
                    VaiTro TEXT NOT NULL DEFAULT 'KHACH_HANG',
                    TrangThai TEXT NOT NULL DEFAULT 'HOAT_DONG',
                    SoLanDangNhapSai INTEGER NOT NULL DEFAULT 0,
                    KhoaDen TEXT NULL,
                    NgayTao TEXT NOT NULL
                );
            """);

            // 4. DiaChiNhanHang
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS DiaChiNhanHang (
                    MaDiaChi INTEGER PRIMARY KEY AUTOINCREMENT,
                    MaNguoiDung INTEGER NOT NULL,
                    TenNguoiNhan TEXT NOT NULL,
                    SoDienThoaiNhan TEXT NOT NULL,
                    DiaChiChiTiet TEXT NOT NULL,
                    PhuongXa TEXT NOT NULL,
                    TinhThanh TEXT NOT NULL,
                    LaMacDinh INTEGER NOT NULL DEFAULT 0,
                    FOREIGN KEY (MaNguoiDung) REFERENCES NguoiDung(MaNguoiDung)
                );
            """);

            // 5. GioHang
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS GioHang (
                    MaGioHang INTEGER PRIMARY KEY AUTOINCREMENT,
                    MaNguoiDung INTEGER NOT NULL UNIQUE,
                    NgayTao TEXT NOT NULL,
                    NgayCapNhat TEXT NOT NULL,
                    FOREIGN KEY (MaNguoiDung) REFERENCES NguoiDung(MaNguoiDung)
                );
            """);

            // 6. ChiTietGioHang
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS ChiTietGioHang (
                    MaGioHang INTEGER NOT NULL,
                    MaLinhKien INTEGER NOT NULL,
                    SoLuong INTEGER NOT NULL,
                    NgayThem TEXT NOT NULL,
                    PRIMARY KEY (MaGioHang, MaLinhKien),
                    FOREIGN KEY (MaGioHang) REFERENCES GioHang(MaGioHang) ON DELETE CASCADE,
                    FOREIGN KEY (MaLinhKien) REFERENCES LinhKien(MaLinhKien)
                );
            """);

            // 7. ThongSoKyThuat
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS ThongSoKyThuat (
                    MaThongSo INTEGER PRIMARY KEY AUTOINCREMENT,
                    MaLinhKien INTEGER NOT NULL,
                    TenThongSo TEXT NOT NULL,
                    GiaTri TEXT NOT NULL,
                    DonVi TEXT NULL,
                    FOREIGN KEY (MaLinhKien) REFERENCES LinhKien(MaLinhKien) ON DELETE CASCADE
                );
            """);

            // 8. MaGiamGia
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS MaGiamGia (
                    MaGiamGia INTEGER PRIMARY KEY AUTOINCREMENT,
                    MaCode TEXT NOT NULL UNIQUE,
                    LoaiGiam TEXT NOT NULL CHECK (LoaiGiam IN ('PHAN_TRAM','SO_TIEN')),
                    GiaTriGiam NUMERIC NOT NULL CHECK (GiaTriGiam > 0),
                    DonHangToiThieu NUMERIC NOT NULL DEFAULT 0,
                    SoLuotToiDa INTEGER NULL,
                    SoLuotDaDung INTEGER NOT NULL DEFAULT 0,
                    NgayBatDau TEXT NOT NULL,
                    NgayKetThuc TEXT NOT NULL,
                    CHECK (NgayKetThuc > NgayBatDau)
                );
            """);

            // 9. DonHang
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS DonHang (
                    MaDonHang INTEGER PRIMARY KEY AUTOINCREMENT,
                    MaNguoiDung INTEGER NOT NULL,
                    MaGiamGia INTEGER NULL,
                    MaYeuCau TEXT NOT NULL UNIQUE,
                    NgayDat TEXT NOT NULL,
                    TenNguoiNhan TEXT NOT NULL,
                    SoDienThoaiNhan TEXT NOT NULL,
                    DiaChiNhan TEXT NOT NULL,
                    TongTienHang NUMERIC NOT NULL CHECK (TongTienHang >= 0),
                    PhiVanChuyen NUMERIC NOT NULL DEFAULT 0 CHECK (PhiVanChuyen >= 0),
                    SoTienGiam NUMERIC NOT NULL DEFAULT 0 CHECK (SoTienGiam >= 0),
                    TongThanhToan NUMERIC NOT NULL,
                    PhuongThucThanhToan TEXT NOT NULL CHECK (PhuongThucThanhToan IN ('COD','CHUYEN_KHOAN')),
                    TrangThaiThanhToan TEXT NOT NULL DEFAULT 'CHO_THANH_TOAN'
                        CHECK (TrangThaiThanhToan IN ('CHO_THANH_TOAN','DA_THANH_TOAN','DANG_HOAN_TIEN','DA_HOAN_TIEN')),
                    TrangThaiDon TEXT NOT NULL DEFAULT 'CHO_XAC_NHAN'
                        CHECK (TrangThaiDon IN ('CHO_XAC_NHAN','DANG_DONG_GOI','DA_BAN_GIAO','DANG_GIAO','DA_GIAO','GIAO_THAT_BAI','DA_HUY')),
                    DaHoanKho INTEGER NOT NULL DEFAULT 0,
                    GhiChu TEXT NULL,
                    FOREIGN KEY (MaNguoiDung) REFERENCES NguoiDung(MaNguoiDung),
                    FOREIGN KEY (MaGiamGia) REFERENCES MaGiamGia(MaGiamGia),
                    CHECK (TongThanhToan = TongTienHang + PhiVanChuyen - SoTienGiam)
                );
            """);

            // 10. ChiTietDonHang
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS ChiTietDonHang (
                    MaDonHang INTEGER NOT NULL,
                    MaLinhKien INTEGER NOT NULL,
                    SoLuong INTEGER NOT NULL CHECK (SoLuong > 0),
                    DonGia NUMERIC NOT NULL CHECK (DonGia >= 0),
                    PRIMARY KEY (MaDonHang, MaLinhKien),
                    FOREIGN KEY (MaDonHang) REFERENCES DonHang(MaDonHang) ON DELETE CASCADE,
                    FOREIGN KEY (MaLinhKien) REFERENCES LinhKien(MaLinhKien)
                );
            """);

            // 11. ThanhToan
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS ThanhToan (
                    MaThanhToan INTEGER PRIMARY KEY AUTOINCREMENT,
                    MaDonHang INTEGER NOT NULL,
                    LoaiGiaoDich TEXT NOT NULL CHECK (LoaiGiaoDich IN ('THU','HOAN')),
                    PhuongThuc TEXT NOT NULL CHECK (PhuongThuc IN ('COD','CHUYEN_KHOAN')),
                    SoTien NUMERIC NOT NULL CHECK (SoTien > 0),
                    MaThamChieu TEXT NULL,
                    TrangThai TEXT NOT NULL DEFAULT 'CHO_XAC_NHAN'
                        CHECK (TrangThai IN ('CHO_XAC_NHAN','THANH_CONG','THAT_BAI')),
                    MaNguoiXacNhan INTEGER NULL,
                    ThoiDiem TEXT NOT NULL,
                    FOREIGN KEY (MaDonHang) REFERENCES DonHang(MaDonHang),
                    FOREIGN KEY (MaNguoiXacNhan) REFERENCES NguoiDung(MaNguoiDung)
                );
            """);

            // 12. VanChuyen
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS VanChuyen (
                    MaVanChuyen INTEGER PRIMARY KEY AUTOINCREMENT,
                    MaDonHang INTEGER NOT NULL UNIQUE,
                    DonViVanChuyen TEXT NOT NULL,
                    MaVanDon TEXT NOT NULL UNIQUE,
                    TrangThaiGiao TEXT NOT NULL DEFAULT 'CHO_LAY_HANG'
                        CHECK (TrangThaiGiao IN ('CHO_LAY_HANG','DANG_GIAO','DA_GIAO','GIAO_THAT_BAI','HOAN_HANG')),
                    NgayBanGiao TEXT NULL,
                    NgayGiaoDuKien TEXT NULL,
                    NgayGiaoThucTe TEXT NULL,
                    DaThuCOD INTEGER NOT NULL DEFAULT 0,
                    GhiChu TEXT NULL,
                    FOREIGN KEY (MaDonHang) REFERENCES DonHang(MaDonHang)
                );
            """);

            // 13. LichSuTrangThaiDon
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS LichSuTrangThaiDon (
                    MaLichSu INTEGER PRIMARY KEY AUTOINCREMENT,
                    MaDonHang INTEGER NOT NULL,
                    TrangThaiCu TEXT NULL,
                    TrangThaiMoi TEXT NOT NULL,
                    MaNguoiThucHien INTEGER NULL,
                    ThoiDiem TEXT NOT NULL,
                    GhiChu TEXT NULL,
                    FOREIGN KEY (MaDonHang) REFERENCES DonHang(MaDonHang),
                    FOREIGN KEY (MaNguoiThucHien) REFERENCES NguoiDung(MaNguoiDung)
                );
            """);

            // 14. LichSuTonKho
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS LichSuTonKho (
                    MaLichSu INTEGER PRIMARY KEY AUTOINCREMENT,
                    MaLinhKien INTEGER NOT NULL,
                    LoaiBienDong TEXT NOT NULL CHECK (LoaiBienDong IN ('NHAP','BAN','HOAN_HUY','DIEU_CHINH')),
                    SoLuongThayDoi INTEGER NOT NULL CHECK (SoLuongThayDoi <> 0),
                    TonTruoc INTEGER NOT NULL CHECK (TonTruoc >= 0),
                    TonSau INTEGER NOT NULL CHECK (TonSau >= 0),
                    MaDonHang INTEGER NULL,
                    MaNguoiThucHien INTEGER NULL,
                    LyDo TEXT NULL,
                    ThoiDiem TEXT NOT NULL,
                    FOREIGN KEY (MaLinhKien) REFERENCES LinhKien(MaLinhKien),
                    FOREIGN KEY (MaDonHang) REFERENCES DonHang(MaDonHang),
                    FOREIGN KEY (MaNguoiThucHien) REFERENCES NguoiDung(MaNguoiDung),
                    CHECK (TonSau = TonTruoc + SoLuongThayDoi)
                );
            """);
        }
    }

    /**
     * Bổ sung các tài khoản nhân viên mẫu (đồng bộ với QL_BanLinhKien_Data_Sample.sql).
     */
    private static void seedNhanVienMau(Connection conn) throws SQLException {
        String sql = """
            INSERT OR IGNORE INTO NguoiDung (MaNguoiDung, HoTen, Email, SoDienThoai, MatKhauHash, VaiTro, TrangThai, NgayTao)
            VALUES (?, ?, ?, ?, ?, ?, 'HOAT_DONG', datetime('now'))
        """;
        String matKhauHash = "e10adc3949ba59abbe56e057f20f883e"; // 123456
        Object[][] nhanVien = {
            {2, "Trần Thị Bích", "bich.tran@linhkien.vn", "0987654321", "NV_BAN_HANG"},
            {3, "Lê Hoàng Long", "long.le@linhkien.vn", "0933445566", "NV_KHO"},
            {4, "Admin Quản Trị", "admin@linhkien.vn", "0909090909", "QUAN_TRI"}
        };
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Object[] nv : nhanVien) {
                ps.setInt(1, (Integer) nv[0]);
                ps.setString(2, (String) nv[1]);
                ps.setString(3, (String) nv[2]);
                ps.setString(4, (String) nv[3]);
                ps.setString(5, matKhauHash);
                ps.setString(6, (String) nv[4]);
                ps.executeUpdate();
            }
        }
    }

    private static void seedDiaChiMau(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM DiaChiNhanHang")) {
            if (rs.next() && rs.getInt(1) > 0) {
                return;
            }
        }
        String sql = """
            INSERT INTO DiaChiNhanHang (MaDiaChi, MaNguoiDung, TenNguoiNhan, SoDienThoaiNhan, DiaChiChiTiet, PhuongXa, TinhThanh, LaMacDinh)
            VALUES (1, 1, ?, ?, ?, ?, ?, 1)
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "Nguyễn Văn An");
            ps.setString(2, "0912345678");
            ps.setString(3, "12 Nguyễn Hữu Thọ");
            ps.setString(4, "Tân Phong, Quận 7");
            ps.setString(5, "TP. Hồ Chí Minh");
            ps.executeUpdate();
        }
    }

    private static void seedThongSoMau(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM ThongSoKyThuat")) {
            if (rs.next() && rs.getInt(1) > 0) {
                return;
            }
        }
        String sql = "INSERT INTO ThongSoKyThuat (MaLinhKien, TenThongSo, GiaTri, DonVi) VALUES (?, ?, ?, ?)";
        Object[][] specs = {
            {1, "Vi điều khiển", "ATmega328P", null},
            {1, "Xung nhịp", "16", "MHz"},
            {1, "Flash / SRAM", "32 KB / 2 KB", null},
            {2, "Vi điều khiển", "ESP-WROOM-32 Dual Core", null},
            {2, "Xung nhịp", "240", "MHz"},
            {2, "Kết nối không dây", "WiFi 802.11 b/g/n & BLE 4.2", null},
            {3, "Vi điều khiển", "ARM Cortex-M3 STM32F103C8T6", null},
            {3, "Xung nhịp", "72", "MHz"},
            {4, "Dải đo nhiệt độ", "0 ~ 50", "°C"},
            {4, "Dải đo độ ẩm", "20 ~ 90", "%RH"},
            {5, "Khoảng cách đo", "2 ~ 400", "cm"},
            {6, "Độ phân giải", "128 × 64", "pixel"}
        };
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Object[] row : specs) {
                ps.setInt(1, (Integer) row[0]);
                ps.setString(2, (String) row[1]);
                ps.setString(3, (String) row[2]);
                ps.setString(4, (String) row[3]);
                ps.executeUpdate();
            }
        }
    }

    private static void seedMaGiamGiaMau(Connection conn) throws SQLException {
        String sql = """
            INSERT OR IGNORE INTO MaGiamGia (MaGiamGia, MaCode, LoaiGiam, GiaTriGiam, DonHangToiThieu, SoLuotToiDa, SoLuotDaDung, NgayBatDau, NgayKetThuc)
            VALUES (?, ?, ?, ?, ?, ?, 0, ?, ?)
        """;
        Object[][] coupons = {
            {1, "GIAM10K", "SO_TIEN", 10000L, 100000L, 500, "2025-01-01T00:00:00", "2030-12-31T23:59:59"},
            {2, "SALE10", "PHAN_TRAM", 10L, 200000L, 100, "2025-01-01T00:00:00", "2030-12-31T23:59:59"}
        };
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Object[] c : coupons) {
                ps.setInt(1, (Integer) c[0]);
                ps.setString(2, (String) c[1]);
                ps.setString(3, (String) c[2]);
                ps.setBigDecimal(4, BigDecimal.valueOf((Long) c[3]));
                ps.setBigDecimal(5, BigDecimal.valueOf((Long) c[4]));
                ps.setInt(6, (Integer) c[5]);
                ps.setString(7, (String) c[6]);
                ps.setString(8, (String) c[7]);
                ps.executeUpdate();
            }
        }
    }

    private static void seedSqliteSampleData(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM LinhKien")) {
            if (rs.next() && rs.getInt(1) > 0) {
                return;
            }
        }

        System.out.println("[CSDL] Đang nạp dữ liệu mẫu ban đầu (Danh mục, Linh kiện, Tài khoản, Giỏ hàng)...");

        String sqlDM = "INSERT INTO DanhMucLinhKien (MaDanhMuc, TenDanhMuc, MaDanhMucCha, MoTa, TrangThai) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sqlDM)) {
            insertDM(ps, 1, "Vi điều khiển & Module", null, "Các loại board mạch lập trình Arduino, ESP32, STM32", "HIEN");
            insertDM(ps, 2, "Cảm biến", null, "Cảm biến nhiệt độ, khoảng cách, ánh sáng, chuyển động", "HIEN");
            insertDM(ps, 3, "Linh kiện thụ động", null, "Điện trở, tụ điện, cuộn cảm, biến trở", "HIEN");
            insertDM(ps, 4, "Nguồn & Pin", null, "Adapter nguồn, pin Lithium, mạch sạc pin", "HIEN");
            insertDM(ps, 5, "Màn hình & Hiển thị", null, "Màn hình OLED, LCD1602, LED 7 thanh", "HIEN");
        }

        String sqlLK = """
            INSERT INTO LinhKien (
                MaLinhKien, MaSKU, TenLinhKien, MaDanhMuc, NhaSanXuat, MoTa,
                KieuDongGoi, DienApHoatDong, GiaoTiep, DuongDanDatasheet,
                DonViTinh, GiaBan, SoLuongTon, NguongTonToiThieu, TrangThaiBan, NgayCapNhat
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, datetime('now'))
        """;

        try (PreparedStatement ps = conn.prepareStatement(sqlLK)) {
            insertLK(ps, 1, "MCU-ARD-UNO-R3", "Kit Arduino Uno R3 Chip Cắm DIP (Kèm cáp USB)",
                    1, "Arduino", "Bo mạch vi điều khiển ATmega328P phổ biến cho người mới bắt đầu",
                    "DIP", "5V DC", "UART, I2C, SPI", "https://docs.arduino.cc/datasheets/uno-r3.pdf",
                    "Bộ", 135000, 45, 10, "DANG_BAN");

            insertLK(ps, 2, "MCU-ESP32-DEVKIT", "Module ESP32 DevKit V1 30 Chân WiFi + Bluetooth",
                    1, "Espressif", "Chip SoC ESP-WROOM-32 2 nhân 240MHz hỗ trợ cả WiFi và BLE 4.2",
                    "MODULE", "3.3V - 5V", "WiFi, BLE, UART, SPI, I2C", "https://espressif.com/esp32.pdf",
                    "Cái", 95000, 60, 15, "DANG_BAN");

            insertLK(ps, 3, "MCU-STM32-BLUEPILL", "Kit STM32F103C8T6 ARM Cortex-M3 (Blue Pill)",
                    1, "STMicroelectronics", "Board ARM 32-bit Cortex-M3 tần số 72MHz, 64KB Flash",
                    "MODULE", "3.3V DC", "SWD, UART, SPI, I2C, CAN", "https://st.com/stm32f103c8.pdf",
                    "Cái", 55000, 30, 10, "DANG_BAN");

            insertLK(ps, 4, "SS-DHT11-MOD", "Cảm biến nhiệt độ và độ ẩm không khí DHT11",
                    2, "Aosong", "Đo nhiệt độ từ 0-50°C (±2°C) và độ ẩm 20-90% RH (±5%)",
                    "MODULE", "3.3V - 5V", "Single-bus (Digital)", "https://aosong.com/dht11.pdf",
                    "Cái", 25000, 80, 20, "DANG_BAN");

            insertLK(ps, 5, "SS-HCSR04-ULTRA", "Cảm biến khoảng cách siêu âm HC-SR04",
                    2, "ElecFreaks", "Khoảng cách đo 2cm đến 400cm, góc đo 15 độ, độ chính xác 3mm",
                    "MODULE", "5V DC", "Trigger/Echo Pulse", "https://cdn.sparkfun.com/hcsr04.pdf",
                    "Cái", 28000, 5, 10, "DANG_BAN");

            insertLK(ps, 6, "DISP-OLED-096-I2C", "Màn hình OLED 0.96 inch I2C (Ánh sáng Trắng)",
                    5, "Solomon", "Độ phân giải 128x64 pixel, driver SSD1306 hiển thị sắc nét",
                    "MODULE", "3.3V - 5V", "I2C (0x3C)", "https://cdn.adafruit.com/ssd1306.pdf",
                    "Cái", 65000, 25, 5, "DANG_BAN");

            insertLK(ps, 7, "PAS-CAP-100UF-25V", "Tụ điện hóa nhôm 100uF 25V (Gói 10 cái)",
                    3, "Nichicon", "Tụ lọc nguồn chất lượng cao, nhiệt độ hoạt động -40°C đến 105°C",
                    "DIP", "Max 25V", "Chân xuyên lỗ (Through-hole)", null,
                    "Gói", 12000, 150, 30, "DANG_BAN");

            insertLK(ps, 8, "MOD-RELAY-5V-2CH", "Module Rơ-le (Relay) 5V 2 Kênh Có Cách Ly Quang",
                    1, "Songle", "Đóng cắt điện 220V AC 10A hoặc 30V DC 10A an toàn với Opto PC817",
                    "MODULE", "5V DC", "Kích mức Thấp (Low Level)", null,
                    "Cái", 32000, 18, 10, "DANG_BAN");

            insertLK(ps, 9, "PWR-ADAPTER-12V2A", "Nguồn Adapter 12V 2A Jack DC 5.5x2.1mm",
                    4, "MeanWell", "Nguồn cấp điện ổn áp chuẩn chống nhiễu cho vi điều khiển và mạch công suất",
                    "KHAC", "220V AC vào, 12V DC ra", "Jack DC tròn", null,
                    "Cái", 75000, 40, 10, "DANG_BAN");

            insertLK(ps, 10, "IC-NE555-DIP8", "IC Tạo dao động NE555P chân cắm DIP-8",
                    3, "Texas Instruments", "Mạch định thời Timer 555 kinh điển dùng cho mạch tạo xung",
                    "DIP", "4.5V - 15V", "Analog/Timer", "https://ti.com/ne555.pdf",
                    "Cái", 6000, 0, 10, "NGUNG_BAN");
        }

        String sqlUser = """
            INSERT INTO NguoiDung (MaNguoiDung, HoTen, Email, SoDienThoai, MatKhauHash, VaiTro, TrangThai, NgayTao)
            VALUES (?, ?, ?, ?, ?, ?, ?, datetime('now'))
        """;
        try (PreparedStatement ps = conn.prepareStatement(sqlUser)) {
            ps.setInt(1, 1);
            ps.setString(2, "Nguyễn Văn An");
            ps.setString(3, "nguyenvanan@gmail.com");
            ps.setString(4, "0912345678");
            ps.setString(5, "e10adc3949ba59abbe56e057f20f883e"); // 123456
            ps.setString(6, "KHACH_HANG");
            ps.setString(7, "HOAT_DONG");
            ps.executeUpdate();
        }

        String sqlCart = """
            INSERT INTO GioHang (MaGioHang, MaNguoiDung, NgayTao, NgayCapNhat)
            VALUES (1, 1, datetime('now'), datetime('now'))
        """;
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sqlCart);
        }

        String sqlCartLine = """
            INSERT INTO ChiTietGioHang (MaGioHang, MaLinhKien, SoLuong, NgayThem)
            VALUES (?, ?, ?, datetime('now'))
        """;
        try (PreparedStatement ps = conn.prepareStatement(sqlCartLine)) {
            ps.setInt(1, 1);
            ps.setInt(2, 1);
            ps.setInt(3, 2);
            ps.executeUpdate();

            ps.setInt(1, 1);
            ps.setInt(2, 4);
            ps.setInt(3, 3);
            ps.executeUpdate();
        }

        System.out.println("[CSDL] Khởi tạo dữ liệu mẫu hoàn tất!");
    }

    private static void insertDM(PreparedStatement ps, int ma, String ten, Integer cha, String moTa, String tt) throws SQLException {
        ps.setInt(1, ma);
        ps.setString(2, ten);
        if (cha != null) {
            ps.setInt(3, cha);
        } else {
            ps.setNull(3, java.sql.Types.INTEGER);
        }
        ps.setString(4, moTa);
        ps.setString(5, tt);
        ps.executeUpdate();
    }

    private static void insertLK(PreparedStatement ps, int ma, String sku, String ten, int maDM,
                                 String nsx, String moTa, String dongGoi, String dienAp,
                                 String giaoTiep, String datasheet, String dvt,
                                 long gia, int ton, int nguong, String tt) throws SQLException {
        ps.setInt(1, ma);
        ps.setString(2, sku);
        ps.setString(3, ten);
        ps.setInt(4, maDM);
        ps.setString(5, nsx);
        ps.setString(6, moTa);
        ps.setString(7, dongGoi);
        ps.setString(8, dienAp);
        ps.setString(9, giaoTiep);
        ps.setString(10, datasheet);
        ps.setString(11, dvt);
        ps.setBigDecimal(12, BigDecimal.valueOf(gia));
        ps.setInt(13, ton);
        ps.setInt(14, nguong);
        ps.setString(15, tt);
        ps.executeUpdate();
    }

    private static void checkSqlServerSchema(Connection conn) {
        try {
            DatabaseMetaData meta = conn.getMetaData();
            try (ResultSet rs = meta.getTables(null, null, "LinhKien", null)) {
                if (!rs.next()) {
                    System.out.println("[CSDL SQL Server] Chưa tìm thấy bảng LinhKien.");
                    System.out.println("[CSDL SQL Server] Vui lòng chạy file script 'database/QL_BanLinhKien.sql' trong SQL Server Management Studio để khởi tạo CSDL.");
                } else {
                    System.out.println("[CSDL SQL Server] Đã kết nối và xác nhận cấu trúc bảng LinhKien hợp lệ.");
                }
            }
        } catch (SQLException e) {
            System.err.println("[CSDL SQL Server] Không thể kiểm tra siêu dữ liệu CSDL: " + e.getMessage());
        }
    }
}

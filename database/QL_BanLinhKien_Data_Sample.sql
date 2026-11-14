-- =========================================================================
-- Script nạp dữ liệu mẫu ban đầu cho CSDL Microsoft SQL Server 2019+
-- Phục vụ chạy thử nghiệm hệ thống Quản lý linh kiện điện tử và Giỏ hàng
-- Chạy script này sau khi đã thực thi file 'QL_BanLinhKien.sql'
-- =========================================================================

USE QL_BanLinhKien;
GO

SET NOCOUNT ON;

-- 1. Nạp danh mục linh kiện mẫu
SET IDENTITY_INSERT DanhMucLinhKien ON;
INSERT INTO DanhMucLinhKien (MaDanhMuc, TenDanhMuc, MaDanhMucCha, MoTa, TrangThai) VALUES
(1, N'Vi điều khiển & Module', NULL, N'Các loại board mạch lập trình Arduino, ESP32, STM32', 'HIEN'),
(2, N'Cảm biến', NULL, N'Cảm biến nhiệt độ, khoảng cách, ánh sáng, chuyển động', 'HIEN'),
(3, N'Linh kiện thụ động', NULL, N'Điện trở, tụ điện, cuộn cảm, biến trở', 'HIEN'),
(4, N'Nguồn & Pin', NULL, N'Adapter nguồn, pin Lithium, mạch sạc pin', 'HIEN'),
(5, N'Màn hình & Hiển thị', NULL, N'Màn hình OLED, LCD1602, LED 7 thanh', 'HIEN');
SET IDENTITY_INSERT DanhMucLinhKien OFF;
GO

-- 2. Nạp linh kiện điện tử mẫu
SET IDENTITY_INSERT LinhKien ON;
INSERT INTO LinhKien (
    MaLinhKien, MaSKU, TenLinhKien, MaDanhMuc, NhaSanXuat, MoTa,
    KieuDongGoi, DienApHoatDong, GiaoTiep, DuongDanDatasheet,
    DonViTinh, GiaBan, SoLuongTon, NguongTonToiThieu, TrangThaiBan, NgayCapNhat
) VALUES
(1, 'MCU-ARD-UNO-R3', N'Kit Arduino Uno R3 Chip Cắm DIP (Kèm cáp USB)', 1, N'Arduino', N'Bo mạch vi điều khiển ATmega328P phổ biến cho người mới bắt đầu', 'DIP', '5V DC', 'UART, I2C, SPI', 'https://docs.arduino.cc/datasheets/uno-r3.pdf', N'Bộ', 135000, 45, 10, 'DANG_BAN', SYSDATETIME()),
(2, 'MCU-ESP32-DEVKIT', N'Module ESP32 DevKit V1 30 Chân WiFi + Bluetooth', 1, N'Espressif', N'Chip SoC ESP-WROOM-32 2 nhân 240MHz hỗ trợ cả WiFi và BLE 4.2', 'MODULE', '3.3V - 5V', 'WiFi, BLE, UART, SPI, I2C', 'https://espressif.com/esp32.pdf', N'Cái', 95000, 60, 15, 'DANG_BAN', SYSDATETIME()),
(3, 'MCU-STM32-BLUEPILL', N'Kit STM32F103C8T6 ARM Cortex-M3 (Blue Pill)', 1, N'STMicroelectronics', N'Board ARM 32-bit Cortex-M3 tần số 72MHz, 64KB Flash', 'MODULE', '3.3V DC', 'SWD, UART, SPI, I2C, CAN', 'https://st.com/stm32f103c8.pdf', N'Cái', 55000, 30, 10, 'DANG_BAN', SYSDATETIME()),
(4, 'SS-DHT11-MOD', N'Cảm biến nhiệt độ và độ ẩm không khí DHT11', 2, N'Aosong', N'Đo nhiệt độ từ 0-50°C (±2°C) và độ ẩm 20-90% RH (±5%)', 'MODULE', '3.3V - 5V', 'Single-bus (Digital)', 'https://aosong.com/dht11.pdf', N'Cái', 25000, 80, 20, 'DANG_BAN', SYSDATETIME()),
(5, 'SS-HCSR04-ULTRA', N'Cảm biến khoảng cách siêu âm HC-SR04', 2, N'ElecFreaks', N'Khoảng cách đo 2cm đến 400cm, góc đo 15 độ, độ chính xác 3mm', 'MODULE', '5V DC', 'Trigger/Echo Pulse', 'https://cdn.sparkfun.com/hcsr04.pdf', N'Cái', 28000, 5, 10, 'DANG_BAN', SYSDATETIME()),
(6, 'DISP-OLED-096-I2C', N'Màn hình OLED 0.96 inch I2C (Ánh sáng Trắng)', 5, N'Solomon', N'Độ phân giải 128x64 pixel, driver SSD1306 hiển thị sắc nét', 'MODULE', '3.3V - 5V', 'I2C (0x3C)', 'https://cdn.adafruit.com/ssd1306.pdf', N'Cái', 65000, 25, 5, 'DANG_BAN', SYSDATETIME()),
(7, 'PAS-CAP-100UF-25V', N'Tụ điện hóa nhôm 100uF 25V (Gói 10 cái)', 3, N'Nichicon', N'Tụ lọc nguồn chất lượng cao, nhiệt độ hoạt động -40°C đến 105°C', 'DIP', 'Max 25V', N'Chân xuyên lỗ (Through-hole)', NULL, N'Gói', 12000, 150, 30, 'DANG_BAN', SYSDATETIME()),
(8, 'MOD-RELAY-5V-2CH', N'Module Rơ-le (Relay) 5V 2 Kênh Có Cách Ly Quang', 1, N'Songle', N'Đóng cắt điện 220V AC 10A hoặc 30V DC 10A an toàn với Opto PC817', 'MODULE', '5V DC', N'Kích mức Thấp (Low Level)', NULL, N'Cái', 32000, 18, 10, 'DANG_BAN', SYSDATETIME()),
(9, 'PWR-ADAPTER-12V2A', N'Nguồn Adapter 12V 2A Jack DC 5.5x2.1mm', 4, N'MeanWell', N'Nguồn cấp điện ổn áp chuẩn chống nhiễu cho vi điều khiển và mạch công suất', 'KHAC', '220V AC vào, 12V DC ra', N'Jack DC tròn', NULL, N'Cái', 75000, 40, 10, 'DANG_BAN', SYSDATETIME()),
(10, 'IC-NE555-DIP8', N'IC Tạo dao động NE555P chân cắm DIP-8', 3, N'Texas Instruments', N'Mạch định thời Timer 555 kinh điển dùng cho mạch tạo xung', 'DIP', '4.5V - 15V', 'Analog/Timer', 'https://ti.com/ne555.pdf', N'Cái', 6000, 0, 10, 'NGUNG_BAN', SYSDATETIME());
SET IDENTITY_INSERT LinhKien OFF;
GO

-- 3. Nạp tài khoản người dùng mẫu
SET IDENTITY_INSERT NguoiDung ON;
INSERT INTO NguoiDung (MaNguoiDung, HoTen, Email, SoDienThoai, MatKhauHash, VaiTro, TrangThai, NgayTao) VALUES
(1, N'Nguyễn Văn An', 'nguyenvanan@gmail.com', '0912345678', 'e10adc3949ba59abbe56e057f20f883e', 'KHACH_HANG', 'HOAT_DONG', SYSDATETIME()),
(2, N'Trần Thị Bích', 'bich.tran@linhkien.vn', '0987654321', 'e10adc3949ba59abbe56e057f20f883e', 'NV_BAN_HANG', 'HOAT_DONG', SYSDATETIME()),
(3, N'Lê Hoàng Long', 'long.le@linhkien.vn', '0933445566', 'e10adc3949ba59abbe56e057f20f883e', 'NV_KHO', 'HOAT_DONG', SYSDATETIME()),
(4, N'Admin Quản Trị', 'admin@linhkien.vn', '0909090909', 'e10adc3949ba59abbe56e057f20f883e', 'QUAN_TRI', 'HOAT_DONG', SYSDATETIME());
SET IDENTITY_INSERT NguoiDung OFF;
GO

-- 4. Nạp Giỏ hàng mẫu cho Khách hàng 1
SET IDENTITY_INSERT GioHang ON;
INSERT INTO GioHang (MaGioHang, MaNguoiDung, NgayTao, NgayCapNhat) VALUES
(1, 1, SYSDATETIME(), SYSDATETIME());
SET IDENTITY_INSERT GioHang OFF;
GO

-- 5. Nạp dòng chi tiết giỏ hàng mẫu
INSERT INTO ChiTietGioHang (MaGioHang, MaLinhKien, SoLuong, NgayThem) VALUES
(1, 1, 2, SYSDATETIME()), -- 2 x Kit Arduino Uno R3
(1, 4, 3, SYSDATETIME()); -- 3 x Cảm biến DHT11
GO

-- 6. Nạp địa chỉ nhận hàng mặc định cho Khách hàng 1
SET IDENTITY_INSERT DiaChiNhanHang ON;
INSERT INTO DiaChiNhanHang (MaDiaChi, MaNguoiDung, TenNguoiNhan, SoDienThoaiNhan, DiaChiChiTiet, PhuongXa, TinhThanh, LaMacDinh) VALUES
(1, 1, N'Nguyễn Văn An', '0912345678', N'12 Nguyễn Hữu Thọ', N'Tân Phong, Quận 7', N'TP. Hồ Chí Minh', 1);
SET IDENTITY_INSERT DiaChiNhanHang OFF;
GO

-- 7. Nạp thông số kỹ thuật mở rộng mẫu
INSERT INTO ThongSoKyThuat (MaLinhKien, TenThongSo, GiaTri, DonVi) VALUES
(1, N'Vi điều khiển', N'ATmega328P', NULL),
(1, N'Xung nhịp', N'16', N'MHz'),
(2, N'Vi điều khiển', N'ESP-WROOM-32 Dual Core', NULL),
(2, N'Xung nhịp', N'240', N'MHz'),
(4, N'Dải đo nhiệt độ', N'0 ~ 50', N'°C'),
(5, N'Khoảng cách đo', N'2 ~ 400', N'cm');
GO

-- 8. Nạp mã giảm giá mẫu
SET IDENTITY_INSERT MaGiamGia ON;
INSERT INTO MaGiamGia (MaGiamGia, MaCode, LoaiGiam, GiaTriGiam, DonHangToiThieu, SoLuotToiDa, SoLuotDaDung, NgayBatDau, NgayKetThuc) VALUES
(1, 'GIAM10K', 'SO_TIEN', 10000, 100000, 500, 0, '2025-01-01', '2030-12-31'),
(2, 'SALE10', 'PHAN_TRAM', 10, 200000, 100, 0, '2025-01-01', '2030-12-31');
SET IDENTITY_INSERT MaGiamGia OFF;
GO

PRINT N'Nạp dữ liệu mẫu ban đầu thành công!';
GO

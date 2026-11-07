package com.linhkien;

import com.linhkien.config.DatabaseConnection;
import com.linhkien.config.DatabaseInitializer;
import com.linhkien.model.ChiTietGioHang;
import com.linhkien.model.GioHang;
import com.linhkien.model.LinhKien;
import com.linhkien.service.GioHangService;
import com.linhkien.service.LinhKienService;

import java.math.BigDecimal;
import java.util.List;

/**
 * Lớp kiểm thử tự động toàn diện các chức năng nghiệp vụ của hệ thống:
 * 1. Quản lý linh kiện: Thêm, Sửa, Xóa, Tìm kiếm, Cảnh báo tồn thấp.
 * 2. Quản lý giỏ hàng: Thêm vào giỏ, Kiểm tra tồn kho, Cập nhật số lượng, Tính tổng tiền, Xóa.
 */
public class AppTest {

    private static int totalPassed = 0;
    private static int totalFailed = 0;

    public static void main(String[] args) {
        System.out.println("=========================================================================");
        System.out.println("   BẮT ĐẦU KIỂM THỬ TỰ ĐỘNG HỆ THỐNG QUẢN LÝ LINH KIỆN VÀ GIỎ HÀNG");
        System.out.println("=========================================================================");

        // Khởi tạo CSDL
        DatabaseInitializer.initializeDatabase();

        LinhKienService linhKienService = new LinhKienService();
        GioHangService gioHangService = new GioHangService();

        testKetNoiCSDL();
        testQuanLyLinhKien(linhKienService);
        testQuanLyGioHang(linhKienService, gioHangService);

        System.out.println("\n=========================================================================");
        System.out.printf("  KẾT QUẢ KIỂM THỬ: %d THÀNH CÔNG, %d THẤT BẠI\n", totalPassed, totalFailed);
        System.out.println("=========================================================================");

        DatabaseConnection.closeConnection();

        if (totalFailed > 0) {
            System.exit(1);
        }
    }

    private static void testKetNoiCSDL() {
        System.out.println("\n--- [TEST 1] Kiểm tra kết nối CSDL ---");
        try {
            boolean connected = DatabaseConnection.getConnection() != null;
            assertTest(connected, "Kết nối CSDL thành công (" + DatabaseConnection.getActiveDbType() + ")");
        } catch (Exception e) {
            assertTest(false, "Lỗi kết nối CSDL: " + e.getMessage());
        }
    }

    private static void testQuanLyLinhKien(LinhKienService service) {
        System.out.println("\n--- [TEST 2] Kiểm tra Chức năng Quản lý Linh kiện ---");

        // 1. Lấy danh sách linh kiện ban đầu
        List<LinhKien> ds = service.layTatCaLinhKien();
        assertTest(!ds.isEmpty(), "Lấy danh sách linh kiện mẫu: " + ds.size() + " linh kiện");

        // 2. Thêm linh kiện mới
        String testSKU = "MCU-RP2040-PICO-" + System.currentTimeMillis();
        LinhKien lk = new LinhKien();
        lk.setMaSKU(testSKU);
        lk.setTenLinhKien("Kit Raspberry Pi Pico RP2040 Dual ARM Cortex-M0+");
        lk.setMaDanhMuc(1);
        lk.setNhaSanXuat("Raspberry Pi");
        lk.setKieuDongGoi("MODULE");
        lk.setDienApHoatDong("1.8V - 5.5V");
        lk.setGiaoTiep("SPI, I2C, UART, PWM, PIO");
        lk.setDonViTinh("Cái");
        lk.setGiaBan(BigDecimal.valueOf(99000));
        lk.setSoLuongTon(50);
        lk.setNguongTonToiThieu(10);
        lk.setMoTa("Bo mạch vi điều khiển của Raspberry Pi với chip RP2040");
        lk.setTrangThaiBan("DANG_BAN");

        service.themLinhKien(lk);
        assertTest(lk.getMaLinhKien() > 0, "Thêm mới linh kiện thành công, mã ID sinh ra: " + lk.getMaLinhKien());

        // 3. Tìm kiếm theo mã SKU
        LinhKien foundSKU = service.timTheoSKU(testSKU);
        assertTest(foundSKU != null && foundSKU.getTenLinhKien().contains("Raspberry Pi"),
            "Tìm kiếm linh kiện theo mã SKU chính xác");

        // 4. Tìm kiếm theo từ khóa tên
        List<LinhKien> searchList = service.timKiem("Pico");
        assertTest(!searchList.isEmpty() && searchList.get(0).getMaSKU().equals(testSKU),
            "Tìm kiếm linh kiện theo từ khóa tên ('Pico') thành công");

        // 5. Cập nhật thông tin linh kiện
        foundSKU.setGiaBan(BigDecimal.valueOf(105000));
        foundSKU.setSoLuongTon(40);
        service.capNhatLinhKien(foundSKU);

        LinhKien updated = service.timTheoMa(foundSKU.getMaLinhKien());
        assertTest(updated.getGiaBan().compareTo(BigDecimal.valueOf(105000)) == 0 && updated.getSoLuongTon() == 40,
            "Cập nhật thông tin linh kiện (giá bán: 105.000 VNĐ, tồn kho: 40) thành công");

        // 6. Điều chỉnh tồn kho (UC13)
        service.dieuChinhTonKho(foundSKU.getMaLinhKien(), -5, "Xuất mẫu kiểm thử");
        LinhKien adjusted = service.timTheoMa(foundSKU.getMaLinhKien());
        assertTest(adjusted.getSoLuongTon() == 35,
            "Điều chỉnh tồn kho (-5) thành công, tồn mới: " + adjusted.getSoLuongTon());

        // 7. Kiểm tra cảnh báo tồn thấp
        List<LinhKien> lowStock = service.layLinhKienTonThap();
        assertTest(!lowStock.isEmpty(), "Phát hiện linh kiện tồn thấp (HC-SR04 tồn 5 <= ngưỡng 10)");

        // 8. Xóa linh kiện (hoặc chuyển sang ngừng bán)
        boolean deleted = service.xoaLinhKien(foundSKU.getMaLinhKien());
        assertTest(deleted, "Xóa/ngừng bán linh kiện thử nghiệm thành công");
    }

    private static void testQuanLyGioHang(LinhKienService lkService, GioHangService ghService) {
        System.out.println("\n--- [TEST 3] Kiểm tra Chức năng Giỏ hàng ---");

        int userId = 1;

        // 1. Lấy hoặc khởi tạo giỏ hàng
        GioHang gh = ghService.layGioHang(userId);
        assertTest(gh != null, "Lấy giỏ hàng của người dùng ID = 1 thành công (Mã giỏ: " + gh.getMaGioHang() + ")");

        // 2. Làm trống giỏ trước khi test để có trạng thái kiểm thử rõ ràng
        ghService.lamTrongGio(userId);
        gh = ghService.layGioHang(userId);
        assertTest(gh.getDanhSachChiTiet().isEmpty(), "Làm trống giỏ hàng thành công, số lượng dòng: 0");

        // 3. Thêm linh kiện vào giỏ hàng
        // Chọn linh kiện 1 (Arduino Uno R3, giá 135.000) với số lượng = 2
        ghService.themVaoGio(userId, 1, 2);
        // Chọn linh kiện 2 (ESP32 DevKit, giá 95.000) với số lượng = 3
        ghService.themVaoGio(userId, 2, 3);

        gh = ghService.layGioHang(userId);
        assertTest(gh.getDanhSachChiTiet().size() == 2,
            "Thêm 2 mặt hàng vào giỏ thành công (Số dòng chi tiết = " + gh.getDanhSachChiTiet().size() + ")");

        // 4. Kiểm tra tính tổng tiền (2 * 135.000 + 3 * 95.000 = 270.000 + 285.000 = 555.000)
        BigDecimal expectedTong = BigDecimal.valueOf(2 * 135000L + 3 * 95000L);
        BigDecimal actualTong = ghService.tinhTongTien(userId);
        assertTest(actualTong.compareTo(expectedTong) == 0,
            String.format("Tính tổng tiền giỏ hàng chính xác: %,d VNĐ (kỳ vọng: %,d VNĐ)",
                actualTong.longValue(), expectedTong.longValue()));

        // 5. Cập nhật số lượng linh kiện trong giỏ
        // Tăng Arduino từ 2 lên 4 cái (Tổng tiền mới: 4 * 135.000 + 3 * 95.000 = 540.000 + 285.000 = 825.000)
        ghService.capNhatSoLuong(userId, 1, 4);
        gh = ghService.layGioHang(userId);
        ChiTietGioHang line1 = gh.timDongTheoMa(1);
        assertTest(line1 != null && line1.getSoLuong() == 4,
            "Cập nhật số lượng linh kiện 1 thành 4 cái thành công");

        BigDecimal newTong = ghService.tinhTongTien(userId);
        BigDecimal expectedNewTong = BigDecimal.valueOf(4 * 135000L + 3 * 95000L);
        assertTest(newTong.compareTo(expectedNewTong) == 0,
            String.format("Tính lại tổng tiền sau khi cập nhật số lượng: %,d VNĐ", newTong.longValue()));

        // 6. Kiểm tra ràng buộc tồn kho: Không cho phép thêm vượt quá tồn kho
        LinhKien lk = lkService.timTheoMa(1);
        int overQuantity = lk.getSoLuongTon() + 100;
        boolean caughtOverStock = false;
        try {
            ghService.themVaoGio(userId, 1, overQuantity);
        } catch (IllegalStateException e) {
            caughtOverStock = true;
        }
        assertTest(caughtOverStock, "Bắt ngoại lệ vượt quá tồn kho thành công (Không cho bán quá tồn)");

        // 7. Xóa linh kiện khỏi giỏ hàng
        ghService.xoaKhoiGio(userId, 2);
        gh = ghService.layGioHang(userId);
        assertTest(gh.timDongTheoMa(2) == null && gh.getDanhSachChiTiet().size() == 1,
            "Xóa linh kiện 2 khỏi giỏ hàng thành công, giỏ còn 1 mặt hàng");

        // 8. Làm trống giỏ hàng
        ghService.lamTrongGio(userId);
        gh = ghService.layGioHang(userId);
        assertTest(gh.getDanhSachChiTiet().isEmpty() && ghService.tinhTongTien(userId).compareTo(BigDecimal.ZERO) == 0,
            "Làm trống giỏ hàng thành công, tổng tiền về 0 VNĐ");

        // 9. Nạp lại 2 món mẫu vào giỏ hàng để phục vụ trải nghiệm người dùng
        ghService.themVaoGio(userId, 1, 2); // 2 x Arduino Uno R3
        ghService.themVaoGio(userId, 4, 3); // 3 x DHT11
    }

    private static void assertTest(boolean condition, String message) {
        if (condition) {
            System.out.println("  [PASS] " + message);
            totalPassed++;
        } else {
            System.err.println("  [FAIL] " + message);
            totalFailed++;
        }
    }
}

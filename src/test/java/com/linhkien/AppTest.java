package com.linhkien;

import com.linhkien.config.DatabaseConnection;
import com.linhkien.config.DatabaseInitializer;
import com.linhkien.model.ChiTietGioHang;
import com.linhkien.model.DiaChiNhanHang;
import com.linhkien.model.DonHang;
import com.linhkien.model.GioHang;
import com.linhkien.model.LichSuTonKho;
import com.linhkien.model.LichSuTrangThaiDon;
import com.linhkien.model.LinhKien;
import com.linhkien.model.MaGiamGia;
import com.linhkien.model.ThanhToan;
import com.linhkien.model.ThongSoKyThuat;
import com.linhkien.model.VanChuyen;
import com.linhkien.service.DiaChiService;
import com.linhkien.service.DonHangService;
import com.linhkien.service.GioHangService;
import com.linhkien.service.LinhKienService;
import com.linhkien.service.MaGiamGiaService;
import com.linhkien.service.ThanhToanService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Lớp kiểm thử tự động toàn diện các chức năng nghiệp vụ của hệ thống:
 * 1. Quản lý linh kiện: Thêm, Sửa, Xóa, Tìm kiếm, Cảnh báo tồn thấp.
 * 2. Quản lý giỏ hàng: Thêm vào giỏ, Kiểm tra tồn kho, Cập nhật số lượng, Tính tổng tiền, Xóa.
 * 3. Đặt hàng và quản lý đơn hàng: Tạo đơn từ giỏ, trừ tồn kho, Transaction, chống trùng đơn,
 *    luồng trạng thái vận chuyển, hủy đơn hoàn kho, phân quyền khách hàng / nhân viên.
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
        DonHangService donHangService = new DonHangService();

        testKetNoiCSDL();
        testQuanLyLinhKien(linhKienService);
        testQuanLyGioHang(linhKienService, gioHangService);
        testDatHangVaQuanLyDon(linhKienService, gioHangService, donHangService);
        testLichSuTonKhoVaThongSo(linhKienService);
        testSoDiaChiVaBaoMat();
        testMaGiamGiaVaThanhToan(linhKienService, gioHangService, donHangService);

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

    @FunctionalInterface
    private interface HanhDong {
        void chay() throws Exception;
    }

    /**
     * Chạy hành động và trả về lớp ngoại lệ được ném ra (null nếu không có lỗi).
     */
    private static Class<? extends Exception> batLoi(HanhDong hanhDong) {
        try {
            hanhDong.chay();
            return null;
        } catch (Exception e) {
            return e.getClass();
        }
    }

    private static void themDonMau(GioHangService ghService, int userId, int maLinhKien, int soLuong) {
        ghService.themVaoGio(userId, maLinhKien, soLuong);
    }

    private static void testDatHangVaQuanLyDon(LinhKienService lkService, GioHangService ghService, DonHangService dhService) {
        System.out.println("\n--- [TEST 4] Kiểm tra Đặt hàng, Quản lý đơn hàng và Vận chuyển ---");

        final int khach = 1;
        final int khachKhac = 3; // Người dùng khác (không phải chủ đơn, không phải nhân viên bán hàng)
        final int nhanVien = 2;
        final String hauTo = String.valueOf(System.currentTimeMillis());
        final String diaChi = "12 Nguyễn Văn Bảo, Phường 4, Gò Vấp, TP.HCM";

        ghService.lamTrongGio(khach);
        int ton1Dau = lkService.timTheoMa(1).getSoLuongTon();
        int ton4Dau = lkService.timTheoMa(4).getSoLuongTon();
        BigDecimal gia1 = lkService.timTheoMa(1).getGiaBan();
        BigDecimal gia4 = lkService.timTheoMa(4).getGiaBan();

        // 1. Tạo đơn từ giỏ hàng
        themDonMau(ghService, khach, 1, 2);
        themDonMau(ghService, khach, 4, 3);
        String maYeuCauA = "TEST-A-" + hauTo;
        DonHang donA = dhService.taoDon(khach, "Nguyễn Văn An", "0912345678", diaChi, "COD", maYeuCauA, "Giao giờ hành chính");
        assertTest(donA != null && donA.getMaDonHang() > 0 && DonHang.CHO_XAC_NHAN.equals(donA.getTrangThaiDon()),
            "Tạo đơn từ giỏ hàng thành công, mã đơn #" + donA.getMaDonHang() + ", trạng thái Chờ xác nhận");

        BigDecimal tongKyVong = gia1.multiply(BigDecimal.valueOf(2)).add(gia4.multiply(BigDecimal.valueOf(3)));
        assertTest(donA.getDanhSachChiTiet().size() == 2 && donA.getTongTienHang().compareTo(tongKyVong) == 0
                && donA.getTongThanhToan().compareTo(tongKyVong) == 0 && !donA.isDaHoanKho(),
            String.format("Chi tiết đơn đủ 2 dòng, tổng thanh toán chốt đúng %,d VNĐ", donA.getTongThanhToan().longValue()));

        // 2. Trừ tồn kho và làm trống giỏ
        assertTest(lkService.timTheoMa(1).getSoLuongTon() == ton1Dau - 2 && lkService.timTheoMa(4).getSoLuongTon() == ton4Dau - 3,
            "Tồn kho bị trừ đúng: Arduino " + ton1Dau + " -> " + (ton1Dau - 2) + ", DHT11 " + ton4Dau + " -> " + (ton4Dau - 3));
        assertTest(ghService.layGioHang(khach).getDanhSachChiTiet().isEmpty(), "Giỏ hàng được làm trống sau khi đặt");

        // 3. Nhật ký trạng thái ban đầu
        List<LichSuTrangThaiDon> lichSu = dhService.layLichSuTrangThai(donA.getMaDonHang());
        assertTest(lichSu.size() == 1 && lichSu.get(0).getTrangThaiCu() == null
                && DonHang.CHO_XAC_NHAN.equals(lichSu.get(0).getTrangThaiMoi()),
            "Ghi nhật ký trạng thái ban đầu (mới -> Chờ xác nhận)");

        // 4. Chống trùng đơn (idempotency)
        DonHang donTrung = dhService.taoDon(khach, "Nguyễn Văn An", "0912345678", diaChi, "COD", maYeuCauA, null);
        assertTest(donTrung.getMaDonHang() == donA.getMaDonHang()
                && lkService.timTheoMa(1).getSoLuongTon() == ton1Dau - 2,
            "Gửi lại cùng mã yêu cầu trả về đơn cũ, không tạo đơn mới và không trừ kho lần hai");

        // 5. Kiểm tra dữ liệu đầu vào
        assertTest(IllegalStateException.class.equals(batLoi(() ->
                dhService.taoDon(khach, "Nguyễn Văn An", "0912345678", diaChi, "COD", "TEST-RONG-" + hauTo, null))),
            "Giỏ hàng trống thì không đặt được đơn");
        themDonMau(ghService, khach, 2, 1);
        assertTest(IllegalArgumentException.class.equals(batLoi(() ->
                dhService.taoDon(khach, "Nguyễn Văn An", "123", diaChi, "COD", "TEST-SDT-" + hauTo, null))),
            "Số điện thoại sai định dạng bị từ chối");
        assertTest(IllegalArgumentException.class.equals(batLoi(() ->
                dhService.taoDon(khach, "Nguyễn Văn An", "0912345678", "  ", "COD", "TEST-DC-" + hauTo, null))),
            "Địa chỉ nhận hàng trống bị từ chối");
        assertTest(IllegalArgumentException.class.equals(batLoi(() ->
                dhService.taoDon(khach, "Nguyễn Văn An", "0912345678", diaChi, "TIEN_MAT", "TEST-PT-" + hauTo, null))),
            "Phương thức thanh toán ngoài COD / CHUYEN_KHOAN bị từ chối");
        ghService.lamTrongGio(khach);

        // 6. Transaction: một dòng thiếu tồn thì hoàn tác toàn bộ (không trừ dòng khác, không tạo đơn, giữ nguyên giỏ)
        int soDonTruoc = dhService.layDonCuaKhach(khach).size();
        int ton1TruocLoi = lkService.timTheoMa(1).getSoLuongTon();
        int ton8 = lkService.timTheoMa(8).getSoLuongTon();
        themDonMau(ghService, khach, 1, 1);
        themDonMau(ghService, khach, 8, 2);
        lkService.dieuChinhTonKho(8, -(ton8 - 1), "Kiểm thử: giả lập người khác vừa mua hết hàng");
        Class<? extends Exception> loiTonKho = batLoi(() ->
            dhService.taoDon(khach, "Nguyễn Văn An", "0912345678", diaChi, "COD", "TEST-ROLLBACK-" + hauTo, null));
        assertTest(IllegalStateException.class.equals(loiTonKho), "Thiếu tồn kho ở một dòng thì đặt hàng bị từ chối");
        assertTest(lkService.timTheoMa(1).getSoLuongTon() == ton1TruocLoi
                && dhService.layDonCuaKhach(khach).size() == soDonTruoc
                && ghService.layGioHang(khach).getDanhSachChiTiet().size() == 2,
            "Rollback toàn bộ: tồn kho dòng hợp lệ không bị trừ, không phát sinh đơn, giỏ hàng giữ nguyên");
        lkService.dieuChinhTonKho(8, ton8 - 1, "Kiểm thử: khôi phục tồn kho");
        ghService.lamTrongGio(khach);

        // 7. Linh kiện ngừng bán sau khi đã vào giỏ thì không đặt được
        LinhKien lkThu = new LinhKien();
        lkThu.setMaSKU("TEST-DH-" + hauTo);
        lkThu.setTenLinhKien("Linh kiện thử nghiệm đặt hàng");
        lkThu.setMaDanhMuc(1);
        lkThu.setKieuDongGoi("MODULE");
        lkThu.setDonViTinh("Cái");
        lkThu.setGiaBan(BigDecimal.valueOf(10000));
        lkThu.setSoLuongTon(5);
        lkThu.setNguongTonToiThieu(1);
        lkThu.setTrangThaiBan("DANG_BAN");
        lkService.themLinhKien(lkThu);
        themDonMau(ghService, khach, lkThu.getMaLinhKien(), 1);
        lkThu.setTrangThaiBan("NGUNG_BAN");
        lkService.capNhatLinhKien(lkThu);
        assertTest(IllegalStateException.class.equals(batLoi(() ->
                dhService.taoDon(khach, "Nguyễn Văn An", "0912345678", diaChi, "COD", "TEST-NB-" + hauTo, null))),
            "Linh kiện đã ngừng bán thì không đặt được");
        ghService.lamTrongGio(khach);
        lkService.xoaLinhKien(lkThu.getMaLinhKien());

        // 8. Phân quyền
        final int maDonA = donA.getMaDonHang();
        assertTest(IllegalStateException.class.equals(batLoi(() ->
                dhService.capNhatTrangThai(maDonA, DonHang.DANG_DONG_GOI, khach, null))),
            "Khách hàng không được cập nhật trạng thái đơn");
        assertTest(IllegalStateException.class.equals(batLoi(() -> dhService.huyDon(maDonA, khachKhac, null))),
            "Khách hàng không hủy được đơn của người khác");
        assertTest(IllegalStateException.class.equals(batLoi(() -> dhService.layChiTietDon(maDonA, khachKhac))),
            "Khách hàng không xem được đơn của người khác");
        assertTest(dhService.layChiTietDon(maDonA, nhanVien).getMaDonHang() == maDonA,
            "Nhân viên bán hàng xem được đơn của mọi khách");

        // 9. Luồng trạng thái hợp lệ: Chờ xác nhận -> Đang đóng gói -> Đang giao -> Đã giao
        assertTest(IllegalStateException.class.equals(batLoi(() ->
                dhService.capNhatTrangThai(maDonA, DonHang.DA_GIAO, nhanVien, null))),
            "Không được nhảy cóc từ Chờ xác nhận sang Đã giao");
        dhService.capNhatTrangThai(maDonA, DonHang.DANG_DONG_GOI, nhanVien, null);
        assertTest(DonHang.DANG_DONG_GOI.equals(dhService.layChiTietDon(maDonA, nhanVien).getTrangThaiDon()),
            "Xác nhận đơn: Chờ xác nhận -> Đang đóng gói");

        assertTest(IllegalArgumentException.class.equals(batLoi(() ->
                dhService.capNhatTrangThai(maDonA, DonHang.DANG_GIAO, nhanVien, null))),
            "Chuyển sang Đang giao bắt buộc đi qua bàn giao vận chuyển");
        assertTest(IllegalArgumentException.class.equals(batLoi(() ->
                dhService.banGiaoVanChuyen(maDonA, "", "VD-X", nhanVien))),
            "Bàn giao thiếu đơn vị vận chuyển bị từ chối");

        final String maVanDonA = "VD-" + hauTo + "-A";
        VanChuyen vcA = dhService.banGiaoVanChuyen(maDonA, "GiaoHangNhanh", maVanDonA, nhanVien);
        assertTest(vcA != null && "DANG_GIAO".equals(vcA.getTrangThaiGiao()) && maVanDonA.equals(vcA.getMaVanDon())
                && vcA.getNgayBanGiao() != null && vcA.getNgayGiaoDuKien() != null
                && DonHang.DANG_GIAO.equals(dhService.layChiTietDon(maDonA, nhanVien).getTrangThaiDon()),
            "Bàn giao vận chuyển: Đang đóng gói -> Đang giao, có mã vận đơn và ngày giao dự kiến");

        assertTest(IllegalStateException.class.equals(batLoi(() -> dhService.huyDon(maDonA, nhanVien, "Thử hủy"))),
            "Đơn đang giao không được hủy");

        dhService.capNhatTrangThai(maDonA, DonHang.DA_GIAO, nhanVien, null);
        DonHang daGiao = dhService.layChiTietDon(maDonA, khach);
        VanChuyen vcGiao = dhService.layVanChuyen(maDonA);
        assertTest(DonHang.DA_GIAO.equals(daGiao.getTrangThaiDon()) && "DA_GIAO".equals(vcGiao.getTrangThaiGiao())
                && vcGiao.getNgayGiaoThucTe() != null && vcGiao.isDaThuCOD()
                && "DA_THANH_TOAN".equals(daGiao.getTrangThaiThanhToan()),
            "Giao thành công: Đang giao -> Đã giao, vận chuyển Đã giao, COD được ghi nhận đã thu tiền");
        assertTest(dhService.layLichSuTrangThai(maDonA).size() == 4,
            "Nhật ký trạng thái ghi đủ 4 mốc của đơn đã giao");
        assertTest(IllegalStateException.class.equals(batLoi(() ->
                dhService.capNhatTrangThai(maDonA, DonHang.DA_HUY, nhanVien, null))),
            "Đơn đã giao không thể hủy hoặc đổi trạng thái nữa");
        assertTest(lkService.timTheoMa(1).getSoLuongTon() == ton1Dau - 2,
            "Đơn đã giao giữ nguyên tồn kho đã trừ");

        // 10. Khách hàng hủy đơn đang chờ xác nhận: hoàn kho đúng một lần
        themDonMau(ghService, khach, 1, 4);
        DonHang donB = dhService.taoDon(khach, "Nguyễn Văn An", "0912345678", diaChi, "CHUYEN_KHOAN", "TEST-B-" + hauTo, null);
        assertTest(lkService.timTheoMa(1).getSoLuongTon() == ton1Dau - 2 - 4, "Đơn mới trừ thêm 4 Arduino khỏi kho");
        dhService.huyDon(donB.getMaDonHang(), khach, "Đặt nhầm số lượng");
        DonHang daHuyB = dhService.layChiTietDon(donB.getMaDonHang(), khach);
        assertTest(DonHang.DA_HUY.equals(daHuyB.getTrangThaiDon()) && daHuyB.isDaHoanKho()
                && lkService.timTheoMa(1).getSoLuongTon() == ton1Dau - 2,
            "Khách hủy đơn Chờ xác nhận: trạng thái Đã hủy, tồn kho hoàn lại đủ 4 cái");
        assertTest(IllegalStateException.class.equals(batLoi(() -> dhService.huyDon(donB.getMaDonHang(), khach, null)))
                && lkService.timTheoMa(1).getSoLuongTon() == ton1Dau - 2,
            "Hủy lần hai bị từ chối, tồn kho không bị cộng trùng");

        // 11. Nhân viên hủy đơn đang đóng gói
        themDonMau(ghService, khach, 4, 5);
        DonHang donC = dhService.taoDon(khach, "Nguyễn Văn An", "0912345678", diaChi, "COD", "TEST-C-" + hauTo, null);
        dhService.capNhatTrangThai(donC.getMaDonHang(), DonHang.DANG_DONG_GOI, nhanVien, null);
        dhService.capNhatTrangThai(donC.getMaDonHang(), DonHang.DA_HUY, nhanVien, "Khách đổi ý");
        assertTest(DonHang.DA_HUY.equals(dhService.layChiTietDon(donC.getMaDonHang(), nhanVien).getTrangThaiDon())
                && lkService.timTheoMa(4).getSoLuongTon() == ton4Dau - 3,
            "Nhân viên hủy đơn Đang đóng gói: tồn kho DHT11 hoàn lại đủ 5 cái");

        // 12. Giao thất bại: hoàn kho, vận chuyển ghi nhận thất bại, mã vận đơn không được trùng
        themDonMau(ghService, khach, 4, 2);
        DonHang donD = dhService.taoDon(khach, "Nguyễn Văn An", "0912345678", diaChi, "COD", "TEST-D-" + hauTo, null);
        dhService.capNhatTrangThai(donD.getMaDonHang(), DonHang.DANG_DONG_GOI, nhanVien, null);
        assertTest(IllegalStateException.class.equals(batLoi(() ->
                dhService.banGiaoVanChuyen(donD.getMaDonHang(), "ViettelPost", maVanDonA, nhanVien))),
            "Mã vận đơn đã dùng cho đơn khác thì bị từ chối");
        dhService.banGiaoVanChuyen(donD.getMaDonHang(), "ViettelPost", "VD-" + hauTo + "-D", nhanVien);
        dhService.capNhatTrangThai(donD.getMaDonHang(), DonHang.GIAO_THAT_BAI, nhanVien, "Khách không nghe máy");
        DonHang thatBai = dhService.layChiTietDon(donD.getMaDonHang(), nhanVien);
        assertTest(DonHang.GIAO_THAT_BAI.equals(thatBai.getTrangThaiDon()) && thatBai.isDaHoanKho()
                && "GIAO_THAT_BAI".equals(dhService.layVanChuyen(donD.getMaDonHang()).getTrangThaiGiao())
                && lkService.timTheoMa(4).getSoLuongTon() == ton4Dau - 3
                && !"DA_THANH_TOAN".equals(thatBai.getTrangThaiThanhToan()),
            "Giao thất bại: hoàn kho 2 cái, vận chuyển Giao thất bại, COD chưa thu tiền");

        // 13. Tra cứu và lọc
        assertTest(dhService.layDonCuaKhach(khach).size() >= soDonTruoc + 3
                && dhService.layDonCuaKhach(khachKhac).isEmpty(),
            "Lịch sử đơn của khách đầy đủ và không lẫn đơn của khách khác");
        assertTest(dhService.layTatCaDon(nhanVien, DonHang.DA_HUY).stream().allMatch(d -> DonHang.DA_HUY.equals(d.getTrangThaiDon()))
                && !dhService.layTatCaDon(nhanVien, DonHang.DA_HUY).isEmpty()
                && IllegalStateException.class.equals(batLoi(() -> dhService.layTatCaDon(khach, null))),
            "Nhân viên lọc đơn theo trạng thái; khách hàng không xem được danh sách toàn bộ đơn");

        // 14. Khôi phục dữ liệu mẫu để các lần chạy sau không bị ảnh hưởng
        lkService.dieuChinhTonKho(1, 2, "Kiểm thử: khôi phục tồn kho sau khi đơn mẫu giao xong");
        lkService.dieuChinhTonKho(4, 3, "Kiểm thử: khôi phục tồn kho sau khi đơn mẫu giao xong");
        assertTest(lkService.timTheoMa(1).getSoLuongTon() == ton1Dau && lkService.timTheoMa(4).getSoLuongTon() == ton4Dau,
            "Khôi phục tồn kho về mức ban đầu sau kiểm thử");
        ghService.themVaoGio(khach, 1, 2);
        ghService.themVaoGio(khach, 4, 3);
    }

    private static void testLichSuTonKhoVaThongSo(LinhKienService lkService) {
        System.out.println("\n--- [TEST 5] Kiểm tra Lịch sử tồn kho (NFR03) và Thông số kỹ thuật ---");
        LinhKien before = lkService.timTheoMa(1);
        int tonBanDau = before.getSoLuongTon();

        lkService.dieuChinhTonKho(1, 5, "Nhập bổ sung kiểm thử", 3);
        lkService.dieuChinhTonKho(1, -5, "Xuất kiểm kê kiểm thử", 3);
        LinhKien after = lkService.timTheoMa(1);
        assertTest(after.getSoLuongTon() == tonBanDau,
            "Nhập +5 và điều chỉnh -5 trả tồn kho Arduino về đúng ban đầu: " + tonBanDau);

        List<LichSuTonKho> history = lkService.layLichSuTonKho(1);
        boolean coDieuChinh = !history.isEmpty() && LichSuTonKho.DIEU_CHINH.equals(history.get(0).getLoaiBienDong())
            && history.get(0).getSoLuongThayDoi() == -5;
        boolean coNhap = history.size() >= 2 && LichSuTonKho.NHAP.equals(history.get(1).getLoaiBienDong())
            && history.get(1).getSoLuongThayDoi() == 5;
        assertTest(coNhap && coDieuChinh,
            "Nhật ký tồn kho ghi nhận chính xác cả biến động NHAP (+5) và DIEU_CHINH (-5)");

        List<ThongSoKyThuat> oldSpecs = lkService.layThongSoKyThuat(1);
        List<ThongSoKyThuat> updated = lkService.capNhatThongSoKyThuat(1, List.of(
            new ThongSoKyThuat(1, "Flash Memory", "32", "KB"),
            new ThongSoKyThuat(1, "Clock Speed", "16", "MHz")
        ));
        LinhKien reloaded = lkService.timTheoMa(1);
        assertTest(updated.size() == 2 && reloaded.getDanhSachThongSo().size() == 2,
            "Cập nhật và tải đầy đủ thông số kỹ thuật mở rộng từ bảng ThongSoKyThuat");
        lkService.capNhatThongSoKyThuat(1, oldSpecs);
    }

    private static void testSoDiaChiVaBaoMat() {
        System.out.println("\n--- [TEST 6] Kiểm tra Sổ địa chỉ nhận hàng (FR06) ---");
        DiaChiService diaChiService = new DiaChiService();
        List<DiaChiNhanHang> initial = diaChiService.layDanhSachDiaChi(1);
        assertTest(!initial.isEmpty(), "Khách hàng 1 có sẵn địa chỉ mẫu trong CSDL (" + initial.size() + " địa chỉ)");

        DiaChiNhanHang moi = new DiaChiNhanHang();
        moi.setMaNguoiDung(1);
        moi.setTenNguoiNhan("Nguyễn Văn An (Cơ quan)");
        moi.setSoDienThoaiNhan("0912345678");
        moi.setDiaChiChiTiet("99 Lý Tự Trọng");
        moi.setPhuongXa("Bến Thành");
        moi.setTinhThanh("TP. Hồ Chí Minh");
        moi.setLaMacDinh(true);

        DiaChiNhanHang saved = diaChiService.luuDiaChi(moi);
        DiaChiNhanHang macDinhHienTai = diaChiService.layDiaChiMacDinh(1);
        assertTest(saved.getMaDiaChi() > 0 && macDinhHienTai != null
                && macDinhHienTai.getMaDiaChi() == saved.getMaDiaChi(),
            "Thêm địa chỉ mặc định mới tự động bỏ mặc định địa chỉ cũ");

        if (!initial.isEmpty()) {
            diaChiService.datDiaChiMacDinh(1, initial.get(0).getMaDiaChi());
        }
        boolean deleted = diaChiService.xoaDiaChi(1, saved.getMaDiaChi());
        assertTest(deleted, "Khôi phục địa chỉ mặc định ban đầu và xóa địa chỉ kiểm thử thành công");
    }

    private static void testMaGiamGiaVaThanhToan(LinhKienService lkService, GioHangService ghService, DonHangService dhService) {
        System.out.println("\n--- [TEST 7] Kiểm tra Mã giảm giá (Module 2) & Thanh toán/Hoàn tiền (Module 3) ---");
        MaGiamGiaService couponService = new MaGiamGiaService();
        ThanhToanService paymentService = new ThanhToanService();

        String code = "TST" + (System.currentTimeMillis() % 1000000L);
        MaGiamGia mg = new MaGiamGia(
            code,
            MaGiamGia.PHAN_TRAM,
            BigDecimal.valueOf(10),
            BigDecimal.valueOf(100000),
            2,
            LocalDateTime.now().minusHours(1),
            LocalDateTime.now().plusDays(7)
        );
        MaGiamGia created = couponService.themMaGiamGia(mg, 4);
        assertTest(created.getMaGiamGia() > 0 && code.equals(created.getMaCode()),
            "Tạo mã giảm giá mới thành công: " + code);

        assertTest(IllegalStateException.class.equals(batLoi(() ->
                couponService.kiemTraMaGiamGia(code, BigDecimal.valueOf(50000)))),
            "Từ chối áp dụng mã giảm giá khi đơn hàng chưa đạt mức tối thiểu");

        MaGiamGia unused = couponService.themMaGiamGia(
            new MaGiamGia("DEL" + (System.currentTimeMillis() % 100000L), MaGiamGia.SO_TIEN,
                BigDecimal.valueOf(5000), BigDecimal.ZERO, 1,
                LocalDateTime.now().minusHours(1), LocalDateTime.now().plusDays(1)), 4);
        assertTest(couponService.xoaMaGiamGia(unused.getMaGiamGia(), 4),
            "Xóa mã giảm giá chưa sử dụng thành công");

        ghService.lamTrongGio(1);
        ghService.themVaoGio(1, 1, 2);
        LinhKien lk1 = lkService.timTheoMa(1);
        int ton1Before = lk1.getSoLuongTon();
        BigDecimal subtotal = lk1.getGiaBan().multiply(BigDecimal.valueOf(2));
        DonHang order = dhService.taoDon(1, "Nguyễn Văn An", "0912345678",
            "123 Lê Lợi, Q.1, TP.HCM", "CHUYEN_KHOAN", "TEST-COUPON-" + System.currentTimeMillis(),
            "Giao giờ hành chính", "standard", code);

        BigDecimal expectedDiscount = created.tinhSoTienGiam(subtotal);
        BigDecimal expectedTotal = subtotal.add(BigDecimal.valueOf(25000)).subtract(expectedDiscount);
        assertTest(order.getSoTienGiam().compareTo(expectedDiscount) == 0
                && order.getTongThanhToan().compareTo(expectedTotal) == 0
                && code.equals(order.getMaCodeGiamGia())
                && couponService.layTheoMa(created.getMaGiamGia()).getSoLuotDaDung() == 1,
            "Đặt hàng áp dụng mã giảm giá 10% đúng công thức và tăng lượt dùng lên 1");

        List<ThanhToan> initTx = paymentService.layGiaoDichTheoDon(order.getMaDonHang());
        assertTest(initTx.size() == 1 && ThanhToan.TRANG_THAI_CHO.equals(initTx.get(0).getTrangThai()),
            "Khởi tạo giao dịch ThanhToan (THU / CHO_XAC_NHAN) cùng lúc tạo đơn");

        ThanhToan confirmedTx = paymentService.xacNhanThanhToan(order.getMaDonHang(), "ACB-998877", 4);
        DonHang paidOrder = dhService.layChiTietDon(order.getMaDonHang(), 4);
        assertTest(ThanhToan.TRANG_THAI_THANH_CONG.equals(confirmedTx.getTrangThai())
                && "DA_THANH_TOAN".equals(paidOrder.getTrangThaiThanhToan()),
            "Nhân viên xác nhận thanh toán chuyển khoản thành công -> DA_THANH_TOAN");

        dhService.huyDon(order.getMaDonHang(), 1, "Kiểm thử hủy đơn có mã giảm giá & hoàn tiền");
        DonHang cancelledOrder = dhService.layChiTietDon(order.getMaDonHang(), 4);
        assertTest(DonHang.DA_HUY.equals(cancelledOrder.getTrangThaiDon())
                && "DANG_HOAN_TIEN".equals(cancelledOrder.getTrangThaiThanhToan())
                && lkService.timTheoMa(1).getSoLuongTon() == ton1Before
                && couponService.layTheoMa(created.getMaGiamGia()).getSoLuotDaDung() == 0,
            "Hủy đơn đã thanh toán chuyển DANG_HOAN_TIEN, hoàn kho và hoàn lại lượt dùng mã giảm giá");

        ThanhToan refundTx = paymentService.xacNhanHoanTien(order.getMaDonHang(), "REF-998877", 4);
        DonHang refundedOrder = dhService.layChiTietDon(order.getMaDonHang(), 4);
        assertTest(ThanhToan.LOAI_HOAN.equals(refundTx.getLoaiGiaoDich())
                && ThanhToan.TRANG_THAI_THANH_CONG.equals(refundTx.getTrangThai())
                && "DA_HOAN_TIEN".equals(refundedOrder.getTrangThaiThanhToan()),
            "Xác nhận hoàn tiền tạo giao dịch HOAN/THANH_CONG và cập nhật đơn -> DA_HOAN_TIEN");

        ghService.themVaoGio(1, 1, 2);
        ghService.themVaoGio(1, 4, 3);
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

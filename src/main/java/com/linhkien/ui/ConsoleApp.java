package com.linhkien.ui;

import com.linhkien.config.DatabaseConnection;
import com.linhkien.dao.DanhMucDAO;
import com.linhkien.dao.NguoiDungDAO;
import com.linhkien.model.ChiTietDonHang;
import com.linhkien.model.ChiTietGioHang;
import com.linhkien.model.DanhMucLinhKien;
import com.linhkien.model.DonHang;
import com.linhkien.model.GioHang;
import com.linhkien.model.LichSuTonKho;
import com.linhkien.model.LichSuTrangThaiDon;
import com.linhkien.model.LinhKien;
import com.linhkien.model.MaGiamGia;
import com.linhkien.model.NguoiDung;
import com.linhkien.model.ThanhToan;
import com.linhkien.model.VanChuyen;
import com.linhkien.service.DonHangService;
import com.linhkien.service.GioHangService;
import com.linhkien.service.LinhKienService;
import com.linhkien.service.MaGiamGiaService;
import com.linhkien.service.ThanhToanService;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.UUID;

/**
 * Giao diện điều khiển Console tương tác trực tiếp người dùng.
 * Cho phép thực hiện đầy đủ các chức năng:
 *  - Quản lý linh kiện điện tử (Thêm/Sửa/Xóa/Tìm kiếm theo mã hoặc tên)
 *  - Quản lý giỏ hàng (Xem giỏ, Thêm vào giỏ, Cập nhật số lượng, Tính tổng tiền, Xóa)
 *  - Đặt hàng từ giỏ (trừ tồn kho), xem và hủy đơn hàng của khách
 *  - Quản lý đơn hàng cho nhân viên bán hàng và cập nhật trạng thái vận chuyển
 */
public class ConsoleApp {
    private final LinhKienService linhKienService;
    private final GioHangService gioHangService;
    private final DonHangService donHangService;
    private final MaGiamGiaService maGiamGiaService;
    private final ThanhToanService thanhToanService;
    private final NguoiDungDAO nguoiDungDAO;
    private final DanhMucDAO danhMucDAO;
    private final Scanner scanner;
    private final NumberFormat currencyFormat;

    // Mặc định sử dụng tài khoản khách hàng mẫu ID = 1 (Nguyễn Văn An)
    private static final int DEFAULT_USER_ID = 1;

    // Tài khoản nhân viên bán hàng mẫu ID = 2 (Trần Thị Bích), trùng với dữ liệu mẫu của SQL Server
    private static final int STAFF_USER_ID = 2;

    // Tài khoản nhân viên kho mẫu ID = 3 (Lê Hoàng Long)
    private static final int WAREHOUSE_USER_ID = 3;

    private static final DateTimeFormatter DINH_DANG_GIO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public ConsoleApp() {
        this.linhKienService = new LinhKienService();
        this.gioHangService = new GioHangService();
        this.donHangService = new DonHangService();
        this.maGiamGiaService = new MaGiamGiaService();
        this.thanhToanService = new ThanhToanService();
        this.nguoiDungDAO = new NguoiDungDAO();
        this.danhMucDAO = new DanhMucDAO();
        this.scanner = new Scanner(System.in);
        this.currencyFormat = NumberFormat.getInstance(Locale.of("vi", "VN"));
    }

    public void run() {
        boolean running = true;
        while (running) {
            printMainMenu();
            System.out.print(">> Vui lòng chọn chức năng [0-25]: ");
            if (!scanner.hasNextLine()) {
                break;
            }
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> hienThiDanhSachLinhKien();
                case "2" -> timKiemLinhKien();
                case "3" -> themLinhKienMoi();
                case "4" -> capNhatLinhKien();
                case "5" -> xoaLinhKien();
                case "6" -> xemLinhKienTonThap();
                case "7" -> xemGioHangVaTongTien();
                case "8" -> themLinhKienVaoGio();
                case "9" -> capNhatSoLuongGioHang();
                case "10" -> xoaLinhKienKhoiGio();
                case "11" -> lamTrongGioHang();
                case "12" -> datHang();
                case "13" -> xemLichSuDonHang();
                case "14" -> xemChiTietDonCuaToi();
                case "15" -> huyDonCuaToi();
                case "16" -> xemTatCaDonHang();
                case "17" -> xemChiTietDonNhanVien();
                case "18" -> capNhatTrangThaiDon();
                case "19" -> dieuChinhTonKhoConsole();
                case "20" -> xemLichSuTonKhoConsole();
                case "21" -> xemDanhSachMaGiamGia();
                case "22" -> themMaGiamGiaMoi();
                case "23" -> xacNhanThanhToanConsole();
                case "24" -> xacNhanHoanTienConsole();
                case "25" -> xemLichSuGiaoDichThanhToan();
                case "0" -> {
                    System.out.println("\n[THOÁT] Cảm ơn bạn đã sử dụng hệ thống Quản lý linh kiện điện tử!");
                    DatabaseConnection.closeConnection();
                    running = false;
                }
                default -> System.out.println("❌ Lựa chọn không hợp lệ, vui lòng chọn từ 0 đến 25.\n");
            }
        }
    }

    private void printMainMenu() {
        System.out.println("\n=========================================================================");
        System.out.println("     HỆ THỐNG QUẢN LÝ LINH KIỆN ĐIỆN TỬ VÀ GIỎ HÀNG (PTTKYC)");
        System.out.println("     CSDL Hiện tại: [" + DatabaseConnection.getActiveDbType().toUpperCase() + "] | Khách hàng: [Nguyễn Văn An - ID: " + DEFAULT_USER_ID + "]");
        System.out.println("=========================================================================");
        System.out.println(" [ QUẢN LÝ LINH KIỆN ĐIỆN TỬ ]");
        System.out.println("   1. Xem danh sách tất cả linh kiện");
        System.out.println("   2. Tìm kiếm linh kiện (theo mã ID, mã SKU hoặc tên linh kiện)");
        System.out.println("   3. Thêm mới linh kiện điện tử");
        System.out.println("   4. Cập nhật thông tin linh kiện");
        System.out.println("   5. Xóa / Ngừng bán linh kiện");
        System.out.println("   6. Xem danh sách linh kiện sắp hết hàng (tồn kho thấp)");
        System.out.println(" ------------------------------------------------------------------------");
        System.out.println(" [ CHỨC NĂNG GIỎ HÀNG ]");
        System.out.println("   7. Xem giỏ hàng & Tính tổng tiền");
        System.out.println("   8. Thêm linh kiện vào giỏ hàng");
        System.out.println("   9. Cập nhật số lượng linh kiện trong giỏ");
        System.out.println("  10. Xóa linh kiện khỏi giỏ hàng");
        System.out.println("  11. Làm trống giỏ hàng (Xóa tất cả)");
        System.out.println(" ------------------------------------------------------------------------");
        System.out.println(" [ ĐẶT HÀNG - KHÁCH HÀNG ]");
        System.out.println("  12. Đặt hàng từ giỏ hàng (tạo đơn, trừ tồn kho)");
        System.out.println("  13. Xem lịch sử đơn hàng của tôi");
        System.out.println("  14. Xem chi tiết & theo dõi vận chuyển đơn hàng của tôi");
        System.out.println("  15. Hủy đơn hàng của tôi");
        System.out.println(" ------------------------------------------------------------------------");
        System.out.println(" [ QUẢN LÝ ĐƠN HÀNG - NHÂN VIÊN BÁN HÀNG (Trần Thị Bích - ID: " + STAFF_USER_ID + ") ]");
        System.out.println("  16. Danh sách đơn hàng (lọc theo trạng thái)");
        System.out.println("  17. Xem chi tiết đơn hàng bất kỳ");
        System.out.println("  18. Cập nhật trạng thái đơn / vận chuyển");
        System.out.println(" ------------------------------------------------------------------------");
        System.out.println(" [ KHO - MÃ GIẢM GIÁ - THANH TOÁN (NV KHO / BÁN HÀNG) ]");
        System.out.println("  19. Điều chỉnh tồn kho (Nhập thêm / Kiểm kê)");
        System.out.println("  20. Xem lịch sử biến động tồn kho");
        System.out.println("  21. Xem danh sách mã giảm giá");
        System.out.println("  22. Tạo mã giảm giá mới");
        System.out.println("  23. Xác nhận thanh toán đơn hàng");
        System.out.println("  24. Xác nhận hoàn tiền đơn đã hủy / thất bại");
        System.out.println("  25. Xem lịch sử giao dịch thanh toán");
        System.out.println(" ------------------------------------------------------------------------");
        System.out.println("   0. Thoát chương trình");
        System.out.println("=========================================================================");
    }

    // ==========================================
    // 1. XEM DANH SÁCH LINH KIỆN
    // ==========================================
    private void hienThiDanhSachLinhKien() {
        System.out.println("\n--- DANH SÁCH TẤT CẢ LINH KIỆN ĐIỆN TỬ ---");
        List<LinhKien> list = linhKienService.layTatCaLinhKien();
        inBangLinhKien(list);
    }

    // ==========================================
    // 2. TÌM KIẾM LINH KIỆN
    // ==========================================
    private void timKiemLinhKien() {
        System.out.println("\n--- TÌM KIẾM LINH KIỆN THEO MÃ HOẶC TÊN ---");
        System.out.print("Nhập từ khóa tìm kiếm (Mã ID, SKU hoặc Tên linh kiện): ");
        String keyword = scanner.nextLine().trim();

        if (keyword.isEmpty()) {
            System.out.println("⚠️ Từ khóa trống, hiển thị toàn bộ linh kiện.");
        }

        List<LinhKien> ketQua = linhKienService.timKiem(keyword);
        if (ketQua.isEmpty()) {
            System.out.println("❌ Không tìm thấy linh kiện nào khớp với từ khóa: '" + keyword + "'");
        } else {
            System.out.println("✅ Tìm thấy " + ketQua.size() + " linh kiện phù hợp:");
            inBangLinhKien(ketQua);
        }
    }

    // ==========================================
    // 3. THÊM LINH KIỆN MỚI
    // ==========================================
    private void themLinhKienMoi() {
        System.out.println("\n--- THÊM MỚI LINH KIỆN ĐIỆN TỬ ---");
        try {
            System.out.print("Nhập mã SKU (duy nhất, VD: MCU-PIC16F877A): ");
            String sku = scanner.nextLine().trim();
            if (sku.isEmpty()) {
                System.out.println("❌ Mã SKU không được để trống!");
                return;
            }

            System.out.print("Nhập tên linh kiện: ");
            String ten = scanner.nextLine().trim();
            if (ten.isEmpty()) {
                System.out.println("❌ Tên linh kiện không được để trống!");
                return;
            }

            // Hiển thị danh mục để chọn
            System.out.println("\nDanh mục hiện có:");
            List<DanhMucLinhKien> dmList = danhMucDAO.findAll();
            for (DanhMucLinhKien dm : dmList) {
                System.out.printf("  [%d] %s\n", dm.getMaDanhMuc(), dm.getTenDanhMuc());
            }
            System.out.print("Chọn mã danh mục: ");
            int maDM = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Nhà sản xuất (VD: Texas Instruments, ST): ");
            String nsx = scanner.nextLine().trim();

            System.out.print("Kiểu đóng gói [DIP / SMD / MODULE / KHAC]: ");
            String dongGoi = scanner.nextLine().trim().toUpperCase();
            if (dongGoi.isEmpty()) dongGoi = "KHAC";

            System.out.print("Điện áp hoạt động (VD: 3.3V, 5V): ");
            String dienAp = scanner.nextLine().trim();

            System.out.print("Giao tiếp (VD: I2C, SPI, UART): ");
            String giaoTiep = scanner.nextLine().trim();

            System.out.print("Đơn vị tính (mặc định: Cái): ");
            String dvt = scanner.nextLine().trim();
            if (dvt.isEmpty()) dvt = "Cái";

            System.out.print("Giá bán (VNĐ, VD: 45000): ");
            long giaLong = Long.parseLong(scanner.nextLine().trim());
            if (giaLong < 0) {
                System.out.println("❌ Giá bán không được âm!");
                return;
            }

            System.out.print("Số lượng tồn kho ban đầu: ");
            int ton = Integer.parseInt(scanner.nextLine().trim());
            if (ton < 0) {
                System.out.println("❌ Số lượng tồn không được âm!");
                return;
            }

            System.out.print("Ngưỡng tồn tối thiểu (cảnh báo tồn thấp, mặc định: 10): ");
            String nguongStr = scanner.nextLine().trim();
            int nguong = nguongStr.isEmpty() ? 10 : Integer.parseInt(nguongStr);

            System.out.print("Mô tả tóm tắt: ");
            String moTa = scanner.nextLine().trim();

            LinhKien lk = new LinhKien();
            lk.setMaSKU(sku);
            lk.setTenLinhKien(ten);
            lk.setMaDanhMuc(maDM);
            lk.setNhaSanXuat(nsx);
            lk.setKieuDongGoi(dongGoi);
            lk.setDienApHoatDong(dienAp);
            lk.setGiaoTiep(giaoTiep);
            lk.setDonViTinh(dvt);
            lk.setGiaBan(BigDecimal.valueOf(giaLong));
            lk.setSoLuongTon(ton);
            lk.setNguongTonToiThieu(nguong);
            lk.setMoTa(moTa);
            lk.setTrangThaiBan("DANG_BAN");

            linhKienService.themLinhKien(lk);
            System.out.println("🎉 Thêm mới linh kiện thành công! Mã ID sinh ra: " + lk.getMaLinhKien());
        } catch (NumberFormatException e) {
            System.out.println("❌ Giá trị số không hợp lệ, vui lòng kiểm tra lại!");
        } catch (Exception e) {
            System.out.println("❌ Thêm linh kiện thất bại: " + e.getMessage());
        }
    }

    // ==========================================
    // 4. CẬP NHẬT LINH KIỆN
    // ==========================================
    private void capNhatLinhKien() {
        System.out.println("\n--- CẬP NHẬT THÔNG TIN LINH KIỆN ---");
        try {
            System.out.print("Nhập mã ID linh kiện cần sửa: ");
            int id = Integer.parseInt(scanner.nextLine().trim());

            LinhKien lk = linhKienService.timTheoMa(id);
            if (lk == null) {
                System.out.println("❌ Không tìm thấy linh kiện với mã ID: " + id);
                return;
            }

            System.out.println("Đang chỉnh sửa: " + lk);
            System.out.println("(Nhấn ENTER để giữ nguyên giá trị cũ)");

            System.out.print("Tên mới [" + lk.getTenLinhKien() + "]: ");
            String ten = scanner.nextLine().trim();
            if (!ten.isEmpty()) lk.setTenLinhKien(ten);

            System.out.print("Mã SKU mới [" + lk.getMaSKU() + "]: ");
            String sku = scanner.nextLine().trim();
            if (!sku.isEmpty()) lk.setMaSKU(sku);

            System.out.print("Giá bán mới [" + formatTien(lk.getGiaBan()) + "]: ");
            String giaStr = scanner.nextLine().trim();
            if (!giaStr.isEmpty()) lk.setGiaBan(BigDecimal.valueOf(Long.parseLong(giaStr)));

            System.out.print("Số lượng tồn kho mới [" + lk.getSoLuongTon() + "]: ");
            String tonStr = scanner.nextLine().trim();
            if (!tonStr.isEmpty()) lk.setSoLuongTon(Integer.parseInt(tonStr));

            System.out.print("Ngưỡng tồn tối thiểu [" + lk.getNguongTonToiThieu() + "]: ");
            String nguongStr = scanner.nextLine().trim();
            if (!nguongStr.isEmpty()) lk.setNguongTonToiThieu(Integer.parseInt(nguongStr));

            System.out.print("Trạng thái bán [DANG_BAN / NGUNG_BAN] [" + lk.getTrangThaiBan() + "]: ");
            String tt = scanner.nextLine().trim().toUpperCase();
            if (!tt.isEmpty()) lk.setTrangThaiBan(tt);

            linhKienService.capNhatLinhKien(lk);
            System.out.println("🎉 Cập nhật linh kiện thành công!");
        } catch (NumberFormatException e) {
            System.out.println("❌ Giá trị số không hợp lệ!");
        } catch (Exception e) {
            System.out.println("❌ Cập nhật thất bại: " + e.getMessage());
        }
    }

    // ==========================================
    // 5. XÓA / NGỪNG BÁN LINH KIỆN
    // ==========================================
    private void xoaLinhKien() {
        System.out.println("\n--- XÓA / NGỪNG BÁN LINH KIỆN ---");
        try {
            System.out.print("Nhập mã ID linh kiện cần xóa: ");
            int id = Integer.parseInt(scanner.nextLine().trim());

            LinhKien lk = linhKienService.timTheoMa(id);
            if (lk == null) {
                System.out.println("❌ Không tìm thấy linh kiện với mã ID: " + id);
                return;
            }

            System.out.println("Linh kiện chọn: " + lk.getTenLinhKien() + " (SKU: " + lk.getMaSKU() + ")");
            System.out.print("Bạn có chắc chắn muốn xóa/ngừng bán linh kiện này? [y/N]: ");
            String confirm = scanner.nextLine().trim().toLowerCase();
            if ("y".equals(confirm) || "yes".equals(confirm)) {
                boolean ok = linhKienService.xoaLinhKien(id);
                if (ok) {
                    System.out.println("✅ Đã xử lý xóa/ngừng bán linh kiện thành công!");
                } else {
                    System.out.println("❌ Không thể thực hiện thao tác.");
                }
            } else {
                System.out.println("Đã hủy thao tác.");
            }
        } catch (NumberFormatException e) {
            System.out.println("❌ Mã ID phải là số nguyên!");
        } catch (Exception e) {
            System.out.println("❌ Lỗi: " + e.getMessage());
        }
    }

    // ==========================================
    // 6. XEM LINH KIỆN TỒN KHO THẤP
    // ==========================================
    private void xemLinhKienTonThap() {
        System.out.println("\n--- DANH SÁCH LINH KIỆN CÓ MỨC TỒN KHO THẤP (<= NGƯỠNG) ---");
        List<LinhKien> list = linhKienService.layLinhKienTonThap();
        if (list.isEmpty()) {
            System.out.println("✅ Tất cả linh kiện đều đang ở mức tồn an toàn!");
        } else {
            System.out.println("⚠️ Có " + list.size() + " linh kiện cần nhập thêm hàng gấp:");
            inBangLinhKien(list);
        }
    }

    // ==========================================
    // 7. XEM GIỎ HÀNG VÀ TÍNH TỔNG TIỀN
    // ==========================================
    private void xemGioHangVaTongTien() {
        System.out.println("\n=========================================================================");
        System.out.println("                  CHI TIẾT GIỎ HÀNG CỦA BẠN");
        System.out.println("=========================================================================");
        try {
            GioHang gioHang = gioHangService.layGioHang(DEFAULT_USER_ID);
            List<ChiTietGioHang> danhSach = gioHang.getDanhSachChiTiet();

            if (danhSach.isEmpty()) {
                System.out.println("🛒 Giỏ hàng của bạn đang trống! Hãy chọn thêm sản phẩm.");
                System.out.println("=========================================================================");
                return;
            }

            System.out.printf("%-4s | %-18s | %-32s | %-10s | %-6s | %-14s\n",
                "STT", "Mã SKU", "Tên linh kiện", "Đơn giá", "SL", "Thành tiền");
            System.out.println("-------------------------------------------------------------------------");

            int stt = 1;
            for (ChiTietGioHang line : danhSach) {
                LinhKien lk = line.getLinhKien();
                String sku = lk != null ? lk.getMaSKU() : "N/A";
                String ten = lk != null ? rutGon(lk.getTenLinhKien(), 32) : "Mã LK: " + line.getMaLinhKien();
                BigDecimal donGia = lk != null ? lk.getGiaBan() : BigDecimal.ZERO;
                BigDecimal thanhTien = line.tinhThanhTien();

                System.out.printf("%-4d | %-18s | %-32s | %10s | %6d | %14s\n",
                    stt++, sku, ten, formatTien(donGia), line.getSoLuong(), formatTien(thanhTien));
            }

            BigDecimal tongTien = gioHangService.tinhTongTien(DEFAULT_USER_ID);
            System.out.println("=========================================================================");
            System.out.printf("  TỔNG SỐ LƯỢNG MẶT HÀNG : %d sản phẩm\n", gioHang.getTongSoLuong());
            System.out.printf("  TỔNG TIỀN THANH TOÁN   : %s VNĐ\n", formatTien(tongTien));
            System.out.println("=========================================================================");
        } catch (Exception e) {
            System.out.println("❌ Lỗi khi đọc giỏ hàng: " + e.getMessage());
        }
    }

    // ==========================================
    // 8. THÊM LINH KIỆN VÀO GIỎ HÀNG
    // ==========================================
    private void themLinhKienVaoGio() {
        System.out.println("\n--- THÊM LINH KIỆN VÀO GIỎ HÀNG ---");
        try {
            System.out.print("Nhập mã ID linh kiện muốn thêm vào giỏ: ");
            int id = Integer.parseInt(scanner.nextLine().trim());

            LinhKien lk = linhKienService.timTheoMa(id);
            if (lk == null) {
                System.out.println("❌ Không tìm thấy linh kiện với mã: " + id);
                return;
            }

            System.out.printf("Linh kiện: %s | Giá: %s VNĐ | Tồn kho hiện có: %d %s | Trạng thái: %s\n",
                lk.getTenLinhKien(), formatTien(lk.getGiaBan()), lk.getSoLuongTon(), lk.getDonViTinh(), lk.getTrangThaiBan());

            if (!"DANG_BAN".equalsIgnoreCase(lk.getTrangThaiBan())) {
                System.out.println("❌ Sản phẩm này đang ngừng bán, không thể thêm vào giỏ!");
                return;
            }

            if (lk.getSoLuongTon() <= 0) {
                System.out.println("❌ Sản phẩm này đã hết hàng!");
                return;
            }

            System.out.print("Nhập số lượng muốn mua: ");
            int sl = Integer.parseInt(scanner.nextLine().trim());

            gioHangService.themVaoGio(DEFAULT_USER_ID, id, sl);
            System.out.printf("🎉 Đã thêm %d %s '%s' vào giỏ hàng thành công!\n",
                sl, lk.getDonViTinh(), lk.getTenLinhKien());

            // Tự động hiển thị lại tổng tiền giỏ hàng sau khi thêm
            BigDecimal tongTien = gioHangService.tinhTongTien(DEFAULT_USER_ID);
            System.out.printf("💰 Tổng tiền giỏ hàng hiện tại: %s VNĐ\n", formatTien(tongTien));
        } catch (NumberFormatException e) {
            System.out.println("❌ Số lượng hoặc mã ID phải là số nguyên!");
        } catch (Exception e) {
            System.out.println("❌ Thêm vào giỏ thất bại: " + e.getMessage());
        }
    }

    // ==========================================
    // 9. CẬP NHẬT SỐ LƯỢNG TRONG GIỎ HÀNG
    // ==========================================
    private void capNhatSoLuongGioHang() {
        System.out.println("\n--- CẬP NHẬT SỐ LƯỢNG LINH KIỆN TRONG GIỎ ---");
        try {
            GioHang gh = gioHangService.layGioHang(DEFAULT_USER_ID);
            if (gh.getDanhSachChiTiet().isEmpty()) {
                System.out.println("🛒 Giỏ hàng hiện đang trống!");
                return;
            }

            System.out.print("Nhập mã ID linh kiện cần đổi số lượng: ");
            int id = Integer.parseInt(scanner.nextLine().trim());

            ChiTietGioHang line = gh.timDongTheoMa(id);
            if (line == null) {
                System.out.println("❌ Linh kiện này không có trong giỏ hàng của bạn!");
                return;
            }

            System.out.printf("Sản phẩm: %s (Hiện có trong giỏ: %d)\n",
                line.getLinhKien() != null ? line.getLinhKien().getTenLinhKien() : "Mã " + id, line.getSoLuong());
            System.out.print("Nhập số lượng mới (Nhập 0 để xóa khỏi giỏ): ");
            int slMoi = Integer.parseInt(scanner.nextLine().trim());

            gioHangService.capNhatSoLuong(DEFAULT_USER_ID, id, slMoi);
            if (slMoi <= 0) {
                System.out.println("🗑️ Đã xóa sản phẩm khỏi giỏ hàng!");
            } else {
                System.out.println("✅ Đã cập nhật số lượng thành công!");
            }

            BigDecimal tongTien = gioHangService.tinhTongTien(DEFAULT_USER_ID);
            System.out.printf("💰 Tổng tiền giỏ hàng sau cập nhật: %s VNĐ\n", formatTien(tongTien));
        } catch (NumberFormatException e) {
            System.out.println("❌ Giá trị số lượng không hợp lệ!");
        } catch (Exception e) {
            System.out.println("❌ Cập nhật thất bại: " + e.getMessage());
        }
    }

    // ==========================================
    // 10. XÓA LINH KIỆN KHỎI GIỎ HÀNG
    // ==========================================
    private void xoaLinhKienKhoiGio() {
        System.out.println("\n--- XÓA LINH KIỆN KHỎI GIỎ HÀNG ---");
        try {
            System.out.print("Nhập mã ID linh kiện muốn xóa khỏi giỏ: ");
            int id = Integer.parseInt(scanner.nextLine().trim());

            gioHangService.xoaKhoiGio(DEFAULT_USER_ID, id);
            System.out.println("🗑️ Đã xóa linh kiện khỏi giỏ hàng!");

            BigDecimal tongTien = gioHangService.tinhTongTien(DEFAULT_USER_ID);
            System.out.printf("💰 Tổng tiền giỏ hàng còn lại: %s VNĐ\n", formatTien(tongTien));
        } catch (NumberFormatException e) {
            System.out.println("❌ Mã ID phải là số nguyên!");
        } catch (Exception e) {
            System.out.println("❌ Xóa thất bại: " + e.getMessage());
        }
    }

    // ==========================================
    // 11. LÀM TRỐNG GIỎ HÀNG
    // ==========================================
    private void lamTrongGioHang() {
        System.out.print("Bạn có chắc chắn muốn xóa toàn bộ sản phẩm trong giỏ hàng? [y/N]: ");
        String confirm = scanner.nextLine().trim().toLowerCase();
        if ("y".equals(confirm) || "yes".equals(confirm)) {
            try {
                gioHangService.lamTrongGio(DEFAULT_USER_ID);
                System.out.println("🧹 Đã làm trống toàn bộ giỏ hàng thành công!");
            } catch (Exception e) {
                System.out.println("❌ Lỗi: " + e.getMessage());
            }
        } else {
            System.out.println("Đã hủy thao tác.");
        }
    }

    // ==========================================
    // 12. ĐẶT HÀNG TỪ GIỎ HÀNG (UC08)
    // ==========================================
    private void datHang() {
        System.out.println("\n--- ĐẶT HÀNG TỪ GIỎ HÀNG ---");
        try {
            GioHang gioHang = gioHangService.layGioHang(DEFAULT_USER_ID);
            if (gioHang.getDanhSachChiTiet().isEmpty()) {
                System.out.println("🛒 Giỏ hàng đang trống, hãy thêm linh kiện vào giỏ trước khi đặt hàng.");
                return;
            }
            xemGioHangVaTongTien();

            NguoiDung khach = nguoiDungDAO.findById(DEFAULT_USER_ID);
            String tenMacDinh = khach != null ? khach.getHoTen() : "";
            String sdtMacDinh = khach != null && khach.getSoDienThoai() != null ? khach.getSoDienThoai() : "";

            System.out.print("Tên người nhận [Enter để dùng '" + tenMacDinh + "']: ");
            String ten = scanner.nextLine().trim();
            if (ten.isEmpty()) {
                ten = tenMacDinh;
            }

            System.out.print("Số điện thoại nhận hàng [Enter để dùng '" + sdtMacDinh + "']: ");
            String sdt = scanner.nextLine().trim();
            if (sdt.isEmpty()) {
                sdt = sdtMacDinh;
            }

            System.out.print("Địa chỉ nhận hàng: ");
            String diaChi = scanner.nextLine().trim();

            System.out.println("Phương thức thanh toán:  1. COD (thanh toán khi nhận hàng)   2. Chuyển khoản");
            System.out.print("Chọn [1-2, mặc định 1]: ");
            String pt = scanner.nextLine().trim();
            String phuongThuc = "2".equals(pt) ? "CHUYEN_KHOAN" : "COD";

            System.out.print("Mã giảm giá (có thể bỏ trống): ");
            String maGiamGia = scanner.nextLine().trim();

            System.out.print("Ghi chú cho đơn hàng (có thể bỏ trống): ");
            String ghiChu = scanner.nextLine().trim();

            System.out.print("Xác nhận đặt hàng? [y/N]: ");
            String xacNhan = scanner.nextLine().trim();
            if (!xacNhan.equalsIgnoreCase("y")) {
                System.out.println("↩️ Đã hủy thao tác đặt hàng, giỏ hàng được giữ nguyên.");
                return;
            }

            // Mỗi lần xác nhận sinh một mã yêu cầu riêng để chống tạo trùng đơn (NFR05)
            String maYeuCau = UUID.randomUUID().toString();
            DonHang dh = donHangService.taoDon(DEFAULT_USER_ID, ten, sdt, diaChi, phuongThuc, maYeuCau, ghiChu, "standard", maGiamGia);

            System.out.println("🎉 Đặt hàng thành công! Tồn kho đã được trừ và giỏ hàng đã được làm trống.");
            inChiTietDon(dh);
        } catch (Exception e) {
            System.out.println("❌ Đặt hàng thất bại: " + e.getMessage());
        }
    }

    // ==========================================
    // 13. XEM LỊCH SỬ ĐƠN HÀNG CỦA TÔI (UC10)
    // ==========================================
    private void xemLichSuDonHang() {
        System.out.println("\n--- LỊCH SỬ ĐƠN HÀNG CỦA TÔI ---");
        try {
            List<DonHang> ds = donHangService.layDonCuaKhach(DEFAULT_USER_ID);
            inBangDonHang(ds, false);
        } catch (Exception e) {
            System.out.println("❌ Lỗi khi đọc lịch sử đơn hàng: " + e.getMessage());
        }
    }

    // ==========================================
    // 14. XEM CHI TIẾT & VẬN CHUYỂN ĐƠN CỦA TÔI (UC10, UC11)
    // ==========================================
    private void xemChiTietDonCuaToi() {
        System.out.println("\n--- CHI TIẾT VÀ THEO DÕI VẬN CHUYỂN ĐƠN HÀNG ---");
        xemChiTietDon(DEFAULT_USER_ID);
    }

    // ==========================================
    // 15. HỦY ĐƠN HÀNG CỦA TÔI (UC19)
    // ==========================================
    private void huyDonCuaToi() {
        System.out.println("\n--- HỦY ĐƠN HÀNG ---");
        try {
            System.out.print("Nhập mã đơn hàng muốn hủy: ");
            int maDon = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Lý do hủy (có thể bỏ trống): ");
            String lyDo = scanner.nextLine().trim();

            donHangService.huyDon(maDon, DEFAULT_USER_ID, lyDo);
            System.out.println("✅ Đã hủy đơn hàng #" + maDon + " và hoàn lại tồn kho các linh kiện.");
        } catch (NumberFormatException e) {
            System.out.println("❌ Mã đơn hàng phải là số nguyên!");
        } catch (Exception e) {
            System.out.println("❌ Hủy đơn thất bại: " + e.getMessage());
        }
    }

    // ==========================================
    // 16. DANH SÁCH ĐƠN HÀNG CHO NHÂN VIÊN (UC14)
    // ==========================================
    private void xemTatCaDonHang() {
        System.out.println("\n--- DANH SÁCH ĐƠN HÀNG CỦA CỬA HÀNG ---");
        System.out.println("Lọc theo trạng thái:  1. Chờ xác nhận  2. Đang đóng gói  3. Đang giao  4. Đã giao  5. Giao thất bại  6. Đã hủy  (Enter = tất cả)");
        System.out.print("Chọn [1-6]: ");
        String chon = scanner.nextLine().trim();
        String loc = switch (chon) {
            case "1" -> DonHang.CHO_XAC_NHAN;
            case "2" -> DonHang.DANG_DONG_GOI;
            case "3" -> DonHang.DANG_GIAO;
            case "4" -> DonHang.DA_GIAO;
            case "5" -> DonHang.GIAO_THAT_BAI;
            case "6" -> DonHang.DA_HUY;
            default -> "";
        };
        try {
            inBangDonHang(donHangService.layTatCaDon(STAFF_USER_ID, loc), true);
        } catch (Exception e) {
            System.out.println("❌ Lỗi khi đọc danh sách đơn hàng: " + e.getMessage());
        }
    }

    // ==========================================
    // 17. XEM CHI TIẾT ĐƠN HÀNG BẤT KỲ (NHÂN VIÊN)
    // ==========================================
    private void xemChiTietDonNhanVien() {
        System.out.println("\n--- CHI TIẾT ĐƠN HÀNG (NHÂN VIÊN) ---");
        xemChiTietDon(STAFF_USER_ID);
    }

    private void xemChiTietDon(int maNguoiXem) {
        try {
            System.out.print("Nhập mã đơn hàng: ");
            int maDon = Integer.parseInt(scanner.nextLine().trim());
            DonHang dh = donHangService.layChiTietDon(maDon, maNguoiXem);
            inChiTietDon(dh);
        } catch (NumberFormatException e) {
            System.out.println("❌ Mã đơn hàng phải là số nguyên!");
        } catch (Exception e) {
            System.out.println("❌ Không xem được đơn hàng: " + e.getMessage());
        }
    }

    // ==========================================
    // 18. CẬP NHẬT TRẠNG THÁI ĐƠN / VẬN CHUYỂN (UC14)
    // ==========================================
    private void capNhatTrangThaiDon() {
        System.out.println("\n--- CẬP NHẬT TRẠNG THÁI ĐƠN HÀNG / VẬN CHUYỂN ---");
        try {
            System.out.print("Nhập mã đơn hàng cần xử lý: ");
            int maDon = Integer.parseInt(scanner.nextLine().trim());
            DonHang dh = donHangService.layChiTietDon(maDon, STAFF_USER_ID);

            System.out.printf("Đơn #%d | Khách: %s | Tổng thanh toán: %s VNĐ | Trạng thái hiện tại: %s\n",
                dh.getMaDonHang(), dh.getTenKhachHang(), formatTien(dh.getTongThanhToan()),
                DonHang.nhanTrangThai(dh.getTrangThaiDon()));

            // Các trạng thái kế tiếp, sắp theo thứ tự luồng xử lý
            String[] thuTu = {DonHang.DANG_DONG_GOI, DonHang.DANG_GIAO, DonHang.DA_GIAO,
                DonHang.GIAO_THAT_BAI, DonHang.DA_HUY};
            java.util.List<String> tiepTheo = new java.util.ArrayList<>();
            for (String tt : thuTu) {
                if (dh.coTheChuyenSang(tt)) {
                    tiepTheo.add(tt);
                }
            }
            if (tiepTheo.isEmpty()) {
                System.out.println("⚠️ Đơn hàng đã ở trạng thái cuối '" + DonHang.nhanTrangThai(dh.getTrangThaiDon())
                    + "', không thể cập nhật thêm.");
                return;
            }

            System.out.println("Có thể chuyển sang:");
            for (int i = 0; i < tiepTheo.size(); i++) {
                System.out.printf("  %d. %s\n", i + 1, DonHang.nhanTrangThai(tiepTheo.get(i)));
            }
            System.out.print("Chọn [1-" + tiepTheo.size() + "]: ");
            int chon = Integer.parseInt(scanner.nextLine().trim());
            if (chon < 1 || chon > tiepTheo.size()) {
                System.out.println("❌ Lựa chọn không hợp lệ.");
                return;
            }
            String moi = tiepTheo.get(chon - 1);

            if (DonHang.DANG_GIAO.equals(moi)) {
                System.out.print("Đơn vị vận chuyển (VD: GiaoHangNhanh, ViettelPost, J&T): ");
                String donVi = scanner.nextLine().trim();
                System.out.print("Mã vận đơn: ");
                String maVanDon = scanner.nextLine().trim();
                VanChuyen vc = donHangService.banGiaoVanChuyen(maDon, donVi, maVanDon, STAFF_USER_ID);
                System.out.printf("✅ Đã bàn giao đơn #%d cho %s, mã vận đơn %s, dự kiến giao %s.\n",
                    maDon, vc.getDonViVanChuyen(), vc.getMaVanDon(),
                    vc.getNgayGiaoDuKien() != null ? vc.getNgayGiaoDuKien().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "chưa rõ");
                return;
            }

            String ghiChu = "";
            if (DonHang.DA_HUY.equals(moi) || DonHang.GIAO_THAT_BAI.equals(moi)) {
                System.out.print("Lý do (có thể bỏ trống): ");
                ghiChu = scanner.nextLine().trim();
            }
            donHangService.capNhatTrangThai(maDon, moi, STAFF_USER_ID, ghiChu);
            System.out.printf("✅ Đã cập nhật đơn #%d sang trạng thái '%s'.\n", maDon, DonHang.nhanTrangThai(moi));
            if (DonHang.DA_HUY.equals(moi) || DonHang.GIAO_THAT_BAI.equals(moi)) {
                System.out.println("   Tồn kho các linh kiện của đơn đã được hoàn lại.");
            }
        } catch (NumberFormatException e) {
            System.out.println("❌ Mã đơn hàng và lựa chọn phải là số nguyên!");
        } catch (Exception e) {
            System.out.println("❌ Cập nhật thất bại: " + e.getMessage());
        }
    }

    // ==========================================
    // HIỂN THỊ ĐƠN HÀNG
    // ==========================================
    private void inBangDonHang(List<DonHang> ds, boolean hienKhach) {
        if (ds == null || ds.isEmpty()) {
            System.out.println("📭 Không có đơn hàng nào.");
            return;
        }
        System.out.println("---------------------------------------------------------------------------------------------");
        System.out.printf("%-6s | %-16s | %-20s | %-15s | %-14s | %-12s\n",
            "Mã đơn", "Ngày đặt", hienKhach ? "Khách hàng" : "Người nhận", "Tổng thanh toán", "Trạng thái", "Thanh toán");
        System.out.println("---------------------------------------------------------------------------------------------");
        for (DonHang dh : ds) {
            String ten = hienKhach ? dh.getTenKhachHang() : dh.getTenNguoiNhan();
            System.out.printf("%-6d | %-16s | %-20s | %15s | %-14s | %-12s\n",
                dh.getMaDonHang(), formatGio(dh.getNgayDat()), rutGon(ten, 20), formatTien(dh.getTongThanhToan()),
                DonHang.nhanTrangThai(dh.getTrangThaiDon()), dh.getPhuongThucThanhToan());
        }
        System.out.println("---------------------------------------------------------------------------------------------");
        System.out.println("Tổng số đơn hàng: " + ds.size());
    }

    private void inChiTietDon(DonHang dh) {
        System.out.println("\n=========================================================================");
        System.out.printf("  ĐƠN HÀNG #%d  |  Trạng thái: %s\n", dh.getMaDonHang(), DonHang.nhanTrangThai(dh.getTrangThaiDon()));
        System.out.println("=========================================================================");
        System.out.println("  Ngày đặt       : " + formatGio(dh.getNgayDat()));
        System.out.println("  Khách hàng     : " + dh.getTenKhachHang());
        System.out.println("  Người nhận     : " + dh.getTenNguoiNhan() + " - " + dh.getSoDienThoaiNhan());
        System.out.println("  Địa chỉ        : " + dh.getDiaChiNhan());
        System.out.println("  Thanh toán     : " + dh.getPhuongThucThanhToan() + " (" + DonHang.nhanThanhToan(dh.getTrangThaiThanhToan()) + ")");
        if (dh.getGhiChu() != null) {
            System.out.println("  Ghi chú        : " + dh.getGhiChu());
        }
        System.out.println("-------------------------------------------------------------------------");
        System.out.printf("%-4s | %-18s | %-30s | %-10s | %-5s | %-13s\n", "STT", "Mã SKU", "Tên linh kiện", "Đơn giá", "SL", "Thành tiền");
        System.out.println("-------------------------------------------------------------------------");
        int stt = 1;
        for (ChiTietDonHang ct : dh.getDanhSachChiTiet()) {
            System.out.printf("%-4d | %-18s | %-30s | %10s | %5d | %13s\n", stt++, ct.getMaSKU(),
                rutGon(ct.getTenLinhKien(), 30), formatTien(ct.getDonGia()), ct.getSoLuong(), formatTien(ct.tinhThanhTien()));
        }
        System.out.println("-------------------------------------------------------------------------");
        System.out.printf("  Tổng tiền hàng     : %s VNĐ\n", formatTien(dh.getTongTienHang()));
        System.out.printf("  Phí vận chuyển     : %s VNĐ\n", formatTien(dh.getPhiVanChuyen()));
        System.out.printf("  Giảm giá           : %s VNĐ\n", formatTien(dh.getSoTienGiam()));
        System.out.printf("  TỔNG THANH TOÁN    : %s VNĐ\n", formatTien(dh.getTongThanhToan()));

        try {
            VanChuyen vc = donHangService.layVanChuyen(dh.getMaDonHang());
            System.out.println("-------------------------------------------------------------------------");
            if (vc == null) {
                System.out.println("  Vận chuyển: chưa bàn giao cho đơn vị vận chuyển.");
            } else {
                System.out.println("  Vận chuyển: " + vc.getDonViVanChuyen() + " | Mã vận đơn: " + vc.getMaVanDon()
                    + " | Trạng thái giao: " + nhanTrangThaiGiao(vc.getTrangThaiGiao()));
                System.out.println("     Bàn giao: " + formatGio(vc.getNgayBanGiao())
                    + " | Dự kiến giao: " + (vc.getNgayGiaoDuKien() != null ? vc.getNgayGiaoDuKien().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "-")
                    + " | Giao thực tế: " + formatGio(vc.getNgayGiaoThucTe())
                    + (vc.isDaThuCOD() ? " | Đã thu COD" : ""));
            }

            List<LichSuTrangThaiDon> lichSu = donHangService.layLichSuTrangThai(dh.getMaDonHang());
            System.out.println("-------------------------------------------------------------------------");
            System.out.println("  Lịch sử trạng thái:");
            for (LichSuTrangThaiDon ls : lichSu) {
                String cu = ls.getTrangThaiCu() == null ? "(mới)" : DonHang.nhanTrangThai(ls.getTrangThaiCu());
                System.out.printf("   - %s | %s -> %s | %s%s\n", formatGio(ls.getThoiDiem()), cu,
                    DonHang.nhanTrangThai(ls.getTrangThaiMoi()),
                    ls.getTenNguoiThucHien() != null ? ls.getTenNguoiThucHien() : "Hệ thống",
                    ls.getGhiChu() != null ? " | " + ls.getGhiChu() : "");
            }
        } catch (Exception e) {
            System.out.println("  (Không đọc được thông tin vận chuyển: " + e.getMessage() + ")");
        }
        System.out.println("=========================================================================");
    }

    private String nhanTrangThaiGiao(String trangThaiGiao) {
        if (trangThaiGiao == null) {
            return "";
        }
        return switch (trangThaiGiao) {
            case "CHO_LAY_HANG" -> "Chờ lấy hàng";
            case "DANG_GIAO" -> "Đang giao";
            case "DA_GIAO" -> "Đã giao";
            case "GIAO_THAT_BAI" -> "Giao thất bại";
            case "HOAN_HANG" -> "Hoàn hàng";
            default -> trangThaiGiao;
        };
    }

    private String formatGio(LocalDateTime time) {
        return time == null ? "-" : time.format(DINH_DANG_GIO);
    }

    // ==========================================
    // CÁC HÀM TIỆN ÍCH HIỂN THỊ
    // ==========================================
    private void inBangLinhKien(List<LinhKien> list) {
        if (list == null || list.isEmpty()) {
            System.out.println("Không có linh kiện nào.");
            return;
        }

        System.out.println("---------------------------------------------------------------------------------------------------------------------");
        System.out.printf("%-4s | %-18s | %-34s | %-18s | %-10s | %-6s | %-9s | %-9s\n",
            "ID", "Mã SKU", "Tên linh kiện", "Danh mục", "Giá bán", "Tồn", "ĐVT", "Trạng thái");
        System.out.println("---------------------------------------------------------------------------------------------------------------------");

        for (LinhKien lk : list) {
            String ten = rutGon(lk.getTenLinhKien(), 34);
            String dm = rutGon(lk.getTenDanhMuc() != null ? lk.getTenDanhMuc() : "Chưa phân loại", 18);
            String tt = "DANG_BAN".equalsIgnoreCase(lk.getTrangThaiBan()) ? "Đang bán" : "Ngừng bán";

            System.out.printf("%-4d | %-18s | %-34s | %-18s | %10s | %6d | %-9s | %-9s\n",
                lk.getMaLinhKien(),
                lk.getMaSKU(),
                ten,
                dm,
                formatTien(lk.getGiaBan()),
                lk.getSoLuongTon(),
                lk.getDonViTinh(),
                tt
            );
        }
        System.out.println("---------------------------------------------------------------------------------------------------------------------");
        System.out.println("Tổng số linh kiện: " + list.size());
    }

    // ==========================================
    // 19-25. KHO, MÃ GIẢM GIÁ VÀ THANH TOÁN
    // ==========================================
    private void dieuChinhTonKhoConsole() {
        System.out.println("\n--- ĐIỀU CHỈNH TỒN KHO (NHẬP / KIỂM KÊ) ---");
        try {
            System.out.print("Nhập mã linh kiện (ID): ");
            int id = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Số lượng thay đổi (+ nhập thêm, - giảm khi kiểm kê): ");
            int delta = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Lý do điều chỉnh: ");
            String lyDo = scanner.nextLine().trim();
            linhKienService.dieuChinhTonKho(id, delta, lyDo, WAREHOUSE_USER_ID);
            LinhKien lk = linhKienService.timTheoMa(id);
            System.out.printf("✅ Đã cập nhật tồn kho '%s': còn %d %s.\n",
                lk.getTenLinhKien(), lk.getSoLuongTon(), lk.getDonViTinh());
        } catch (NumberFormatException e) {
            System.out.println("❌ Mã linh kiện và số lượng phải là số nguyên!");
        } catch (Exception e) {
            System.out.println("❌ Điều chỉnh thất bại: " + e.getMessage());
        }
    }

    private void xemLichSuTonKhoConsole() {
        System.out.println("\n--- LỊCH SỬ BIẾN ĐỘNG TỒN KHO ---");
        System.out.print("Lọc theo mã linh kiện (Enter = xem tất cả): ");
        String raw = scanner.nextLine().trim();
        Integer maLk = null;
        if (!raw.isEmpty()) {
            try {
                maLk = Integer.parseInt(raw);
            } catch (NumberFormatException e) {
                System.out.println("❌ Mã linh kiện không hợp lệ!");
                return;
            }
        }
        List<LichSuTonKho> list = linhKienService.layLichSuTonKho(maLk);
        if (list.isEmpty()) {
            System.out.println("📭 Chưa có biến động tồn kho nào.");
            return;
        }
        System.out.println("----------------------------------------------------------------------------------------------------");
        System.out.printf("%-5s | %-12s | %-24s | %-10s | %-7s | %-5s -> %-5s | %-20s\n",
            "ID", "SKU", "Tên linh kiện", "Loại", "Đổi", "Trước", "Sau", "Lý do");
        System.out.println("----------------------------------------------------------------------------------------------------");
        for (LichSuTonKho item : list) {
            System.out.printf("%-5d | %-12s | %-24s | %-10s | %+7d | %5d -> %-5d | %-20s\n",
                item.getMaLichSu(), item.getMaSKU(), rutGon(item.getTenLinhKien(), 24),
                item.getLoaiBienDong(), item.getSoLuongThayDoi(), item.getTonTruoc(), item.getTonSau(),
                rutGon(item.getLyDo() != null ? item.getLyDo() : "-", 20));
        }
    }

    private void xemDanhSachMaGiamGia() {
        System.out.println("\n--- DANH SÁCH MÃ GIẢM GIÁ ---");
        List<MaGiamGia> list = maGiamGiaService.layTatCa();
        if (list.isEmpty()) {
            System.out.println("📭 Chưa có mã giảm giá nào.");
            return;
        }
        System.out.println("--------------------------------------------------------------------------------------------");
        System.out.printf("%-4s | %-12s | %-10s | %-12s | %-12s | %-10s | %-16s\n",
            "ID", "Mã Code", "Loại", "Giá trị", "Đơn tối thiểu", "Đã dùng", "Hạn dùng");
        System.out.println("--------------------------------------------------------------------------------------------");
        for (MaGiamGia mg : list) {
            String luot = mg.getSoLuotDaDung() + "/" + (mg.getSoLuotToiDa() != null ? mg.getSoLuotToiDa() : "∞");
            System.out.printf("%-4d | %-12s | %-10s | %12s | %12s | %-10s | %-16s\n",
                mg.getMaGiamGia(), mg.getMaCode(), mg.getLoaiGiam(),
                MaGiamGia.PHAN_TRAM.equals(mg.getLoaiGiam()) ? mg.getGiaTriGiam() + "%" : formatTien(mg.getGiaTriGiam()),
                formatTien(mg.getDonHangToiThieu()), luot, formatGio(mg.getNgayKetThuc()));
        }
    }

    private void themMaGiamGiaMoi() {
        System.out.println("\n--- TẠO MÃ GIẢM GIÁ MỚI ---");
        try {
            System.out.print("Mã code (VD: TECH15): ");
            String code = scanner.nextLine().trim().toUpperCase();
            System.out.print("Loại giảm (1. PHAN_TRAM  2. SO_TIEN) [1/2]: ");
            String loai = "1".equals(scanner.nextLine().trim()) ? MaGiamGia.PHAN_TRAM : MaGiamGia.SO_TIEN;
            System.out.print("Giá trị giảm: ");
            BigDecimal giaTri = new BigDecimal(scanner.nextLine().trim());
            System.out.print("Đơn tối thiểu (VNĐ, Enter = 0): ");
            String minStr = scanner.nextLine().trim();
            BigDecimal min = minStr.isEmpty() ? BigDecimal.ZERO : new BigDecimal(minStr);
            System.out.print("Số lượt tối đa (Enter = không giới hạn): ");
            String maxStr = scanner.nextLine().trim();
            Integer max = maxStr.isEmpty() ? null : Integer.parseInt(maxStr);
            System.out.print("Số ngày hiệu lực [mặc định 30]: ");
            String daysStr = scanner.nextLine().trim();
            int days = daysStr.isEmpty() ? 30 : Integer.parseInt(daysStr);

            MaGiamGia mg = new MaGiamGia(code, loai, giaTri, min, max, LocalDateTime.now(), LocalDateTime.now().plusDays(days));
            MaGiamGia saved = maGiamGiaService.themMaGiamGia(mg, STAFF_USER_ID);
            System.out.println("✅ Đã tạo mã giảm giá '" + saved.getMaCode() + "' (ID: " + saved.getMaGiamGia() + ").");
        } catch (Exception e) {
            System.out.println("❌ Tạo mã giảm giá thất bại: " + e.getMessage());
        }
    }

    private void xacNhanThanhToanConsole() {
        System.out.println("\n--- XÁC NHẬN THANH TOÁN ĐƠN HÀNG ---");
        try {
            System.out.print("Nhập mã đơn hàng: ");
            int maDon = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Mã tham chiếu / mã giao dịch ngân hàng (có thể bỏ trống): ");
            String ref = scanner.nextLine().trim();
            ThanhToan tt = thanhToanService.xacNhanThanhToan(maDon, ref, STAFF_USER_ID);
            System.out.printf("✅ Đã xác nhận thanh toán đơn #%d (%s VNĐ, Mã GD #%d).\n",
                maDon, formatTien(tt.getSoTien()), tt.getMaThanhToan());
        } catch (Exception e) {
            System.out.println("❌ Xác nhận thanh toán thất bại: " + e.getMessage());
        }
    }

    private void xacNhanHoanTienConsole() {
        System.out.println("\n--- XÁC NHẬN HOÀN TIỀN ĐƠN HÀNG ---");
        try {
            System.out.print("Nhập mã đơn hàng cần hoàn tiền: ");
            int maDon = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Mã tham chiếu hoàn tiền (có thể bỏ trống): ");
            String ref = scanner.nextLine().trim();
            ThanhToan tt = thanhToanService.xacNhanHoanTien(maDon, ref, STAFF_USER_ID);
            System.out.printf("✅ Đã xác nhận hoàn tiền đơn #%d (%s VNĐ, Mã GD #%d).\n",
                maDon, formatTien(tt.getSoTien()), tt.getMaThanhToan());
        } catch (Exception e) {
            System.out.println("❌ Xác nhận hoàn tiền thất bại: " + e.getMessage());
        }
    }

    private void xemLichSuGiaoDichThanhToan() {
        System.out.println("\n--- LỊCH SỬ GIAO DỊCH THANH TOÁN ---");
        List<ThanhToan> list = thanhToanService.layTatCaGiaoDich(STAFF_USER_ID);
        if (list.isEmpty()) {
            System.out.println("📭 Chưa có giao dịch nào.");
            return;
        }
        System.out.println("----------------------------------------------------------------------------------------------");
        System.out.printf("%-5s | %-7s | %-5s | %-12s | %-13s | %-12s | %-16s\n",
            "ID", "Đơn #", "Loại", "Phương thức", "Số tiền", "Trạng thái", "Thời điểm");
        System.out.println("----------------------------------------------------------------------------------------------");
        for (ThanhToan tt : list) {
            System.out.printf("%-5d | #%-6d | %-5s | %-12s | %13s | %-12s | %-16s\n",
                tt.getMaThanhToan(), tt.getMaDonHang(), tt.getLoaiGiaoDich(), tt.getPhuongThuc(),
                formatTien(tt.getSoTien()), tt.getTrangThai(), formatGio(tt.getThoiDiem()));
        }
    }

    private String formatTien(BigDecimal tien) {
        if (tien == null) return "0";
        return currencyFormat.format(tien.longValue());
    }

    private String rutGon(String text, int maxLen) {
        if (text == null) return "";
        if (text.length() <= maxLen) return text;
        return text.substring(0, maxLen - 3) + "...";
    }
}

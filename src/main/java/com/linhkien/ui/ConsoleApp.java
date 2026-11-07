package com.linhkien.ui;

import com.linhkien.config.DatabaseConnection;
import com.linhkien.dao.DanhMucDAO;
import com.linhkien.model.ChiTietGioHang;
import com.linhkien.model.DanhMucLinhKien;
import com.linhkien.model.GioHang;
import com.linhkien.model.LinhKien;
import com.linhkien.service.GioHangService;
import com.linhkien.service.LinhKienService;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

/**
 * Giao diện điều khiển Console tương tác trực tiếp người dùng.
 * Cho phép thực hiện đầy đủ các chức năng:
 *  - Quản lý linh kiện điện tử (Thêm/Sửa/Xóa/Tìm kiếm theo mã hoặc tên)
 *  - Quản lý giỏ hàng (Xem giỏ, Thêm vào giỏ, Cập nhật số lượng, Tính tổng tiền, Xóa)
 */
public class ConsoleApp {
    private final LinhKienService linhKienService;
    private final GioHangService gioHangService;
    private final DanhMucDAO danhMucDAO;
    private final Scanner scanner;
    private final NumberFormat currencyFormat;

    // Mặc định sử dụng tài khoản khách hàng mẫu ID = 1 (Nguyễn Văn An)
    private static final int DEFAULT_USER_ID = 1;

    public ConsoleApp() {
        this.linhKienService = new LinhKienService();
        this.gioHangService = new GioHangService();
        this.danhMucDAO = new DanhMucDAO();
        this.scanner = new Scanner(System.in);
        this.currencyFormat = NumberFormat.getInstance(Locale.of("vi", "VN"));
    }

    public void run() {
        boolean running = true;
        while (running) {
            printMainMenu();
            System.out.print(">> Vui lòng chọn chức năng [0-11]: ");
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
                case "0" -> {
                    System.out.println("\n[THOÁT] Cảm ơn bạn đã sử dụng hệ thống Quản lý linh kiện điện tử!");
                    DatabaseConnection.closeConnection();
                    running = false;
                }
                default -> System.out.println("❌ Lựa chọn không hợp lệ, vui lòng chọn từ 0 đến 11.\n");
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

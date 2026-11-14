package com.linhkien.service;

import com.linhkien.dao.DonHangDAO;
import com.linhkien.dao.GioHangDAO;
import com.linhkien.dao.LinhKienDAO;
import com.linhkien.dao.MaGiamGiaDAO;
import com.linhkien.dao.NguoiDungDAO;
import com.linhkien.dao.ThanhToanDAO;
import com.linhkien.model.ChiTietDonHang;
import com.linhkien.model.ChiTietGioHang;
import com.linhkien.model.DonHang;
import com.linhkien.model.GioHang;
import com.linhkien.model.LichSuTrangThaiDon;
import com.linhkien.model.LinhKien;
import com.linhkien.model.MaGiamGia;
import com.linhkien.model.NguoiDung;
import com.linhkien.model.ThanhToan;
import com.linhkien.model.VanChuyen;
import com.linhkien.util.JdbcHelper;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Lớp dịch vụ xử lý nghiệp vụ Đặt hàng, Quản lý đơn hàng và Cập nhật trạng thái vận chuyển
 * (UC08 Đặt hàng, UC10 Xem lịch sử đơn, UC14 Xử lý đơn hàng, UC19 Hủy đơn hàng).
 *
 * Mọi thao tác thay đổi nhiều bảng (tạo đơn, hủy đơn, giao thất bại) đều chạy trong một Transaction:
 * hoặc thành công toàn bộ, hoặc hoàn tác toàn bộ.
 */
public class DonHangService {
    private static final Set<String> PHUONG_THUC_HOP_LE = Set.of("COD", "CHUYEN_KHOAN");
    private static final Set<String> TRANG_THAI_DON_HOP_LE = Set.of(
        DonHang.CHO_XAC_NHAN, DonHang.DANG_DONG_GOI, DonHang.DANG_GIAO,
        DonHang.DA_GIAO, DonHang.GIAO_THAT_BAI, DonHang.DA_HUY);
    private static final int SO_NGAY_GIAO_DU_KIEN = 3;

    private final DonHangDAO donHangDAO;
    private final GioHangDAO gioHangDAO;
    private final LinhKienDAO linhKienDAO;
    private final NguoiDungDAO nguoiDungDAO;
    private final GioHangService gioHangService;
    private final MaGiamGiaDAO maGiamGiaDAO;
    private final ThanhToanDAO thanhToanDAO;
    private final MaGiamGiaService maGiamGiaService;

    public DonHangService() {
        this.donHangDAO = new DonHangDAO();
        this.gioHangDAO = new GioHangDAO();
        this.linhKienDAO = new LinhKienDAO();
        this.nguoiDungDAO = new NguoiDungDAO();
        this.gioHangService = new GioHangService(gioHangDAO, linhKienDAO);
        this.maGiamGiaDAO = new MaGiamGiaDAO();
        this.thanhToanDAO = new ThanhToanDAO();
        this.maGiamGiaService = new MaGiamGiaService(maGiamGiaDAO, nguoiDungDAO);
    }

    public DonHangService(DonHangDAO donHangDAO, GioHangDAO gioHangDAO, LinhKienDAO linhKienDAO,
                          NguoiDungDAO nguoiDungDAO, MaGiamGiaDAO maGiamGiaDAO, ThanhToanDAO thanhToanDAO) {
        this.donHangDAO = donHangDAO;
        this.gioHangDAO = gioHangDAO;
        this.linhKienDAO = linhKienDAO;
        this.nguoiDungDAO = nguoiDungDAO;
        this.gioHangService = new GioHangService(gioHangDAO, linhKienDAO);
        this.maGiamGiaDAO = maGiamGiaDAO;
        this.thanhToanDAO = thanhToanDAO;
        this.maGiamGiaService = new MaGiamGiaService(maGiamGiaDAO, nguoiDungDAO);
    }

    // ==========================================
    // ĐẶT HÀNG (UC08)
    // ==========================================

    /**
     * Tạo đơn hàng từ giỏ hàng của khách và trừ tồn kho các linh kiện (CV07.1).
     * Các bước chạy trong một Transaction:
     * 1. Kiểm tra từng dòng: linh kiện còn đang bán, lấy đơn giá hiện tại để chốt vào đơn.
     * 2. Áp dụng mã giảm giá (nếu có) và tính tổng thanh toán.
     * 3. Ghi đơn hàng ở trạng thái CHO_XAC_NHAN và bản ghi ThanhToan chờ xác nhận.
     * 4. Với từng dòng: ghi chi tiết đơn, trừ tồn kho nguyên tử, ghi nhật ký tồn kho.
     *    Nếu bất kỳ dòng nào không đủ tồn thì hoàn tác toàn bộ.
     * 5. Ghi nhật ký trạng thái ban đầu và làm trống giỏ hàng.
     *
     * @param maYeuCau Khóa chống trùng đơn (NFR05). Gửi lại cùng mã sẽ trả về đơn đã tạo thay vì tạo thêm đơn mới.
     * @return Đơn hàng đã tạo, kèm danh sách chi tiết
     */
    public DonHang taoDon(int maNguoiDung, String tenNguoiNhan, String soDienThoaiNhan, String diaChiNhan,
                          String phuongThucThanhToan, String maYeuCau, String ghiChu) {
        return taoDon(maNguoiDung, tenNguoiNhan, soDienThoaiNhan, diaChiNhan, phuongThucThanhToan, maYeuCau, ghiChu, "legacy", null);
    }

    public DonHang taoDon(int maNguoiDung, String tenNguoiNhan, String soDienThoaiNhan, String diaChiNhan,
                          String phuongThucThanhToan, String maYeuCau, String ghiChu, String shipping) {
        return taoDon(maNguoiDung, tenNguoiNhan, soDienThoaiNhan, diaChiNhan, phuongThucThanhToan, maYeuCau, ghiChu, shipping, null);
    }

    public DonHang taoDon(int maNguoiDung, String tenNguoiNhan, String soDienThoaiNhan, String diaChiNhan,
                          String phuongThucThanhToan, String maYeuCau, String ghiChu,
                          String shipping, String maCodeGiamGia) {
        String kieuVanChuyen = (shipping == null || shipping.isBlank()) ? "standard" : shipping.trim().toLowerCase();
        if (!Set.of("legacy", "standard", "express").contains(kieuVanChuyen)) {
            throw new IllegalArgumentException("Vận chuyển không hợp lệ");
        }
        String ten = tenNguoiNhan == null ? "" : tenNguoiNhan.trim();
        String sdt = soDienThoaiNhan == null ? "" : soDienThoaiNhan.replaceAll("[\\s.-]", "").trim();
        String diaChi = diaChiNhan == null ? "" : diaChiNhan.trim();
        String phuongThuc = phuongThucThanhToan == null ? "" : phuongThucThanhToan.trim().toUpperCase();
        String yeuCau = maYeuCau == null ? "" : maYeuCau.trim();
        String maCode = (maCodeGiamGia == null || maCodeGiamGia.isBlank()) ? null : maCodeGiamGia.trim().toUpperCase();

        if (ten.isEmpty()) {
            throw new IllegalArgumentException("Tên người nhận không được để trống");
        }
        if (!sdt.matches("^0\\d{9,10}$")) {
            throw new IllegalArgumentException("Số điện thoại người nhận không hợp lệ (bắt đầu bằng 0, gồm 10 hoặc 11 chữ số)");
        }
        if (diaChi.isEmpty()) {
            throw new IllegalArgumentException("Địa chỉ nhận hàng không được để trống");
        }
        if (!PHUONG_THUC_HOP_LE.contains(phuongThuc)) {
            throw new IllegalArgumentException("Phương thức thanh toán phải là COD hoặc CHUYEN_KHOAN");
        }
        if (yeuCau.isEmpty() || yeuCau.length() > 64) {
            throw new IllegalArgumentException("Mã yêu cầu (chống trùng đơn) phải có từ 1 đến 64 ký tự");
        }

        NguoiDung khach = nguoiDungDAO.findById(maNguoiDung);
        if (khach == null) {
            throw new IllegalArgumentException("Không tìm thấy người dùng có mã: " + maNguoiDung);
        }
        if (!"HOAT_DONG".equalsIgnoreCase(khach.getTrangThai())) {
            throw new IllegalStateException("Tài khoản đang bị khóa, không thể đặt hàng");
        }

        // Chống trùng đơn: cùng mã yêu cầu thì trả về đơn đã tạo
        DonHang daCo = JdbcHelper.chay(() -> {
            DonHang dh = donHangDAO.findByMaYeuCau(yeuCau);
            return dh != null ? donHangDAO.findById(dh.getMaDonHang()) : null;
        });
        if (daCo != null) {
            if (daCo.getMaNguoiDung() != maNguoiDung) {
                throw new IllegalStateException("Mã yêu cầu đã được sử dụng cho một đơn hàng khác");
            }
            return daCo;
        }

        GioHang gioHang = gioHangService.layGioHang(maNguoiDung);
        if (gioHang.getDanhSachChiTiet().isEmpty()) {
            throw new IllegalStateException("Giỏ hàng đang trống, không thể đặt hàng");
        }

        List<ChiTietGioHang> dongGio = new ArrayList<>(gioHang.getDanhSachChiTiet());
        dongGio.sort(Comparator.comparingInt(ChiTietGioHang::getMaLinhKien));

        int maDon = JdbcHelper.giaoDich(() -> {
            DonHang dh = new DonHang();
            dh.setMaNguoiDung(maNguoiDung);
            dh.setMaYeuCau(yeuCau);
            dh.setTenNguoiNhan(ten);
            dh.setSoDienThoaiNhan(sdt);
            dh.setDiaChiNhan(diaChi);
            dh.setPhuongThucThanhToan(phuongThuc);
            dh.setGhiChu(ghiChu == null || ghiChu.isBlank() ? null : ghiChu.trim());

            // 1. Chốt đơn giá và kiểm tra trạng thái bán của từng dòng
            List<ChiTietDonHang> dongDon = new ArrayList<>();
            for (ChiTietGioHang dong : dongGio) {
                LinhKien lk = linhKienDAO.findById(dong.getMaLinhKien());
                if (lk == null) {
                    throw new IllegalStateException("Linh kiện mã " + dong.getMaLinhKien() + " không còn tồn tại");
                }
                if (!"DANG_BAN".equalsIgnoreCase(lk.getTrangThaiBan())) {
                    throw new IllegalStateException(
                        String.format("Linh kiện '%s' đã ngừng bán, vui lòng xóa khỏi giỏ hàng trước khi đặt", lk.getTenLinhKien()));
                }
                dongDon.add(new ChiTietDonHang(lk.getMaLinhKien(), dong.getSoLuong(), lk.getGiaBan()));
            }
            dh.setDanhSachChiTiet(dongDon);
            dh.setTongTienHang(dh.tinhTongTienHang());
            dh.setPhiVanChuyen(BigDecimal.valueOf(
                kieuVanChuyen.equals("express") ? 49000 : kieuVanChuyen.equals("standard") ? 25000 : 0));

            if (maCode != null) {
                MaGiamGia mg = maGiamGiaService.kiemTraMaGiamGia(maCode, dh.getTongTienHang());
                if (!maGiamGiaDAO.tangSoLuotDung(mg.getMaGiamGia())) {
                    throw new IllegalStateException("Mã giảm giá '" + mg.getMaCode() + "' đã hết lượt sử dụng");
                }
                dh.setMaGiamGia(mg.getMaGiamGia());
                dh.setMaCodeGiamGia(mg.getMaCode());
                dh.setSoTienGiam(mg.tinhSoTienGiam(dh.getTongTienHang()));
            }
            dh.setTongThanhToan(dh.tinhTongThanhToan());

            // 2. Ghi đơn hàng & giao dịch thanh toán chờ xác nhận
            donHangDAO.insert(dh);
            if (dh.getTongThanhToan().compareTo(BigDecimal.ZERO) > 0) {
                ThanhToan tt = new ThanhToan(dh.getMaDonHang(), ThanhToan.LOAI_THU, phuongThuc, dh.getTongThanhToan());
                thanhToanDAO.insert(tt);
            }

            // 3. Ghi chi tiết đơn và trừ tồn kho nguyên tử
            for (ChiTietDonHang ct : dongDon) {
                ct.setMaDonHang(dh.getMaDonHang());
                donHangDAO.insertChiTiet(ct);

                int tonTruoc = donHangDAO.layTonKho(ct.getMaLinhKien());
                if (!donHangDAO.truTonNguyenTu(ct.getMaLinhKien(), ct.getSoLuong())) {
                    LinhKien lk = linhKienDAO.findById(ct.getMaLinhKien());
                    String tenLk = lk != null ? lk.getTenLinhKien() : "mã " + ct.getMaLinhKien();
                    throw new IllegalStateException(
                        String.format("Không đủ tồn kho cho linh kiện '%s': còn %d, bạn đặt %d. Đơn hàng chưa được tạo",
                            tenLk, Math.max(tonTruoc, 0), ct.getSoLuong()));
                }
                donHangDAO.ghiLichSuTonKho(ct.getMaLinhKien(), "BAN", -ct.getSoLuong(), tonTruoc,
                    tonTruoc - ct.getSoLuong(), dh.getMaDonHang(), maNguoiDung, "Bán hàng - đơn #" + dh.getMaDonHang());
            }

            // 4. Nhật ký trạng thái ban đầu và làm trống giỏ
            donHangDAO.ghiLichSuTrangThai(dh.getMaDonHang(), null, DonHang.CHO_XAC_NHAN, maNguoiDung, "Khách hàng đặt hàng");
            if (!gioHangDAO.xoaToanBoGio(gioHang.getMaGioHang())) {
                throw new IllegalStateException("Không thể làm trống giỏ hàng sau khi đặt");
            }
            return dh.getMaDonHang();
        });

        return JdbcHelper.chay(() -> donHangDAO.findById(maDon));
    }

    // ==========================================
    // TRA CỨU ĐƠN HÀNG (UC10, UC14)
    // ==========================================

    /**
     * Lịch sử đơn hàng của một khách hàng, đơn mới nhất lên đầu (UC10).
     */
    public List<DonHang> layDonCuaKhach(int maNguoiDung) {
        return JdbcHelper.chay(() -> donHangDAO.findByNguoiDung(maNguoiDung));
    }

    /**
     * Danh sách toàn bộ đơn hàng của cửa hàng cho nhân viên xử lý (UC14).
     * @param trangThai Trạng thái cần lọc; null hoặc rỗng để xem tất cả
     */
    public List<DonHang> layTatCaDon(int maNguoiThucHien, String trangThai) {
        yeuCauNhanVien(maNguoiThucHien);
        String loc = trangThai == null ? "" : trangThai.trim().toUpperCase();
        if (!loc.isEmpty() && !TRANG_THAI_DON_HOP_LE.contains(loc)) {
            throw new IllegalArgumentException("Trạng thái đơn không hợp lệ: " + trangThai);
        }
        return JdbcHelper.chay(() -> donHangDAO.findAll(loc));
    }

    /**
     * Xem chi tiết một đơn hàng. Khách chỉ xem được đơn của mình, nhân viên xem được mọi đơn.
     */
    public DonHang layChiTietDon(int maDonHang, int maNguoiXem) {
        return timDonCuaKhachHoacNhanVien(maDonHang, maNguoiXem);
    }

    public DonHang timDonCuaKhachHoacNhanVien(int maDonHang, int maNguoiXem) {
        DonHang dh = JdbcHelper.chay(() -> donHangDAO.findById(maDonHang));
        if (dh == null) {
            throw new IllegalArgumentException("Không tìm thấy đơn hàng mã: " + maDonHang);
        }
        NguoiDung nguoi = nguoiDungDAO.findById(maNguoiXem);
        if (nguoi == null) {
            throw new IllegalArgumentException("Không tìm thấy người dùng có mã: " + maNguoiXem);
        }
        if (dh.getMaNguoiDung() != maNguoiXem && !nguoi.kiemTraQuyen("BAN_HANG")) {
            throw new IllegalStateException("Bạn không có quyền xem đơn hàng của người khác");
        }
        return dh;
    }

    public List<LichSuTrangThaiDon> layLichSuTrangThai(int maDonHang) {
        return JdbcHelper.chay(() -> donHangDAO.findLichSuTrangThai(maDonHang));
    }

    public VanChuyen layVanChuyen(int maDonHang) {
        return JdbcHelper.chay(() -> donHangDAO.findVanChuyenByDon(maDonHang));
    }

    // ==========================================
    // CẬP NHẬT TRẠNG THÁI ĐƠN VÀ VẬN CHUYỂN (UC14, UC19)
    // ==========================================

    /**
     * Cập nhật trạng thái đơn hàng theo đúng luồng nghiệp vụ (chỉ nhân viên bán hàng hoặc quản trị):
     *   CHO_XAC_NHAN -> DANG_DONG_GOI           (xác nhận đơn)
     *   DANG_GIAO    -> DA_GIAO                 (giao thành công, COD được ghi nhận đã thu tiền)
     *   DANG_GIAO    -> GIAO_THAT_BAI           (hoàn lại tồn kho)
     *   CHO_XAC_NHAN / DANG_DONG_GOI -> DA_HUY  (hủy đơn, hoàn lại tồn kho)
     * Chuyển sang DANG_GIAO cần thông tin vận chuyển nên dùng {@link #banGiaoVanChuyen}.
     */
    public void capNhatTrangThai(int maDonHang, String trangThaiMoi, int maNguoiThucHien, String ghiChu) {
        yeuCauNhanVien(maNguoiThucHien);
        String moi = trangThaiMoi == null ? "" : trangThaiMoi.trim().toUpperCase();
        if (!TRANG_THAI_DON_HOP_LE.contains(moi)) {
            throw new IllegalArgumentException("Trạng thái đơn không hợp lệ: " + trangThaiMoi);
        }
        if (DonHang.DA_HUY.equals(moi)) {
            huyDon(maDonHang, maNguoiThucHien, ghiChu);
            return;
        }
        if (DonHang.DANG_GIAO.equals(moi)) {
            throw new IllegalArgumentException("Chuyển sang 'Đang giao' cần nhập đơn vị vận chuyển và mã vận đơn (bàn giao vận chuyển)");
        }

        JdbcHelper.giaoDich(() -> {
            DonHang dh = layDonHoacBaoLoi(maDonHang);
            kiemTraChuyenTrangThai(dh, moi);
            String cu = dh.getTrangThaiDon();
            String nhatKy = ghiChu == null || ghiChu.isBlank() ? null : ghiChu.trim();

            switch (moi) {
                case DonHang.DANG_DONG_GOI -> {
                    donHangDAO.capNhatTrangThaiDon(maDonHang, moi);
                    donHangDAO.ghiLichSuTrangThai(maDonHang, cu, moi, maNguoiThucHien,
                        nhatKy != null ? nhatKy : "Nhân viên xác nhận đơn, bắt đầu đóng gói");
                }
                case DonHang.DA_GIAO -> {
                    boolean laCod = "COD".equals(dh.getPhuongThucThanhToan());
                    donHangDAO.capNhatTrangThaiDon(maDonHang, moi);
                    donHangDAO.capNhatKetQuaGiao(maDonHang, "DA_GIAO", LocalDateTime.now(), laCod, nhatKy);
                    if (laCod) {
                        donHangDAO.capNhatTrangThaiThanhToan(maDonHang, "DA_THANH_TOAN");
                        capNhatGiaoDichThuThanhCong(dh, maNguoiThucHien);
                    }
                    donHangDAO.ghiLichSuTrangThai(maDonHang, cu, moi, maNguoiThucHien,
                        nhatKy != null ? nhatKy : (laCod ? "Giao thành công, đã thu tiền COD" : "Giao thành công"));
                }
                case DonHang.GIAO_THAT_BAI -> {
                    String lyDo = nhatKy != null ? nhatKy : "Giao hàng thất bại";
                    hoanLaiTonVaMaGiamGia(dh, maNguoiThucHien, lyDo);
                    donHangDAO.capNhatTrangThaiDon(maDonHang, moi);
                    donHangDAO.capNhatKetQuaGiao(maDonHang, "GIAO_THAT_BAI", null, false, lyDo);
                    xuLyThanhToanKhiHuyHoacThatBai(dh, maNguoiThucHien);
                    donHangDAO.ghiLichSuTrangThai(maDonHang, cu, moi, maNguoiThucHien, lyDo);
                }
                default -> throw new IllegalArgumentException("Không hỗ trợ chuyển sang trạng thái: " + moi);
            }
            return null;
        });
    }

    /**
     * Bàn giao đơn cho đơn vị vận chuyển: tạo bản ghi vận chuyển và chuyển đơn từ
     * DANG_DONG_GOI sang DANG_GIAO (UC14).
     */
    public VanChuyen banGiaoVanChuyen(int maDonHang, String donViVanChuyen, String maVanDon, int maNguoiThucHien) {
        yeuCauNhanVien(maNguoiThucHien);
        String donVi = donViVanChuyen == null ? "" : donViVanChuyen.trim();
        String vanDon = maVanDon == null ? "" : maVanDon.trim();
        if (donVi.isEmpty()) {
            throw new IllegalArgumentException("Đơn vị vận chuyển không được để trống");
        }
        if (vanDon.isEmpty()) {
            throw new IllegalArgumentException("Mã vận đơn không được để trống");
        }

        JdbcHelper.giaoDich(() -> {
            DonHang dh = layDonHoacBaoLoi(maDonHang);
            kiemTraChuyenTrangThai(dh, DonHang.DANG_GIAO);
            if (donHangDAO.tonTaiMaVanDon(vanDon)) {
                throw new IllegalStateException("Mã vận đơn '" + vanDon + "' đã được sử dụng cho đơn khác");
            }

            VanChuyen vc = new VanChuyen(maDonHang, donVi, vanDon);
            vc.setTrangThaiGiao("DANG_GIAO");
            vc.setNgayBanGiao(LocalDateTime.now());
            vc.setNgayGiaoDuKien(LocalDate.now().plusDays(SO_NGAY_GIAO_DU_KIEN));
            donHangDAO.insertVanChuyen(vc);

            donHangDAO.capNhatTrangThaiDon(maDonHang, DonHang.DANG_GIAO);
            donHangDAO.ghiLichSuTrangThai(maDonHang, dh.getTrangThaiDon(), DonHang.DANG_GIAO, maNguoiThucHien,
                "Bàn giao cho " + donVi + ", mã vận đơn " + vanDon);
            return null;
        });

        return JdbcHelper.chay(() -> donHangDAO.findVanChuyenByDon(maDonHang));
    }

    /**
     * Hủy đơn hàng (UC19, CV07.2). Chỉ hủy được khi đơn đang CHO_XAC_NHAN hoặc DANG_DONG_GOI.
     * Khách hàng chỉ hủy được đơn của mình; nhân viên bán hàng và quản trị hủy được mọi đơn.
     * Tồn kho được cộng lại đúng một lần nhờ cờ DaHoanKho.
     */
    public void huyDon(int maDonHang, int maNguoiThucHien, String lyDo) {
        NguoiDung nguoi = nguoiDungDAO.findById(maNguoiThucHien);
        if (nguoi == null) {
            throw new IllegalArgumentException("Không tìm thấy người dùng có mã: " + maNguoiThucHien);
        }
        boolean laNhanVien = nguoi.kiemTraQuyen("BAN_HANG");

        JdbcHelper.giaoDich(() -> {
            DonHang dh = layDonHoacBaoLoi(maDonHang);
            if (!laNhanVien && dh.getMaNguoiDung() != maNguoiThucHien) {
                throw new IllegalStateException("Bạn không có quyền hủy đơn hàng của người khác");
            }
            if (!dh.coTheHuy()) {
                throw new IllegalStateException(
                    String.format("Không thể hủy đơn #%d vì đang ở trạng thái '%s' (chỉ hủy được khi 'Chờ xác nhận' hoặc 'Đang đóng gói')",
                        maDonHang, DonHang.nhanTrangThai(dh.getTrangThaiDon())));
            }

            String ly = lyDo == null || lyDo.isBlank()
                ? (laNhanVien ? "Nhân viên hủy đơn" : "Khách hàng hủy đơn")
                : lyDo.trim();
            hoanLaiTonVaMaGiamGia(dh, maNguoiThucHien, "Hủy đơn #" + maDonHang + ": " + ly);
            donHangDAO.capNhatTrangThaiDon(maDonHang, DonHang.DA_HUY);
            xuLyThanhToanKhiHuyHoacThatBai(dh, maNguoiThucHien);
            donHangDAO.ghiLichSuTrangThai(maDonHang, dh.getTrangThaiDon(), DonHang.DA_HUY, maNguoiThucHien, ly);
            return null;
        });
    }

    // ==========================================
    // HÀM NỘI BỘ
    // ==========================================

    private void hoanLaiTonVaMaGiamGia(DonHang dh, int maNguoiThucHien, String lyDo) throws SQLException {
        boolean chuaHoan = !dh.isDaHoanKho();
        hoanKho(dh, maNguoiThucHien, lyDo);
        if (chuaHoan && dh.getMaGiamGia() != null) {
            maGiamGiaDAO.giamSoLuotDung(dh.getMaGiamGia());
        }
    }

    private void capNhatGiaoDichThuThanhCong(DonHang dh, int maNguoiThucHien) throws SQLException {
        boolean daCapNhat = false;
        for (ThanhToan tt : thanhToanDAO.findByDonHang(dh.getMaDonHang())) {
            if (ThanhToan.LOAI_THU.equals(tt.getLoaiGiaoDich())
                    && ThanhToan.TRANG_THAI_CHO.equals(tt.getTrangThai())) {
                thanhToanDAO.capNhatTrangThai(tt.getMaThanhToan(), ThanhToan.TRANG_THAI_THANH_CONG,
                    maNguoiThucHien, "COD-" + dh.getMaDonHang());
                daCapNhat = true;
            }
        }
        if (!daCapNhat && dh.getTongThanhToan() != null && dh.getTongThanhToan().compareTo(BigDecimal.ZERO) > 0) {
            ThanhToan tt = new ThanhToan(dh.getMaDonHang(), ThanhToan.LOAI_THU,
                dh.getPhuongThucThanhToan(), dh.getTongThanhToan());
            tt.setMaThamChieu("COD-" + dh.getMaDonHang());
            tt.xacNhan(maNguoiThucHien);
            thanhToanDAO.insert(tt);
        }
    }

    private void xuLyThanhToanKhiHuyHoacThatBai(DonHang dh, int maNguoiThucHien) throws SQLException {
        if ("DA_THANH_TOAN".equals(dh.getTrangThaiThanhToan())) {
            donHangDAO.capNhatTrangThaiThanhToan(dh.getMaDonHang(), "DANG_HOAN_TIEN");
            return;
        }
        for (ThanhToan tt : thanhToanDAO.findByDonHang(dh.getMaDonHang())) {
            if (ThanhToan.LOAI_THU.equals(tt.getLoaiGiaoDich())
                    && ThanhToan.TRANG_THAI_CHO.equals(tt.getTrangThai())) {
                thanhToanDAO.capNhatTrangThai(tt.getMaThanhToan(), ThanhToan.TRANG_THAI_THAT_BAI,
                    maNguoiThucHien, tt.getMaThamChieu());
            }
        }
    }

    /**
     * Cộng lại tồn kho cho các dòng của đơn, chỉ thực hiện một lần nhờ cờ DaHoanKho.
     */
    private void hoanKho(DonHang dh, int maNguoiThucHien, String lyDo) throws SQLException {
        if (dh.isDaHoanKho()) {
            return;
        }
        for (ChiTietDonHang ct : dh.getDanhSachChiTiet()) {
            int tonTruoc = donHangDAO.layTonKho(ct.getMaLinhKien());
            donHangDAO.congTon(ct.getMaLinhKien(), ct.getSoLuong());
            donHangDAO.ghiLichSuTonKho(ct.getMaLinhKien(), "HOAN_HUY", ct.getSoLuong(), tonTruoc,
                tonTruoc + ct.getSoLuong(), dh.getMaDonHang(), maNguoiThucHien, lyDo);
        }
        donHangDAO.danhDauDaHoanKho(dh.getMaDonHang());
        dh.setDaHoanKho(true);
    }

    private DonHang layDonHoacBaoLoi(int maDonHang) throws SQLException {
        DonHang dh = donHangDAO.findById(maDonHang);
        if (dh == null) {
            throw new IllegalArgumentException("Không tìm thấy đơn hàng mã: " + maDonHang);
        }
        return dh;
    }

    private void kiemTraChuyenTrangThai(DonHang dh, String trangThaiMoi) {
        if (!dh.coTheChuyenSang(trangThaiMoi)) {
            throw new IllegalStateException(
                String.format("Không thể chuyển đơn #%d từ '%s' sang '%s'",
                    dh.getMaDonHang(), DonHang.nhanTrangThai(dh.getTrangThaiDon()), DonHang.nhanTrangThai(trangThaiMoi)));
        }
    }

    private void yeuCauNhanVien(int maNguoiThucHien) {
        NguoiDung nguoi = nguoiDungDAO.findById(maNguoiThucHien);
        if (nguoi == null) {
            throw new IllegalArgumentException("Không tìm thấy người dùng có mã: " + maNguoiThucHien);
        }
        if (!nguoi.kiemTraQuyen("BAN_HANG")) {
            throw new IllegalStateException("Chỉ nhân viên bán hàng hoặc quản trị viên mới được thực hiện chức năng này");
        }
    }

}

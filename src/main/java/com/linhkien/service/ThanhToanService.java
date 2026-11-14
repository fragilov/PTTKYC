package com.linhkien.service;

import com.linhkien.dao.DonHangDAO;
import com.linhkien.dao.NguoiDungDAO;
import com.linhkien.dao.ThanhToanDAO;
import com.linhkien.model.DonHang;
import com.linhkien.model.NguoiDung;
import com.linhkien.model.ThanhToan;
import com.linhkien.util.JdbcHelper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Lớp dịch vụ quản lý xác nhận thanh toán và hoàn tiền (UC20).
 */
public class ThanhToanService {
    private final ThanhToanDAO thanhToanDAO;
    private final DonHangDAO donHangDAO;
    private final NguoiDungDAO nguoiDungDAO;

    public ThanhToanService() {
        this.thanhToanDAO = new ThanhToanDAO();
        this.donHangDAO = new DonHangDAO();
        this.nguoiDungDAO = new NguoiDungDAO();
    }

    public ThanhToanService(ThanhToanDAO thanhToanDAO, DonHangDAO donHangDAO, NguoiDungDAO nguoiDungDAO) {
        this.thanhToanDAO = thanhToanDAO;
        this.donHangDAO = donHangDAO;
        this.nguoiDungDAO = nguoiDungDAO;
    }

    public List<ThanhToan> layGiaoDichTheoDon(int maDonHang) {
        return JdbcHelper.chay(() -> thanhToanDAO.findByDonHang(maDonHang));
    }

    public List<ThanhToan> layTatCaGiaoDich(int maNguoiThucHien) {
        yeuCauNhanVien(maNguoiThucHien);
        return JdbcHelper.chay(() -> thanhToanDAO.findAll());
    }

    /**
     * Xác nhận thu tiền cho đơn hàng (chủ yếu với CHUYEN_KHOAN hoặc thu tiền thủ công).
     */
    public ThanhToan xacNhanThanhToan(int maDonHang, String maThamChieu, int maNguoiXacNhan) {
        yeuCauNhanVien(maNguoiXacNhan);
        String ref = maThamChieu == null || maThamChieu.isBlank() ? null : maThamChieu.trim();

        return JdbcHelper.giaoDich(() -> {
            DonHang dh = donHangDAO.findById(maDonHang);
            if (dh == null) {
                throw new IllegalArgumentException("Không tìm thấy đơn hàng mã: " + maDonHang);
            }
            if (DonHang.DA_HUY.equals(dh.getTrangThaiDon()) || DonHang.GIAO_THAT_BAI.equals(dh.getTrangThaiDon())) {
                throw new IllegalStateException("Không thể xác nhận thanh toán cho đơn đã hủy hoặc giao thất bại");
            }
            if ("DA_THANH_TOAN".equals(dh.getTrangThaiThanhToan())) {
                throw new IllegalStateException("Đơn hàng #" + maDonHang + " đã được thanh toán trước đó");
            }

            ThanhToan pending = timGiaoDichThuChoXacNhan(maDonHang);
            int maGiaoDich;
            if (pending != null) {
                thanhToanDAO.capNhatTrangThai(pending.getMaThanhToan(), ThanhToan.TRANG_THAI_THANH_CONG, maNguoiXacNhan, ref);
                maGiaoDich = pending.getMaThanhToan();
            } else {
                ThanhToan moi = new ThanhToan(maDonHang, ThanhToan.LOAI_THU, dh.getPhuongThucThanhToan(), dh.getTongThanhToan());
                moi.setMaThamChieu(ref);
                moi.xacNhan(maNguoiXacNhan);
                maGiaoDich = thanhToanDAO.insert(moi);
            }

            donHangDAO.capNhatTrangThaiThanhToan(maDonHang, "DA_THANH_TOAN");
            donHangDAO.ghiLichSuTrangThai(maDonHang, dh.getTrangThaiDon(), dh.getTrangThaiDon(), maNguoiXacNhan,
                ref != null ? "Xác nhận đã thanh toán (Mã tham chiếu: " + ref + ")" : "Xác nhận đã thanh toán");
            return thanhToanDAO.findById(maGiaoDich);
        });
    }

    /**
     * Xác nhận hoàn tiền cho đơn đã hủy hoặc giao thất bại khi khách đã thanh toán.
     */
    public ThanhToan xacNhanHoanTien(int maDonHang, String maThamChieu, int maNguoiXacNhan) {
        yeuCauNhanVien(maNguoiXacNhan);
        String ref = maThamChieu == null || maThamChieu.isBlank() ? null : maThamChieu.trim();

        return JdbcHelper.giaoDich(() -> {
            DonHang dh = donHangDAO.findById(maDonHang);
            if (dh == null) {
                throw new IllegalArgumentException("Không tìm thấy đơn hàng mã: " + maDonHang);
            }
            if (!Set.of(DonHang.DA_HUY, DonHang.GIAO_THAT_BAI).contains(dh.getTrangThaiDon())) {
                throw new IllegalStateException("Chỉ hoàn tiền cho đơn đã hủy hoặc giao thất bại");
            }
            if (!Set.of("DANG_HOAN_TIEN", "DA_THANH_TOAN").contains(dh.getTrangThaiThanhToan())) {
                throw new IllegalStateException("Đơn hàng #" + maDonHang + " không ở trạng thái cần hoàn tiền");
            }

            ThanhToan hoan = new ThanhToan(maDonHang, ThanhToan.LOAI_HOAN, dh.getPhuongThucThanhToan(), dh.getTongThanhToan());
            hoan.setMaThamChieu(ref);
            hoan.setThoiDiem(LocalDateTime.now());
            hoan.xacNhan(maNguoiXacNhan);
            int maGiaoDich = thanhToanDAO.insert(hoan);

            donHangDAO.capNhatTrangThaiThanhToan(maDonHang, "DA_HOAN_TIEN");
            donHangDAO.ghiLichSuTrangThai(maDonHang, dh.getTrangThaiDon(), dh.getTrangThaiDon(), maNguoiXacNhan,
                ref != null ? "Đã hoàn tiền cho khách (Mã tham chiếu: " + ref + ")" : "Đã hoàn tiền cho khách");
            return thanhToanDAO.findById(maGiaoDich);
        });
    }

    private ThanhToan timGiaoDichThuChoXacNhan(int maDonHang) throws java.sql.SQLException {
        for (ThanhToan tt : thanhToanDAO.findByDonHang(maDonHang)) {
            if (ThanhToan.LOAI_THU.equals(tt.getLoaiGiaoDich())
                    && ThanhToan.TRANG_THAI_CHO.equals(tt.getTrangThai())) {
                return tt;
            }
        }
        return null;
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

package com.linhkien.service;

import com.linhkien.dao.MaGiamGiaDAO;
import com.linhkien.dao.NguoiDungDAO;
import com.linhkien.model.MaGiamGia;
import com.linhkien.model.NguoiDung;
import com.linhkien.util.JdbcHelper;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Lớp dịch vụ quản lý và kiểm tra mã giảm giá (UC09, UC17).
 */
public class MaGiamGiaService {
    private static final Set<String> LOAI_HOP_LE = Set.of(MaGiamGia.PHAN_TRAM, MaGiamGia.SO_TIEN);

    private final MaGiamGiaDAO maGiamGiaDAO;
    private final NguoiDungDAO nguoiDungDAO;

    public MaGiamGiaService() {
        this.maGiamGiaDAO = new MaGiamGiaDAO();
        this.nguoiDungDAO = new NguoiDungDAO();
    }

    public MaGiamGiaService(MaGiamGiaDAO maGiamGiaDAO, NguoiDungDAO nguoiDungDAO) {
        this.maGiamGiaDAO = maGiamGiaDAO;
        this.nguoiDungDAO = nguoiDungDAO;
    }

    public List<MaGiamGia> layTatCa() {
        return JdbcHelper.chay(() -> maGiamGiaDAO.findAll());
    }

    public MaGiamGia layTheoMa(int maGiamGia) {
        if (maGiamGia <= 0) {
            throw new IllegalArgumentException("Mã ID giảm giá không hợp lệ");
        }
        return JdbcHelper.chay(() -> maGiamGiaDAO.findById(maGiamGia));
    }

    public MaGiamGia themMaGiamGia(MaGiamGia mg) {
        return themMaGiamGia(mg, 0);
    }

    public MaGiamGia themMaGiamGia(MaGiamGia mg, int maNguoiThucHien) {
        if (maNguoiThucHien > 0) {
            yeuCauNhanVien(maNguoiThucHien);
        }
        chuanHoaVaKiemTra(mg);
        return JdbcHelper.giaoDich(() -> {
            MaGiamGia daCo = maGiamGiaDAO.findByCode(mg.getMaCode());
            if (daCo != null) {
                throw new IllegalArgumentException("Mã giảm giá '" + mg.getMaCode() + "' đã tồn tại");
            }
            maGiamGiaDAO.insert(mg);
            return maGiamGiaDAO.findById(mg.getMaGiamGia());
        });
    }

    public MaGiamGia capNhatMaGiamGia(MaGiamGia mg) {
        return capNhatMaGiamGia(mg, 0);
    }

    public MaGiamGia capNhatMaGiamGia(MaGiamGia mg, int maNguoiThucHien) {
        if (maNguoiThucHien > 0) {
            yeuCauNhanVien(maNguoiThucHien);
        }
        if (mg == null || mg.getMaGiamGia() <= 0) {
            throw new IllegalArgumentException("Mã ID giảm giá cần cập nhật không hợp lệ");
        }
        chuanHoaVaKiemTra(mg);
        return JdbcHelper.giaoDich(() -> {
            MaGiamGia hienTai = maGiamGiaDAO.findById(mg.getMaGiamGia());
            if (hienTai == null) {
                throw new IllegalArgumentException("Không tìm thấy mã giảm giá ID: " + mg.getMaGiamGia());
            }
            MaGiamGia trungCode = maGiamGiaDAO.findByCode(mg.getMaCode());
            if (trungCode != null && trungCode.getMaGiamGia() != mg.getMaGiamGia()) {
                throw new IllegalArgumentException("Mã code '" + mg.getMaCode() + "' đã thuộc về mã giảm giá khác");
            }
            maGiamGiaDAO.update(mg);
            return maGiamGiaDAO.findById(mg.getMaGiamGia());
        });
    }

    public boolean xoaMaGiamGia(int maGiamGia, int maNguoiThucHien) {
        if (maNguoiThucHien > 0) {
            yeuCauNhanVien(maNguoiThucHien);
        }
        if (maGiamGia <= 0) {
            throw new IllegalArgumentException("Mã ID giảm giá không hợp lệ");
        }
        return JdbcHelper.giaoDich(() -> {
            MaGiamGia hienTai = maGiamGiaDAO.findById(maGiamGia);
            if (hienTai == null) {
                throw new IllegalArgumentException("Không tìm thấy mã giảm giá ID: " + maGiamGia);
            }
            try {
                return maGiamGiaDAO.delete(maGiamGia);
            } catch (SQLException e) {
                throw new IllegalStateException("Không thể xóa mã giảm giá đã được sử dụng trong đơn hàng");
            }
        });
    }

    /**
     * Kiểm tra tính hợp lệ của mã giảm giá đối với giá trị đơn hàng (UC09).
     */
    public MaGiamGia kiemTraMaGiamGia(String maCode, BigDecimal tongTienHang) {
        if (maCode == null || maCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập mã giảm giá");
        }
        String code = maCode.trim().toUpperCase();
        MaGiamGia mg = JdbcHelper.chay(() -> maGiamGiaDAO.findByCode(code));
        if (mg == null) {
            throw new IllegalArgumentException("Mã giảm giá '" + code + "' không tồn tại");
        }

        LocalDateTime now = LocalDateTime.now();
        if (mg.getNgayBatDau() != null && now.isBefore(mg.getNgayBatDau())) {
            throw new IllegalStateException("Mã giảm giá '" + code + "' chưa đến thời gian áp dụng");
        }
        if (mg.getNgayKetThuc() != null && now.isAfter(mg.getNgayKetThuc())) {
            throw new IllegalStateException("Mã giảm giá '" + code + "' đã hết hạn");
        }
        if (mg.getSoLuotToiDa() != null && mg.getSoLuotDaDung() >= mg.getSoLuotToiDa()) {
            throw new IllegalStateException("Mã giảm giá '" + code + "' đã hết lượt sử dụng");
        }

        BigDecimal tong = tongTienHang != null ? tongTienHang : BigDecimal.ZERO;
        BigDecimal toiThieu = mg.getDonHangToiThieu() != null ? mg.getDonHangToiThieu() : BigDecimal.ZERO;
        if (tong.compareTo(toiThieu) < 0) {
            throw new IllegalStateException(
                String.format("Đơn hàng cần tối thiểu %s đ để áp dụng mã '%s'",
                    toiThieu.toPlainString(), code));
        }

        if (!mg.kiemTraMa(tong)) {
            throw new IllegalStateException("Mã giảm giá '" + code + "' không đủ điều kiện áp dụng");
        }
        return mg;
    }

    private void chuanHoaVaKiemTra(MaGiamGia mg) {
        if (mg == null) {
            throw new IllegalArgumentException("Dữ liệu mã giảm giá không được để trống");
        }
        String code = mg.getMaCode() == null ? "" : mg.getMaCode().trim().toUpperCase();
        if (!code.matches("^[A-Z0-9_-]{3,32}$")) {
            throw new IllegalArgumentException("Mã code phải dài 3-32 ký tự (chữ in hoa, số, '-' hoặc '_')");
        }
        mg.setMaCode(code);

        String loai = mg.getLoaiGiam() == null ? "" : mg.getLoaiGiam().trim().toUpperCase();
        if (!LOAI_HOP_LE.contains(loai)) {
            throw new IllegalArgumentException("Loại giảm giá phải là PHAN_TRAM hoặc SO_TIEN");
        }
        mg.setLoaiGiam(loai);

        if (mg.getGiaTriGiam() == null || mg.getGiaTriGiam().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Giá trị giảm phải lớn hơn 0");
        }
        if (MaGiamGia.PHAN_TRAM.equals(loai) && mg.getGiaTriGiam().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("Mức giảm theo phần trăm không được vượt quá 100%");
        }
        if (mg.getDonHangToiThieu() == null) {
            mg.setDonHangToiThieu(BigDecimal.ZERO);
        }
        if (mg.getDonHangToiThieu().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Giá trị đơn hàng tối thiểu không được âm");
        }
        if (mg.getSoLuotToiDa() != null && mg.getSoLuotToiDa() <= 0) {
            throw new IllegalArgumentException("Số lượt tối đa phải lớn hơn 0 (hoặc để trống nếu không giới hạn)");
        }
        if (mg.getNgayBatDau() == null || mg.getNgayKetThuc() == null) {
            throw new IllegalArgumentException("Ngày bắt đầu và ngày kết thúc không được để trống");
        }
        if (!mg.getNgayKetThuc().isAfter(mg.getNgayBatDau())) {
            throw new IllegalArgumentException("Ngày kết thúc phải sau ngày bắt đầu");
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

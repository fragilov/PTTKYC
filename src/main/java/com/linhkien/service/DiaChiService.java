package com.linhkien.service;

import com.linhkien.dao.DiaChiNhanHangDAO;
import com.linhkien.dao.NguoiDungDAO;
import com.linhkien.model.DiaChiNhanHang;
import com.linhkien.model.NguoiDung;
import com.linhkien.util.JdbcHelper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Lớp dịch vụ xử lý sổ địa chỉ nhận hàng và bảo mật tài khoản người dùng (FR06).
 */
public class DiaChiService {
    private final DiaChiNhanHangDAO diaChiDAO;
    private final NguoiDungDAO nguoiDungDAO;

    public DiaChiService() {
        this.diaChiDAO = new DiaChiNhanHangDAO();
        this.nguoiDungDAO = new NguoiDungDAO();
    }

    public DiaChiService(DiaChiNhanHangDAO diaChiDAO, NguoiDungDAO nguoiDungDAO) {
        this.diaChiDAO = diaChiDAO;
        this.nguoiDungDAO = nguoiDungDAO;
    }

    public List<DiaChiNhanHang> layDanhSachDiaChi(int maNguoiDung) {
        kiemTraNguoiDung(maNguoiDung);
        return JdbcHelper.chay(() -> diaChiDAO.findByNguoiDung(maNguoiDung));
    }

    public DiaChiNhanHang layDiaChiMacDinh(int maNguoiDung) {
        kiemTraNguoiDung(maNguoiDung);
        return JdbcHelper.chay(() -> diaChiDAO.findMacDinh(maNguoiDung));
    }

    public DiaChiNhanHang luuDiaChi(DiaChiNhanHang dc) {
        if (dc == null) {
            throw new IllegalArgumentException("Thông tin địa chỉ không được để trống");
        }
        kiemTraNguoiDung(dc.getMaNguoiDung());
        String ten = yeuCauChuoi(dc.getTenNguoiNhan(), "Tên người nhận không được để trống");
        String sdt = chuanHoaSoDienThoai(dc.getSoDienThoaiNhan());
        String chiTiet = yeuCauChuoi(dc.getDiaChiChiTiet(), "Địa chỉ chi tiết không được để trống");
        String phuongXa = yeuCauChuoi(dc.getPhuongXa(), "Phường/Xã không được để trống");
        String tinhThanh = yeuCauChuoi(dc.getTinhThanh(), "Tỉnh/Thành phố không được để trống");

        dc.setTenNguoiNhan(ten);
        dc.setSoDienThoaiNhan(sdt);
        dc.setDiaChiChiTiet(chiTiet);
        dc.setPhuongXa(phuongXa);
        dc.setTinhThanh(tinhThanh);

        return JdbcHelper.giaoDich(() -> {
            List<DiaChiNhanHang> hienTai = diaChiDAO.findByNguoiDung(dc.getMaNguoiDung());
            if (dc.isLaMacDinh()) {
                diaChiDAO.boMacDinhTatCa(dc.getMaNguoiDung());
            } else if (hienTai.isEmpty()) {
                dc.datMacDinh();
            }

            if (dc.getMaDiaChi() <= 0) {
                diaChiDAO.insert(dc);
            } else {
                DiaChiNhanHang daCo = diaChiDAO.findById(dc.getMaDiaChi());
                if (daCo == null || daCo.getMaNguoiDung() != dc.getMaNguoiDung()) {
                    throw new IllegalArgumentException("Không tìm thấy địa chỉ cần cập nhật");
                }
                diaChiDAO.update(dc);
            }
            return diaChiDAO.findById(dc.getMaDiaChi());
        });
    }

    public void datDiaChiMacDinh(int maNguoiDung, int maDiaChi) {
        kiemTraNguoiDung(maNguoiDung);
        JdbcHelper.giaoDich(() -> {
            DiaChiNhanHang dc = diaChiDAO.findById(maDiaChi);
            if (dc == null || dc.getMaNguoiDung() != maNguoiDung) {
                throw new IllegalArgumentException("Không tìm thấy địa chỉ thuộc người dùng này");
            }
            diaChiDAO.datMacDinh(maNguoiDung, maDiaChi);
            return null;
        });
    }

    public boolean xoaDiaChi(int maNguoiDung, int maDiaChi) {
        kiemTraNguoiDung(maNguoiDung);
        return JdbcHelper.giaoDich(() -> {
            DiaChiNhanHang dc = diaChiDAO.findById(maDiaChi);
            if (dc == null || dc.getMaNguoiDung() != maNguoiDung) {
                throw new IllegalArgumentException("Không tìm thấy địa chỉ cần xóa");
            }
            return diaChiDAO.delete(maDiaChi);
        });
    }

    public void doiMatKhau(int maNguoiDung, String matKhauCu, String matKhauMoi) {
        kiemTraNguoiDung(maNguoiDung);
        if (matKhauCu == null || matKhauCu.isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập mật khẩu hiện tại");
        }
        if (matKhauMoi == null || matKhauMoi.length() < 6) {
            throw new IllegalArgumentException("Mật khẩu mới phải có ít nhất 6 ký tự");
        }
        String currentHash = nguoiDungDAO.findMatKhauHashById(maNguoiDung);
        String oldHash = md5Hex(matKhauCu);
        if (currentHash == null || !currentHash.equalsIgnoreCase(oldHash)) {
            throw new IllegalArgumentException("Mật khẩu hiện tại không chính xác");
        }
        String newHash = md5Hex(matKhauMoi);
        JdbcHelper.giaoDich(() -> {
            if (!nguoiDungDAO.updateMatKhauHash(maNguoiDung, newHash)) {
                throw new RuntimeException("Không thể cập nhật mật khẩu");
            }
            return null;
        });
    }

    private void kiemTraNguoiDung(int maNguoiDung) {
        NguoiDung nd = nguoiDungDAO.findById(maNguoiDung);
        if (nd == null) {
            throw new IllegalArgumentException("Không tìm thấy người dùng có mã: " + maNguoiDung);
        }
    }

    private String yeuCauChuoi(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private String chuanHoaSoDienThoai(String raw) {
        String sdt = raw == null ? "" : raw.replaceAll("[\\s.-]", "").trim();
        if (!sdt.matches("^0\\d{9,10}$")) {
            throw new IllegalArgumentException("Số điện thoại không hợp lệ (bắt đầu bằng 0, gồm 10 hoặc 11 chữ số)");
        }
        return sdt;
    }

    private static String md5Hex(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Thuật toán MD5 không khả dụng", e);
        }
    }
}

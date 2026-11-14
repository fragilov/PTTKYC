package com.linhkien.model;

/**
 * Lớp đại diện cho Nhân viên kế thừa từ {@link NguoiDung} theo Class Diagram.
 * Vai trò nhân viên: NV_BAN_HANG, NV_KHO, QUAN_TRI.
 */
public class NhanVien extends NguoiDung {

    public NhanVien() {
        super();
        this.vaiTro = "NV_BAN_HANG";
    }

    public NhanVien(int maNguoiDung, String hoTen, String email, String soDienThoai, String vaiTro) {
        super(maNguoiDung, hoTen, email, soDienThoai, vaiTro != null ? vaiTro : "NV_BAN_HANG");
    }

    public boolean laQuanTri() {
        return "QUAN_TRI".equalsIgnoreCase(this.vaiTro);
    }

    public boolean coQuyenKho() {
        return kiemTraQuyen("KHO");
    }

    public boolean coQuyenBanHang() {
        return kiemTraQuyen("BAN_HANG");
    }
}

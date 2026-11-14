package com.linhkien.model;

/**
 * Lớp đại diện cho Khách hàng kế thừa từ {@link NguoiDung} theo Class Diagram.
 */
public class KhachHang extends NguoiDung {

    public KhachHang() {
        super();
        this.vaiTro = "KHACH_HANG";
    }

    public KhachHang(int maNguoiDung, String hoTen, String email, String soDienThoai) {
        super(maNguoiDung, hoTen, email, soDienThoai, "KHACH_HANG");
    }

    public boolean coTheMuaHang() {
        return "HOAT_DONG".equalsIgnoreCase(this.trangThai) && kiemTraQuyen("MUA_HANG");
    }
}

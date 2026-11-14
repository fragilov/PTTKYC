-- Database script – Online Sales Management System for an Electronic Components Store
-- DBMS: Microsoft SQL Server 2019+
-- Naming: identifiers in Vietnamese without diacritics, consistent with the team's analysis documents
CREATE DATABASE QL_BanLinhKien;
GO
USE QL_BanLinhKien;
GO

-- Accounts of everyone who signs in: customers, sales staff, warehouse staff and administrators (UC01, UC02).
CREATE TABLE NguoiDung (
    MaNguoiDung INT IDENTITY(1,1) NOT NULL,
    HoTen NVARCHAR(100) NOT NULL,
    Email VARCHAR(100) NOT NULL CONSTRAINT UQ_NguoiDung_Email UNIQUE,
    SoDienThoai VARCHAR(15) NULL,
    MatKhauHash VARCHAR(255) NOT NULL,
    VaiTro VARCHAR(20) NOT NULL CONSTRAINT DF_NguoiDung_VaiTro DEFAULT 'KHACH_HANG' CONSTRAINT CK_NguoiDung_VaiTro CHECK (VaiTro IN ('KHACH_HANG','NV_BAN_HANG','NV_KHO','QUAN_TRI')),
    TrangThai VARCHAR(20) NOT NULL CONSTRAINT DF_NguoiDung_TrangThai DEFAULT 'HOAT_DONG' CONSTRAINT CK_NguoiDung_TrangThai CHECK (TrangThai IN ('HOAT_DONG','BI_KHOA')),
    SoLanDangNhapSai INT NOT NULL CONSTRAINT DF_NguoiDung_SoLanDangNhapSai DEFAULT 0 CONSTRAINT CK_NguoiDung_SoLanDangNhapSai CHECK (SoLanDangNhapSai >= 0),
    KhoaDen DATETIME2 NULL,
    NgayTao DATETIME2 NOT NULL CONSTRAINT DF_NguoiDung_NgayTao DEFAULT SYSDATETIME(),
    CONSTRAINT PK_NguoiDung PRIMARY KEY (MaNguoiDung)
);
GO

-- Customer address book; a customer can have many addresses, one of them the default (FR06).
CREATE TABLE DiaChiNhanHang (
    MaDiaChi INT IDENTITY(1,1) NOT NULL,
    MaNguoiDung INT NOT NULL,
    TenNguoiNhan NVARCHAR(100) NOT NULL,
    SoDienThoaiNhan VARCHAR(15) NOT NULL,
    DiaChiChiTiet NVARCHAR(200) NOT NULL,
    PhuongXa NVARCHAR(100) NOT NULL,
    TinhThanh NVARCHAR(100) NOT NULL,
    LaMacDinh BIT NOT NULL CONSTRAINT DF_DiaChiNhanHang_LaMacDinh DEFAULT 0,
    CONSTRAINT PK_DiaChiNhanHang PRIMARY KEY (MaDiaChi),
    CONSTRAINT FK_DiaChiNhanHang_MaNguoiDung FOREIGN KEY (MaNguoiDung) REFERENCES NguoiDung(MaNguoiDung)
);
GO

-- Hierarchical component categories: microcontrollers, modules, sensors, passive components, power/batteries… (UC03, UC12).
CREATE TABLE DanhMucLinhKien (
    MaDanhMuc INT IDENTITY(1,1) NOT NULL,
    TenDanhMuc NVARCHAR(100) NOT NULL CONSTRAINT UQ_DanhMucLinhKien_TenDanhMuc UNIQUE,
    MaDanhMucCha INT NULL,
    MoTa NVARCHAR(500) NULL,
    TrangThai VARCHAR(20) NOT NULL CONSTRAINT DF_DanhMucLinhKien_TrangThai DEFAULT 'HIEN' CONSTRAINT CK_DanhMucLinhKien_TrangThai CHECK (TrangThai IN ('HIEN','AN')),
    CONSTRAINT PK_DanhMucLinhKien PRIMARY KEY (MaDanhMuc),
    CONSTRAINT FK_DanhMucLinhKien_MaDanhMucCha FOREIGN KEY (MaDanhMucCha) REFERENCES DanhMucLinhKien(MaDanhMuc)
);
GO

-- Component master data: SKU, key filterable specifications, datasheet, price, stock and low-stock threshold (UC03–UC05, UC12, UC13).
CREATE TABLE LinhKien (
    MaLinhKien INT IDENTITY(1,1) NOT NULL,
    MaSKU VARCHAR(50) NOT NULL CONSTRAINT UQ_LinhKien_MaSKU UNIQUE,
    TenLinhKien NVARCHAR(200) NOT NULL,
    MaDanhMuc INT NOT NULL,
    NhaSanXuat NVARCHAR(100) NULL,
    MoTa NVARCHAR(MAX) NULL,
    KieuDongGoi VARCHAR(20) NULL CONSTRAINT CK_LinhKien_KieuDongGoi CHECK (KieuDongGoi IN ('DIP','SMD','MODULE','KHAC')),
    DienApHoatDong VARCHAR(30) NULL,
    GiaoTiep VARCHAR(50) NULL,
    DuongDanDatasheet VARCHAR(500) NULL,
    HinhAnh VARCHAR(500) NULL,
    DonViTinh NVARCHAR(20) NOT NULL CONSTRAINT DF_LinhKien_DonViTinh DEFAULT N'Cái',
    GiaBan DECIMAL(12,0) NOT NULL CONSTRAINT CK_LinhKien_GiaBan CHECK (GiaBan >= 0),
    SoLuongTon INT NOT NULL CONSTRAINT DF_LinhKien_SoLuongTon DEFAULT 0 CONSTRAINT CK_LinhKien_SoLuongTon CHECK (SoLuongTon >= 0),
    NguongTonToiThieu INT NOT NULL CONSTRAINT DF_LinhKien_NguongTonToiThieu DEFAULT 10 CONSTRAINT CK_LinhKien_NguongTonToiThieu CHECK (NguongTonToiThieu >= 0),
    TrangThaiBan VARCHAR(20) NOT NULL CONSTRAINT DF_LinhKien_TrangThaiBan DEFAULT 'DANG_BAN' CONSTRAINT CK_LinhKien_TrangThaiBan CHECK (TrangThaiBan IN ('DANG_BAN','NGUNG_BAN')),
    PhienBan ROWVERSION NOT NULL,
    NgayCapNhat DATETIME2 NOT NULL CONSTRAINT DF_LinhKien_NgayCapNhat DEFAULT SYSDATETIME(),
    CONSTRAINT PK_LinhKien PRIMARY KEY (MaLinhKien),
    CONSTRAINT FK_LinhKien_MaDanhMuc FOREIGN KEY (MaDanhMuc) REFERENCES DanhMucLinhKien(MaDanhMuc)
);
GO

-- Extended technical specifications as name–value pairs, so new specification types can be added without changing the table structure (NFR06).
CREATE TABLE ThongSoKyThuat (
    MaThongSo INT IDENTITY(1,1) NOT NULL,
    MaLinhKien INT NOT NULL,
    TenThongSo NVARCHAR(100) NOT NULL,
    GiaTri NVARCHAR(100) NOT NULL,
    DonVi NVARCHAR(20) NULL,
    CONSTRAINT PK_ThongSoKyThuat PRIMARY KEY (MaThongSo),
    CONSTRAINT FK_ThongSoKyThuat_MaLinhKien FOREIGN KEY (MaLinhKien) REFERENCES LinhKien(MaLinhKien) ON DELETE CASCADE,
    CONSTRAINT UQ_ThongSo_LinhKien_Ten UNIQUE (MaLinhKien, TenThongSo)
);
GO

-- Shopping cart; each customer has exactly one active cart (UC06).
CREATE TABLE GioHang (
    MaGioHang INT IDENTITY(1,1) NOT NULL,
    MaNguoiDung INT NOT NULL CONSTRAINT UQ_GioHang_MaNguoiDung UNIQUE,
    NgayTao DATETIME2 NOT NULL CONSTRAINT DF_GioHang_NgayTao DEFAULT SYSDATETIME(),
    NgayCapNhat DATETIME2 NOT NULL CONSTRAINT DF_GioHang_NgayCapNhat DEFAULT SYSDATETIME(),
    CONSTRAINT PK_GioHang PRIMARY KEY (MaGioHang),
    CONSTRAINT FK_GioHang_MaNguoiDung FOREIGN KEY (MaNguoiDung) REFERENCES NguoiDung(MaNguoiDung)
);
GO

-- Cart lines. Adding to the cart neither reserves nor deducts stock (CV07.1, UC06.1).
CREATE TABLE ChiTietGioHang (
    MaGioHang INT NOT NULL,
    MaLinhKien INT NOT NULL,
    SoLuong INT NOT NULL CONSTRAINT CK_ChiTietGioHang_SoLuong CHECK (SoLuong > 0),
    NgayThem DATETIME2 NOT NULL CONSTRAINT DF_ChiTietGioHang_NgayThem DEFAULT SYSDATETIME(),
    CONSTRAINT PK_ChiTietGioHang PRIMARY KEY (MaGioHang, MaLinhKien),
    CONSTRAINT FK_ChiTietGioHang_MaGioHang FOREIGN KEY (MaGioHang) REFERENCES GioHang(MaGioHang) ON DELETE CASCADE,
    CONSTRAINT FK_ChiTietGioHang_MaLinhKien FOREIGN KEY (MaLinhKien) REFERENCES LinhKien(MaLinhKien)
);
GO

-- Discount codes created by the administrator (UC09, UC17 – Could have).
CREATE TABLE MaGiamGia (
    MaGiamGia INT IDENTITY(1,1) NOT NULL,
    MaCode VARCHAR(30) NOT NULL CONSTRAINT UQ_MaGiamGia_MaCode UNIQUE,
    LoaiGiam VARCHAR(10) NOT NULL CONSTRAINT CK_MaGiamGia_LoaiGiam CHECK (LoaiGiam IN ('PHAN_TRAM','SO_TIEN')),
    GiaTriGiam DECIMAL(12,0) NOT NULL CONSTRAINT CK_MaGiamGia_GiaTriGiam CHECK (GiaTriGiam > 0),
    DonHangToiThieu DECIMAL(12,0) NOT NULL CONSTRAINT DF_MaGiamGia_DonHangToiThieu DEFAULT 0,
    SoLuotToiDa INT NULL,
    SoLuotDaDung INT NOT NULL CONSTRAINT DF_MaGiamGia_SoLuotDaDung DEFAULT 0,
    NgayBatDau DATETIME2 NOT NULL,
    NgayKetThuc DATETIME2 NOT NULL,
    CONSTRAINT PK_MaGiamGia PRIMARY KEY (MaGiamGia),
    CONSTRAINT CK_MaGiamGia_ThoiGian CHECK (NgayKetThuc > NgayBatDau)
);
GO

-- Orders: snapshot of recipient details, locked amounts, payment method and status, order status (UC08, UC10, UC14, UC19).
CREATE TABLE DonHang (
    MaDonHang INT IDENTITY(1,1) NOT NULL,
    MaNguoiDung INT NOT NULL,
    MaGiamGia INT NULL,
    MaYeuCau VARCHAR(64) NOT NULL CONSTRAINT UQ_DonHang_MaYeuCau UNIQUE,
    NgayDat DATETIME2 NOT NULL CONSTRAINT DF_DonHang_NgayDat DEFAULT SYSDATETIME(),
    TenNguoiNhan NVARCHAR(100) NOT NULL,
    SoDienThoaiNhan VARCHAR(15) NOT NULL,
    DiaChiNhan NVARCHAR(400) NOT NULL,
    TongTienHang DECIMAL(12,0) NOT NULL CONSTRAINT CK_DonHang_TongTienHang CHECK (TongTienHang >= 0),
    PhiVanChuyen DECIMAL(12,0) NOT NULL CONSTRAINT DF_DonHang_PhiVanChuyen DEFAULT 0 CONSTRAINT CK_DonHang_PhiVanChuyen CHECK (PhiVanChuyen >= 0),
    SoTienGiam DECIMAL(12,0) NOT NULL CONSTRAINT DF_DonHang_SoTienGiam DEFAULT 0 CONSTRAINT CK_DonHang_SoTienGiam CHECK (SoTienGiam >= 0),
    TongThanhToan DECIMAL(12,0) NOT NULL,
    PhuongThucThanhToan VARCHAR(20) NOT NULL CONSTRAINT CK_DonHang_PhuongThucThanhToan CHECK (PhuongThucThanhToan IN ('COD','CHUYEN_KHOAN')),
    TrangThaiThanhToan VARCHAR(20) NOT NULL CONSTRAINT DF_DonHang_TrangThaiThanhToan DEFAULT 'CHO_THANH_TOAN' CONSTRAINT CK_DonHang_TrangThaiThanhToan CHECK (TrangThaiThanhToan IN ('CHO_THANH_TOAN','DA_THANH_TOAN','DANG_HOAN_TIEN','DA_HOAN_TIEN')),
    TrangThaiDon VARCHAR(20) NOT NULL CONSTRAINT DF_DonHang_TrangThaiDon DEFAULT 'CHO_XAC_NHAN' CONSTRAINT CK_DonHang_TrangThaiDon CHECK (TrangThaiDon IN ('CHO_XAC_NHAN','DANG_DONG_GOI','DA_BAN_GIAO','DANG_GIAO','DA_GIAO','GIAO_THAT_BAI','DA_HUY')),
    DaHoanKho BIT NOT NULL CONSTRAINT DF_DonHang_DaHoanKho DEFAULT 0,
    GhiChu NVARCHAR(500) NULL,
    CONSTRAINT PK_DonHang PRIMARY KEY (MaDonHang),
    CONSTRAINT FK_DonHang_MaNguoiDung FOREIGN KEY (MaNguoiDung) REFERENCES NguoiDung(MaNguoiDung),
    CONSTRAINT FK_DonHang_MaGiamGia FOREIGN KEY (MaGiamGia) REFERENCES MaGiamGia(MaGiamGia),
    CONSTRAINT CK_DonHang_TongThanhToan CHECK (TongThanhToan = TongTienHang + PhiVanChuyen - SoTienGiam)
);
GO

-- Order lines, storing the unit price locked at checkout so past orders do not change when prices change.
CREATE TABLE ChiTietDonHang (
    MaDonHang INT NOT NULL,
    MaLinhKien INT NOT NULL,
    SoLuong INT NOT NULL CONSTRAINT CK_ChiTietDonHang_SoLuong CHECK (SoLuong > 0),
    DonGia DECIMAL(12,0) NOT NULL CONSTRAINT CK_ChiTietDonHang_DonGia CHECK (DonGia >= 0),
    ThanhTien AS (SoLuong*DonGia) PERSISTED,
    CONSTRAINT PK_ChiTietDonHang PRIMARY KEY (MaDonHang, MaLinhKien),
    CONSTRAINT FK_ChiTietDonHang_MaDonHang FOREIGN KEY (MaDonHang) REFERENCES DonHang(MaDonHang) ON DELETE CASCADE,
    CONSTRAINT FK_ChiTietDonHang_MaLinhKien FOREIGN KEY (MaLinhKien) REFERENCES LinhKien(MaLinhKien)
);
GO

-- Payment and refund transactions of an order; staff confirm COD or bank transfers manually for reconciliation.
CREATE TABLE ThanhToan (
    MaThanhToan INT IDENTITY(1,1) NOT NULL,
    MaDonHang INT NOT NULL,
    LoaiGiaoDich VARCHAR(10) NOT NULL CONSTRAINT CK_ThanhToan_LoaiGiaoDich CHECK (LoaiGiaoDich IN ('THU','HOAN')),
    PhuongThuc VARCHAR(20) NOT NULL CONSTRAINT CK_ThanhToan_PhuongThuc CHECK (PhuongThuc IN ('COD','CHUYEN_KHOAN')),
    SoTien DECIMAL(12,0) NOT NULL CONSTRAINT CK_ThanhToan_SoTien CHECK (SoTien > 0),
    MaThamChieu VARCHAR(100) NULL,
    TrangThai VARCHAR(20) NOT NULL CONSTRAINT DF_ThanhToan_TrangThai DEFAULT 'CHO_XAC_NHAN' CONSTRAINT CK_ThanhToan_TrangThai CHECK (TrangThai IN ('CHO_XAC_NHAN','THANH_CONG','THAT_BAI')),
    MaNguoiXacNhan INT NULL,
    ThoiDiem DATETIME2 NOT NULL CONSTRAINT DF_ThanhToan_ThoiDiem DEFAULT SYSDATETIME(),
    CONSTRAINT PK_ThanhToan PRIMARY KEY (MaThanhToan),
    CONSTRAINT FK_ThanhToan_MaDonHang FOREIGN KEY (MaDonHang) REFERENCES DonHang(MaDonHang),
    CONSTRAINT FK_ThanhToan_MaNguoiXacNhan FOREIGN KEY (MaNguoiXacNhan) REFERENCES NguoiDung(MaNguoiDung)
);
GO

-- Shipping details of an order: carrier, tracking number, delivery status and milestones (UC11, UC14).
CREATE TABLE VanChuyen (
    MaVanChuyen INT IDENTITY(1,1) NOT NULL,
    MaDonHang INT NOT NULL CONSTRAINT UQ_VanChuyen_MaDonHang UNIQUE,
    DonViVanChuyen NVARCHAR(100) NOT NULL,
    MaVanDon VARCHAR(50) NOT NULL CONSTRAINT UQ_VanChuyen_MaVanDon UNIQUE,
    TrangThaiGiao VARCHAR(20) NOT NULL CONSTRAINT DF_VanChuyen_TrangThaiGiao DEFAULT 'CHO_LAY_HANG' CONSTRAINT CK_VanChuyen_TrangThaiGiao CHECK (TrangThaiGiao IN ('CHO_LAY_HANG','DANG_GIAO','DA_GIAO','GIAO_THAT_BAI','HOAN_HANG')),
    NgayBanGiao DATETIME2 NULL,
    NgayGiaoDuKien DATE NULL,
    NgayGiaoThucTe DATETIME2 NULL,
    DaThuCOD BIT NOT NULL CONSTRAINT DF_VanChuyen_DaThuCOD DEFAULT 0,
    GhiChu NVARCHAR(300) NULL,
    CONSTRAINT PK_VanChuyen PRIMARY KEY (MaVanChuyen),
    CONSTRAINT FK_VanChuyen_MaDonHang FOREIGN KEY (MaDonHang) REFERENCES DonHang(MaDonHang)
);
GO

-- Log of every order/delivery status change: who changed it and when (NFR03, NFR05; CV07.2 note).
CREATE TABLE LichSuTrangThaiDon (
    MaLichSu BIGINT IDENTITY(1,1) NOT NULL,
    MaDonHang INT NOT NULL,
    TrangThaiCu VARCHAR(20) NULL,
    TrangThaiMoi VARCHAR(20) NOT NULL,
    MaNguoiThucHien INT NULL,
    ThoiDiem DATETIME2 NOT NULL CONSTRAINT DF_LichSuTrangThaiDon_ThoiDiem DEFAULT SYSDATETIME(),
    GhiChu NVARCHAR(300) NULL,
    CONSTRAINT PK_LichSuTrangThaiDon PRIMARY KEY (MaLichSu),
    CONSTRAINT FK_LichSuTrangThaiDon_MaDonHang FOREIGN KEY (MaDonHang) REFERENCES DonHang(MaDonHang),
    CONSTRAINT FK_LichSuTrangThaiDon_MaNguoiThucHien FOREIGN KEY (MaNguoiThucHien) REFERENCES NguoiDung(MaNguoiDung)
);
GO

-- Stock movement log: sale, restore on cancellation, receipt, adjustment, with before/after quantity (UC13, NFR03).
CREATE TABLE LichSuTonKho (
    MaLichSu BIGINT IDENTITY(1,1) NOT NULL,
    MaLinhKien INT NOT NULL,
    LoaiBienDong VARCHAR(20) NOT NULL CONSTRAINT CK_LichSuTonKho_LoaiBienDong CHECK (LoaiBienDong IN ('NHAP','BAN','HOAN_HUY','DIEU_CHINH')),
    SoLuongThayDoi INT NOT NULL CONSTRAINT CK_LichSuTonKho_SoLuongThayDoi CHECK (SoLuongThayDoi <> 0),
    TonTruoc INT NOT NULL CONSTRAINT CK_LichSuTonKho_TonTruoc CHECK (TonTruoc >= 0),
    TonSau INT NOT NULL CONSTRAINT CK_LichSuTonKho_TonSau CHECK (TonSau >= 0),
    MaDonHang INT NULL,
    MaNguoiThucHien INT NULL,
    LyDo NVARCHAR(300) NULL,
    ThoiDiem DATETIME2 NOT NULL CONSTRAINT DF_LichSuTonKho_ThoiDiem DEFAULT SYSDATETIME(),
    CONSTRAINT PK_LichSuTonKho PRIMARY KEY (MaLichSu),
    CONSTRAINT FK_LichSuTonKho_MaLinhKien FOREIGN KEY (MaLinhKien) REFERENCES LinhKien(MaLinhKien),
    CONSTRAINT FK_LichSuTonKho_MaDonHang FOREIGN KEY (MaDonHang) REFERENCES DonHang(MaDonHang),
    CONSTRAINT FK_LichSuTonKho_MaNguoiThucHien FOREIGN KEY (MaNguoiThucHien) REFERENCES NguoiDung(MaNguoiDung),
    CONSTRAINT CK_LichSuTonKho_TonSau CHECK (TonSau = TonTruoc + SoLuongThayDoi)
);
GO

-- Indexes for component search/filtering (NFR01) and order lookup
CREATE INDEX IX_LinhKien_DanhMuc ON LinhKien(MaDanhMuc, TrangThaiBan);
CREATE INDEX IX_LinhKien_Ten ON LinhKien(TenLinhKien);
CREATE INDEX IX_LinhKien_Loc ON LinhKien(KieuDongGoi, GiaoTiep, DienApHoatDong);
CREATE INDEX IX_ThongSo_TenGiaTri ON ThongSoKyThuat(TenThongSo, GiaTri);
CREATE INDEX IX_DonHang_Khach ON DonHang(MaNguoiDung, NgayDat DESC);
CREATE INDEX IX_DonHang_TrangThai ON DonHang(TrangThaiDon, NgayDat);
CREATE INDEX IX_LichSuTrangThaiDon_Don ON LichSuTrangThaiDon(MaDonHang, ThoiDiem);
CREATE INDEX IX_LichSuTonKho_LinhKien ON LichSuTonKho(MaLinhKien, ThoiDiem);
-- Phone is optional but unique when present (a plain UNIQUE allows only one NULL)
CREATE UNIQUE INDEX UX_NguoiDung_SoDienThoai ON NguoiDung(SoDienThoai) WHERE SoDienThoai IS NOT NULL;
-- Each customer has at most one default address
CREATE UNIQUE INDEX UX_DiaChi_MacDinh ON DiaChiNhanHang(MaNguoiDung) WHERE LaMacDinh = 1;
GO

-- Example: atomic stock deduction at checkout (NFR04) – deduct only when enough stock remains
-- UPDATE LinhKien SET SoLuongTon = SoLuongTon - @SoLuong
-- WHERE MaLinhKien = @MaLinhKien AND SoLuongTon >= @SoLuong AND TrangThaiBan = 'DANG_BAN';
-- IF @@ROWCOUNT = 0 → ROLLBACK the whole checkout transaction and report out of stock.

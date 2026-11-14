export type Page = 'shop' | 'detail' | 'cart' | 'checkout' | 'tracking' | 'account' | 'admin'

export type IconName =
  | 'search'
  | 'cart'
  | 'user'
  | 'chevron'
  | 'sliders'
  | 'grid'
  | 'list'
  | 'heart'
  | 'arrow'
  | 'truck'
  | 'shield'
  | 'box'
  | 'minus'
  | 'plus'
  | 'trash'
  | 'check'
  | 'clock'
  | 'package'
  | 'chart'
  | 'settings'
  | 'orders'
  | 'logout'
  | 'download'
  | 'menu'
  | 'close'

export type SortOrder = 'newest' | 'price-asc' | 'price-desc'
export type ViewMode = 'grid' | 'list'

export type ProductSpec = {
  maThongSo?: number
  maLinhKien?: number
  tenThongSo: string
  giaTri: string
  donVi?: string | null
}

export type Product = {
  id: number
  name: string
  sku: string
  category: string
  maker: string
  price: number
  stock: number
  minStock?: number
  accent: string
  label: string
  specs: string[]
  extendedSpecs?: ProductSpec[]
  categoryId?: number
  status?: string
  description?: string
  packageType?: string
  voltage?: string
  interfaceType?: string
  unit?: string
  image?: string
  datasheet?: string
}

export type Category = {
  maDanhMuc: number
  tenDanhMuc: string
  moTa?: string
}

export type Profile = {
  fullName: string
  phone: string
  email: string
  city: string
  district: string
  ward: string
  street: string
}

export type AddressRecord = {
  maDiaChi: number
  maNguoiDung: number
  tenNguoiNhan: string
  soDienThoaiNhan: string
  diaChiChiTiet: string
  phuongXa: string
  tinhThanh: string
  laMacDinh: boolean
}

export type CartMap = Record<number, number>

export type OrderLine = {
  maDonHang?: number
  maLinhKien: number
  maSKU?: string
  tenLinhKien?: string
  donViTinh?: string
  soLuong: number
  donGia: number
}

export type Order = {
  maDonHang: number
  maNguoiDung?: number
  maGiamGia?: number | null
  maCodeGiamGia?: string | null
  tenNguoiNhan: string
  soDienThoaiNhan: string
  diaChiNhan: string
  tongTienHang?: number
  phiVanChuyen?: number
  soTienGiam?: number
  tongThanhToan: number
  phuongThucThanhToan?: string
  trangThaiThanhToan?: string
  trangThaiDon: string
  daHoanKho?: boolean
  ghiChu?: string | null
  ngayDat: string
  tenKhachHang?: string
  danhSachChiTiet: OrderLine[]
}

export type OrderHistoryEvent = {
  maLichSu: number
  maDonHang: number
  trangThaiCu?: string | null
  trangThaiMoi: string
  maNguoiThucHien?: number | null
  tenNguoiThucHien?: string | null
  thoiDiem: string
  ghiChu?: string | null
}

export type Shipment = {
  maVanChuyen: number
  maDonHang: number
  donViVanChuyen: string
  maVanDon: string
  trangThaiGiao: string
  ngayBanGiao?: string | null
  ngayGiaoDuKien?: string | null
  ngayGiaoThucTe?: string | null
  daThuCOD?: boolean
  ghiChu?: string | null
}

export type PaymentTransaction = {
  maThanhToan: number
  maDonHang: number
  loaiGiaoDich: 'THU' | 'HOAN'
  phuongThuc: 'COD' | 'CHUYEN_KHOAN'
  soTien: number
  maThamChieu?: string | null
  trangThai: 'CHO_XAC_NHAN' | 'THANH_CONG' | 'THAT_BAI'
  maNguoiXacNhan?: number | null
  tenNguoiXacNhan?: string | null
  thoiDiem: string
}

export type TrackingPayload = {
  order: Order
  history: OrderHistoryEvent[]
  shipping: Shipment | null
  payments?: PaymentTransaction[]
}

export type Coupon = {
  maGiamGia: number
  maCode: string
  loaiGiam: 'PHAN_TRAM' | 'SO_TIEN'
  giaTriGiam: number
  donHangToiThieu: number
  soLuotToiDa?: number | null
  soLuotDaDung: number
  ngayBatDau: string
  ngayKetThuc: string
}

export type CouponValidation = {
  valid: boolean
  coupon: Coupon
  discountAmount: number
  finalSubtotal: number
}

export type InventoryHistoryItem = {
  maLichSu: number
  maLinhKien: number
  tenLinhKien: string
  maSKU: string
  loaiBienDong: 'NHAP' | 'BAN' | 'HOAN_HUY' | 'DIEU_CHINH'
  soLuongThayDoi: number
  tonTruoc: number
  tonSau: number
  maDonHang?: number | null
  maNguoiThucHien?: number | null
  tenNguoiThucHien?: string | null
  lyDo?: string | null
  thoiDiem: string
}

import type { AddressRecord, Coupon, Product, Profile, SortOrder } from '../types'

export const statusNames: Record<string, string> = {
  CHO_XAC_NHAN: 'Chờ xác nhận',
  DANG_DONG_GOI: 'Đang đóng gói',
  DA_BAN_GIAO: 'Đã bàn giao',
  DANG_GIAO: 'Đang giao',
  DA_GIAO: 'Đã giao',
  DA_HUY: 'Đã hủy',
  GIAO_THAT_BAI: 'Giao thất bại',
}

export const paymentStatusNames: Record<string, string> = {
  CHO_THANH_TOAN: 'Chờ thanh toán',
  DA_THANH_TOAN: 'Đã thanh toán',
  DANG_HOAN_TIEN: 'Đang hoàn tiền',
  DA_HOAN_TIEN: 'Đã hoàn tiền',
}

export const paymentTransactionStatusNames: Record<string, string> = {
  CHO_XAC_NHAN: 'Chờ xác nhận',
  THANH_CONG: 'Thành công',
  THAT_BAI: 'Thất bại',
}

export const stockChangeTypeNames: Record<string, string> = {
  NHAP: 'Nhập kho',
  BAN: 'Xuất bán',
  HOAN_HUY: 'Hoàn kho',
  DIEU_CHINH: 'Điều chỉnh',
}

export function money(value: number): string {
  return new Intl.NumberFormat('vi-VN').format(Math.round(value || 0)) + '₫'
}

export function formatDateTime(iso?: string | null): string {
  if (!iso) return '—'
  return iso.replace('T', ' ').slice(0, 16)
}

export function calculateShippingFee(shipping: string): number {
  return shipping === 'express' ? 49000 : 25000
}

export function calculateCouponDiscount(coupon: Pick<Coupon, 'loaiGiam' | 'giaTriGiam' | 'donHangToiThieu'> | null, subtotal: number): number {
  if (!coupon || subtotal <= 0) return 0
  if (subtotal < (coupon.donHangToiThieu || 0)) return 0
  const raw =
    coupon.loaiGiam === 'PHAN_TRAM'
      ? Math.round((subtotal * coupon.giaTriGiam) / 100)
      : coupon.giaTriGiam
  return Math.max(0, Math.min(raw, subtotal))
}

export function filterAndSortProducts(
  products: Product[],
  options: {
    search?: string
    category?: string
    maker?: string
    minPrice?: number
    maxPrice?: number
    sortOrder?: SortOrder
    onlyActive?: boolean
  }
): Product[] {
  const {
    search = '',
    category = 'Tất cả',
    maker = 'Tất cả',
    minPrice = 0,
    maxPrice = Number.MAX_SAFE_INTEGER,
    sortOrder = 'newest',
    onlyActive = true,
  } = options

  const query = search.trim().toLowerCase()
  const filtered = products.filter((p) => {
    if (onlyActive && p.status !== 'DANG_BAN') return false
    if (category !== 'Tất cả' && p.category !== category) return false
    if (maker !== 'Tất cả' && p.maker !== maker) return false
    if (p.price < minPrice || p.price > maxPrice) return false
    if (query && !`${p.id} ${p.name} ${p.sku}`.toLowerCase().includes(query)) return false
    return true
  })

  return [...filtered].sort((a, b) => {
    if (sortOrder === 'price-asc') return a.price - b.price
    if (sortOrder === 'price-desc') return b.price - a.price
    return b.id - a.id
  })
}

export function mapApiProduct(p: any): Product {
  return {
    id: p.maLinhKien,
    name: p.tenLinhKien,
    sku: p.maSKU,
    category: p.tenDanhMuc || '',
    categoryId: p.maDanhMuc,
    maker: p.nhaSanXuat || '',
    price: Number(p.giaBan || 0),
    stock: Number(p.soLuongTon || 0),
    minStock: Number(p.nguongTonToiThieu ?? 10),
    accent: 'blue',
    label: p.maSKU,
    specs: [p.dienApHoatDong, p.giaoTiep, p.kieuDongGoi].filter(Boolean),
    extendedSpecs: Array.isArray(p.danhSachThongSo) ? p.danhSachThongSo : [],
    status: p.trangThaiBan,
    description: p.moTa,
    packageType: p.kieuDongGoi,
    voltage: p.dienApHoatDong,
    interfaceType: p.giaoTiep,
    unit: p.donViTinh || 'Cái',
    image: p.hinhAnh,
    datasheet: p.duongDanDatasheet,
  }
}

export function validateRecipient(recipient: Profile): string | null {
  if (!recipient.fullName.trim()) {
    return 'Vui lòng nhập họ và tên người nhận.'
  }
  const cleanPhone = recipient.phone.replace(/[\s.-]/g, '').trim()
  if (!/^0\d{9,10}$/.test(cleanPhone)) {
    return 'Số điện thoại không hợp lệ (bắt đầu bằng 0, gồm 10 hoặc 11 chữ số).'
  }
  if (!recipient.street.trim() || !recipient.city.trim()) {
    return 'Vui lòng nhập đầy đủ địa chỉ nhận hàng.'
  }
  return null
}

export function addressRecordToProfile(record: AddressRecord, current: Profile): Profile {
  return {
    ...current,
    fullName: record.tenNguoiNhan || current.fullName,
    phone: record.soDienThoaiNhan || current.phone,
    street: record.diaChiChiTiet || current.street,
    ward: record.phuongXa || current.ward,
    city: record.tinhThanh || current.city,
  }
}

export function formatOrderAddress(recipient: Profile): string {
  return [recipient.street, recipient.ward, recipient.district, recipient.city]
    .map((s) => s.trim())
    .filter(Boolean)
    .join(', ')
}

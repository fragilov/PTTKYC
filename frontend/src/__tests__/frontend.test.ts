import { describe, expect, test } from 'bun:test'
import type { Product, Profile } from '../types'
import {
  addressRecordToProfile,
  calculateCouponDiscount,
  calculateShippingFee,
  filterAndSortProducts,
  formatOrderAddress,
  mapApiProduct,
  money,
  validateRecipient,
} from '../utils/format'
import { toggleFavoriteId } from '../utils/profileStorage'

const sampleProducts: Product[] = [
  {
    id: 1,
    name: 'Arduino Uno R3 DIP',
    sku: 'ARD-UNO-R3',
    category: 'Vi điều khiển',
    maker: 'Arduino',
    price: 120000,
    stock: 45,
    minStock: 10,
    accent: 'blue',
    label: 'ARD-UNO-R3',
    specs: ['5V', 'UART, I2C, SPI'],
    status: 'DANG_BAN',
  },
  {
    id: 2,
    name: 'Cảm biến DHT22',
    sku: 'SEN-DHT22',
    category: 'Cảm biến',
    maker: 'Aosong',
    price: 85000,
    stock: 5,
    minStock: 10,
    accent: 'blue',
    label: 'SEN-DHT22',
    specs: ['3.3V-5.5V', '1-Wire'],
    status: 'DANG_BAN',
  },
  {
    id: 3,
    name: 'Module đã ngừng bán',
    sku: 'OLD-001',
    category: 'Cảm biến',
    maker: 'Generic',
    price: 30000,
    stock: 0,
    minStock: 5,
    accent: 'blue',
    label: 'OLD-001',
    specs: [],
    status: 'NGUNG_BAN',
  },
]

describe('Frontend format & domain helpers', () => {
  test('formats VND currency and calculates shipping fee', () => {
    expect(money(120000)).toContain('120.000')
    expect(money(120000).endsWith('₫')).toBe(true)
    expect(calculateShippingFee('standard')).toBe(25000)
    expect(calculateShippingFee('express')).toBe(49000)
  })

  test('calculates percentage and fixed-amount coupon discounts safely', () => {
    expect(
      calculateCouponDiscount(
        { loaiGiam: 'PHAN_TRAM', giaTriGiam: 10, donHangToiThieu: 100000 },
        240000
      )
    ).toBe(24000)
    expect(
      calculateCouponDiscount(
        { loaiGiam: 'PHAN_TRAM', giaTriGiam: 10, donHangToiThieu: 300000 },
        240000
      )
    ).toBe(0)
    expect(
      calculateCouponDiscount(
        { loaiGiam: 'SO_TIEN', giaTriGiam: 50000, donHangToiThieu: 0 },
        30000
      )
    ).toBe(30000)
  })

  test('filters active products and sorts by newest, price-asc, price-desc', () => {
    const activeAsc = filterAndSortProducts(sampleProducts, { sortOrder: 'price-asc' })
    expect(activeAsc.map((p) => p.id)).toEqual([2, 1])

    const activeDesc = filterAndSortProducts(sampleProducts, { sortOrder: 'price-desc' })
    expect(activeDesc.map((p) => p.id)).toEqual([1, 2])

    const searchRes = filterAndSortProducts(sampleProducts, { search: 'DHT22' })
    expect(searchRes.length).toBe(1)
    expect(searchRes[0].sku).toBe('SEN-DHT22')
  })

  test('validates recipient fields and formats order address', () => {
    const valid: Profile = {
      fullName: 'Nguyễn Văn An',
      phone: '0901 234 567',
      email: 'an@example.com',
      city: 'TP.HCM',
      district: 'Quận 1',
      ward: 'Bến Nghé',
      street: '12 Lê Lợi',
    }
    expect(validateRecipient(valid)).toBeNull()
    expect(formatOrderAddress(valid)).toBe('12 Lê Lợi, Bến Nghé, Quận 1, TP.HCM')
    expect(validateRecipient({ ...valid, phone: '12345' })).not.toBeNull()
    expect(validateRecipient({ ...valid, fullName: '' })).not.toBeNull()
  })

  test('maps backend LinhKien payload and toggles favorite IDs', () => {
    const mapped = mapApiProduct({
      maLinhKien: 9,
      tenLinhKien: 'ESP32 DevKit',
      maSKU: 'MCU-ESP32',
      tenDanhMuc: 'Vi điều khiển',
      maDanhMuc: 1,
      nhaSanXuat: 'Espressif',
      giaBan: 135000,
      soLuongTon: 20,
      nguongTonToiThieu: 5,
      dienApHoatDong: '3.3V',
      giaoTiep: 'WiFi/BLE',
      trangThaiBan: 'DANG_BAN',
      danhSachThongSo: [{ tenThongSo: 'RAM', giaTri: '520', donVi: 'KB' }],
    })
    expect(mapped.id).toBe(9)
    expect(mapped.extendedSpecs?.length).toBe(1)

    const merged = addressRecordToProfile(
      {
        maDiaChi: 2,
        maNguoiDung: 1,
        tenNguoiNhan: 'Trần B',
        soDienThoaiNhan: '0987654321',
        diaChiChiTiet: '45 Hai Bà Trưng',
        phuongXa: 'Đa Kao',
        tinhThanh: 'TP.HCM',
        laMacDinh: true,
      },
      {
        fullName: 'A',
        phone: '0901234567',
        email: 'a@b.c',
        city: 'HN',
        district: 'Q1',
        ward: 'P1',
        street: '1',
      }
    )
    expect(merged.phone).toBe('0987654321')
    expect(merged.fullName).toBe('Trần B')
    expect(merged.street).toBe('45 Hai Bà Trưng')

    const first = toggleFavoriteId([], 5)
    expect(first).toEqual({ next: [5], added: true })
    const second = toggleFavoriteId(first.next, 5)
    expect(second).toEqual({ next: [], added: false })
  })
})

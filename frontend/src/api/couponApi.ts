import { api } from './client'
import type { Coupon, CouponValidation } from '../types'

export type SaveCouponPayload = {
  id: number
  code: string
  type: 'PHAN_TRAM' | 'SO_TIEN'
  value: number
  minOrder: number
  maxUses: number | string
  startDate: string
  endDate: string
}

export const fetchCoupons = () => api<Coupon[]>('/coupons')

export const saveCoupon = (payload: SaveCouponPayload) =>
  api<Coupon>('/coupons', { ...payload, staffId: 4 })

export const deleteCoupon = (id: number) =>
  api<{ deleted: boolean }>('/coupons', { id, staffId: 4 }, 'DELETE')

export const validateCoupon = (code: string, subtotal?: number) =>
  api<CouponValidation>(
    '/coupons/validate',
    subtotal !== undefined ? { code, subtotal } : { code }
  )

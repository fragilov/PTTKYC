import { api } from './client'
import type { Order, Shipment, TrackingPayload } from '../types'

export type CreateOrderPayload = {
  name: string
  phone: string
  address: string
  shipping: string
  payment: string
  requestId: string
  note?: string
  couponCode?: string
}

export const fetchOrders = (admin = false, status = '') =>
  api<Order[]>(
    admin
      ? '/orders?admin=true' + (status ? '&status=' + encodeURIComponent(status) : '')
      : '/orders'
  )

export const createOrder = (payload: CreateOrderPayload) => api<Order>('/orders', payload)

export const fetchTracking = (id: string | number, admin = false) =>
  api<TrackingPayload>(
    '/tracking?id=' + encodeURIComponent(id) + (admin ? '&admin=true' : '')
  )

export const updateOrderStatus = (id: number, status: string, note = '') =>
  api<{ ok: boolean }>('/status', { id, status, note: note || 'Cập nhật từ giao diện' })

export const handoverShipping = (id: number, carrier: string, tracking: string) =>
  api<Shipment>('/shipping', { id, carrier, tracking })

export const cancelOrder = (id: number, reason = '', admin = false) =>
  api<{ ok: boolean; order: Order }>('/cancel', { id, reason, admin: admin ? 'true' : 'false' })

import { api } from './client'
import type { PaymentTransaction } from '../types'

export const fetchPayments = (orderId?: number) =>
  api<PaymentTransaction[]>(
    orderId ? '/payments?orderId=' + encodeURIComponent(orderId) : '/payments?staffId=4'
  )

export const confirmOrderPayment = (orderId: number, reference = '') =>
  api<{ ok: boolean; payment: PaymentTransaction }>('/payments/confirm', {
    orderId,
    reference,
    staffId: 4,
  })

export const refundOrderPayment = (orderId: number, reference = '') =>
  api<{ ok: boolean; payment: PaymentTransaction }>('/payments/refund', {
    orderId,
    reference,
    staffId: 4,
  })

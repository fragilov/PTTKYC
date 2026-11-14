import { api } from './client'
import type { AddressRecord } from '../types'

export type SaveAddressPayload = {
  id?: number
  name: string
  phone: string
  street: string
  ward: string
  city: string
  isDefault?: boolean
}

export const fetchAddresses = () => api<AddressRecord[]>('/addresses')

export const saveAddress = (payload: SaveAddressPayload) =>
  api<AddressRecord>('/addresses', {
    id: payload.id ?? 0,
    name: payload.name,
    phone: payload.phone,
    street: payload.street,
    ward: payload.ward,
    city: payload.city,
    isDefault: payload.isDefault ?? true ? 'true' : 'false',
  })

export const setDefaultAddress = (id: number) =>
  api<AddressRecord[]>('/addresses', { action: 'setDefault', id })

export const deleteAddress = (id: number) =>
  api<{ deleted: boolean }>('/addresses', { id }, 'DELETE')

export const updateAccountPassword = (currentPassword: string, newPassword: string) =>
  api<{ ok: boolean; message: string }>('/account/password', {
    currentPassword,
    newPassword,
  })

import { api } from './client'
import type { Category, InventoryHistoryItem, ProductSpec } from '../types'

export type SaveProductPayload = {
  id: number
  sku: string
  name: string
  category: string
  price: number
  stock: number
  maker: string
  minStock?: number
  voltage?: string
  interfaceType?: string
  description?: string
  status?: string
}

export const fetchProducts = () => api<any[]>('/products')

export const fetchProductById = (id: number) => api<any>('/products?id=' + encodeURIComponent(id))

export const fetchCategories = () => api<Category[]>('/categories')

export const fetchLowStockProducts = () => api<any[]>('/products/low-stock')

export const saveProduct = (payload: SaveProductPayload) => api<any>('/products', payload)

export const deleteProduct = (id: number) => api<{ deleted: boolean }>('/products', { id }, 'DELETE')

export const adjustInventory = (id: number, delta: number, reason: string) =>
  api<any>('/inventory/adjust', { id, delta, reason, staffId: 4 })

export const fetchInventoryHistory = (productId?: number) =>
  api<InventoryHistoryItem[]>(
    productId && productId > 0
      ? '/inventory/history?id=' + encodeURIComponent(productId)
      : '/inventory/history'
  )

export const saveProductSpecs = (id: number, specs: ProductSpec[]) => {
  const encoded = specs
    .filter((s) => s.tenThongSo.trim() && s.giaTri.trim())
    .map((s) => `${s.tenThongSo.trim()}|${s.giaTri.trim()}|${(s.donVi || '').trim()}`)
    .join('\n')
  return api<ProductSpec[]>('/products/specs', { id, specs: encoded })
}

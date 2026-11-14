import { api } from './client'

export const fetchCart = () => api<any>('/cart')

export const addToCart = (id: number, quantity: number) =>
  api<any>('/cart', { id, quantity, action: 'add' })

export const updateCartQty = (id: number, quantity: number) =>
  api<any>('/cart', { id, quantity })

export const clearCart = () => api<any>('/cart', { action: 'clear' })

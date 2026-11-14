import type { Profile } from '../types'

const PROFILE_STORAGE_KEY = 'volthub-default-address'
const FAVORITES_STORAGE_KEY = 'volthub-favorites'

export const defaultProfile: Profile = {
  fullName: 'Nguyễn Hoài Bảo',
  phone: '0901 234 567',
  email: 'bao.nguyen@example.com',
  city: 'TP. Hồ Chí Minh',
  district: 'Quận 7',
  ward: 'Tân Phong',
  street: '12 Nguyễn Hữu Thọ',
}

export function loadProfile(): Profile {
  if (typeof window === 'undefined' || !window.localStorage) {
    return defaultProfile
  }
  try {
    const saved = window.localStorage.getItem(PROFILE_STORAGE_KEY)
    return saved ? { ...defaultProfile, ...JSON.parse(saved) } : defaultProfile
  } catch {
    return defaultProfile
  }
}

export function saveProfileToStorage(profile: Profile): void {
  if (typeof window === 'undefined' || !window.localStorage) return
  try {
    window.localStorage.setItem(PROFILE_STORAGE_KEY, JSON.stringify(profile))
  } catch {
    // Ignore storage quota errors in restricted browsers
  }
}

export function loadFavorites(): number[] {
  if (typeof window === 'undefined' || !window.localStorage) {
    return []
  }
  try {
    const raw = window.localStorage.getItem(FAVORITES_STORAGE_KEY)
    const parsed = raw ? JSON.parse(raw) : []
    return Array.isArray(parsed) ? parsed.filter((id) => typeof id === 'number') : []
  } catch {
    return []
  }
}

export function saveFavoritesToStorage(ids: number[]): void {
  if (typeof window === 'undefined' || !window.localStorage) return
  try {
    window.localStorage.setItem(FAVORITES_STORAGE_KEY, JSON.stringify(ids))
  } catch {
    // Ignore storage errors
  }
}

export function toggleFavoriteId(current: number[], productId: number): { next: number[]; added: boolean } {
  const exists = current.includes(productId)
  const next = exists ? current.filter((id) => id !== productId) : [...current, productId]
  return { next, added: !exists }
}

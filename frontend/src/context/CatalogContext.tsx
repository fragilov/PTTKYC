import { createContext, useContext } from 'react'
import type { Product } from '../types'

export type CatalogContextValue = {
  products: Product[]
  favorites: number[]
  toggleFavorite: (product: Product) => void
  isFavorite: (productId: number) => boolean
}

export const CatalogContext = createContext<CatalogContextValue>({
  products: [],
  favorites: [],
  toggleFavorite: () => {},
  isFavorite: () => false,
})

export function useCatalog() {
  return useContext(CatalogContext)
}

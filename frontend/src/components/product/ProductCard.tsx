import { useCatalog } from '../../context/CatalogContext'
import type { Product } from '../../types'
import { money } from '../../utils/format'
import { Button } from '../common/Button'
import { Icon } from '../common/Icon'
import { ProductVisual } from './ProductVisual'

export function ProductCard({
  product,
  openProduct,
  add,
}: {
  product: Product
  openProduct: (p: Product) => void
  add: (p: Product) => void
}) {
  const { isFavorite, toggleFavorite } = useCatalog()
  const liked = isFavorite(product.id)

  return (
    <article className="product-card">
      <button className="card-visual-button" onClick={() => openProduct(product)}>
        <ProductVisual product={product} />
        <span className={product.stock <= (product.minStock ?? 10) ? 'stock low' : 'stock'}>
          {product.stock <= 0
            ? 'Hết hàng'
            : product.stock <= (product.minStock ?? 10)
            ? `Chỉ còn ${product.stock}`
            : 'Còn hàng'}
        </span>
      </button>
      <div className="card-body">
        <div className="card-meta">
          <span>{product.category}</span>
          <button
            aria-label="Yêu thích"
            className={liked ? 'heart-active' : ''}
            onClick={() => toggleFavorite(product)}
          >
            <Icon name="heart" size={18} />
          </button>
        </div>
        <button className="product-title" onClick={() => openProduct(product)}>
          {product.name}
        </button>
        <p className="sku">
          {product.sku} · {product.maker}
        </p>
        <div className="spec-row">
          {product.specs.slice(0, 2).map((s) => (
            <span key={s}>{s}</span>
          ))}
        </div>
        <div className="card-footer">
          <strong>{money(product.price)}</strong>
          <Button disabled={product.stock <= 0} onClick={() => add(product)}>
            <Icon name="cart" size={17} /> Thêm
          </Button>
        </div>
      </div>
    </article>
  )
}

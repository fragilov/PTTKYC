import { useMemo, useState } from 'react'
import { Button } from '../components/common/Button'
import { Icon } from '../components/common/Icon'
import { SelectField } from '../components/common/SelectField'
import { ProductCard } from '../components/product/ProductCard'
import { useCatalog } from '../context/CatalogContext'
import type { Product, SortOrder, ViewMode } from '../types'
import { filterAndSortProducts, money } from '../utils/format'

export function ShopPage({
  openProduct,
  add,
}: {
  openProduct: (p: Product) => void
  add: (p: Product) => void
}) {
  const { products: allProducts, favorites } = useCatalog()
  const products = useMemo(
    () => allProducts.filter((p) => p.status === 'DANG_BAN'),
    [allProducts]
  )
  const [search, setSearch] = useState('')
  const [category, setCategory] = useState('Tất cả')
  const [maker, setMaker] = useState('Tất cả')
  const [minPrice, setMinPrice] = useState(0)
  const [maxPrice, setMaxPrice] = useState(5000000)
  const [sortOrder, setSortOrder] = useState<SortOrder>('newest')
  const [viewMode, setViewMode] = useState<ViewMode>('grid')
  const [onlyFavorites, setOnlyFavorites] = useState(false)
  const [filterOpen, setFilterOpen] = useState(false)

  const filtered = useMemo(() => {
    const base = filterAndSortProducts(products, {
      search,
      category,
      maker,
      minPrice,
      maxPrice,
      sortOrder,
      onlyActive: true,
    })
    return onlyFavorites ? base.filter((p) => favorites.includes(p.id)) : base
  }, [products, search, category, maker, minPrice, maxPrice, sortOrder, onlyFavorites, favorites])

  const categories = ['Tất cả', ...new Set(products.map((p) => p.category).filter(Boolean))]
  const makers = [...new Set(products.map((p) => p.maker).filter(Boolean))]

  const resetFilters = () => {
    setCategory('Tất cả')
    setMaker('Tất cả')
    setSearch('')
    setMinPrice(0)
    setMaxPrice(5000000)
    setOnlyFavorites(false)
    setSortOrder('newest')
  }

  return (
    <>
      <section className="hero">
        <div className="circuit-lines" />
        <div className="shell hero-inner">
          <div className="eyebrow">Linh kiện chuẩn · Giao hàng nhanh</div>
          <h1>
            Từ linh kiện nhỏ,
            <br />
            <span>đến mạch chạy thật.</span>
          </h1>
          <p>Bo mạch, cảm biến và nguồn điện cho bài thực hành, đồ án và dự án của bạn.</p>
          <div className="hero-search">
            <Icon name="search" />
            <input
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Tìm theo tên hoặc mã linh kiện..."
            />
            <Button
              onClick={() =>
                document.getElementById('products')?.scrollIntoView({ behavior: 'smooth' })
              }
            >
              Tìm linh kiện
            </Button>
          </div>
          <div className="quick-links">
            <span>Phổ biến:</span>
            {['Arduino', 'ESP32', 'Cảm biến', 'Nguồn 12V'].map((x) => (
              <button key={x} onClick={() => setSearch(x)}>
                {x}
              </button>
            ))}
          </div>
        </div>
      </section>
      <main className="shell shop-layout" id="products">
        <aside className={filterOpen ? 'filters open' : 'filters'}>
          <div className="filter-title">
            <strong>Bộ lọc</strong>
            <button onClick={resetFilters}>Xóa tất cả</button>
          </div>
          <div className="filter-group">
            <span>Danh mục</span>
            {categories.map((c) => (
              <label key={c}>
                <input
                  type="radio"
                  checked={category === c}
                  onChange={() => setCategory(c)}
                />
                <span>{c}</span>
                <small>
                  {c === 'Tất cả'
                    ? products.length
                    : products.filter((p) => p.category === c).length}
                </small>
              </label>
            ))}
          </div>
          <div className="filter-group price-filter">
            <span>Khoảng giá</span>
            <div className="price-values">
              <strong>{money(minPrice)}</strong>
              <span>đến</span>
              <strong>{money(maxPrice)}</strong>
            </div>
            <div
              className="dual-range"
              style={
                {
                  '--range-start': `${(minPrice / 5000000) * 100}%`,
                  '--range-end': `${(maxPrice / 5000000) * 100}%`,
                } as React.CSSProperties & Record<string, string>
              }
            >
              <div className="dual-range-track">
                <span />
              </div>
              <input
                aria-label="Giá thấp nhất"
                type="range"
                min="0"
                max="5000000"
                step="10000"
                value={minPrice}
                onChange={(e) => setMinPrice(Math.min(Number(e.target.value), maxPrice - 10000))}
              />
              <input
                aria-label="Giá cao nhất"
                type="range"
                min="0"
                max="5000000"
                step="10000"
                value={maxPrice}
                onChange={(e) => setMaxPrice(Math.max(Number(e.target.value), minPrice + 10000))}
              />
            </div>
            <div className="price-limits">
              <span>0₫</span>
              <span>5.000.000₫</span>
            </div>
          </div>
          <div className="filter-group">
            <span>Nhà sản xuất</span>
            {makers.map((m) => (
              <label key={m}>
                <input
                  type="checkbox"
                  checked={maker === m}
                  onChange={() => setMaker(maker === m ? 'Tất cả' : m)}
                />
                <span>{m}</span>
              </label>
            ))}
          </div>
          <div className="filter-group">
            <span>Bộ sưu tập</span>
            <label>
              <input
                type="checkbox"
                checked={onlyFavorites}
                onChange={() => setOnlyFavorites(!onlyFavorites)}
              />
              <span>Sản phẩm yêu thích</span>
              <small>{favorites.length}</small>
            </label>
          </div>
        </aside>
        <section className="catalog">
          <div className="catalog-head">
            <div>
              <p className="breadcrumb">
                Trang chủ <Icon name="chevron" size={13} /> Linh kiện
              </p>
              <h2>Tất cả linh kiện</h2>
              <p>
                Tìm thấy <strong>{filtered.length}</strong> sản phẩm phù hợp
              </p>
            </div>
            <div className="catalog-tools">
              <Button
                variant="secondary"
                className="mobile-filter"
                onClick={() => setFilterOpen(!filterOpen)}
              >
                <Icon name="sliders" size={17} /> Bộ lọc
              </Button>
              <SelectField
                value={sortOrder}
                onChange={(v) => setSortOrder(v as SortOrder)}
              >
                <option value="newest">Mới nhất</option>
                <option value="price-asc">Giá tăng dần</option>
                <option value="price-desc">Giá giảm dần</option>
              </SelectField>
              <div className="view-toggle">
                <button
                  aria-label="Hiển thị dạng lưới"
                  className={viewMode === 'grid' ? 'active' : ''}
                  onClick={() => setViewMode('grid')}
                >
                  <Icon name="grid" size={17} />
                </button>
                <button
                  aria-label="Hiển thị dạng danh sách"
                  className={viewMode === 'list' ? 'active' : ''}
                  onClick={() => setViewMode('list')}
                >
                  <Icon name="list" size={17} />
                </button>
              </div>
            </div>
          </div>
          {filtered.length > 0 ? (
            <div className={`product-grid ${viewMode === 'list' ? 'list-view' : ''}`.trim()}>
              {filtered.map((p) => (
                <ProductCard key={p.id} product={p} openProduct={openProduct} add={add} />
              ))}
            </div>
          ) : (
            <div className="empty-state">
              <Icon name="search" size={32} />
              <h3>Không tìm thấy linh kiện</h3>
              <p>Thử thay đổi từ khóa hoặc xóa bớt bộ lọc đang áp dụng.</p>
              <Button onClick={resetFilters}>Xóa bộ lọc</Button>
            </div>
          )}
        </section>
      </main>
    </>
  )
}

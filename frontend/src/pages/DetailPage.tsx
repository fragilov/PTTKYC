import { useEffect, useState } from 'react'
import { fetchProductById } from '../api/productApi'
import { Button } from '../components/common/Button'
import { Icon } from '../components/common/Icon'
import { ProductVisual } from '../components/product/ProductVisual'
import { useCatalog } from '../context/CatalogContext'
import type { Page, Product } from '../types'
import { mapApiProduct, money } from '../utils/format'

export function DetailPage({
  product,
  add,
  navigate,
}: {
  product: Product
  add: (p: Product, q?: number) => void
  navigate: (p: Page) => void
}) {
  const { isFavorite, toggleFavorite } = useCatalog()
  const [detail, setDetail] = useState<Product>(product)
  const [qty, setQty] = useState(1)
  const [tab, setTab] = useState('Thông số kỹ thuật')
  const [activeThumb, setActiveThumb] = useState(0)

  useEffect(() => {
    setDetail(product)
    setQty(1)
    setActiveThumb(0)
    fetchProductById(product.id)
      .then((raw) => setDetail(mapApiProduct(raw)))
      .catch(() => {
        // Fallback to list snapshot if detail fetch fails
      })
  }, [product])

  const liked = isFavorite(detail.id)
  const previewSlides = [
    { title: 'Ảnh linh kiện', desc: detail.sku },
    { title: 'Thông số điện áp', desc: detail.voltage || detail.specs[0] || 'Theo chuẩn module' },
    { title: 'Giao tiếp kết nối', desc: detail.interfaceType || detail.specs[1] || 'Chuẩn công nghiệp' },
    { title: 'Đóng gói & kho', desc: `${detail.packageType || 'MODULE'} · Còn ${detail.stock} ${detail.unit || 'Cái'}` },
  ]

  return (
    <main className="shell detail-page">
      <p className="breadcrumb">
        Trang chủ <Icon name="chevron" size={13} /> {detail.category}{' '}
        <Icon name="chevron" size={13} /> {detail.name}
      </p>
      <div className="detail-grid">
        <div className="gallery">
          {activeThumb === 0 ? (
            <ProductVisual product={detail} large />
          ) : (
            <div className="product-visual large gallery-spec-slide">
              <div className={`mini-board mini-${activeThumb}`} />
              <strong>{previewSlides[activeThumb].title}</strong>
              <p>{previewSlides[activeThumb].desc}</p>
              <span className="visual-code">{detail.sku}</span>
            </div>
          )}
          <div className="thumbnail-row">
            <button
              className={activeThumb === 0 ? 'active' : ''}
              onClick={() => setActiveThumb(0)}
              aria-label="Ảnh chính sản phẩm"
            >
              <ProductVisual product={detail} />
            </button>
            {[1, 2, 3].map((i) => (
              <button
                key={i}
                className={activeThumb === i ? 'active' : ''}
                onClick={() => setActiveThumb(i)}
                aria-label={previewSlides[i].title}
              >
                <div className={`mini-board mini-${i}`} />
              </button>
            ))}
          </div>
        </div>
        <div className="detail-copy">
          <div className="detail-tag">{detail.category}</div>
          <h1>{detail.name}</h1>
          <p className="detail-sku">
            Mã: <strong>{detail.sku}</strong> · Thương hiệu: <strong>{detail.maker || 'Chính hãng'}</strong>
          </p>
          <div className="rating">
            <span>★ ★ ★ ★ ★</span> 4.9 <i /> 32 đánh giá <i /> Đã bán 126
          </div>
          <div className="detail-price">
            {money(detail.price)} <small>Đã bao gồm VAT</small>
          </div>
          <p className="lead">
            {detail.description || 'Thông tin linh kiện được đồng bộ từ cửa hàng.'}
          </p>
          <div className="spec-preview">
            {(detail.specs.length > 0 ? detail.specs.slice(0, 3) : [detail.packageType || 'Module']).map(
              (s, i) => (
                <div key={`${s}-${i}`}>
                  <span>{['Điện áp / thông số', 'Giao tiếp', 'Kiểu đóng gói'][i] || 'Thuộc tính'}</span>
                  <strong>{s}</strong>
                </div>
              )
            )}
          </div>
          <div className="availability">
            <span>
              <Icon name="check" size={16} />{' '}
              {detail.stock > 0 ? 'Còn hàng' : 'Tạm hết hàng'}
            </span>
            <small>
              {detail.stock} {detail.unit || 'sản phẩm'} trong kho
            </small>
          </div>
          <div className="buy-row">
            <div className="qty">
              <button onClick={() => setQty(Math.max(1, qty - 1))}>
                <Icon name="minus" size={16} />
              </button>
              <span>{qty}</span>
              <button
                onClick={() => setQty(Math.min(Math.max(1, detail.stock), qty + 1))}
              >
                <Icon name="plus" size={16} />
              </button>
            </div>
            <Button
              disabled={detail.stock <= 0 || detail.status !== 'DANG_BAN'}
              className="add-large"
              onClick={() => add(detail, qty)}
            >
              <Icon name="cart" /> Thêm vào giỏ
            </Button>
            <Button
              variant="secondary"
              className={`heart-btn ${liked ? 'heart-active' : ''}`.trim()}
              onClick={() => toggleFavorite(detail)}
            >
              <Icon name="heart" />
            </Button>
          </div>
          <div className="benefits">
            <div>
              <Icon name="truck" />
              <span>
                <strong>Giao nhanh toàn quốc</strong>
                <small>Từ 1–3 ngày làm việc</small>
              </span>
            </div>
            <div>
              <Icon name="shield" />
              <span>
                <strong>Bảo hành 12 tháng</strong>
                <small>Đổi mới trong 7 ngày</small>
              </span>
            </div>
            <div>
              <Icon name="box" />
              <span>
                <strong>Kiểm tra trước khi nhận</strong>
                <small>Đóng gói chống tĩnh điện</small>
              </span>
            </div>
          </div>
        </div>
      </div>
      <section className="detail-tabs">
        <div className="tab-list">
          {['Mô tả sản phẩm', 'Thông số kỹ thuật', 'Tài liệu'].map((x) => (
            <button className={tab === x ? 'active' : ''} onClick={() => setTab(x)} key={x}>
              {x}
            </button>
          ))}
        </div>
        {tab === 'Thông số kỹ thuật' && (
          <div className="spec-table">
            <div>
              <span>SKU</span>
              <strong>{detail.sku}</strong>
            </div>
            <div>
              <span>Nhà sản xuất</span>
              <strong>{detail.maker || 'Chưa cập nhật'}</strong>
            </div>
            <div>
              <span>Điện áp hoạt động</span>
              <strong>{detail.voltage || detail.specs[0] || 'Chưa cập nhật'}</strong>
            </div>
            <div>
              <span>Giao tiếp</span>
              <strong>{detail.interfaceType || detail.specs[1] || 'Tiêu chuẩn'}</strong>
            </div>
            <div>
              <span>Kiểu đóng gói</span>
              <strong>{detail.packageType || 'MODULE'}</strong>
            </div>
            <div>
              <span>Đơn vị tính</span>
              <strong>{detail.unit || 'Cái'}</strong>
            </div>
            {detail.extendedSpecs?.map((spec, idx) => (
              <div key={`${spec.maThongSo || idx}-${spec.tenThongSo}`}>
                <span>{spec.tenThongSo}</span>
                <strong>
                  {spec.giaTri}
                  {spec.donVi ? ` ${spec.donVi}` : ''}
                </strong>
              </div>
            ))}
          </div>
        )}
        {tab === 'Mô tả sản phẩm' && (
          <p className="tab-copy">
            {detail.description ||
              'Linh kiện được tuyển chọn cho độ ổn định cao, có đầy đủ chân kết nối và tài liệu kỹ thuật. Phù hợp từ nguyên mẫu đến các hệ thống vận hành dài hạn.'}
          </p>
        )}
        {tab === 'Tài liệu' && (
          <div className="document-card">
            <Icon name="download" />
            <span>
              <strong>Datasheet — {detail.sku}.pdf</strong>
              <small>
                {detail.datasheet ? detail.datasheet : 'Chưa có tài liệu đính kèm'}
              </small>
            </span>
            {detail.datasheet && /^https?:\/\//.test(detail.datasheet) && (
              <a href={detail.datasheet} target="_blank" rel="noreferrer">
                Tải xuống
              </a>
            )}
          </div>
        )}
      </section>
      <button className="back-link" onClick={() => navigate('shop')}>
        <Icon name="arrow" size={17} /> Tiếp tục xem linh kiện
      </button>
    </main>
  )
}

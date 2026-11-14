import { useState, type FormEvent } from 'react'
import { validateCoupon } from '../api/couponApi'
import { Button } from '../components/common/Button'
import { Field } from '../components/common/Field'
import { Icon } from '../components/common/Icon'
import { ProductVisual } from '../components/product/ProductVisual'
import { useCatalog } from '../context/CatalogContext'
import type { CartMap, CouponValidation, Page } from '../types'
import { calculateCouponDiscount, money } from '../utils/format'

export function CartPage({
  cart,
  setCart,
  navigate,
  appliedCoupon,
  setAppliedCoupon,
}: {
  cart: CartMap
  setCart: (c: CartMap) => void
  navigate: (p: Page) => void
  appliedCoupon: CouponValidation | null
  setAppliedCoupon: (c: CouponValidation | null) => void
}) {
  const { products } = useCatalog()
  const rows = products.filter((p) => cart[p.id])
  const subtotal = rows.reduce((s, p) => s + p.price * cart[p.id], 0)

  const [couponInput, setCouponInput] = useState(appliedCoupon?.coupon.maCode || '')
  const [couponError, setCouponError] = useState('')
  const [checkingCoupon, setCheckingCoupon] = useState(false)

  const discount = appliedCoupon ? calculateCouponDiscount(appliedCoupon.coupon, subtotal) : 0
  const estimatedTotal = Math.max(0, subtotal - discount)

  const update = (id: number, qty: number) => setCart({ ...cart, [id]: qty })
  const remove = (id: number) => {
    const next = { ...cart }
    delete next[id]
    setCart(next)
  }

  const handleApplyCoupon = async (e: FormEvent) => {
    e.preventDefault()
    if (!couponInput.trim()) {
      setCouponError('Vui lòng nhập mã giảm giá')
      return
    }
    setCheckingCoupon(true)
    setCouponError('')
    try {
      const result = await validateCoupon(couponInput.trim(), subtotal)
      setAppliedCoupon(result)
      setCouponInput(result.coupon.maCode)
    } catch (err) {
      setAppliedCoupon(null)
      setCouponError((err as Error).message)
    } finally {
      setCheckingCoupon(false)
    }
  }

  return (
    <main className="shell cart-page">
      <div className="page-heading">
        <div>
          <p className="breadcrumb">
            Trang chủ <Icon name="chevron" size={13} /> Giỏ hàng
          </p>
          <h1>Giỏ hàng của bạn</h1>
          <p>{rows.length} sản phẩm đang chờ bạn</p>
        </div>
        <button onClick={() => navigate('shop')}>
          Tiếp tục mua sắm <Icon name="arrow" size={17} />
        </button>
      </div>
      {rows.length === 0 ? (
        <div className="empty-state cart-empty">
          <Icon name="cart" size={36} />
          <h3>Giỏ hàng đang trống</h3>
          <p>Khám phá các linh kiện phù hợp cho dự án tiếp theo của bạn.</p>
          <Button onClick={() => navigate('shop')}>Khám phá linh kiện</Button>
        </div>
      ) : (
        <div className="cart-layout">
          <section className="cart-list">
            <div className="cart-list-head">
              <span>Sản phẩm</span>
              <span>Đơn giá</span>
              <span>Số lượng</span>
              <span>Thành tiền</span>
            </div>
            {rows.map((p) => (
              <article className="cart-row" key={p.id}>
                <ProductVisual product={p} />
                <div className="cart-product">
                  <span>{p.category}</span>
                  <strong>{p.name}</strong>
                  <small>
                    {p.sku} · Còn {p.stock} sản phẩm
                  </small>
                  <button onClick={() => remove(p.id)}>
                    <Icon name="trash" size={15} /> Xóa
                  </button>
                </div>
                <div className="cart-unit">{money(p.price)}</div>
                <div className="qty">
                  <button onClick={() => update(p.id, Math.max(1, cart[p.id] - 1))}>
                    <Icon name="minus" size={15} />
                  </button>
                  <span>{cart[p.id]}</span>
                  <button onClick={() => update(p.id, Math.min(p.stock, cart[p.id] + 1))}>
                    <Icon name="plus" size={15} />
                  </button>
                </div>
                <strong className="cart-line-total">{money(p.price * cart[p.id])}</strong>
              </article>
            ))}
          </section>
          <aside className="summary-card">
            <h2>Tóm tắt đơn hàng</h2>
            <form className="coupon" onSubmit={handleApplyCoupon}>
              <Field
                placeholder="Mã giảm giá (VD: GIAM10K, SALE10)"
                value={couponInput}
                onChange={(v) => {
                  setCouponInput(v.toUpperCase())
                  setCouponError('')
                }}
              />
              <Button variant="secondary" type="submit" disabled={checkingCoupon}>
                {checkingCoupon ? '...' : 'Áp dụng'}
              </Button>
            </form>
            {couponError && <p className="inline-error">{couponError}</p>}
            {appliedCoupon && (
              <div className="coupon-applied">
                <span>
                  Đã áp dụng <strong>{appliedCoupon.coupon.maCode}</strong> (-{money(discount)})
                </span>
                <button
                  type="button"
                  onClick={() => {
                    setAppliedCoupon(null)
                    setCouponInput('')
                  }}
                >
                  Gỡ
                </button>
              </div>
            )}
            <div className="summary-lines">
              <p>
                <span>Tạm tính</span>
                <strong>{money(subtotal)}</strong>
              </p>
              {discount > 0 && (
                <p>
                  <span>Giảm giá</span>
                  <strong style={{ color: '#16845b' }}>-{money(discount)}</strong>
                </p>
              )}
              <p>
                <span>Phí vận chuyển</span>
                <span>Tính ở bước sau</span>
              </p>
            </div>
            <div className="summary-total">
              <span>
                Tổng dự kiến<small>Đã bao gồm VAT</small>
              </span>
              <strong>{money(estimatedTotal)}</strong>
            </div>
            <Button className="checkout-btn" onClick={() => navigate('checkout')}>
              Tiến hành thanh toán <Icon name="arrow" />
            </Button>
            <p className="secure-note">
              <Icon name="shield" size={16} /> Thanh toán an toàn và bảo mật
            </p>
          </aside>
        </div>
      )}
    </main>
  )
}

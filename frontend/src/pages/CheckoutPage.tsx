import { useState, type FormEvent } from 'react'
import { validateCoupon } from '../api/couponApi'
import { Button } from '../components/common/Button'
import { Field } from '../components/common/Field'
import { Icon } from '../components/common/Icon'
import { BankTransfer } from '../components/payment/BankTransfer'
import { ProductVisual } from '../components/product/ProductVisual'
import { useCatalog } from '../context/CatalogContext'
import type { CartMap, CouponValidation, Page, Profile } from '../types'
import {
  calculateCouponDiscount,
  calculateShippingFee,
  money,
  validateRecipient,
} from '../utils/format'

export function CheckoutPage({
  cart,
  navigate,
  complete,
  profile,
  appliedCoupon,
  setAppliedCoupon,
}: {
  cart: CartMap
  navigate: (p: Page) => void
  complete: (
    recipient: Profile,
    shipping: string,
    payment: string,
    requestId: string,
    note: string,
    couponCode?: string
  ) => Promise<void>
  profile: Profile
  appliedCoupon: CouponValidation | null
  setAppliedCoupon: (c: CouponValidation | null) => void
}) {
  const { products } = useCatalog()
  const [shipping, setShipping] = useState('standard')
  const [payment, setPayment] = useState('cod')
  const [step, setStep] = useState(1)
  const [recipient, setRecipient] = useState(profile)
  const [note, setNote] = useState('')
  const [couponInput, setCouponInput] = useState(appliedCoupon?.coupon.maCode || '')
  const [couponError, setCouponError] = useState('')
  const [checkingCoupon, setCheckingCoupon] = useState(false)

  const rows = products.filter((p) => cart[p.id])
  const subtotal = rows.reduce((s, p) => s + p.price * cart[p.id], 0)
  const fee = calculateShippingFee(shipping)
  const discount = appliedCoupon ? calculateCouponDiscount(appliedCoupon.coupon, subtotal) : 0
  const grandTotal = Math.max(0, subtotal + fee - discount)

  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [requestId] = useState(() => crypto.randomUUID())

  const handleApplyCoupon = async (e: FormEvent) => {
    e.preventDefault()
    if (!couponInput.trim()) {
      setCouponError('Vui lòng nhập mã giảm giá')
      return
    }
    setCheckingCoupon(true)
    setCouponError('')
    try {
      const res = await validateCoupon(couponInput.trim(), subtotal)
      setAppliedCoupon(res)
      setCouponInput(res.coupon.maCode)
    } catch (err) {
      setAppliedCoupon(null)
      setCouponError((err as Error).message)
    } finally {
      setCheckingCoupon(false)
    }
  }

  const next = async () => {
    const validationError = validateRecipient(recipient)
    if (validationError) {
      setError(validationError)
      return
    }
    if (step < 3) {
      setError('')
      setStep(step + 1)
      return
    }
    setBusy(true)
    setError('')
    try {
      await complete(
        recipient,
        shipping,
        payment,
        requestId,
        note,
        appliedCoupon?.coupon.maCode
      )
    } catch (e) {
      setError((e as Error).message)
    } finally {
      setBusy(false)
    }
  }

  return (
    <main className="shell checkout-page">
      <button
        className="back-link"
        onClick={() => (step > 1 ? setStep(step - 1) : navigate('cart'))}
      >
        <Icon name="arrow" size={17} /> Quay lại
      </button>
      <div className="checkout-title">
        <h1>Thanh toán</h1>
        <p>Hoàn tất đơn hàng trong vài bước đơn giản</p>
      </div>
      <div className="stepper">
        {['Thông tin nhận hàng', 'Vận chuyển & thanh toán', 'Xác nhận'].map((x, i) => (
          <div className={step >= i + 1 ? 'active' : ''} key={x}>
            <span>{step > i + 1 ? <Icon name="check" size={15} /> : i + 1}</span>
            <strong>{x}</strong>
          </div>
        ))}
      </div>
      <div className="checkout-layout">
        <section className="checkout-form">
          {step === 1 && (
            <div className="form-card">
              <div className="section-head">
                <span>01</span>
                <div>
                  <h2>Thông tin nhận hàng</h2>
                  <p>Đã điền theo địa chỉ mặc định trong tài khoản. Bạn có thể sửa cho đơn hàng này.</p>
                </div>
              </div>
              <div className="default-address-note">
                <Icon name="check" size={15} />
                <span>Đang dùng địa chỉ mặc định</span>
                <button onClick={() => navigate('account')}>Cập nhật trong tài khoản</button>
              </div>
              <div className="form-grid">
                <Field
                  label="Họ và tên"
                  value={recipient.fullName}
                  onChange={(v) => setRecipient({ ...recipient, fullName: v })}
                />
                <Field
                  label="Số điện thoại"
                  value={recipient.phone}
                  onChange={(v) => setRecipient({ ...recipient, phone: v })}
                />
                <Field
                  label="Email"
                  value={recipient.email}
                  onChange={(v) => setRecipient({ ...recipient, email: v })}
                />
                <Field
                  label="Tỉnh / Thành phố"
                  value={recipient.city}
                  onChange={(v) => setRecipient({ ...recipient, city: v })}
                />
                <Field
                  label="Quận / Huyện"
                  value={recipient.district}
                  onChange={(v) => setRecipient({ ...recipient, district: v })}
                />
                <Field
                  label="Phường / Xã"
                  value={recipient.ward}
                  onChange={(v) => setRecipient({ ...recipient, ward: v })}
                />
                <div className="full">
                  <Field
                    label="Địa chỉ cụ thể"
                    value={recipient.street}
                    onChange={(v) => setRecipient({ ...recipient, street: v })}
                  />
                </div>
                <div className="full">
                  <Field
                    label="Ghi chú giao hàng (không bắt buộc)"
                    placeholder="Ví dụ: Gọi trước khi giao..."
                    value={note}
                    onChange={setNote}
                  />
                </div>
              </div>
            </div>
          )}
          {step === 2 && (
            <>
              <div className="form-card">
                <div className="section-head">
                  <span>02</span>
                  <div>
                    <h2>Phương thức vận chuyển</h2>
                    <p>Chọn tốc độ giao hàng phù hợp</p>
                  </div>
                </div>
                <div className="choice-list">
                  <label className={shipping === 'standard' ? 'selected' : ''}>
                    <input
                      type="radio"
                      checked={shipping === 'standard'}
                      onChange={() => setShipping('standard')}
                    />
                    <Icon name="truck" />
                    <span>
                      <strong>Giao hàng tiêu chuẩn</strong>
                      <small>Dự kiến 2–4 ngày</small>
                    </span>
                    <b>{money(25000)}</b>
                  </label>
                  <label className={shipping === 'express' ? 'selected' : ''}>
                    <input
                      type="radio"
                      checked={shipping === 'express'}
                      onChange={() => setShipping('express')}
                    />
                    <Icon name="package" />
                    <span>
                      <strong>Giao nhanh</strong>
                      <small>Nhận hàng ngày mai</small>
                    </span>
                    <b>{money(49000)}</b>
                  </label>
                </div>
              </div>
              <div className="form-card">
                <div className="section-head">
                  <span>03</span>
                  <div>
                    <h2>Phương thức thanh toán</h2>
                    <p>Giao dịch được bảo vệ an toàn</p>
                  </div>
                </div>
                <div className="choice-list payment-choices">
                  {[
                    {
                      id: 'cod',
                      title: 'Thanh toán khi nhận hàng',
                      note: 'Tiền mặt hoặc chuyển khoản cho shipper',
                    },
                    {
                      id: 'bank',
                      title: 'QR / Chuyển khoản ngân hàng',
                      note: 'Chuyển khoản được xác nhận thủ công',
                    },
                  ].map((x) => (
                    <label className={payment === x.id ? 'selected' : ''} key={x.id}>
                      <input
                        type="radio"
                        checked={payment === x.id}
                        onChange={() => setPayment(x.id)}
                      />
                      <span className="pay-icon">{x.id === 'cod' ? 'COD' : 'QR'}</span>
                      <span>
                        <strong>{x.title}</strong>
                        <small>{x.note}</small>
                      </span>
                    </label>
                  ))}
                </div>
                {payment === 'bank' && <BankTransfer amount={grandTotal} />}
              </div>
            </>
          )}
          {step === 3 && (
            <div className="form-card confirmation">
              <div className="confirm-mark">
                <Icon name="check" size={28} />
              </div>
              <h2>Sẵn sàng đặt hàng</h2>
              <p>Vui lòng kiểm tra lại thông tin trước khi xác nhận.</p>
              <div className="confirm-grid">
                <div>
                  <span>Người nhận</span>
                  <strong>{recipient.fullName}</strong>
                  <small>{recipient.phone}</small>
                </div>
                <div>
                  <span>Địa chỉ giao</span>
                  <strong>
                    {recipient.street}, {recipient.ward}
                  </strong>
                  <small>
                    {[recipient.district, recipient.city].filter(Boolean).join(', ')}
                  </small>
                </div>
                <div>
                  <span>Vận chuyển</span>
                  <strong>
                    {shipping === 'express' ? 'Giao nhanh' : 'Giao hàng tiêu chuẩn'}
                  </strong>
                  <small>{shipping === 'express' ? 'Dự kiến ngày mai' : 'Dự kiến 2–4 ngày'}</small>
                </div>
                <div>
                  <span>Thanh toán</span>
                  <strong>
                    {payment === 'cod' ? 'Thanh toán khi nhận hàng' : 'Chuyển khoản ngân hàng'}
                  </strong>
                  <small>{payment === 'cod' ? 'Chờ thu khi giao' : 'Xử lý an toàn'}</small>
                </div>
                {note.trim() && (
                  <div style={{ gridColumn: '1 / -1' }}>
                    <span>Ghi chú giao hàng</span>
                    <strong>{note.trim()}</strong>
                  </div>
                )}
              </div>
              {payment === 'bank' && <BankTransfer amount={grandTotal} />}
            </div>
          )}
          {error && <p className="inline-error" role="alert">{error}</p>}
          <Button
            disabled={busy || rows.length === 0}
            className="continue-btn"
            onClick={next}
          >
            {step === 3 ? 'Xác nhận đặt hàng' : 'Tiếp tục'} <Icon name="arrow" />
          </Button>
        </section>
        <aside className="order-card">
          <h2>Đơn hàng của bạn</h2>
          {rows.map((p) => (
            <div className="order-mini" key={p.id}>
              <ProductVisual product={p} />
              <span>
                <strong>{p.name}</strong>
                <small>SL: {cart[p.id]}</small>
              </span>
              <b>{money(p.price * cart[p.id])}</b>
            </div>
          ))}
          <form className="coupon" onSubmit={handleApplyCoupon}>
            <Field
              placeholder="Mã giảm giá..."
              value={couponInput}
              onChange={(v) => {
                setCouponInput(v.toUpperCase())
                setCouponError('')
              }}
            />
            <Button variant="secondary" type="submit" disabled={checkingCoupon}>
              Áp dụng
            </Button>
          </form>
          {couponError && <p className="inline-error">{couponError}</p>}
          {appliedCoupon && (
            <div className="coupon-applied">
              <span>
                Mã <strong>{appliedCoupon.coupon.maCode}</strong> (-{money(discount)})
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
            <p>
              <span>Phí vận chuyển</span>
              <strong>{money(fee)}</strong>
            </p>
            {discount > 0 && (
              <p>
                <span>Giảm giá ({appliedCoupon?.coupon.maCode})</span>
                <strong style={{ color: '#16845b' }}>-{money(discount)}</strong>
              </p>
            )}
          </div>
          <div className="summary-total">
            <span>
              Tổng thanh toán<small>Đã bao gồm VAT</small>
            </span>
            <strong>{money(grandTotal)}</strong>
          </div>
        </aside>
      </div>
    </main>
  )
}

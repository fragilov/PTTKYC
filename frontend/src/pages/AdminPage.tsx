import { useCallback, useEffect, useMemo, useState, type FormEvent } from 'react'
import { deleteCoupon, fetchCoupons, saveCoupon } from '../api/couponApi'
import {
  cancelOrder,
  fetchOrders,
  handoverShipping,
  updateOrderStatus,
} from '../api/orderApi'
import { confirmOrderPayment, refundOrderPayment } from '../api/paymentApi'
import {
  adjustInventory,
  deleteProduct,
  fetchCategories,
  fetchInventoryHistory,
  fetchLowStockProducts,
  saveProduct,
} from '../api/productApi'
import { Button } from '../components/common/Button'
import { Field } from '../components/common/Field'
import { Icon } from '../components/common/Icon'
import { Logo } from '../components/common/Logo'
import { SelectField } from '../components/common/SelectField'
import { ProductVisual } from '../components/product/ProductVisual'
import { useCatalog } from '../context/CatalogContext'
import type { Category, Coupon, InventoryHistoryItem, Order, Page, Product } from '../types'
import {
  formatDateTime,
  money,
  paymentStatusNames,
  statusNames,
  stockChangeTypeNames,
} from '../utils/format'

type AdminSection = 'Linh kiện' | 'Đơn hàng' | 'Vận chuyển' | 'Mã giảm giá'

export function AdminPage({
  navigate,
  refresh,
  notify,
}: {
  navigate: (p: Page) => void
  refresh: () => Promise<void>
  notify: (msg: string) => void
}) {
  const { products } = useCatalog()
  const [section, setSection] = useState<AdminSection>('Linh kiện')
  const [orders, setOrders] = useState<Order[]>([])
  const [categories, setCategories] = useState<Category[]>([])
  const [coupons, setCoupons] = useState<Coupon[]>([])
  const [history, setHistory] = useState<InventoryHistoryItem[]>([])
  const [lowStockCount, setLowStockCount] = useState(0)

  const [search, setSearch] = useState('')
  const [orderStatusFilter, setOrderStatusFilter] = useState('')
  const [showStockHistory, setShowStockHistory] = useState(false)
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  // Product editor state
  const emptyProduct = {
    id: 0,
    sku: '',
    name: '',
    category: '',
    price: 0,
    stock: 0,
    minStock: 10,
    maker: '',
    voltage: '',
    interfaceType: '',
    description: '',
    status: 'DANG_BAN',
  }
  const [draft, setDraft] = useState<typeof emptyProduct | null>(null)

  // Inline Stock Adjust state
  const [adjustTarget, setAdjustTarget] = useState<Product | null>(null)
  const [adjustDelta, setAdjustDelta] = useState('10')
  const [adjustReason, setAdjustReason] = useState('Nhập bổ sung tồn kho')

  // Inline Handover / Delivery fail state
  const [handoverOrderId, setHandoverOrderId] = useState<number | null>(null)
  const [carrierInput, setCarrierInput] = useState('Giao Hàng Nhanh')
  const [trackingInput, setTrackingInput] = useState('')

  const [failOrderId, setFailOrderId] = useState<number | null>(null)
  const [failReason, setFailReason] = useState('Khách không nghe máy / từ chối nhận')

  // Coupon editor state
  const today = new Date().toISOString().slice(0, 10)
  const nextMonth = new Date(Date.now() + 30 * 86400000).toISOString().slice(0, 10)
  const emptyCoupon = {
    id: 0,
    code: '',
    type: 'SO_TIEN' as 'PHAN_TRAM' | 'SO_TIEN',
    value: 10000,
    minOrder: 100000,
    maxUses: '',
    startDate: today,
    endDate: nextMonth,
  }
  const [couponDraft, setCouponDraft] = useState<typeof emptyCoupon | null>(null)

  const load = useCallback(async () => {
    const [orderList, catList, couponList, historyList, lowList] = await Promise.all([
      fetchOrders(true),
      fetchCategories(),
      fetchCoupons(),
      fetchInventoryHistory(),
      fetchLowStockProducts(),
    ])
    setOrders(orderList)
    setCategories(catList)
    setCoupons(couponList)
    setHistory(historyList)
    setLowStockCount(lowList.length)
  }, [])

  useEffect(() => {
    load().catch((e) => setError((e as Error).message))
  }, [load])

  const run = async (action: () => Promise<unknown>, successMessage?: string) => {
    setBusy(true)
    setError('')
    try {
      await action()
      await Promise.all([refresh(), load()])
      if (successMessage) notify(successMessage)
    } catch (e) {
      setError((e as Error).message)
    } finally {
      setBusy(false)
    }
  }

  const pendingOrdersCount = useMemo(
    () => orders.filter((o) => o.trangThaiDon === 'CHO_XAC_NHAN').length,
    [orders]
  )
  const activeRevenue = useMemo(
    () =>
      orders
        .filter((o) => o.trangThaiDon !== 'DA_HUY' && o.trangThaiDon !== 'GIAO_THAT_BAI')
        .reduce((sum, o) => sum + Number(o.tongThanhToan || 0), 0),
    [orders]
  )
  const filteredProducts = useMemo(
    () =>
      products.filter((p) =>
        `${p.id} ${p.sku} ${p.name}`.toLowerCase().includes(search.toLowerCase())
      ),
    [products, search]
  )
  const filteredOrders = useMemo(
    () =>
      orderStatusFilter
        ? orders.filter((o) => o.trangThaiDon === orderStatusFilter)
        : orders,
    [orders, orderStatusFilter]
  )
  const shippingOrders = useMemo(
    () =>
      orders.filter((o) =>
        ['DANG_DONG_GOI', 'DANG_GIAO', 'DA_GIAO', 'GIAO_THAT_BAI'].includes(o.trangThaiDon)
      ),
    [orders]
  )

  const handleProductSubmit = (e: FormEvent) => {
    e.preventDefault()
    if (!draft) return
    void run(async () => {
      await saveProduct(draft)
      setDraft(null)
    }, draft.id ? 'Đã cập nhật linh kiện' : 'Đã thêm linh kiện mới')
  }

  const handleAdjustSubmit = (e: FormEvent) => {
    e.preventDefault()
    if (!adjustTarget) return
    const delta = Number(adjustDelta)
    if (!Number.isInteger(delta) || delta === 0) {
      setError('Số lượng điều chỉnh phải là số nguyên khác 0')
      return
    }
    void run(async () => {
      await adjustInventory(adjustTarget.id, delta, adjustReason)
      setAdjustTarget(null)
    }, `Đã cập nhật tồn kho cho ${adjustTarget.name}`)
  }

  const handleHandoverSubmit = (e: FormEvent) => {
    e.preventDefault()
    if (!handoverOrderId) return
    if (!carrierInput.trim() || !trackingInput.trim()) {
      setError('Vui lòng nhập đơn vị vận chuyển và mã vận đơn')
      return
    }
    void run(async () => {
      await handoverShipping(handoverOrderId, carrierInput.trim(), trackingInput.trim())
      setHandoverOrderId(null)
      setTrackingInput('')
    }, `Đã bàn giao vận chuyển đơn #${handoverOrderId}`)
  }

  const handleCouponSubmit = (e: FormEvent) => {
    e.preventDefault()
    if (!couponDraft) return
    void run(async () => {
      await saveCoupon({
        ...couponDraft,
        maxUses: couponDraft.maxUses === '' ? '' : Number(couponDraft.maxUses),
      })
      setCouponDraft(null)
    }, couponDraft.id ? 'Đã cập nhật mã giảm giá' : 'Đã tạo mã giảm giá mới')
  }

  return (
    <div className="admin-shell">
      <aside className="admin-sidebar">
        <Logo onClick={() => navigate('shop')} />
        <p className="admin-label">QUẢN TRỊ HỆ THỐNG</p>
        <nav>
          <button
            className={section === 'Linh kiện' ? 'active' : ''}
            onClick={() => setSection('Linh kiện')}
          >
            <Icon name="box" size={17} /> Linh kiện
            {lowStockCount > 0 && <span>{lowStockCount}</span>}
          </button>
          <button
            className={section === 'Đơn hàng' ? 'active' : ''}
            onClick={() => setSection('Đơn hàng')}
          >
            <Icon name="orders" size={17} /> Đơn hàng
            {pendingOrdersCount > 0 && <span>{pendingOrdersCount}</span>}
          </button>
          <button
            className={section === 'Vận chuyển' ? 'active' : ''}
            onClick={() => setSection('Vận chuyển')}
          >
            <Icon name="truck" size={17} /> Vận chuyển
          </button>
          <button
            className={section === 'Mã giảm giá' ? 'active' : ''}
            onClick={() => setSection('Mã giảm giá')}
          >
            <Icon name="chart" size={17} /> Mã giảm giá
          </button>
        </nav>
        <div className="admin-user">
          <span className="avatar">AD</span>
          <span>
            <strong>Quản trị viên</strong>
            <small>Admin #4</small>
          </span>
          <button aria-label="Về cửa hàng" onClick={() => navigate('shop')}>
            <Icon name="logout" size={17} />
          </button>
        </div>
      </aside>

      <main className="admin-main">
        <header>
          <div>
            <h1>{section}</h1>
            <p>Quản lý danh mục, tồn kho, đơn hàng, vận chuyển, thanh toán và khuyến mãi.</p>
          </div>
          <div>
            <Button variant="secondary" onClick={() => navigate('shop')}>
              Về cửa hàng
            </Button>
          </div>
        </header>

        <section className="stats-grid">
          <div>
            <span className="stat-icon">
              <Icon name="box" size={18} />
            </span>
            <span>Tổng linh kiện</span>
            <strong>{products.length}</strong>
            <small className="up">Đang quản lý trong danh mục</small>
          </div>
          <div>
            <span className="stat-icon">
              <Icon name="clock" size={18} />
            </span>
            <span>Sắp hết hàng</span>
            <strong>{lowStockCount}</strong>
            <small className={lowStockCount > 0 ? 'warn' : 'up'}>
              {lowStockCount > 0 ? 'Cần nhập bổ sung' : 'Tồn kho ổn định'}
            </small>
          </div>
          <div>
            <span className="stat-icon">
              <Icon name="orders" size={18} />
            </span>
            <span>Đơn chờ xác nhận</span>
            <strong>{pendingOrdersCount}</strong>
            <small>Tổng {orders.length} đơn hàng</small>
          </div>
          <div>
            <span className="stat-icon">
              <Icon name="chart" size={18} />
            </span>
            <span>Giá trị đơn hợp lệ</span>
            <strong>{money(activeRevenue)}</strong>
            <small className="up">{coupons.length} mã giảm giá đang quản lý</small>
          </div>
        </section>

        {error && (
          <p className="inline-error" role="alert" style={{ marginTop: 14 }}>
            {error}
          </p>
        )}

        {section === 'Linh kiện' && (
          <section className="management-card" style={{ marginTop: 16 }}>
            <div className="card-heading" style={{ gap: 12, flexWrap: 'wrap' }}>
              <Field
                placeholder="Tìm theo ID, SKU hoặc tên linh kiện..."
                value={search}
                onChange={setSearch}
              />
              <div className="inline-actions">
                <Button
                  variant="secondary"
                  onClick={() => setShowStockHistory(!showStockHistory)}
                >
                  {showStockHistory ? 'Ẩn lịch sử kho' : `Lịch sử kho (${history.length})`}
                </Button>
                <Button
                  onClick={() =>
                    setDraft({
                      ...emptyProduct,
                      category: String(categories[0]?.maDanhMuc || 1),
                    })
                  }
                >
                  Thêm linh kiện
                </Button>
              </div>
            </div>

            {draft && (
              <form className="inline-panel" onSubmit={handleProductSubmit}>
                <h3 style={{ marginBottom: 12 }}>
                  {draft.id ? `Cập nhật linh kiện #${draft.id}` : 'Thêm linh kiện mới'}
                </h3>
                <div className="form-grid">
                  <Field
                    label="Mã SKU"
                    value={draft.sku}
                    onChange={(v) => setDraft({ ...draft, sku: v })}
                  />
                  <Field
                    label="Tên linh kiện"
                    value={draft.name}
                    onChange={(v) => setDraft({ ...draft, name: v })}
                  />
                  <Field
                    label="Giá bán (₫)"
                    type="number"
                    value={String(draft.price)}
                    onChange={(v) => setDraft({ ...draft, price: Number(v) })}
                  />
                  <Field
                    label="Tồn kho"
                    type="number"
                    value={String(draft.stock)}
                    onChange={(v) => setDraft({ ...draft, stock: Number(v) })}
                  />
                  <Field
                    label="Ngưỡng tồn tối thiểu"
                    type="number"
                    value={String(draft.minStock)}
                    onChange={(v) => setDraft({ ...draft, minStock: Number(v) })}
                  />
                  <Field
                    label="Nhà sản xuất"
                    value={draft.maker}
                    onChange={(v) => setDraft({ ...draft, maker: v })}
                  />
                  <Field
                    label="Điện áp hoạt động"
                    value={draft.voltage}
                    onChange={(v) => setDraft({ ...draft, voltage: v })}
                  />
                  <Field
                    label="Giao tiếp"
                    value={draft.interfaceType}
                    onChange={(v) => setDraft({ ...draft, interfaceType: v })}
                  />
                  <SelectField
                    label="Danh mục"
                    value={draft.category}
                    onChange={(v) => setDraft({ ...draft, category: v })}
                  >
                    {categories.map((c) => (
                      <option key={c.maDanhMuc} value={c.maDanhMuc}>
                        {c.tenDanhMuc}
                      </option>
                    ))}
                  </SelectField>
                  <SelectField
                    label="Trạng thái bán"
                    value={draft.status}
                    onChange={(v) => setDraft({ ...draft, status: v })}
                  >
                    <option value="DANG_BAN">Đang bán</option>
                    <option value="NGUNG_BAN">Ngừng bán</option>
                  </SelectField>
                  <div className="full">
                    <Field
                      label="Mô tả"
                      value={draft.description}
                      onChange={(v) => setDraft({ ...draft, description: v })}
                    />
                  </div>
                </div>
                <div className="inline-actions" style={{ marginTop: 14 }}>
                  <Button disabled={busy} type="submit">
                    Lưu linh kiện
                  </Button>
                  <Button variant="secondary" onClick={() => setDraft(null)}>
                    Hủy
                  </Button>
                </div>
              </form>
            )}

            {adjustTarget && (
              <form className="inline-panel" onSubmit={handleAdjustSubmit}>
                <h3>
                  Điều chỉnh tồn kho: {adjustTarget.name} (Hiện có: {adjustTarget.stock})
                </h3>
                <div className="form-grid" style={{ marginTop: 12 }}>
                  <Field
                    label="Số lượng thay đổi (+ nhập, - xuất kiểm kê)"
                    type="number"
                    value={adjustDelta}
                    onChange={setAdjustDelta}
                  />
                  <Field
                    label="Lý do điều chỉnh"
                    value={adjustReason}
                    onChange={setAdjustReason}
                  />
                </div>
                <div className="inline-actions" style={{ marginTop: 12 }}>
                  <Button disabled={busy} type="submit">
                    Xác nhận điều chỉnh
                  </Button>
                  <Button variant="secondary" onClick={() => setAdjustTarget(null)}>
                    Đóng
                  </Button>
                </div>
              </form>
            )}

            <div className="inventory-grid">
              {filteredProducts.map((p) => {
                const isLow = p.stock <= (p.minStock ?? 10)
                return (
                  <div key={p.id} className="inventory-item-row">
                    <ProductVisual product={p} />
                    <span>
                      <strong>
                        {p.name}
                        {p.status !== 'DANG_BAN' && (
                          <span className="badge badge-danger">Ngừng bán</span>
                        )}
                      </strong>
                      <small>
                        {p.sku} · {p.category}
                      </small>
                    </span>
                    <b>{money(p.price)}</b>
                    <em className={isLow ? 'low' : ''}>
                      {p.stock} trong kho {isLow ? '(Tồn thấp)' : ''}
                    </em>
                    <div className="inline-actions">
                      <Button
                        variant="secondary"
                        onClick={() => {
                          setAdjustTarget(p)
                          setAdjustDelta('10')
                          setAdjustReason('Nhập bổ sung tồn kho')
                        }}
                      >
                        +/- Kho
                      </Button>
                      <Button
                        variant="secondary"
                        onClick={() =>
                          setDraft({
                            id: p.id,
                            sku: p.sku,
                            name: p.name,
                            category: String(p.categoryId || ''),
                            price: p.price,
                            stock: p.stock,
                            minStock: p.minStock ?? 10,
                            maker: p.maker,
                            voltage: p.voltage || '',
                            interfaceType: p.interfaceType || '',
                            description: p.description || '',
                            status: p.status || 'DANG_BAN',
                          })
                        }
                      >
                        Sửa
                      </Button>
                      <Button
                        disabled={busy}
                        variant="secondary"
                        onClick={() => {
                          if (window.confirm(`Xóa hoặc ngừng bán ${p.name}?`)) {
                            void run(
                              () => deleteProduct(p.id),
                              `Đã xử lý xóa/ngừng bán ${p.name}`
                            )
                          }
                        }}
                      >
                        Xóa
                      </Button>
                    </div>
                  </div>
                )
              })}
            </div>

            {showStockHistory && (
              <div className="orders-table-card" style={{ marginTop: 22 }}>
                <div className="card-heading">
                  <h2>Lịch sử biến động tồn kho</h2>
                </div>
                <div className="table-scroll">
                  <table>
                    <thead>
                      <tr>
                        <th>Thời điểm</th>
                        <th>Linh kiện</th>
                        <th>Loại</th>
                        <th>Thay đổi</th>
                        <th>Trước → Sau</th>
                        <th>Lý do</th>
                      </tr>
                    </thead>
                    <tbody>
                      {history.map((h) => (
                        <tr key={h.maLichSu}>
                          <td>{formatDateTime(h.thoiDiem)}</td>
                          <td>
                            <strong>{h.tenLinhKien}</strong> ({h.maSKU})
                          </td>
                          <td>{stockChangeTypeNames[h.loaiBienDong] || h.loaiBienDong}</td>
                          <td
                            style={{
                              color: h.soLuongThayDoi > 0 ? '#16845b' : '#d92d20',
                              fontWeight: 600,
                            }}
                          >
                            {h.soLuongThayDoi > 0
                              ? `+${h.soLuongThayDoi}`
                              : h.soLuongThayDoi}
                          </td>
                          <td>
                            {h.tonTruoc} → {h.tonSau}
                          </td>
                          <td>{h.lyDo || '—'}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            )}
          </section>
        )}

        {section === 'Đơn hàng' && (
          <section className="orders-table-card" style={{ marginTop: 16 }}>
            <div className="card-heading" style={{ flexWrap: 'wrap', gap: 12 }}>
              <h2>Danh sách đơn hàng ({filteredOrders.length})</h2>
              <SelectField value={orderStatusFilter} onChange={setOrderStatusFilter}>
                <option value="">Tất cả trạng thái</option>
                {Object.entries(statusNames).map(([key, label]) => (
                  <option key={key} value={key}>
                    {label}
                  </option>
                ))}
              </SelectField>
            </div>

            {handoverOrderId && (
              <form
                className="inline-panel"
                style={{ margin: '0 19px 16px' }}
                onSubmit={handleHandoverSubmit}
              >
                <h3>Bàn giao vận chuyển đơn #{handoverOrderId}</h3>
                <div className="form-grid" style={{ marginTop: 10 }}>
                  <Field
                    label="Đơn vị vận chuyển"
                    value={carrierInput}
                    onChange={setCarrierInput}
                  />
                  <Field
                    label="Mã vận đơn"
                    placeholder="VD: GHN998877"
                    value={trackingInput}
                    onChange={setTrackingInput}
                  />
                </div>
                <div className="inline-actions" style={{ marginTop: 12 }}>
                  <Button disabled={busy} type="submit">
                    Xác nhận bàn giao
                  </Button>
                  <Button variant="secondary" onClick={() => setHandoverOrderId(null)}>
                    Đóng
                  </Button>
                </div>
              </form>
            )}

            {failOrderId && (
              <form
                className="inline-panel"
                style={{ margin: '0 19px 16px' }}
                onSubmit={(e) => {
                  e.preventDefault()
                  void run(async () => {
                    await updateOrderStatus(failOrderId, 'GIAO_THAT_BAI', failReason)
                    setFailOrderId(null)
                  }, `Đã cập nhật đơn #${failOrderId} giao thất bại`)
                }}
              >
                <h3>Ghi nhận giao thất bại đơn #{failOrderId}</h3>
                <div style={{ marginTop: 10 }}>
                  <Field label="Lý do giao thất bại" value={failReason} onChange={setFailReason} />
                </div>
                <div className="inline-actions" style={{ marginTop: 12 }}>
                  <Button disabled={busy} type="submit">
                    Xác nhận giao thất bại
                  </Button>
                  <Button variant="secondary" onClick={() => setFailOrderId(null)}>
                    Đóng
                  </Button>
                </div>
              </form>
            )}

            <div className="table-scroll">
              <table>
                <thead>
                  <tr>
                    <th>Mã đơn</th>
                    <th>Người nhận</th>
                    <th>Tổng tiền</th>
                    <th>Thanh toán</th>
                    <th>Trạng thái đơn</th>
                    <th>Thao tác</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredOrders.map((o) => {
                    const canConfirmPay =
                      o.trangThaiThanhToan === 'CHO_THANH_TOAN' &&
                      o.trangThaiDon !== 'DA_HUY' &&
                      o.trangThaiDon !== 'GIAO_THAT_BAI'
                    const canRefund = o.trangThaiThanhToan === 'DANG_HOAN_TIEN'
                    return (
                      <tr key={o.maDonHang}>
                        <td>
                          <strong>#{o.maDonHang}</strong>
                          {o.maCodeGiamGia && (
                            <span className="badge badge-info">{o.maCodeGiamGia}</span>
                          )}
                        </td>
                        <td>
                          <strong>{o.tenNguoiNhan}</strong> · {o.soDienThoaiNhan}
                        </td>
                        <td>
                          <strong>{money(o.tongThanhToan)}</strong>
                        </td>
                        <td>
                          <span>
                            {o.phuongThucThanhToan} ·{' '}
                            {paymentStatusNames[o.trangThaiThanhToan || ''] ||
                              o.trangThaiThanhToan}
                          </span>
                        </td>
                        <td>
                          <span
                            className={`status-select status-${(statusNames[o.trangThaiDon] || '').toLowerCase().replace(/\s+/g, '-')}`}
                          >
                            {statusNames[o.trangThaiDon] || o.trangThaiDon}
                          </span>
                        </td>
                        <td>
                          <div className="inline-actions">
                            {o.trangThaiDon === 'CHO_XAC_NHAN' && (
                              <Button
                                disabled={busy}
                                onClick={() =>
                                  void run(
                                    () => updateOrderStatus(o.maDonHang, 'DANG_DONG_GOI'),
                                    `Đơn #${o.maDonHang} đang đóng gói`
                                  )
                                }
                              >
                                Đóng gói
                              </Button>
                            )}
                            {o.trangThaiDon === 'DANG_DONG_GOI' && (
                              <Button
                                disabled={busy}
                                onClick={() => {
                                  setHandoverOrderId(o.maDonHang)
                                  setTrackingInput(`VH${Date.now().toString().slice(-6)}`)
                                }}
                              >
                                Bàn giao vận chuyển
                              </Button>
                            )}
                            {o.trangThaiDon === 'DANG_GIAO' && (
                              <>
                                <Button
                                  disabled={busy}
                                  onClick={() =>
                                    void run(
                                      () => updateOrderStatus(o.maDonHang, 'DA_GIAO'),
                                      `Đơn #${o.maDonHang} đã giao thành công`
                                    )
                                  }
                                >
                                  Đã giao
                                </Button>
                                <Button
                                  disabled={busy}
                                  variant="secondary"
                                  onClick={() => setFailOrderId(o.maDonHang)}
                                >
                                  Giao thất bại
                                </Button>
                              </>
                            )}
                            {canConfirmPay && (
                              <Button
                                disabled={busy}
                                variant="secondary"
                                onClick={() =>
                                  void run(
                                    () =>
                                      confirmOrderPayment(
                                        o.maDonHang,
                                        `PAY-${o.maDonHang}`
                                      ),
                                    `Đã xác nhận thanh toán đơn #${o.maDonHang}`
                                  )
                                }
                              >
                                Xác nhận thanh toán
                              </Button>
                            )}
                            {canRefund && (
                              <Button
                                disabled={busy}
                                variant="secondary"
                                onClick={() =>
                                  void run(
                                    () =>
                                      refundOrderPayment(
                                        o.maDonHang,
                                        `REF-${o.maDonHang}`
                                      ),
                                    `Đã hoàn tiền đơn #${o.maDonHang}`
                                  )
                                }
                              >
                                Hoàn tiền
                              </Button>
                            )}
                            {['CHO_XAC_NHAN', 'DANG_DONG_GOI'].includes(o.trangThaiDon) && (
                              <Button
                                disabled={busy}
                                variant="secondary"
                                onClick={() =>
                                  void run(
                                    () =>
                                      cancelOrder(o.maDonHang, 'Quản trị viên hủy đơn', true),
                                    `Đã hủy đơn #${o.maDonHang}`
                                  )
                                }
                              >
                                Hủy đơn
                              </Button>
                            )}
                          </div>
                        </td>
                      </tr>
                    )
                  })}
                </tbody>
              </table>
              {filteredOrders.length === 0 && (
                <p style={{ padding: 20 }}>Chưa có đơn hàng phù hợp.</p>
              )}
            </div>
          </section>
        )}

        {section === 'Vận chuyển' && (
          <section className="management-card" style={{ marginTop: 16 }}>
            <div className="card-heading">
              <h2>Điều phối & Bàn giao vận chuyển ({shippingOrders.length})</h2>
              <Button variant="secondary" onClick={() => setSection('Đơn hàng')}>
                Xem tất cả đơn hàng
              </Button>
            </div>
            {handoverOrderId && (
              <form className="inline-panel" onSubmit={handleHandoverSubmit}>
                <h3>Bàn giao vận chuyển đơn #{handoverOrderId}</h3>
                <div className="form-grid" style={{ marginTop: 10 }}>
                  <Field
                    label="Đơn vị vận chuyển"
                    value={carrierInput}
                    onChange={setCarrierInput}
                  />
                  <Field
                    label="Mã vận đơn"
                    value={trackingInput}
                    onChange={setTrackingInput}
                  />
                </div>
                <div className="inline-actions" style={{ marginTop: 12 }}>
                  <Button disabled={busy} type="submit">
                    Xác nhận bàn giao
                  </Button>
                  <Button variant="secondary" onClick={() => setHandoverOrderId(null)}>
                    Đóng
                  </Button>
                </div>
              </form>
            )}
            {shippingOrders.map((o) => (
              <div className="shipping-admin" key={o.maDonHang}>
                <span className="shipping-icon">
                  <Icon name="truck" size={18} />
                </span>
                <span>
                  <strong>
                    Đơn #{o.maDonHang} — {o.tenNguoiNhan}
                  </strong>
                  <small>
                    {o.diaChiNhan} · Đặt lúc {formatDateTime(o.ngayDat)}
                  </small>
                </span>
                <b>{money(o.tongThanhToan)}</b>
                <em>{statusNames[o.trangThaiDon] || o.trangThaiDon}</em>
                <div className="inline-actions">
                  {o.trangThaiDon === 'DANG_DONG_GOI' && (
                    <Button
                      disabled={busy}
                      onClick={() => {
                        setHandoverOrderId(o.maDonHang)
                        setTrackingInput(`VH${Date.now().toString().slice(-6)}`)
                      }}
                    >
                      Bàn giao
                    </Button>
                  )}
                  {o.trangThaiDon === 'DANG_GIAO' && (
                    <Button
                      disabled={busy}
                      onClick={() =>
                        void run(
                          () => updateOrderStatus(o.maDonHang, 'DA_GIAO'),
                          `Đơn #${o.maDonHang} đã giao`
                        )
                      }
                    >
                      Hoàn tất giao
                    </Button>
                  )}
                </div>
              </div>
            ))}
            {shippingOrders.length === 0 && (
              <p style={{ marginTop: 16 }}>Chưa có đơn hàng ở bước đóng gói hoặc giao hàng.</p>
            )}
          </section>
        )}

        {section === 'Mã giảm giá' && (
          <section className="management-card" style={{ marginTop: 16 }}>
            <div className="card-heading">
              <h2>Quản lý mã giảm giá ({coupons.length})</h2>
              <Button onClick={() => setCouponDraft({ ...emptyCoupon })}>
                Tạo mã giảm giá
              </Button>
            </div>

            {couponDraft && (
              <form className="inline-panel" onSubmit={handleCouponSubmit}>
                <h3>
                  {couponDraft.id
                    ? `Chỉnh sửa mã ${couponDraft.code}`
                    : 'Tạo mã giảm giá mới'}
                </h3>
                <div className="form-grid" style={{ marginTop: 12 }}>
                  <Field
                    label="Mã code (VD: TECH15)"
                    value={couponDraft.code}
                    onChange={(v) => setCouponDraft({ ...couponDraft, code: v.toUpperCase() })}
                  />
                  <SelectField
                    label="Loại giảm"
                    value={couponDraft.type}
                    onChange={(v) =>
                      setCouponDraft({
                        ...couponDraft,
                        type: v as 'PHAN_TRAM' | 'SO_TIEN',
                      })
                    }
                  >
                    <option value="SO_TIEN">Số tiền cố định (₫)</option>
                    <option value="PHAN_TRAM">Phần trăm (%)</option>
                  </SelectField>
                  <Field
                    label="Giá trị giảm"
                    type="number"
                    value={String(couponDraft.value)}
                    onChange={(v) => setCouponDraft({ ...couponDraft, value: Number(v) })}
                  />
                  <Field
                    label="Đơn tối thiểu (₫)"
                    type="number"
                    value={String(couponDraft.minOrder)}
                    onChange={(v) =>
                      setCouponDraft({ ...couponDraft, minOrder: Number(v) })
                    }
                  />
                  <Field
                    label="Số lượt tối đa (để trống = không giới hạn)"
                    type="number"
                    value={couponDraft.maxUses}
                    onChange={(v) => setCouponDraft({ ...couponDraft, maxUses: v })}
                  />
                  <Field
                    label="Ngày bắt đầu"
                    type="date"
                    value={couponDraft.startDate}
                    onChange={(v) => setCouponDraft({ ...couponDraft, startDate: v })}
                  />
                  <Field
                    label="Ngày kết thúc"
                    type="date"
                    value={couponDraft.endDate}
                    onChange={(v) => setCouponDraft({ ...couponDraft, endDate: v })}
                  />
                </div>
                <div className="inline-actions" style={{ marginTop: 14 }}>
                  <Button disabled={busy} type="submit">
                    Lưu mã giảm giá
                  </Button>
                  <Button variant="secondary" onClick={() => setCouponDraft(null)}>
                    Hủy
                  </Button>
                </div>
              </form>
            )}

            <div className="orders-table-card" style={{ marginTop: 16 }}>
              <div className="table-scroll">
                <table>
                  <thead>
                    <tr>
                      <th>Mã Code</th>
                      <th>Loại</th>
                      <th>Mức giảm</th>
                      <th>Đơn tối thiểu</th>
                      <th>Lượt dùng</th>
                      <th>Hạn dùng</th>
                      <th>Thao tác</th>
                    </tr>
                  </thead>
                  <tbody>
                    {coupons.map((c) => (
                      <tr key={c.maGiamGia}>
                        <td>
                          <strong>{c.maCode}</strong>
                        </td>
                        <td>{c.loaiGiam === 'PHAN_TRAM' ? 'Phần trăm' : 'Số tiền'}</td>
                        <td>
                          <strong>
                            {c.loaiGiam === 'PHAN_TRAM'
                              ? `${c.giaTriGiam}%`
                              : money(c.giaTriGiam)}
                          </strong>
                        </td>
                        <td>{money(c.donHangToiThieu)}</td>
                        <td>
                          {c.soLuotDaDung} / {c.soLuotToiDa ?? '∞'}
                        </td>
                        <td>
                          {formatDateTime(c.ngayBatDau).slice(0, 10)} →{' '}
                          {formatDateTime(c.ngayKetThuc).slice(0, 10)}
                        </td>
                        <td>
                          <div className="inline-actions">
                            <Button
                              variant="secondary"
                              onClick={() =>
                                setCouponDraft({
                                  id: c.maGiamGia,
                                  code: c.maCode,
                                  type: c.loaiGiam,
                                  value: c.giaTriGiam,
                                  minOrder: c.donHangToiThieu,
                                  maxUses: c.soLuotToiDa ? String(c.soLuotToiDa) : '',
                                  startDate: (c.ngayBatDau || today).slice(0, 10),
                                  endDate: (c.ngayKetThuc || nextMonth).slice(0, 10),
                                })
                              }
                            >
                              Sửa
                            </Button>
                            <Button
                              disabled={busy}
                              variant="secondary"
                              onClick={() => {
                                if (window.confirm(`Xóa mã giảm giá ${c.maCode}?`)) {
                                  void run(
                                    () => deleteCoupon(c.maGiamGia),
                                    `Đã xóa mã ${c.maCode}`
                                  )
                                }
                              }}
                            >
                              Xóa
                            </Button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          </section>
        )}
      </main>
    </div>
  )
}

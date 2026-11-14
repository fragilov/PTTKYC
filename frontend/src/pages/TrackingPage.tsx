import { useCallback, useEffect, useState } from 'react'
import { cancelOrder, fetchOrders, fetchTracking } from '../api/orderApi'
import { Button } from '../components/common/Button'
import { Icon } from '../components/common/Icon'
import { BankTransfer } from '../components/payment/BankTransfer'
import { useCatalog } from '../context/CatalogContext'
import type { Order, TrackingPayload } from '../types'
import {
  formatDateTime,
  money,
  paymentStatusNames,
  paymentTransactionStatusNames,
  statusNames,
} from '../utils/format'

export function TrackingPage({
  latest,
  onOrderChanged,
  notify,
}: {
  latest: number
  onOrderChanged: () => Promise<void>
  notify: (msg: string) => void
}) {
  const { products } = useCatalog()
  const [code, setCode] = useState(String(latest || ''))
  const [data, setData] = useState<TrackingPayload | null>(null)
  const [error, setError] = useState('')
  const [orders, setOrders] = useState<Order[]>([])
  const [cancelOpen, setCancelOpen] = useState(false)
  const [cancelReason, setCancelReason] = useState('')
  const [cancelling, setCancelling] = useState(false)

  const lookup = useCallback(async (id: string) => {
    const trimmed = id.trim()
    if (!trimmed) return
    setError('')
    setCancelOpen(false)
    try {
      const payload = await fetchTracking(trimmed)
      setData(payload)
    } catch (e) {
      setData(null)
      setError((e as Error).message)
    }
  }, [])

  const loadOrders = useCallback(async () => {
    try {
      const list = await fetchOrders(false)
      setOrders(list)
      return list
    } catch (e) {
      setError((e as Error).message)
      return []
    }
  }, [])

  useEffect(() => {
    void loadOrders().then((list) => {
      const target = latest || list[0]?.maDonHang
      if (target) {
        setCode(String(target))
        void lookup(String(target))
      }
    })
  }, [latest, loadOrders, lookup])

  const handleCancelOrder = async () => {
    if (!data?.order) return
    setCancelling(true)
    setError('')
    try {
      await cancelOrder(data.order.maDonHang, cancelReason.trim() || 'Khách hàng yêu cầu hủy đơn')
      await Promise.all([
        lookup(String(data.order.maDonHang)),
        loadOrders(),
        onOrderChanged(),
      ])
      setCancelOpen(false)
      setCancelReason('')
      notify(`Đã hủy đơn hàng #${data.order.maDonHang} và hoàn lại tồn kho`)
    } catch (e) {
      setError((e as Error).message)
    } finally {
      setCancelling(false)
    }
  }

  const order = data?.order
  const canCancel =
    order && (order.trangThaiDon === 'CHO_XAC_NHAN' || order.trangThaiDon === 'DANG_DONG_GOI')

  return (
    <main className="tracking-page">
      <section className="tracking-hero">
        <div className="shell">
          <div className="eyebrow">Cập nhật trạng thái theo thời gian thực</div>
          <h1>Theo dõi đơn hàng</h1>
          <p>Tra cứu hành trình giao hàng, trạng thái thanh toán và chi tiết sản phẩm đã đặt.</p>
          <div className="track-search">
            <Icon name="search" size={18} />
            <input
              aria-label="Mã đơn hàng"
              value={code}
              onChange={(e) => setCode(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === 'Enter') void lookup(code)
              }}
              placeholder="Nhập mã đơn dạng số (VD: 1, 2...)"
            />
            <Button onClick={() => void lookup(code)}>Tra cứu</Button>
          </div>
        </div>
      </section>

      <div className="shell tracking-content">
        {orders.length > 0 && (
          <div className="quick-links tracking-order-pills">
            {orders.map((o) => (
              <Button
                key={o.maDonHang}
                variant={order?.maDonHang === o.maDonHang ? 'primary' : 'secondary'}
                onClick={() => {
                  setCode(String(o.maDonHang))
                  void lookup(String(o.maDonHang))
                }}
              >
                #{o.maDonHang} · {statusNames[o.trangThaiDon] || o.trangThaiDon}
              </Button>
            ))}
          </div>
        )}

        {error && (
          <p className="inline-error" role="alert" style={{ marginTop: 14 }}>
            {error}
          </p>
        )}

        {data && order && (
          <>
            <div className="tracking-summary" style={{ marginTop: 18 }}>
              <div>
                <span className="status-dot" />
                <span>
                  <small>Đơn hàng #{order.maDonHang}</small>
                  <strong>{statusNames[order.trangThaiDon] || order.trangThaiDon}</strong>
                </span>
              </div>
              <div>
                <small>Thanh toán</small>
                <strong>
                  {paymentStatusNames[order.trangThaiThanhToan || ''] ||
                    order.trangThaiThanhToan ||
                    'Chờ thanh toán'}
                </strong>
              </div>
              <div>
                <small>Phương thức</small>
                <strong>
                  {order.phuongThucThanhToan === 'CHUYEN_KHOAN'
                    ? 'Chuyển khoản QR'
                    : 'Thu hộ COD'}
                </strong>
              </div>
              <div>
                <small>Vận chuyển</small>
                <strong>
                  {data.shipping
                    ? `${data.shipping.donViVanChuyen} · ${data.shipping.maVanDon}`
                    : 'Chưa bàn giao'}
                </strong>
              </div>
              <div>
                <small>Tổng thanh toán</small>
                <strong style={{ color: '#1358d3' }}>{money(order.tongThanhToan)}</strong>
              </div>
            </div>

            <div className="tracking-layout">
              <section className="timeline-card">
                <div className="card-heading">
                  <div>
                    <h2>Hành trình đơn hàng</h2>
                    <p>Đặt lúc {formatDateTime(order.ngayDat)}</p>
                  </div>
                  <span className="live">
                    <i /> TRỰC TIẾP
                  </span>
                </div>
                <div className="timeline">
                  {data.history.map((h, i) => {
                    const isLatest = i === data.history.length - 1
                    return (
                      <div className={isLatest ? 'event active' : 'event'} key={h.maLichSu || i}>
                        <span>
                          <Icon name={isLatest ? 'truck' : 'check'} size={15} />
                        </span>
                        <div>
                          <strong>{statusNames[h.trangThaiMoi] || h.trangThaiMoi}</strong>
                          <small>{formatDateTime(h.thoiDiem)}</small>
                          <p>
                            {h.ghiChu || 'Cập nhật trạng thái'}
                            {h.tenNguoiThucHien ? ` · ${h.tenNguoiThucHien}` : ''}
                          </p>
                        </div>
                      </div>
                    )
                  })}
                </div>
              </section>

              <aside className="delivery-card">
                <h3>Thông tin giao hàng</h3>
                <div>
                  <span>Người nhận</span>
                  <strong>{order.tenNguoiNhan}</strong>
                </div>
                <div>
                  <span>Điện thoại</span>
                  <strong>{order.soDienThoaiNhan}</strong>
                </div>
                <div>
                  <span>Địa chỉ</span>
                  <strong>{order.diaChiNhan}</strong>
                </div>
                {order.ghiChu && (
                  <div>
                    <span>Ghi chú</span>
                    <strong>{order.ghiChu}</strong>
                  </div>
                )}
                <hr />
                <h3>Sản phẩm ({order.danhSachChiTiet.length})</h3>
                {order.danhSachChiTiet.map((line) => {
                  const catalogMatch = products.find((p) => p.id === line.maLinhKien)
                  const itemName =
                    line.tenLinhKien || catalogMatch?.name || `Linh kiện #${line.maLinhKien}`
                  return (
                    <div key={line.maLinhKien}>
                      <span>
                        {itemName} × {line.soLuong}
                      </span>
                      <strong>{money(line.donGia * line.soLuong)}</strong>
                    </div>
                  )
                })}
                <hr />
                <div>
                  <span>Tiền hàng</span>
                  <strong>{money(order.tongTienHang ?? order.tongThanhToan)}</strong>
                </div>
                <div>
                  <span>Phí vận chuyển</span>
                  <strong>{money(order.phiVanChuyen ?? 0)}</strong>
                </div>
                {(order.soTienGiam ?? 0) > 0 && (
                  <div>
                    <span>
                      Giảm giá{order.maCodeGiamGia ? ` (${order.maCodeGiamGia})` : ''}
                    </span>
                    <strong style={{ color: '#16845b' }}>-{money(order.soTienGiam || 0)}</strong>
                  </div>
                )}
                <div>
                  <span>Tổng thanh toán</span>
                  <strong style={{ color: '#1358d3' }}>{money(order.tongThanhToan)}</strong>
                </div>

                {data.payments && data.payments.length > 0 && (
                  <>
                    <hr />
                    <h3>Giao dịch thanh toán</h3>
                    {data.payments.map((tx) => (
                      <div key={tx.maThanhToan}>
                        <span>
                          {tx.loaiGiaoDich === 'HOAN' ? 'Hoàn tiền' : 'Thu tiền'} ({tx.phuongThuc})
                        </span>
                        <strong>
                          {paymentTransactionStatusNames[tx.trangThai] || tx.trangThai}
                          {tx.maThamChieu ? ` · ${tx.maThamChieu}` : ''}
                        </strong>
                      </div>
                    ))}
                  </>
                )}

                {order.phuongThucThanhToan === 'CHUYEN_KHOAN' &&
                  order.trangThaiDon !== 'DA_HUY' &&
                  order.trangThaiThanhToan !== 'DA_THANH_TOAN' && (
                    <BankTransfer amount={order.tongThanhToan} orderId={order.maDonHang} />
                  )}

                {canCancel && !cancelOpen && (
                  <Button
                    variant="secondary"
                    className="danger-outline-btn"
                    onClick={() => setCancelOpen(true)}
                  >
                    Hủy đơn hàng
                  </Button>
                )}

                {canCancel && cancelOpen && (
                  <div className="inline-panel">
                    <label className="field">
                      <span>Lý do hủy đơn</span>
                      <input
                        value={cancelReason}
                        onChange={(e) => setCancelReason(e.target.value)}
                        placeholder="Ví dụ: Đổi nhu cầu, đặt nhầm linh kiện..."
                      />
                    </label>
                    <div className="inline-actions">
                      <Button
                        variant="secondary"
                        onClick={() => setCancelOpen(false)}
                        disabled={cancelling}
                      >
                        Đóng
                      </Button>
                      <Button onClick={() => void handleCancelOrder()} disabled={cancelling}>
                        {cancelling ? 'Đang hủy...' : 'Xác nhận hủy'}
                      </Button>
                    </div>
                  </div>
                )}

                <div className="help-box">
                  <Icon name="shield" size={18} />
                  <span>
                    <strong>Hỗ trợ kỹ thuật VoltHub</strong>
                    <small>Hotline 1900 6868 · support@volthub.vn</small>
                  </span>
                </div>
              </aside>
            </div>
          </>
        )}
      </div>
    </main>
  )
}

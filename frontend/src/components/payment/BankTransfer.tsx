import { money } from '../../utils/format'

export function BankTransfer({ amount, orderId }: { amount: number; orderId?: number }) {
  return (
    <section className="bank-transfer" aria-label="Thông tin chuyển khoản">
      <h3>Quét QR chuyển khoản</h3>
      <img
        src={import.meta.env.BASE_URL + 'images/bank-transfer-qr.png'}
        alt="QR chuyển khoản ACB, LE TRUNG HIEU, số tài khoản 17940701"
        width="720"
        height="959"
      />
      <dl>
        <div>
          <dt>Ngân hàng</dt>
          <dd>ACB</dd>
        </div>
        <div>
          <dt>Chủ tài khoản</dt>
          <dd>LE TRUNG HIEU</dd>
        </div>
        <div>
          <dt>Số tài khoản</dt>
          <dd>17940701</dd>
        </div>
        <div>
          <dt>Số tiền</dt>
          <dd>{money(amount)}</dd>
        </div>
        {orderId && (
          <div>
            <dt>Nội dung</dt>
            <dd>VOLTHUB {orderId}</dd>
          </div>
        )}
      </dl>
      <p>
        {orderId
          ? 'Nhập đúng số tiền và nội dung khi chuyển khoản. Cửa hàng xác nhận thanh toán thủ công.'
          : 'Xác nhận đặt hàng để nhận mã đơn dùng làm nội dung chuyển khoản. Mã QR chưa điền sẵn số tiền.'}
      </p>
    </section>
  )
}

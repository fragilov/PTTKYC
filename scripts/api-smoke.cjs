(async () => {
  const fs = require('fs')
  const os = require('os')
  const path = require('path')
  const { spawn, execFileSync } = require('child_process')
  const assert = require('assert/strict')

  const root = path.resolve(__dirname, '..')
  const dir = fs.mkdtempSync(path.join(os.tmpdir(), 'volthub-api-'))
  const javaExe = path.join(execFileSync('where.exe', ['javac'], { encoding: 'utf8' }).trim().split(/\r?\n/)[0], '..', 'java.exe')
  const proc = spawn(javaExe, ['-Dfile.encoding=UTF-8', '-cp', path.join(root, 'bin') + ';' + path.join(root, 'lib/*'), 'com.linhkien.api.ApiServer', '18080'], { cwd: dir })
  let log = ''
  proc.stdout.on('data', (x) => (log += x))
  proc.stderr.on('data', (x) => (log += x))

  async function api(p, data, method = 'POST') {
    const r = await fetch('http://127.0.0.1:18080/api' + p, data ? { method, body: new URLSearchParams(data) } : undefined)
    const b = await r.json()
    if (!r.ok) throw Error(JSON.stringify(b))
    return b
  }

  try {
    for (let i = 0; i < 50; i++) {
      try {
        await api('/products')
        break
      } catch {
        await new Promise((r) => setTimeout(r, 200))
      }
    }

    // 1. Products & Categories CRUD + Extended Specs + Low Stock + Inventory History
    const products = await api('/products')
    const p = products[0]
    assert(products.length > 0)
    const categories = await api('/categories')
    assert(categories.length > 0)

    await api('/products', { sku: 'TEST-API', name: 'Linh kiện API', category: categories[0].maDanhMuc, price: 10000, stock: 5 })
    const created = (await api('/products')).find((x) => x.maSKU === 'TEST-API')
    assert(created)
    await api('/products', { id: created.maLinhKien, sku: 'TEST-API', name: 'Đã sửa', category: categories[0].maDanhMuc, price: 12000, stock: 6 })
    assert.equal((await api('/products')).find((x) => x.maSKU === 'TEST-API').tenLinhKien, 'Đã sửa')

    await api('/products/specs', { id: created.maLinhKien, specs: 'Điện áp|5|V\nChuẩn giao tiếp|I2C|' })
    const detailWithSpecs = await api('/products?id=' + created.maLinhKien)
    assert.equal(detailWithSpecs.danhSachThongSo.length, 2)

    await api('/inventory/adjust', { id: created.maLinhKien, delta: 4, reason: 'Nhập test API' })
    await api('/inventory/adjust', { id: created.maLinhKien, delta: -2, reason: 'Kiểm kê test API' })
    const invHist = await api('/inventory/history?id=' + created.maLinhKien)
    assert(invHist.length >= 2)
    assert.equal(invHist[0].loaiBienDong, 'DIEU_CHINH')
    assert.equal(invHist[1].loaiBienDong, 'NHAP')

    const lowStock = await api('/products/low-stock')
    assert(Array.isArray(lowStock))
    await api('/products', { id: created.maLinhKien }, 'DELETE')

    // 2. Cart & Standard Order Checkout + Idempotency + Shipping + Delivery
    const basket = await api('/cart')
    for (const l of basket.danhSachChiTiet) {
      await api('/cart', { id: l.maLinhKien, quantity: 0 })
    }
    await api('/cart', { id: p.maLinhKien, quantity: 1, action: 'add' })
    await api('/cart', { id: p.maLinhKien, quantity: 2 })

    const request = {
      name: 'Khách API',
      phone: '0901234567',
      address: '12 Nguyễn Huệ, TP.HCM',
      payment: 'COD',
      requestId: 'api-smoke-1',
      shipping: 'express',
      note: 'Giao giờ hành chính',
    }
    const order = await api('/orders', request)
    assert.equal(order.tongThanhToan, p.giaBan * 2 + 49000)
    assert.equal((await api('/cart')).danhSachChiTiet.length, 0)
    assert.equal((await api('/products')).find((x) => x.maLinhKien === p.maLinhKien).soLuongTon, p.soLuongTon - 2)
    assert.equal((await api('/orders', request)).maDonHang, order.maDonHang)

    await api('/status', { id: order.maDonHang, status: 'DANG_DONG_GOI' })
    await api('/shipping', { id: order.maDonHang, carrier: 'GHN', tracking: 'API-001' })
    await api('/status', { id: order.maDonHang, status: 'DA_GIAO' })
    const tracking = await api('/tracking?id=' + order.maDonHang)
    assert.equal(tracking.order.trangThaiDon, 'DA_GIAO')
    assert.equal(tracking.order.trangThaiThanhToan, 'DA_THANH_TOAN')
    assert.equal(tracking.shipping.maVanDon, 'API-001')
    assert(tracking.history.length >= 4)

    // 3. Cancellation & Restock (backward-compatible /status DA_HUY)
    await api('/cart', { id: p.maLinhKien, quantity: 1, action: 'add' })
    const cancelled = await api('/orders', { ...request, requestId: 'api-smoke-2', shipping: 'standard' })
    await api('/status', { id: cancelled.maDonHang, status: 'DA_HUY' })
    assert.equal((await api('/products')).find((x) => x.maLinhKien === p.maLinhKien).soLuongTon, p.soLuongTon - 2)

    // 4. Coupons + Bank Transfer Payment Confirmation + Customer Cancel (/api/cancel) + Refund
    const tempCoupon = await api('/coupons', {
      code: 'SMOKETMP',
      type: 'SO_TIEN',
      value: 5000,
      minOrder: 0,
      maxUses: 1,
      startDate: '2025-01-01',
      endDate: '2029-12-31',
    })
    assert.equal(tempCoupon.maCode, 'SMOKETMP')
    await api('/coupons', { id: tempCoupon.maGiamGia }, 'DELETE')

    const couponList = await api('/coupons')
    assert(couponList.some((c) => c.maCode === 'SALE10'))
    const validated = await api('/coupons/validate', { code: 'SALE10', subtotal: p.giaBan * 2 })
    assert.equal(validated.valid, true)
    assert.equal(validated.discountAmount, Math.round(p.giaBan * 2 * 0.1))

    await api('/cart', { id: p.maLinhKien, quantity: 2, action: 'add' })
    const paidOrder = await api('/orders', {
      name: 'Khách Chuyển Khoản',
      phone: '0901234567',
      address: '15 Lê Lợi, Q.1, TP.HCM',
      payment: 'CHUYEN_KHOAN',
      requestId: 'api-smoke-3',
      shipping: 'standard',
      couponCode: 'SALE10',
    })
    assert.equal(paidOrder.soTienGiam, validated.discountAmount)
    assert.equal(paidOrder.tongThanhToan, p.giaBan * 2 + 25000 - validated.discountAmount)

    const confirmRes = await api('/payments/confirm', { orderId: paidOrder.maDonHang, reference: 'ACB-SMOKE-1' })
    assert.equal(confirmRes.payment.trangThai, 'THANH_CONG')

    const cancelRes = await api('/cancel', { id: paidOrder.maDonHang, reason: 'Khách hủy test API' })
    assert.equal(cancelRes.order.trangThaiDon, 'DA_HUY')
    assert.equal(cancelRes.order.trangThaiThanhToan, 'DANG_HOAN_TIEN')

    const refundRes = await api('/payments/refund', { orderId: paidOrder.maDonHang, reference: 'REF-SMOKE-1' })
    assert.equal(refundRes.payment.loaiGiaoDich, 'HOAN')
    const refundedTrack = await api('/tracking?id=' + paidOrder.maDonHang)
    assert.equal(refundedTrack.order.trangThaiThanhToan, 'DA_HOAN_TIEN')

    // 5. Address Book Sync
    const savedAddr = await api('/addresses', {
      name: 'Nguyễn Văn A',
      phone: '0911223344',
      street: '100 Pasteur',
      ward: 'Bến Nghé',
      city: 'TP.HCM',
      isDefault: 'false',
    })
    assert(savedAddr.maDiaChi > 0)
    const addrList = await api('/addresses')
    assert(addrList.some((a) => a.maDiaChi === savedAddr.maDiaChi))
    await api('/addresses', { id: savedAddr.maDiaChi }, 'DELETE')

    console.log(
      'PASS: product CRUD, specs, inventory history, cart update, checkout + shipping fee + coupon, stock deduction, idempotency, shipment tracking, delivery, payment confirm, cancellation + restock + refund, addresses. Isolated database: ' +
        dir
    )
  } catch (e) {
    console.error(log)
    throw e
  } finally {
    proc.kill()
  }
})().catch((e) => {
  console.error(e)
  process.exitCode = 1
})

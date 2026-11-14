import { useCallback, useEffect, useState } from 'react'
import { fetchAddresses } from './api/accountApi'
import { addToCart, fetchCart, updateCartQty } from './api/cartApi'
import { createOrder } from './api/orderApi'
import { fetchProducts } from './api/productApi'
import { Button } from './components/common/Button'
import { Toast } from './components/common/Toast'
import { Header } from './components/layout/Header'
import { CatalogContext } from './context/CatalogContext'
import { AccountPage } from './pages/AccountPage'
import { AdminPage } from './pages/AdminPage'
import { CartPage } from './pages/CartPage'
import { CheckoutPage } from './pages/CheckoutPage'
import { DetailPage } from './pages/DetailPage'
import { ShopPage } from './pages/ShopPage'
import { TrackingPage } from './pages/TrackingPage'
import type { CartMap, CouponValidation, Page, Product, Profile } from './types'
import {
  addressRecordToProfile,
  formatOrderAddress,
  mapApiProduct,
} from './utils/format'
import {
  loadFavorites,
  loadProfile,
  saveFavoritesToStorage,
  saveProfileToStorage,
  toggleFavoriteId,
} from './utils/profileStorage'

export default function App() {
  const [products, setProducts] = useState<Product[]>([])
  const [favorites, setFavorites] = useState<number[]>(loadFavorites)
  const [ready, setReady] = useState(false)
  const [error, setError] = useState('')
  const [latest, setLatest] = useState(0)
  const [busy, setBusy] = useState(false)
  const [page, setPage] = useState<Page>('shop')
  const [selected, setSelected] = useState<Product | null>(null)
  const [cart, setCart] = useState<CartMap>({})
  const [appliedCoupon, setAppliedCoupon] = useState<CouponValidation | null>(null)
  const [profile, setProfile] = useState<Profile>(loadProfile)
  const [toast, setToast] = useState('')

  const cartCount = Object.values(cart).reduce((a, b) => a + b, 0)

  const navigate = (p: Page) => {
    setPage(p)
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  const notify = useCallback((text: string) => {
    setToast(text)
    window.setTimeout(() => setToast(''), 2600)
  }, [])

  const refresh = useCallback(async () => {
    const [catalog, basket] = await Promise.all([fetchProducts(), fetchCart()])
    setProducts(catalog.map(mapApiProduct))
    setCart(
      Object.fromEntries(
        (basket.danhSachChiTiet || []).map((l: any) => [l.maLinhKien, l.soLuong])
      )
    )
  }, [])

  useEffect(() => {
    Promise.all([refresh(), fetchAddresses().catch(() => [])])
      .then(([, addresses]) => {
        const defaultAddress = Array.isArray(addresses)
          ? addresses.find((a) => a.laMacDinh) || addresses[0]
          : undefined
        if (defaultAddress && !window.localStorage.getItem('volthub-default-address')) {
          setProfile((prev) => addressRecordToProfile(defaultAddress, prev))
        }
        setReady(true)
      })
      .catch((e) => setError((e as Error).message))
  }, [refresh])

  const mutate = async (action: () => Promise<unknown>) => {
    if (busy) return
    setBusy(true)
    setError('')
    try {
      await action()
      await refresh()
    } catch (e) {
      setError((e as Error).message)
    } finally {
      setBusy(false)
    }
  }

  const toggleFavorite = (product: Product) => {
    const { next, added } = toggleFavoriteId(favorites, product.id)
    setFavorites(next)
    saveFavoritesToStorage(next)
    notify(added ? `Đã thêm ${product.name} vào yêu thích` : `Đã bỏ ${product.name} khỏi yêu thích`)
  }

  const isFavorite = (productId: number) => favorites.includes(productId)

  const add = (product: Product, qty = 1) => {
    void mutate(async () => {
      await addToCart(product.id, qty)
      notify('Đã thêm vào giỏ')
    })
  }

  const updateCart = (next: CartMap) => {
    void mutate(async () => {
      for (const id of new Set([...Object.keys(cart), ...Object.keys(next)])) {
        if (cart[+id] !== next[+id]) {
          await updateCartQty(+id, next[+id] || 0)
        }
      }
    })
  }

  const openProduct = (p: Product) => {
    setSelected(p)
    navigate('detail')
  }

  const complete = async (
    recipient: Profile,
    shipping: string,
    payment: string,
    requestId: string,
    note: string,
    couponCode?: string
  ) => {
    const order = await createOrder({
      name: recipient.fullName,
      phone: recipient.phone.replace(/\s/g, ''),
      address: formatOrderAddress(recipient),
      shipping,
      payment: payment === 'cod' ? 'COD' : 'CHUYEN_KHOAN',
      requestId,
      note,
      couponCode,
    })
    setLatest(order.maDonHang)
    setAppliedCoupon(null)
    await refresh()
    navigate('tracking')
    notify('Đặt hàng thành công — mã đơn ' + order.maDonHang)
  }

  const saveProfile = (nextProfile: Profile) => {
    setProfile(nextProfile)
    saveProfileToStorage(nextProfile)
    notify('Đã lưu địa chỉ nhận hàng mặc định')
  }

  const contextValue = { products, favorites, toggleFavorite, isFavorite }

  if (!ready) {
    return (
      <main className="shell" style={{ paddingBlock: 40 }}>
        <h1>VoltHub</h1>
        <p>{error || 'Đang tải dữ liệu từ backend…'}</p>
        <Button
          onClick={() => {
            setError('')
            refresh()
              .then(() => setReady(true))
              .catch((e) => setError((e as Error).message))
          }}
        >
          Thử lại
        </Button>
      </main>
    )
  }

  if (page === 'admin') {
    return (
      <CatalogContext.Provider value={contextValue}>
        <AdminPage navigate={navigate} refresh={refresh} notify={notify} />
        {toast && <Toast text={toast} />}
      </CatalogContext.Provider>
    )
  }

  return (
    <CatalogContext.Provider value={contextValue}>
      <div aria-busy={busy}>
        {error && (
          <p role="alert" className="shell inline-error">
            {error}
          </p>
        )}
        <Header
          page={page}
          navigate={navigate}
          cartCount={cartCount}
          profile={profile}
          onSupportClick={() =>
            notify('Hỗ trợ kỹ thuật VoltHub: 1900 6868 · support@volthub.vn')
          }
        />
        {page === 'shop' && <ShopPage openProduct={openProduct} add={add} />}
        {page === 'detail' && selected && (
          <DetailPage
            product={products.find((p) => p.id === selected.id) || selected}
            add={add}
            navigate={navigate}
          />
        )}
        {page === 'cart' && (
          <CartPage
            cart={cart}
            setCart={updateCart}
            navigate={navigate}
            appliedCoupon={appliedCoupon}
            setAppliedCoupon={setAppliedCoupon}
          />
        )}
        {page === 'checkout' && (
          <CheckoutPage
            cart={cart}
            navigate={navigate}
            complete={complete}
            profile={profile}
            appliedCoupon={appliedCoupon}
            setAppliedCoupon={setAppliedCoupon}
          />
        )}
        {page === 'tracking' && (
          <TrackingPage
            latest={latest}
            onOrderChanged={refresh}
            notify={notify}
          />
        )}
        {page === 'account' && (
          <AccountPage
            profile={profile}
            save={saveProfile}
            navigate={navigate}
            notify={notify}
          />
        )}
        {toast && <Toast text={toast} />}
      </div>
    </CatalogContext.Provider>
  )
}

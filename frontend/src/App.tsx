import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from 'react'

type Page = 'shop' | 'detail' | 'cart' | 'checkout' | 'tracking' | 'account' | 'admin'
type IconName =
  | 'search'
  | 'cart'
  | 'user'
  | 'chevron'
  | 'sliders'
  | 'grid'
  | 'list'
  | 'heart'
  | 'arrow'
  | 'truck'
  | 'shield'
  | 'box'
  | 'minus'
  | 'plus'
  | 'trash'
  | 'check'
  | 'clock'
  | 'package'
  | 'chart'
  | 'settings'
  | 'orders'
  | 'logout'
  | 'download'
  | 'menu'
  | 'close'

type Product = {
  id: number
  name: string
  sku: string
  category: string
  maker: string
  price: number
  stock: number
  accent: string
  label: string
  specs: string[]
  categoryId?: number
  status?: string
  description?: string
  image?: string
  datasheet?: string
}

type Profile = {
  fullName: string
  phone: string
  email: string
  city: string
  district: string
  ward: string
  street: string
}

const defaultProfile: Profile = {
  fullName: 'Nguyễn Hoài Bảo',
  phone: '0901 234 567',
  email: 'bao.nguyen@example.com',
  city: 'TP. Hồ Chí Minh',
  district: 'Quận 7',
  ward: 'Tân Phong',
  street: '12 Nguyễn Hữu Thọ',
}

function loadProfile(): Profile {
  try {
    const saved = window.localStorage.getItem('volthub-default-address')
    return saved ? { ...defaultProfile, ...JSON.parse(saved) } : defaultProfile
  } catch {
    return defaultProfile
  }
}

const Catalog = createContext<Product[]>([])
async function api(path: string, data?: Record<string, string | number>, method = 'POST') {
  const response = await fetch('/api' + path, data ? { method, headers: { 'Content-Type': 'application/x-www-form-urlencoded' }, body: new URLSearchParams(Object.entries(data).map(([k,v]) => [k,String(v)])) } : undefined)
  const body = await response.json()
  if (!response.ok) throw new Error(body.message || 'Không thể kết nối API')
  return body
}
const statusNames: Record<string,string> = { CHO_XAC_NHAN: 'Chờ xác nhận', DANG_DONG_GOI: 'Đang đóng gói', DANG_GIAO: 'Đang giao', DA_GIAO: 'Đã giao', DA_HUY: 'Đã hủy', GIAO_THAT_BAI: 'Giao thất bại' }

const money = (value: number) => new Intl.NumberFormat('vi-VN').format(value) + '₫'

function Icon({ name, size = 20 }: { name: IconName; size?: number }) {
  const paths: Record<IconName, ReactNode> = {
    search: <><circle cx="11" cy="11" r="7" /><path d="m20 20-4-4" /></>,
    cart: <><path d="M3 4h2l2.2 10.2a2 2 0 0 0 2 1.6h7.7a2 2 0 0 0 2-1.6L20 7H6" /><circle cx="9.5" cy="20" r="1" /><circle cx="17.5" cy="20" r="1" /></>,
    user: <><circle cx="12" cy="8" r="4" /><path d="M4.5 21a7.5 7.5 0 0 1 15 0" /></>,
    chevron: <path d="m9 18 6-6-6-6" />,
    sliders: <><path d="M4 6h16M4 12h16M4 18h16" /><circle cx="9" cy="6" r="2" /><circle cx="15" cy="12" r="2" /><circle cx="8" cy="18" r="2" /></>,
    grid: <><rect x="4" y="4" width="6" height="6" rx="1" /><rect x="14" y="4" width="6" height="6" rx="1" /><rect x="4" y="14" width="6" height="6" rx="1" /><rect x="14" y="14" width="6" height="6" rx="1" /></>,
    list: <><path d="M9 6h11M9 12h11M9 18h11" /><circle cx="4.5" cy="6" r=".5" /><circle cx="4.5" cy="12" r=".5" /><circle cx="4.5" cy="18" r=".5" /></>,
    heart: <path d="M20.8 4.6a5.5 5.5 0 0 0-7.8 0L12 5.7l-1.1-1.1a5.5 5.5 0 0 0-7.8 7.8l1.1 1.1L12 21l7.8-7.5 1.1-1.1a5.5 5.5 0 0 0-.1-7.8Z" />,
    arrow: <><path d="M5 12h14" /><path d="m13 6 6 6-6 6" /></>,
    truck: <><path d="M3 6h11v10H3zM14 10h4l3 3v3h-7z" /><circle cx="7" cy="18" r="2" /><circle cx="18" cy="18" r="2" /></>,
    shield: <path d="M12 3 4 6v5c0 5.2 3.4 8.6 8 10 4.6-1.4 8-4.8 8-10V6l-8-3Z" />,
    box: <><path d="m4 7 8-4 8 4-8 4-8-4Z" /><path d="m4 7 8 4v10l-8-4V7ZM20 7l-8 4v10l8-4V7Z" /></>,
    minus: <path d="M5 12h14" />,
    plus: <path d="M12 5v14M5 12h14" />,
    trash: <><path d="M4 7h16M9 7V4h6v3M7 7l1 14h8l1-14" /></>,
    check: <path d="m5 12 4 4L19 6" />,
    clock: <><circle cx="12" cy="12" r="9" /><path d="M12 7v6l4 2" /></>,
    package: <><path d="m3 7 9-4 9 4-9 4-9-4Z" /><path d="m3 7 9 4v10l-9-4V7ZM21 7l-9 4v10l9-4V7Z" /></>,
    chart: <><path d="M4 20V10M10 20V4M16 20v-7M22 20H2" /></>,
    settings: <><circle cx="12" cy="12" r="3" /><path d="M19 12a7 7 0 0 0-.1-1l2-1.6-2-3.4-2.5 1a7 7 0 0 0-1.7-1L14.3 3h-4.6l-.4 3a7 7 0 0 0-1.7 1L5.1 6 3 9.4 5.1 11a7 7 0 0 0 0 2L3 14.6 5 18l2.6-1a7 7 0 0 0 1.7 1l.4 3h4.6l.4-3a7 7 0 0 0 1.7-1l2.6 1 2-3.4-2.1-1.6a7 7 0 0 0 .1-1Z" /></>,
    orders: <><path d="M6 3h12v18H6z" /><path d="M9 8h6M9 12h6M9 16h4" /></>,
    logout: <><path d="M10 4H4v16h6M14 8l4 4-4 4M8 12h10" /></>,
    download: <><path d="M12 3v12m0 0 5-5m-5 5-5-5M4 20h16" /></>,
    menu: <path d="M4 7h16M4 12h16M4 17h16" />,
    close: <path d="m6 6 12 12M18 6 6 18" />,
  }
  return <svg className="icon" width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">{paths[name]}</svg>
}

function Button({ children, variant = 'primary', className = '', onClick, disabled = false, type = 'button' }: { children: ReactNode; variant?: 'primary' | 'secondary' | 'ghost' | 'dark'; className?: string; onClick?: () => void; disabled?: boolean; type?: 'button' | 'submit' }) {
  return <button type={type} disabled={disabled} onClick={onClick} className={`btn btn-${variant} ${className}`}>{children}</button>
}

function Field({ label, value, onChange, placeholder, type = 'text' }: { label?: string; value?: string; onChange?: (v: string) => void; placeholder?: string; type?: string }) {
  return <label className="field">{label && <span>{label}</span>}<input type={type} value={value} onChange={(e) => onChange?.(e.target.value)} placeholder={placeholder} /></label>
}

function SelectField({ label, value, onChange, children }: { label?: string; value?: string; onChange?: (v: string) => void; children: ReactNode }) {
  return <label className="field">{label && <span>{label}</span>}<select value={value} onChange={(e) => onChange?.(e.target.value)}>{children}</select></label>
}

function Logo({ onClick }: { onClick: () => void }) {
  return <button className="logo" onClick={onClick}><span className="logo-mark"><span /></span><span>Volt<span>Hub</span></span></button>
}

function Header({ page, navigate, cartCount }: { page: Page; navigate: (p: Page) => void; cartCount: number }) {
  const [open, setOpen] = useState(false)
  return <header className="header">
    <div className="shell header-inner">
      <Logo onClick={() => navigate('shop')} />
      <nav className={open ? 'nav open' : 'nav'}>
        <button className={page === 'shop' || page === 'detail' ? 'active' : ''} onClick={() => { navigate('shop'); setOpen(false) }}>Linh kiện</button>
        <button className={page === 'tracking' ? 'active' : ''} onClick={() => { navigate('tracking'); setOpen(false) }}>Theo dõi đơn</button>
        <button onClick={() => setOpen(false)}>Hỗ trợ kỹ thuật</button>
      </nav>
      <div className="header-actions">
        <button className="icon-btn desktop-only" aria-label="Tìm kiếm" onClick={() => navigate('shop')}><Icon name="search" /></button>
        <button className="icon-btn cart-button" aria-label="Giỏ hàng" onClick={() => navigate('cart')}><Icon name="cart" />{cartCount > 0 && <span>{cartCount}</span>}</button>
        <button className="account desktop-only" onClick={() => navigate('account')}><span className="avatar">NB</span><span><small>Xin chào</small>Nguyễn Bảo</span><Icon name="chevron" size={15} /></button>
        <button className="icon-btn menu-btn" onClick={() => setOpen(!open)}><Icon name={open ? 'close' : 'menu'} /></button>
      </div>
    </div>
  </header>
}

const productPhotos: Record<number, string> = {"1":"https://lafvintech.com/cdn/shop/products/product-image-1925125281.jpg?v=1643098279","2":"https://cdn.awsli.com.br/78/78150/produto/6107454/56cd396d82.jpg","3":"https://www.hellasdigital.gr/images/detailed/32/HS0204-5.jpeg","4":"https://witblox.com/cdn/shop/files/products_2F4-channel-relay-module-5v_2FCzAEhQoRIu.jpg?v=1717490949","5":"https://www.cloudraylaser.com/cdn/shop/products/17CS04A-170E-2.jpg?v=1632880417&width=1000","6":"https://www.soleprice.com/3160-large_default/12v-10a-switching-power-supply-120w-led-driver-transformer-ac-to-dc-smps-for-led-strip-light-diy-ac.jpg"}

function ProductVisual({ product, large = false }: { product: Product; large?: boolean }) {
  const photo = product.image || (/arduino/i.test(product.name) ? productPhotos[1] : /dht22/i.test(product.name) ? productPhotos[2] : /esp32/i.test(product.name) ? productPhotos[3] : /relay/i.test(product.name) ? productPhotos[4] : undefined)
  return <div className={`product-visual photo-visual ${large ? "large" : ""}`}>{photo ? <img src={photo} alt={product.name} loading="lazy" referrerPolicy="no-referrer"/> : <span style={{fontSize:24,fontWeight:700,padding:20}}>{product.label}</span>}<span className="visual-code">{product.sku}</span></div>
}

function ProductCard({ product, openProduct, add }: { product: Product; openProduct: (p: Product) => void; add: (p: Product) => void }) {
  return <article className="product-card">
    <button className="card-visual-button" onClick={() => openProduct(product)}><ProductVisual product={product} /><span className={product.stock < 10 ? 'stock low' : 'stock'}>{product.stock < 10 ? `Chỉ còn ${product.stock}` : 'Còn hàng'}</span></button>
    <div className="card-body">
      <div className="card-meta"><span>{product.category}</span><button aria-label="Yêu thích"><Icon name="heart" size={18} /></button></div>
      <button className="product-title" onClick={() => openProduct(product)}>{product.name}</button>
      <p className="sku">{product.sku} · {product.maker}</p>
      <div className="spec-row">{product.specs.slice(0, 2).map((s) => <span key={s}>{s}</span>)}</div>
      <div className="card-footer"><strong>{money(product.price)}</strong><Button disabled={product.stock <= 0} onClick={() => add(product)}><Icon name="cart" size={17} /> Thêm</Button></div>
    </div>
  </article>
}

function Shop({ openProduct, add }: { openProduct: (p: Product) => void; add: (p: Product) => void }) {
  const products = useContext(Catalog).filter(p => p.status === 'DANG_BAN')
  const [search, setSearch] = useState('')
  const [category, setCategory] = useState('Tất cả')
  const [maker, setMaker] = useState('Tất cả')
  const [minPrice, setMinPrice] = useState(0)
  const [maxPrice, setMaxPrice] = useState(5000000)
  const [filterOpen, setFilterOpen] = useState(false)
  const filtered = useMemo(() => products.filter((p) =>
    (category === 'Tất cả' || p.category === category) &&
    (maker === 'Tất cả' || p.maker === maker) &&
    p.price >= minPrice && p.price <= maxPrice &&
    (`${p.id} ${p.name} ${p.sku}`.toLowerCase().includes(search.toLowerCase()))
  ), [products, search, category, maker, minPrice, maxPrice])
  const categories = ['Tất cả', ...new Set(products.map((p) => p.category))]
  return <>
    <section className="hero">
      <div className="circuit-lines" />
      <div className="shell hero-inner">
        <div className="eyebrow">Linh kiện chuẩn · Giao hàng nhanh</div>
        <h1>Từ linh kiện nhỏ,<br /><span>đến mạch chạy thật.</span></h1>
        <p>Bo mạch, cảm biến và nguồn điện cho bài thực hành, đồ án và dự án của bạn.</p>
        <div className="hero-search"><Icon name="search" /><input value={search} onChange={(e) => setSearch(e.target.value)} placeholder="Tìm theo tên hoặc mã linh kiện..." /><Button onClick={() => document.getElementById('products')?.scrollIntoView({ behavior: 'smooth' })}>Tìm linh kiện</Button></div>
        <div className="quick-links"><span>Phổ biến:</span>{['Arduino', 'ESP32', 'Cảm biến', 'Nguồn 12V'].map((x) => <button key={x} onClick={() => setSearch(x)}>{x}</button>)}</div>
      </div>
    </section>
    <main className="shell shop-layout" id="products">
      <aside className={filterOpen ? 'filters open' : 'filters'}>
        <div className="filter-title"><strong>Bộ lọc</strong><button onClick={() => { setCategory('Tất cả'); setMaker('Tất cả'); setSearch(''); setMinPrice(0); setMaxPrice(5000000) }}>Xóa tất cả</button></div>
        <div className="filter-group"><span>Danh mục</span>{categories.map((c) => <label key={c}><input type="radio" checked={category === c} onChange={() => setCategory(c)} /><span>{c}</span><small>{c === 'Tất cả' ? products.length : products.filter((p) => p.category === c).length}</small></label>)}</div>
        <div className="filter-group price-filter">
          <span>Khoảng giá</span>
          <div className="price-values"><strong>{money(minPrice)}</strong><span>đến</span><strong>{money(maxPrice)}</strong></div>
          <div className="dual-range" style={{'--range-start': `${minPrice / 5000000 * 100}%`, '--range-end': `${maxPrice / 5000000 * 100}%`} as React.CSSProperties & Record<string, string>}>
            <div className="dual-range-track"><span /></div>
            <input aria-label="Giá thấp nhất" type="range" min="0" max="5000000" step="10000" value={minPrice} onChange={(e) => setMinPrice(Math.min(Number(e.target.value), maxPrice - 10000))} />
            <input aria-label="Giá cao nhất" type="range" min="0" max="5000000" step="10000" value={maxPrice} onChange={(e) => setMaxPrice(Math.max(Number(e.target.value), minPrice + 10000))} />
          </div>
          <div className="price-limits"><span>0₫</span><span>5.000.000₫</span></div>
        </div>
        <div className="filter-group"><span>Nhà sản xuất</span>{[...new Set(products.map(p=>p.maker).filter(Boolean))].map((m) => <label key={m}><input type="checkbox" checked={maker === m} onChange={() => setMaker(maker === m ? 'Tất cả' : m)} /><span>{m}</span></label>)}</div>
      </aside>
      <section className="catalog">
        <div className="catalog-head"><div><p className="breadcrumb">Trang chủ <Icon name="chevron" size={13} /> Linh kiện</p><h2>Tất cả linh kiện</h2><p>Tìm thấy <strong>{filtered.length}</strong> sản phẩm phù hợp</p></div><div className="catalog-tools"><Button variant="secondary" className="mobile-filter" onClick={() => setFilterOpen(!filterOpen)}><Icon name="sliders" size={17} /> Bộ lọc</Button><SelectField><option>Mới nhất</option><option>Giá tăng dần</option><option>Giá giảm dần</option></SelectField><div className="view-toggle"><button className="active"><Icon name="grid" size={17} /></button><button><Icon name="list" size={17} /></button></div></div></div>
        {filtered.length > 0 ? <div className="product-grid">{filtered.map((p) => <ProductCard key={p.id} product={p} openProduct={openProduct} add={add} />)}</div> : <div className="empty-state"><Icon name="search" size={32} /><h3>Không tìm thấy linh kiện</h3><p>Thử thay đổi từ khóa hoặc xóa bớt bộ lọc đang áp dụng.</p><Button onClick={() => { setSearch(''); setCategory('Tất cả'); setMaker('Tất cả'); setMinPrice(0); setMaxPrice(5000000) }}>Xóa bộ lọc</Button></div>}
      </section>
    </main>
  </>
}

function Detail({ product, add, navigate }: { product: Product; add: (p: Product, q?: number) => void; navigate: (p: Page) => void }) {
  const products = useContext(Catalog)
  const [qty, setQty] = useState(1)
  const [tab, setTab] = useState('Thông số kỹ thuật')
  return <main className="shell detail-page">
    <p className="breadcrumb">Trang chủ <Icon name="chevron" size={13} /> {product.category} <Icon name="chevron" size={13} /> {product.name}</p>
    <div className="detail-grid">
      <div className="gallery"><ProductVisual product={product} large /><div className="thumbnail-row"><button className="active"><ProductVisual product={product} /></button>{[1, 2, 3].map((i) => <button key={i}><div className={`mini-board mini-${i}`} /></button>)}</div></div>
      <div className="detail-copy">
        <div className="detail-tag">{product.category}</div><h1>{product.name}</h1><p className="detail-sku">Mã: <strong>{product.sku}</strong> · Thương hiệu: <strong>{product.maker}</strong></p>
        <div className="rating"><span>★ ★ ★ ★ ★</span> 4.9 <i /> 32 đánh giá <i /> Đã bán 126</div>
        <div className="detail-price">{money(product.price)} <small>Đã bao gồm VAT</small></div>
        <p className="lead">{product.description || 'Thông tin linh kiện được đồng bộ từ cửa hàng.'}</p>
        <div className="spec-preview">{product.specs.map((s, i) => <div key={s}><span>{['Vi điều khiển', 'Kết nối', 'Giao tiếp'][i]}</span><strong>{s}</strong></div>)}</div>
        <div className="availability"><span><Icon name="check" size={16} /> Còn hàng</span><small>{product.stock} sản phẩm trong kho</small></div>
        <div className="buy-row"><div className="qty"><button onClick={() => setQty(Math.max(1, qty - 1))}><Icon name="minus" size={16} /></button><span>{qty}</span><button onClick={() => setQty(Math.min(product.stock, qty + 1))}><Icon name="plus" size={16} /></button></div><Button disabled={product.stock <= 0 || product.status !== 'DANG_BAN'} className="add-large" onClick={() => add(product, qty)}><Icon name="cart" /> Thêm vào giỏ</Button><Button variant="secondary" className="heart-btn"><Icon name="heart" /></Button></div>
        <div className="benefits"><div><Icon name="truck" /><span><strong>Giao nhanh toàn quốc</strong><small>Từ 1–3 ngày làm việc</small></span></div><div><Icon name="shield" /><span><strong>Bảo hành 12 tháng</strong><small>Đổi mới trong 7 ngày</small></span></div><div><Icon name="box" /><span><strong>Kiểm tra trước khi nhận</strong><small>Đóng gói chống tĩnh điện</small></span></div></div>
      </div>
    </div>
    <section className="detail-tabs"><div className="tab-list">{['Mô tả sản phẩm', 'Thông số kỹ thuật', 'Tài liệu'].map((x) => <button className={tab === x ? 'active' : ''} onClick={() => setTab(x)} key={x}>{x}</button>)}</div>
      {tab === 'Thông số kỹ thuật' && <div className="spec-table"><div><span>SKU</span><strong>{product.sku}</strong></div><div><span>Nhà sản xuất</span><strong>{product.maker}</strong></div><div><span>Điện áp hoạt động</span><strong>{product.specs[0] || 'Chưa cập nhật'}</strong></div><div><span>Nhiệt độ hoạt động</span><strong>-20°C đến 85°C</strong></div><div><span>Kích thước</span><strong>68.6 × 53.4 mm</strong></div><div><span>Xuất xứ</span><strong>Chính hãng</strong></div></div>}
      {tab === 'Mô tả sản phẩm' && <p className="tab-copy">Linh kiện được tuyển chọn cho độ ổn định cao, có đầy đủ chân kết nối và tài liệu kỹ thuật. Phù hợp từ nguyên mẫu đến các hệ thống vận hành dài hạn.</p>}
      {tab === 'Tài liệu' && <div className="document-card"><Icon name="download" /><span><strong>Datasheet — {product.sku}.pdf</strong><small>{product.datasheet ? 'Tài liệu kỹ thuật' : 'Chưa có tài liệu'}</small></span>{product.datasheet && /^https?:\/\//.test(product.datasheet) && <a href={product.datasheet} target="_blank" rel="noreferrer">Tải xuống</a>}</div>}
    </section>
    <button className="back-link" onClick={() => navigate('shop')}><Icon name="arrow" size={17} /> Tiếp tục xem linh kiện</button>
  </main>
}

type CartMap = Record<number, number>

function CartPage({ cart, setCart, navigate }: { cart: CartMap; setCart: (c: CartMap) => void; navigate: (p: Page) => void }) {
  const products = useContext(Catalog)
  const rows = products.filter((p) => cart[p.id])
  const subtotal = rows.reduce((s, p) => s + p.price * cart[p.id], 0)
  const update = (id: number, qty: number) => setCart({ ...cart, [id]: qty })
  const remove = (id: number) => { const next = { ...cart }; delete next[id]; setCart(next) }
  return <main className="shell cart-page">
    <div className="page-heading"><div><p className="breadcrumb">Trang chủ <Icon name="chevron" size={13} /> Giỏ hàng</p><h1>Giỏ hàng của bạn</h1><p>{rows.length} sản phẩm đang chờ bạn</p></div><button onClick={() => navigate('shop')}>Tiếp tục mua sắm <Icon name="arrow" size={17} /></button></div>
    {rows.length === 0 ? <div className="empty-state cart-empty"><Icon name="cart" size={36} /><h3>Giỏ hàng đang trống</h3><p>Khám phá các linh kiện phù hợp cho dự án tiếp theo của bạn.</p><Button onClick={() => navigate('shop')}>Khám phá linh kiện</Button></div> :
    <div className="cart-layout"><section className="cart-list"><div className="cart-list-head"><span>Sản phẩm</span><span>Đơn giá</span><span>Số lượng</span><span>Thành tiền</span></div>{rows.map((p) => <article className="cart-row" key={p.id}><ProductVisual product={p} /><div className="cart-product"><span>{p.category}</span><strong>{p.name}</strong><small>{p.sku} · Còn {p.stock} sản phẩm</small><button onClick={() => remove(p.id)}><Icon name="trash" size={15} /> Xóa</button></div><div className="cart-unit">{money(p.price)}</div><div className="qty"><button onClick={() => update(p.id, Math.max(1, cart[p.id] - 1))}><Icon name="minus" size={15} /></button><span>{cart[p.id]}</span><button onClick={() => update(p.id, Math.min(p.stock, cart[p.id] + 1))}><Icon name="plus" size={15} /></button></div><strong className="cart-line-total">{money(p.price * cart[p.id])}</strong></article>)}</section>
      <aside className="summary-card"><h2>Tóm tắt đơn hàng</h2><div className="summary-lines"><p><span>Tạm tính</span><strong>{money(subtotal)}</strong></p><p><span>Phí vận chuyển</span><span>Tính ở bước sau</span></p></div><div className="summary-total"><span>Tổng dự kiến<small>Đã bao gồm VAT</small></span><strong>{money(subtotal)}</strong></div><Button className="checkout-btn" onClick={() => navigate('checkout')}>Tiến hành thanh toán <Icon name="arrow" /></Button><p className="secure-note"><Icon name="shield" size={16} /> Thanh toán an toàn và bảo mật</p></aside>
    </div>}
  </main>
}

function BankTransfer({ amount, orderId }: { amount: number; orderId?: number }) {
  return <section className="bank-transfer" aria-label="Thông tin chuyển khoản"><h3>Quét QR chuyển khoản</h3><img src={import.meta.env.BASE_URL + 'images/bank-transfer-qr.png'} alt="QR chuyển khoản ACB, LE TRUNG HIEU, số tài khoản 17940701" width="720" height="959"/><dl><div><dt>Ngân hàng</dt><dd>ACB</dd></div><div><dt>Chủ tài khoản</dt><dd>LE TRUNG HIEU</dd></div><div><dt>Số tài khoản</dt><dd>17940701</dd></div><div><dt>Số tiền</dt><dd>{money(amount)}</dd></div>{orderId && <div><dt>Nội dung</dt><dd>VOLTHUB {orderId}</dd></div>}</dl><p>{orderId ? 'Nhập đúng số tiền và nội dung khi chuyển khoản. Cửa hàng xác nhận thanh toán thủ công.' : 'Xác nhận đặt hàng để nhận mã đơn dùng làm nội dung chuyển khoản. Mã QR chưa điền sẵn số tiền.'}</p></section>
}

function Checkout({ cart, navigate, complete, profile }: { cart: CartMap; navigate: (p: Page) => void; complete: (recipient: Profile, shipping: string, payment: string, requestId: string) => Promise<void>; profile: Profile }) {
  const products = useContext(Catalog)
  const [shipping, setShipping] = useState('standard')
  const [payment, setPayment] = useState('cod')
  const [step, setStep] = useState(1)
  const [recipient, setRecipient] = useState(profile)
  const rows = products.filter((p) => cart[p.id])
  const subtotal = rows.reduce((s, p) => s + p.price * cart[p.id], 0)
  const fee = shipping === 'express' ? 49000 : 25000
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [requestId] = useState(() => crypto.randomUUID())
  const next = async () => {
    if (!recipient.fullName.trim() || !/^0\d{9,10}$/.test(recipient.phone.replace(/\s/g, '')) || !recipient.street.trim() || !recipient.city.trim()) { setError('Vui lòng điền tên, số điện thoại hợp lệ và địa chỉ.'); return }
    if (step < 3) { setError(''); setStep(step + 1); return }
    setBusy(true); setError('')
    try { await complete(recipient, shipping, payment, requestId) } catch(e) { setError((e as Error).message) } finally { setBusy(false) }
  }
  return <main className="shell checkout-page">
    <button className="back-link" onClick={() => step > 1 ? setStep(step - 1) : navigate('cart')}><Icon name="arrow" size={17} /> Quay lại</button>
    <div className="checkout-title"><h1>Thanh toán</h1><p>Hoàn tất đơn hàng trong vài bước đơn giản</p></div>
    <div className="stepper">{['Thông tin nhận hàng', 'Vận chuyển & thanh toán', 'Xác nhận'].map((x, i) => <div className={step >= i + 1 ? 'active' : ''} key={x}><span>{step > i + 1 ? <Icon name="check" size={15} /> : i + 1}</span><strong>{x}</strong></div>)}</div>
    <div className="checkout-layout"><section className="checkout-form">
      {step === 1 && <div className="form-card"><div className="section-head"><span>01</span><div><h2>Thông tin nhận hàng</h2><p>Đã điền theo địa chỉ mặc định trong tài khoản. Bạn có thể sửa cho đơn hàng này.</p></div></div><div className="default-address-note"><Icon name="check" size={15} /><span>Đang dùng địa chỉ mặc định</span><button onClick={() => navigate('account')}>Cập nhật trong tài khoản</button></div><div className="form-grid"><Field label="Họ và tên" value={recipient.fullName} onChange={(v) => setRecipient({ ...recipient, fullName: v })} /><Field label="Số điện thoại" value={recipient.phone} onChange={(v) => setRecipient({ ...recipient, phone: v })} /><Field label="Email" value={recipient.email} onChange={(v) => setRecipient({ ...recipient, email: v })} /><Field label="Tỉnh / Thành phố" value={recipient.city} onChange={(v) => setRecipient({ ...recipient, city: v })} /><Field label="Quận / Huyện" value={recipient.district} onChange={(v) => setRecipient({ ...recipient, district: v })} /><Field label="Phường / Xã" value={recipient.ward} onChange={(v) => setRecipient({ ...recipient, ward: v })} /><div className="full"><Field label="Địa chỉ cụ thể" value={recipient.street} onChange={(v) => setRecipient({ ...recipient, street: v })} /></div><div className="full"><Field label="Ghi chú giao hàng (không bắt buộc)" placeholder="Ví dụ: Gọi trước khi giao..." /></div></div></div>}
      {step === 2 && <><div className="form-card"><div className="section-head"><span>02</span><div><h2>Phương thức vận chuyển</h2><p>Chọn tốc độ giao hàng phù hợp</p></div></div><div className="choice-list"><label className={shipping === 'standard' ? 'selected' : ''}><input type="radio" checked={shipping === 'standard'} onChange={() => setShipping('standard')} /><Icon name="truck" /><span><strong>Giao hàng tiêu chuẩn</strong><small>Dự kiến 2–4 ngày</small></span><b>{money(25000)}</b></label><label className={shipping === 'express' ? 'selected' : ''}><input type="radio" checked={shipping === 'express'} onChange={() => setShipping('express')} /><Icon name="package" /><span><strong>Giao nhanh</strong><small>Nhận hàng ngày mai</small></span><b>{money(49000)}</b></label></div></div>
      <div className="form-card"><div className="section-head"><span>03</span><div><h2>Phương thức thanh toán</h2><p>Giao dịch được bảo vệ an toàn</p></div></div><div className="choice-list payment-choices">{[{ id: 'cod', title: 'Thanh toán khi nhận hàng', note: 'Tiền mặt hoặc chuyển khoản cho shipper' }, { id: 'bank', title: 'QR / Chuyển khoản ngân hàng', note: 'Chuyển khoản được xác nhận thủ công' }].map((x) => <label className={payment === x.id ? 'selected' : ''} key={x.id}><input type="radio" checked={payment === x.id} onChange={() => setPayment(x.id)} /><span className="pay-icon">{x.id === 'cod' ? 'COD' : x.id === 'wallet' ? 'VH' : 'QR'}</span><span><strong>{x.title}</strong><small>{x.note}</small></span></label>)}</div>{payment === 'bank' && <BankTransfer amount={subtotal + fee}/>}</div></>}
      {step === 3 && <div className="form-card confirmation"><div className="confirm-mark"><Icon name="check" size={28} /></div><h2>Sẵn sàng đặt hàng</h2><p>Vui lòng kiểm tra lại thông tin trước khi xác nhận.</p><div className="confirm-grid"><div><span>Người nhận</span><strong>{recipient.fullName}</strong><small>{recipient.phone}</small></div><div><span>Địa chỉ giao</span><strong>{recipient.street}, {recipient.ward}</strong><small>{recipient.district}, {recipient.city}</small></div><div><span>Vận chuyển</span><strong>{shipping === 'express' ? 'Giao nhanh' : 'Giao hàng tiêu chuẩn'}</strong><small>Dự kiến 2–4 ngày</small></div><div><span>Thanh toán</span><strong>{payment === 'cod' ? 'Thanh toán khi nhận hàng' : payment === 'wallet' ? 'Ví VoltHub' : 'Chuyển khoản ngân hàng'}</strong><small>{payment === 'cod' ? 'Chờ thu khi giao' : 'Xử lý an toàn'}</small></div></div>{payment === 'bank' && <BankTransfer amount={subtotal + fee}/>}</div>}
      {error && <p role="alert">{error}</p>}<Button disabled={busy || rows.length === 0} className="continue-btn" onClick={next}>{step === 3 ? 'Xác nhận đặt hàng' : 'Tiếp tục'} <Icon name="arrow" /></Button>
    </section>
    <aside className="order-card"><h2>Đơn hàng của bạn</h2>{rows.map((p) => <div className="order-mini" key={p.id}><ProductVisual product={p} /><span><strong>{p.name}</strong><small>SL: {cart[p.id]}</small></span><b>{money(p.price * cart[p.id])}</b></div>)}<div className="summary-lines"><p><span>Tạm tính</span><strong>{money(subtotal)}</strong></p><p><span>Phí vận chuyển</span><strong>{money(fee)}</strong></p></div><div className="summary-total"><span>Tổng thanh toán<small>Đã bao gồm VAT</small></span><strong>{money(subtotal + fee)}</strong></div></aside></div>
  </main>
}

function AccountSettings({ profile, save, navigate }: { profile: Profile; save: (profile: Profile) => void; navigate: (p: Page) => void }) {
  const [draft, setDraft] = useState(profile)
  return <main className="shell account-page">
    <p className="breadcrumb">Trang chủ <Icon name="chevron" size={13} /> Tài khoản</p>
    <div className="account-heading"><div><h1>Cài đặt tài khoản</h1><p>Cập nhật thông tin liên hệ và địa chỉ nhận hàng mặc định.</p></div><Button variant="secondary" onClick={() => navigate('admin')}>Mở trang quản trị</Button></div>
    <div className="account-layout">
      <aside className="account-menu"><button className="active"><Icon name="user" /> Thông tin cá nhân</button><button onClick={()=>navigate('tracking')}><Icon name="package" /> Đơn hàng của tôi</button><button><Icon name="shield" /> Bảo mật tài khoản</button></aside>
      <section className="account-settings-card">
        <div className="section-head"><span>01</span><div><h2>Thông tin liên hệ</h2><p>Dùng để xác nhận đơn hàng và liên hệ khi giao hàng.</p></div></div>
        <div className="form-grid"><Field label="Họ và tên" value={draft.fullName} onChange={(v) => setDraft({ ...draft, fullName: v })} /><Field label="Số điện thoại" value={draft.phone} onChange={(v) => setDraft({ ...draft, phone: v })} /><div className="full"><Field label="Email" value={draft.email} onChange={(v) => setDraft({ ...draft, email: v })} /></div></div>
        <div className="account-divider" />
        <div className="section-head"><span>02</span><div><h2>Địa chỉ nhận hàng mặc định</h2><p>Địa chỉ này sẽ được tự động điền khi bạn mở trang thanh toán.</p></div></div>
        <div className="form-grid"><Field label="Tỉnh / Thành phố" value={draft.city} onChange={(v) => setDraft({ ...draft, city: v })} /><Field label="Quận / Huyện" value={draft.district} onChange={(v) => setDraft({ ...draft, district: v })} /><Field label="Phường / Xã" value={draft.ward} onChange={(v) => setDraft({ ...draft, ward: v })} /><Field label="Địa chỉ cụ thể" value={draft.street} onChange={(v) => setDraft({ ...draft, street: v })} /></div>
        <div className="account-actions"><Button variant="secondary" onClick={() => setDraft(profile)}>Hủy thay đổi</Button><Button onClick={() => save(draft)}><Icon name="check" size={16} /> Lưu địa chỉ mặc định</Button></div>
      </section>
    </div>
  </main>
}

type Order = { maDonHang: number; tenNguoiNhan: string; soDienThoaiNhan: string; diaChiNhan: string; trangThaiDon: string; tongThanhToan: number; ngayDat: string; danhSachChiTiet: {maLinhKien:number;soLuong:number;donGia:number}[] }
function Tracking({ latest }: { latest: number }) {
  const [code, setCode] = useState(String(latest || ''))
  const [data, setData] = useState<any>(null)
  const [error, setError] = useState('')
  const [orders, setOrders] = useState<Order[]>([])
  const lookup = async (id: string) => { setError(''); setData(null); try { setData(await api('/tracking?id='+encodeURIComponent(id))) } catch(e) { setError((e as Error).message) } }
  useEffect(() => { api('/orders').then(setOrders).catch(e=>setError(e.message)); if(latest) void lookup(String(latest)) },[latest])
  return <main className="shell tracking-page"><h1>Theo dõi đơn hàng</h1><div className="track-search"><input aria-label="Mã đơn hàng" value={code} onChange={e=>setCode(e.target.value)} placeholder="Nhập mã đơn dạng số"/><Button onClick={()=>lookup(code)}>Tra cứu</Button></div><div className="quick-links">{orders.map(o=><Button key={o.maDonHang} variant="secondary" onClick={()=>{setCode(String(o.maDonHang));void lookup(String(o.maDonHang))}}>#{o.maDonHang} · {statusNames[o.trangThaiDon]}</Button>)}</div>{error && <p role="alert">{error}</p>}{data && <><div className="tracking-summary"><strong>Đơn #{data.order.maDonHang}</strong><strong>{statusNames[data.order.trangThaiDon]}</strong><strong>{money(data.order.tongThanhToan)}</strong></div><div className="tracking-layout"><section className="timeline-card"><h2>Hành trình đơn hàng</h2>{data.history.map((h:any,i:number)=><div className="event" key={i}><div><strong>{statusNames[h.trangThaiMoi] || h.trangThaiMoi}</strong><p>{h.thoiDiem} · {h.ghiChu}</p></div></div>)}</section><aside className="delivery-card"><h3>Thông tin giao hàng</h3><p>{data.order.tenNguoiNhan} · {data.order.soDienThoaiNhan}</p><p>{data.order.diaChiNhan}</p><p>{data.shipping ? data.shipping.donViVanChuyen+' · '+data.shipping.maVanDon : 'Chưa bàn giao vận chuyển'}</p>{data.order.phuongThucThanhToan === 'CHUYEN_KHOAN' && data.order.trangThaiDon !== 'DA_HUY' && <BankTransfer amount={data.order.tongThanhToan} orderId={data.order.maDonHang}/>}<h3>Chi tiết đơn</h3>{data.order.danhSachChiTiet.map((l:any)=><p key={l.maLinhKien}>Linh kiện #{l.maLinhKien} × {l.soLuong} · {money(l.donGia*l.soLuong)}</p>)}</aside></div></>}</main>
}
function Admin({ navigate, refresh }: { navigate: (p: Page) => void; refresh: () => Promise<void> }) {
  const products = useContext(Catalog)
  const [section,setSection] = useState('Linh kiện')
  const [orders,setOrders] = useState<Order[]>([])
  const [categories,setCategories] = useState<any[]>([])
  const [search,setSearch] = useState('')
  const [error,setError] = useState('')
  const [busy,setBusy] = useState(false)
  const empty = {id:0,sku:'',name:'',category:'',price:0,stock:0,maker:''}
  const [draft,setDraft] = useState<typeof empty | null>(null)
  const load = async () => {setOrders(await api('/orders?admin=true'));setCategories(await api('/categories'))}
  useEffect(()=>{load().catch(e=>setError(e.message))},[])
  const run = async (action:()=>Promise<unknown>) => {setBusy(true);setError('');try {await action();await refresh();await load()} catch(e){setError((e as Error).message)}finally{setBusy(false)}}
  return <div className="admin-shell"><aside className="admin-sidebar"><Logo onClick={()=>navigate('shop')}/><p>QUẢN TRỊ PROTOTYPE</p>{['Linh kiện','Đơn hàng','Vận chuyển'].map(x=><Button key={x} variant="secondary" onClick={()=>setSection(x)}>{x}</Button>)}<Button onClick={()=>navigate('shop')}>Về cửa hàng</Button></aside><main className="admin-main"><h1>{section}</h1>{error && <p role="alert">{error}</p>}{section==='Linh kiện' ? <section className="management-card"><div className="card-heading"><Field placeholder="Tìm mã hoặc tên" value={search} onChange={setSearch}/><Button onClick={()=>setDraft({...empty,category:String(categories[0]?.maDanhMuc || '')})}>Thêm linh kiện</Button></div>{draft && <form onSubmit={e=>{e.preventDefault();void run(async()=>{await api('/products',draft);setDraft(null)})}}><div className="form-grid">{(['sku','name','price','stock','maker'] as const).map((k)=><Field key={k} label={({sku:'Mã SKU',name:'Tên linh kiện',price:'Giá bán',stock:'Tồn kho',maker:'Nhà sản xuất'})[k]} type={k==='price'||k==='stock'?'number':'text'} value={String(draft[k])} onChange={v=>setDraft({...draft,[k]:v})}/>)}<SelectField label="Danh mục" value={draft.category} onChange={v=>setDraft({...draft,category:v})}>{categories.map(c=><option key={c.maDanhMuc} value={c.maDanhMuc}>{c.tenDanhMuc}</option>)}</SelectField></div><Button disabled={busy} type="submit">Lưu</Button><Button variant="secondary" onClick={()=>setDraft(null)}>Hủy</Button></form>}<div className="inventory-grid">{products.filter(p=>(p.id+' '+p.sku+' '+p.name).toLowerCase().includes(search.toLowerCase())).map(p=><div key={p.id}><ProductVisual product={p}/><span><strong>{p.name}</strong><small>{p.sku}</small></span><b>{money(p.price)}</b><em>{p.stock} trong kho</em><Button variant="secondary" onClick={()=>setDraft({id:p.id,sku:p.sku,name:p.name,category:String(p.categoryId),price:p.price,stock:p.stock,maker:p.maker})}>Sửa</Button><Button disabled={busy} variant="secondary" onClick={()=>{if(window.confirm('Xóa hoặc ngừng bán '+p.name+'?')) void run(()=>api('/products',{id:p.id},'DELETE'))}}>Xóa</Button></div>)}</div></section> : <section className="orders-table-card"><table><thead><tr><th>Mã đơn</th><th>Người nhận</th><th>Tổng tiền</th><th>Trạng thái</th><th>Thao tác</th></tr></thead><tbody>{orders.map(o=><tr key={o.maDonHang}><td>#{o.maDonHang}</td><td>{o.tenNguoiNhan}</td><td>{money(o.tongThanhToan)}</td><td>{statusNames[o.trangThaiDon]}</td><td>{o.trangThaiDon==='CHO_XAC_NHAN' && <Button disabled={busy} onClick={()=>run(()=>api('/status',{id:o.maDonHang,status:'DANG_DONG_GOI'}))}>Đóng gói</Button>}{o.trangThaiDon==='DANG_DONG_GOI' && <Button disabled={busy} onClick={()=>{const carrier=prompt('Đơn vị vận chuyển','Giao Hàng Nhanh');if(!carrier)return;const tracking=prompt('Mã vận đơn');if(tracking)void run(()=>api('/shipping',{id:o.maDonHang,carrier,tracking}))}}>Bàn giao vận chuyển</Button>}{o.trangThaiDon==='DANG_GIAO' && <Button disabled={busy} onClick={()=>run(()=>api('/status',{id:o.maDonHang,status:'DA_GIAO'}))}>Đã giao</Button>}{['CHO_XAC_NHAN','DANG_DONG_GOI'].includes(o.trangThaiDon) && <Button disabled={busy} variant="secondary" onClick={()=>run(()=>api('/status',{id:o.maDonHang,status:'DA_HUY'}))}>Hủy đơn</Button>}</td></tr>)}</tbody></table>{orders.length===0 && <p>Chưa có đơn hàng.</p>}</section>}</main></div>
}

function Toast({ text }: { text: string }) {
  return <div className="toast"><span><Icon name="check" size={16} /></span>{text}</div>
}

export default function App() {
  const [products, setProducts] = useState<Product[]>([])
  const [ready,setReady] = useState(false)
  const [error,setError] = useState('')
  const [latest,setLatest] = useState(0)
  const [busy,setBusy] = useState(false)
  const [page, setPage] = useState<Page>('shop')
  const [selected, setSelected] = useState<Product | null>(null)
  const [cart, setCart] = useState<CartMap>({})
  const [profile, setProfile] = useState<Profile>(loadProfile)
  const [toast, setToast] = useState('')
  const cartCount = Object.values(cart).reduce((a, b) => a + b, 0)
  const navigate = (p: Page) => { setPage(p); window.scrollTo({ top: 0, behavior: 'smooth' }) }
  const notify = (text: string) => { setToast(text); window.setTimeout(() => setToast(''), 2600) }
  const refresh = async () => {
    const [catalog,basket] = await Promise.all([api('/products'),api('/cart')])
    setProducts(catalog.map((p:any)=>({id:p.maLinhKien,name:p.tenLinhKien,sku:p.maSKU,category:p.tenDanhMuc,categoryId:p.maDanhMuc,maker:p.nhaSanXuat || '',price:p.giaBan,stock:p.soLuongTon,accent:'blue',label:p.maSKU,specs:[p.dienApHoatDong,p.giaoTiep].filter(Boolean),status:p.trangThaiBan,description:p.moTa,image:p.hinhAnh,datasheet:p.duongDanDatasheet})))
    setCart(Object.fromEntries(basket.danhSachChiTiet.map((l:any)=>[l.maLinhKien,l.soLuong])))
  }
  useEffect(()=>{refresh().then(()=>setReady(true)).catch(e=>setError(e.message))},[])
  const mutate = async (action:()=>Promise<unknown>) => {if(busy)return;setBusy(true);setError('');try{await action();await refresh()}catch(e){setError((e as Error).message)}finally{setBusy(false)}}
  const add = (product: Product, qty = 1) => { void mutate(async()=>{await api('/cart',{id:product.id,quantity:qty,action:'add'});notify('Đã thêm vào giỏ')}) }
  const updateCart = (next: CartMap) => {void mutate(async()=>{for(const id of new Set([...Object.keys(cart),...Object.keys(next)])) if(cart[+id]!==next[+id]) await api('/cart',{id,quantity:next[+id] || 0})})}
  const openProduct = (p: Product) => {setSelected(p);navigate('detail')}
  const complete = async (recipient: Profile, shipping: string, payment: string, requestId: string) => {
    const order = await api('/orders',{name:recipient.fullName,phone:recipient.phone.replace(/\s/g,''),address:[recipient.street,recipient.ward,recipient.district,recipient.city].join(', '),shipping,payment:payment==='cod'?'COD':'CHUYEN_KHOAN',requestId})
    setLatest(order.maDonHang);await refresh();navigate('tracking');notify('Đặt hàng thành công — mã đơn '+order.maDonHang)
  }
  const saveProfile = (nextProfile: Profile) => {
    setProfile(nextProfile)
    window.localStorage.setItem('volthub-default-address', JSON.stringify(nextProfile))
    notify('Đã lưu địa chỉ nhận hàng mặc định')
  }
  if (!ready) return <main className="shell"><h1>VoltHub</h1><p>{error || 'Đang tải dữ liệu từ backend…'}</p><Button onClick={()=>{setError('');refresh().then(()=>setReady(true)).catch(e=>setError(e.message))}}>Thử lại</Button></main>
  if (page === 'admin') return <Catalog.Provider value={products}><Admin navigate={navigate} refresh={refresh}/>{toast && <Toast text={toast}/>}</Catalog.Provider>
  return <Catalog.Provider value={products}><div aria-busy={busy}>{error && <p role="alert" className="shell">{error}</p>}<Header page={page} navigate={navigate} cartCount={cartCount} />
    {page === 'shop' && <Shop openProduct={openProduct} add={add} />}
    {page === 'detail' && selected && <Detail product={products.find(p=>p.id===selected.id) || selected} add={add} navigate={navigate} />}
    {page === 'cart' && <CartPage cart={cart} setCart={updateCart} navigate={navigate} />}
    {page === 'checkout' && <Checkout cart={cart} navigate={navigate} complete={complete} profile={profile} />}
    {page === 'tracking' && <Tracking latest={latest} />}
    {page === 'account' && <AccountSettings profile={profile} save={saveProfile} navigate={navigate} />}
    {toast && <Toast text={toast} />}
  </div></Catalog.Provider>
}

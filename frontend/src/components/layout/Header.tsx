import { useState } from 'react'
import type { Page, Profile } from '../../types'
import { Icon } from '../common/Icon'
import { Logo } from '../common/Logo'

export function Header({
  page,
  navigate,
  cartCount,
  profile,
  onSupportClick,
}: {
  page: Page
  navigate: (p: Page) => void
  cartCount: number
  profile: Profile
  onSupportClick: () => void
}) {
  const [open, setOpen] = useState(false)
  const initials = profile.fullName
    .split(/\s+/)
    .filter(Boolean)
    .slice(-2)
    .map((w) => w[0]?.toUpperCase() || '')
    .join('')

  return (
    <header className="header">
      <div className="shell header-inner">
        <Logo onClick={() => navigate('shop')} />
        <nav className={open ? 'nav open' : 'nav'}>
          <button
            className={page === 'shop' || page === 'detail' ? 'active' : ''}
            onClick={() => {
              navigate('shop')
              setOpen(false)
            }}
          >
            Linh kiện
          </button>
          <button
            className={page === 'tracking' ? 'active' : ''}
            onClick={() => {
              navigate('tracking')
              setOpen(false)
            }}
          >
            Theo dõi đơn
          </button>
          <button
            onClick={() => {
              onSupportClick()
              setOpen(false)
            }}
          >
            Hỗ trợ kỹ thuật
          </button>
        </nav>
        <div className="header-actions">
          <button
            className="icon-btn desktop-only"
            aria-label="Tìm kiếm"
            onClick={() => navigate('shop')}
          >
            <Icon name="search" />
          </button>
          <button
            className="icon-btn cart-button"
            aria-label="Giỏ hàng"
            onClick={() => navigate('cart')}
          >
            <Icon name="cart" />
            {cartCount > 0 && <span>{cartCount}</span>}
          </button>
          <button
            className="account desktop-only"
            onClick={() => navigate('account')}
          >
            <span className="avatar">{initials || 'NB'}</span>
            <span>
              <small>Xin chào</small>
              {profile.fullName}
            </span>
            <Icon name="chevron" size={15} />
          </button>
          <button className="icon-btn menu-btn" onClick={() => setOpen(!open)}>
            <Icon name={open ? 'close' : 'menu'} />
          </button>
        </div>
      </div>
    </header>
  )
}

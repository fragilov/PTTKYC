import { useCallback, useEffect, useState, type FormEvent } from 'react'
import {
  deleteAddress,
  fetchAddresses,
  saveAddress,
  setDefaultAddress,
  updateAccountPassword,
} from '../api/accountApi'
import { Button } from '../components/common/Button'
import { Field } from '../components/common/Field'
import { Icon } from '../components/common/Icon'
import type { AddressRecord, Page, Profile } from '../types'
import { addressRecordToProfile, validateRecipient } from '../utils/format'

export function AccountPage({
  profile,
  save,
  navigate,
  notify,
}: {
  profile: Profile
  save: (profile: Profile) => void
  navigate: (p: Page) => void
  notify: (msg: string) => void
}) {
  const [tab, setTab] = useState<'profile' | 'security'>('profile')
  const [draft, setDraft] = useState<Profile>(profile)
  const [addresses, setAddresses] = useState<AddressRecord[]>([])
  const [selectedAddressId, setSelectedAddressId] = useState<number>(0)
  const [profileError, setProfileError] = useState('')
  const [saving, setSaving] = useState(false)

  const [currentPassword, setCurrentPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [securityError, setSecurityError] = useState('')
  const [savingPassword, setSavingPassword] = useState(false)

  const loadAddresses = useCallback(async () => {
    try {
      const list = await fetchAddresses()
      setAddresses(list)
      const def = list.find((a) => a.laMacDinh) || list[0]
      if (def) {
        setSelectedAddressId(def.maDiaChi)
      }
    } catch {
      // Keep local profile fallback if address book endpoint fails
    }
  }, [])

  useEffect(() => {
    setDraft(profile)
    void loadAddresses()
  }, [profile, loadAddresses])

  const handleSaveProfile = async () => {
    const err = validateRecipient(draft)
    if (err) {
      setProfileError(err)
      return
    }
    setSaving(true)
    setProfileError('')
    try {
      const saved = await saveAddress({
        id: selectedAddressId || 0,
        name: draft.fullName.trim(),
        phone: draft.phone.replace(/[\s.-]/g, '').trim(),
        street: draft.street.trim(),
        ward: draft.ward.trim() || 'Phường 1',
        city: draft.city.trim(),
        isDefault: true,
      })
      setSelectedAddressId(saved.maDiaChi)
      save(draft)
      await loadAddresses()
    } catch (e) {
      setProfileError((e as Error).message)
    } finally {
      setSaving(false)
    }
  }

  const handlePickDefault = async (addr: AddressRecord) => {
    setProfileError('')
    try {
      await setDefaultAddress(addr.maDiaChi)
      const next = addressRecordToProfile(addr, draft)
      setDraft(next)
      setSelectedAddressId(addr.maDiaChi)
      save(next)
      await loadAddresses()
    } catch (e) {
      setProfileError((e as Error).message)
    }
  }

  const handleDeleteAddress = async (addr: AddressRecord) => {
    setProfileError('')
    try {
      await deleteAddress(addr.maDiaChi)
      if (selectedAddressId === addr.maDiaChi) {
        setSelectedAddressId(0)
      }
      await loadAddresses()
      notify('Đã xóa địa chỉ khỏi sổ địa chỉ')
    } catch (e) {
      setProfileError((e as Error).message)
    }
  }

  const handleChangePassword = async (e: FormEvent) => {
    e.preventDefault()
    if (!currentPassword || !newPassword) {
      setSecurityError('Vui lòng nhập đầy đủ mật khẩu hiện tại và mật khẩu mới.')
      return
    }
    if (newPassword.length < 6) {
      setSecurityError('Mật khẩu mới phải có ít nhất 6 ký tự.')
      return
    }
    if (newPassword !== confirmPassword) {
      setSecurityError('Xác nhận mật khẩu mới chưa khớp.')
      return
    }
    setSavingPassword(true)
    setSecurityError('')
    try {
      await updateAccountPassword(currentPassword, newPassword)
      setCurrentPassword('')
      setNewPassword('')
      setConfirmPassword('')
      notify('Cập nhật mật khẩu tài khoản thành công')
    } catch (err) {
      setSecurityError((err as Error).message)
    } finally {
      setSavingPassword(false)
    }
  }

  return (
    <main className="shell account-page">
      <p className="breadcrumb">
        Trang chủ <Icon name="chevron" size={13} /> Tài khoản
      </p>
      <div className="account-heading">
        <div>
          <h1>Cài đặt tài khoản</h1>
          <p>Cập nhật thông tin liên hệ và địa chỉ nhận hàng mặc định.</p>
        </div>
        <Button variant="secondary" onClick={() => navigate('admin')}>
          Mở trang quản trị
        </Button>
      </div>
      <div className="account-layout">
        <aside className="account-menu">
          <button
            className={tab === 'profile' ? 'active' : ''}
            onClick={() => setTab('profile')}
          >
            <Icon name="user" /> Thông tin cá nhân
          </button>
          <button onClick={() => navigate('tracking')}>
            <Icon name="package" /> Đơn hàng của tôi
          </button>
          <button
            className={tab === 'security' ? 'active' : ''}
            onClick={() => setTab('security')}
          >
            <Icon name="shield" /> Bảo mật tài khoản
          </button>
        </aside>

        {tab === 'profile' ? (
          <section className="account-settings-card">
            <div className="section-head">
              <span>01</span>
              <div>
                <h2>Thông tin liên hệ</h2>
                <p>Dùng để xác nhận đơn hàng và liên hệ khi giao hàng.</p>
              </div>
            </div>
            <div className="form-grid">
              <Field
                label="Họ và tên"
                value={draft.fullName}
                onChange={(v) => setDraft({ ...draft, fullName: v })}
              />
              <Field
                label="Số điện thoại"
                value={draft.phone}
                onChange={(v) => setDraft({ ...draft, phone: v })}
              />
              <div className="full">
                <Field
                  label="Email"
                  value={draft.email}
                  onChange={(v) => setDraft({ ...draft, email: v })}
                />
              </div>
            </div>
            <div className="account-divider" />
            <div className="section-head">
              <span>02</span>
              <div>
                <h2>Địa chỉ nhận hàng mặc định</h2>
                <p>Địa chỉ này được đồng bộ với máy chủ và điền sẵn khi đặt hàng.</p>
              </div>
            </div>
            <div className="form-grid">
              <Field
                label="Tỉnh / Thành phố"
                value={draft.city}
                onChange={(v) => setDraft({ ...draft, city: v })}
              />
              <Field
                label="Quận / Huyện"
                value={draft.district}
                onChange={(v) => setDraft({ ...draft, district: v })}
              />
              <Field
                label="Phường / Xã"
                value={draft.ward}
                onChange={(v) => setDraft({ ...draft, ward: v })}
              />
              <Field
                label="Địa chỉ cụ thể"
                value={draft.street}
                onChange={(v) => setDraft({ ...draft, street: v })}
              />
            </div>

            {addresses.length > 0 && (
              <>
                <div className="account-divider" />
                <div className="section-head">
                  <span>03</span>
                  <div>
                    <h2>Sổ địa chỉ đã lưu ({addresses.length})</h2>
                    <p>Chọn nhanh một địa chỉ đã lưu hoặc thêm mới từ biểu mẫu trên.</p>
                  </div>
                </div>
                <div className="address-book-list">
                  {addresses.map((addr) => (
                    <div
                      key={addr.maDiaChi}
                      className={`address-book-item ${addr.laMacDinh ? 'default' : ''}`.trim()}
                    >
                      <div>
                        <strong>
                          {addr.tenNguoiNhan} · {addr.soDienThoaiNhan}
                          {addr.laMacDinh && <em className="badge badge-success">Mặc định</em>}
                        </strong>
                        <small>
                          {addr.diaChiChiTiet}, {addr.phuongXa}, {addr.tinhThanh}
                        </small>
                      </div>
                      <div className="inline-actions">
                        {!addr.laMacDinh && (
                          <>
                            <Button
                              variant="secondary"
                              onClick={() => void handlePickDefault(addr)}
                            >
                              Đặt mặc định
                            </Button>
                            <Button
                              variant="secondary"
                              onClick={() => void handleDeleteAddress(addr)}
                            >
                              Xóa
                            </Button>
                          </>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              </>
            )}

            {profileError && (
              <p className="inline-error" role="alert" style={{ marginTop: 14 }}>
                {profileError}
              </p>
            )}
            <div className="account-actions">
              <Button variant="secondary" onClick={() => setDraft(profile)}>
                Hủy thay đổi
              </Button>
              <Button variant="secondary" onClick={() => setSelectedAddressId(0)}>
                Thêm địa chỉ mới
              </Button>
              <Button disabled={saving} onClick={() => void handleSaveProfile()}>
                <Icon name="check" size={16} />{' '}
                {saving ? 'Đang lưu...' : 'Lưu địa chỉ mặc định'}
              </Button>
            </div>
          </section>
        ) : (
          <section className="account-settings-card">
            <div className="section-head">
              <span>01</span>
              <div>
                <h2>Bảo mật tài khoản</h2>
                <p>Đổi mật khẩu đăng nhập định kỳ để bảo vệ tài khoản và lịch sử đơn hàng.</p>
              </div>
            </div>
            <form onSubmit={(e) => void handleChangePassword(e)}>
              <div className="form-grid">
                <div className="full">
                  <Field
                    label="Mật khẩu hiện tại"
                    type="password"
                    placeholder="Nhập mật khẩu hiện tại (demo: 123456)"
                    value={currentPassword}
                    onChange={setCurrentPassword}
                  />
                </div>
                <Field
                  label="Mật khẩu mới"
                  type="password"
                  placeholder="Ít nhất 6 ký tự"
                  value={newPassword}
                  onChange={setNewPassword}
                />
                <Field
                  label="Nhập lại mật khẩu mới"
                  type="password"
                  placeholder="Xác nhận mật khẩu mới"
                  value={confirmPassword}
                  onChange={setConfirmPassword}
                />
              </div>
              {securityError && (
                <p className="inline-error" role="alert" style={{ marginTop: 14 }}>
                  {securityError}
                </p>
              )}
              <div className="account-actions">
                <Button type="submit" disabled={savingPassword}>
                  <Icon name="shield" size={16} />{' '}
                  {savingPassword ? 'Đang cập nhật...' : 'Cập nhật mật khẩu'}
                </Button>
              </div>
            </form>
          </section>
        )}
      </div>
    </main>
  )
}

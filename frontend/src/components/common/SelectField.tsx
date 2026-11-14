import type { ReactNode } from 'react'

export function SelectField({
  label,
  value,
  onChange,
  children,
}: {
  label?: string
  value?: string
  onChange?: (v: string) => void
  children: ReactNode
}) {
  return (
    <label className="field">
      {label && <span>{label}</span>}
      <select value={value} onChange={(e) => onChange?.(e.target.value)}>
        {children}
      </select>
    </label>
  )
}

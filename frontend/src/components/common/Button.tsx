import type { ReactNode } from 'react'

export function Button({
  children,
  variant = 'primary',
  className = '',
  onClick,
  disabled = false,
  type = 'button',
}: {
  children: ReactNode
  variant?: 'primary' | 'secondary' | 'ghost' | 'dark'
  className?: string
  onClick?: () => void
  disabled?: boolean
  type?: 'button' | 'submit'
}) {
  return (
    <button
      type={type}
      disabled={disabled}
      onClick={onClick}
      className={`btn btn-${variant} ${className}`.trim()}
    >
      {children}
    </button>
  )
}

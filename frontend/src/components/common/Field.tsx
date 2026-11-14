export function Field({
  label,
  value,
  onChange,
  placeholder,
  type = 'text',
  required = false,
}: {
  label?: string
  value?: string
  onChange?: (v: string) => void
  placeholder?: string
  type?: string
  required?: boolean
}) {
  return (
    <label className="field">
      {label && <span>{label}</span>}
      <input
        type={type}
        value={value}
        required={required}
        onChange={(e) => onChange?.(e.target.value)}
        placeholder={placeholder}
      />
    </label>
  )
}

export function Logo({ onClick }: { onClick: () => void }) {
  return (
    <button className="logo" onClick={onClick}>
      <span className="logo-mark">
        <span />
      </span>
      <span>
        Volt<span>Hub</span>
      </span>
    </button>
  )
}

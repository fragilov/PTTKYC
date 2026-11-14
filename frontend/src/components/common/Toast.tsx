import { Icon } from './Icon'

export function Toast({ text }: { text: string }) {
  return (
    <div className="toast">
      <span>
        <Icon name="check" size={16} />
      </span>
      {text}
    </div>
  )
}

import type { Product } from '../../types'

const productPhotos: Record<number, string> = {
  1: 'https://lafvintech.com/cdn/shop/products/product-image-1925125281.jpg?v=1643098279',
  2: 'https://cdn.awsli.com.br/78/78150/produto/6107454/56cd396d82.jpg',
  3: 'https://www.hellasdigital.gr/images/detailed/32/HS0204-5.jpeg',
  4: 'https://witblox.com/cdn/shop/files/products_2F4-channel-relay-module-5v_2FCzAEhQoRIu.jpg?v=1717490949',
  5: 'https://www.cloudraylaser.com/cdn/shop/products/17CS04A-170E-2.jpg?v=1632880417&width=1000',
  6: 'https://www.soleprice.com/3160-large_default/12v-10a-switching-power-supply-120w-led-driver-transformer-ac-to-dc-smps-for-led-strip-light-diy-ac.jpg',
}

export function resolveProductPhoto(product: Product): string | undefined {
  if (product.image) return product.image
  if (/arduino/i.test(product.name)) return productPhotos[1]
  if (/dht22/i.test(product.name)) return productPhotos[2]
  if (/esp32/i.test(product.name)) return productPhotos[3]
  if (/relay/i.test(product.name)) return productPhotos[4]
  return productPhotos[product.id]
}

export function ProductVisual({
  product,
  large = false,
}: {
  product: Product
  large?: boolean
}) {
  const photo = resolveProductPhoto(product)
  return (
    <div className={`product-visual photo-visual ${large ? 'large' : ''}`.trim()}>
      {photo ? (
        <img src={photo} alt={product.name} loading="lazy" referrerPolicy="no-referrer" />
      ) : (
        <span style={{ fontSize: 24, fontWeight: 700, padding: 20 }}>{product.label}</span>
      )}
      <span className="visual-code">{product.sku}</span>
    </div>
  )
}

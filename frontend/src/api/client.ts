export async function api<T = any>(
  path: string,
  data?: Record<string, string | number | boolean | null | undefined>,
  method = 'POST'
): Promise<T> {
  const params = new URLSearchParams()
  if (data) {
    for (const [key, value] of Object.entries(data)) {
      if (value !== undefined && value !== null) {
        params.append(key, String(value))
      }
    }
  }
  const response = await fetch(
    '/api' + path,
    data
      ? {
          method,
          headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
          body: params,
        }
      : undefined
  )
  const body = await response.json()
  if (!response.ok) {
    throw new Error(body.message || 'Không thể kết nối API')
  }
  return body as T
}

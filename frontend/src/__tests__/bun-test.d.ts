declare module 'bun:test' {
  export function describe(name: string, fn: () => void): void
  export function test(name: string, fn: () => void): void

  export interface Matchers<T> {
    not: Matchers<T>
    toBe(expected: T): void
    toEqual(expected: unknown): void
    toContain(expected: unknown): void
    toBeNull(): void
  }

  export function expect<T>(actual: T): Matchers<T>
}

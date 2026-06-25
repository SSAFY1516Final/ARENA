import { beforeEach, describe, expect, it, vi } from 'vitest'

describe('router auth guard', () => {
  beforeEach(() => {
    vi.resetModules()
    vi.clearAllMocks()
    window.scrollTo = vi.fn()
  })

  it('waits for fetchMe before allowing a protected route with a stale token', async () => {
    let authenticated = true
    const fetchMe = vi.fn(() => new Promise((resolve) => {
      window.setTimeout(() => {
        authenticated = false
        resolve(null)
      }, 0)
    }))

    vi.doMock('@/stores/authStore', () => ({
      useAuthStore: () => ({
        fetchMe,
        get isAuthenticated() {
          return authenticated
        },
      }),
    }))

    const { default: router } = await import('@/router')

    await router.push('/new')
    await router.isReady()
    await new Promise((resolve) => {
      window.setTimeout(resolve, 0)
    })

    expect(fetchMe).toHaveBeenCalled()
    expect(router.currentRoute.value.path).toBe('/auth')
  })
})

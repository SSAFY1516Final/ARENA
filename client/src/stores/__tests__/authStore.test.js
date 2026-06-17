import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useAuthStore } from '@/stores/authStore'
import { authApi } from '@/api/authApi'

vi.mock('@/api/authApi', () => ({
  authApi: {
    kakaoLogin: vi.fn(),
  },
}))

function createJwt(payload) {
  return `header.${btoa(JSON.stringify(payload))}.signature`
}

describe('authStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
    vi.clearAllMocks()
  })

  it('starts logged out and stores the JWT returned from Kakao backend login', async () => {
    const store = useAuthStore()
    authApi.kakaoLogin.mockResolvedValue({
      data: {
        tokenType: 'Bearer',
        accessToken: createJwt({ userId: 7, sub: 'kakao_12345', role: 'USER' }),
      },
    })

    expect(store.isAuthenticated).toBe(false)
    expect(store.user).toBeNull()

    const user = await store.loginWithKakaoCode('auth-code', 'http://localhost:5173/auth/kakao/callback')

    expect(authApi.kakaoLogin).toHaveBeenCalledWith({
      code: 'auth-code',
      redirectUri: 'http://localhost:5173/auth/kakao/callback',
    })
    expect(localStorage.getItem('arena_access_token')).toContain('header.')
    expect(user.loginId).toBe('kakao_12345')
    expect(store.isAuthenticated).toBe(true)
  })
})

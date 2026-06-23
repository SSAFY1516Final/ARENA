import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useAuthStore } from '@/stores/authStore'
import { authApi } from '@/api/authApi'
import { userApi } from '@/api/userApi'

vi.mock('@/api/authApi', () => ({
  authApi: {
    kakaoLogin: vi.fn(),
  },
}))

vi.mock('@/api/userApi', () => ({
  userApi: {
    me: vi.fn(),
    updateNickname: vi.fn(),
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
    userApi.me.mockResolvedValue({
      data: {
        userId: 7,
        loginId: 'kakao_12345',
        nickname: '민용',
      },
    })

    expect(store.isAuthenticated).toBe(false)
    expect(store.user).toBeNull()

    const user = await store.loginWithKakaoCode('auth-code', 'http://localhost:5173/auth/kakao/callback')

    expect(authApi.kakaoLogin).toHaveBeenCalledWith({
      code: 'auth-code',
      redirectUri: 'http://localhost:5173/auth/kakao/callback',
    })
    expect(userApi.me).toHaveBeenCalled()
    expect(localStorage.getItem('arena_access_token')).toContain('header.')
    expect(user.loginId).toBe('kakao_12345')
    expect(user.nickname).toBe('민용')
    expect(store.isAuthenticated).toBe(true)
  })

  it('updates the profile nickname from the user API', async () => {
    const store = useAuthStore()
    store.setSession({
      tokenType: 'Bearer',
      accessToken: createJwt({ userId: 7, sub: 'kakao_12345', role: 'USER' }),
    })
    userApi.updateNickname.mockResolvedValue({
      data: {
        userId: 7,
        loginId: 'kakao_12345',
        nickname: '아레나유저',
      },
    })

    const user = await store.updateNickname('아레나유저')

    expect(userApi.updateNickname).toHaveBeenCalledWith({ nickname: '아레나유저' })
    expect(user.nickname).toBe('아레나유저')
    expect(store.user.nickname).toBe('아레나유저')
  })
})

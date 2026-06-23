import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { authApi } from '@/api/authApi'
import { userApi } from '@/api/userApi'

const TOKEN_STORAGE_KEY = 'arena_access_token'

function decodeJwtPayload(token) {
  const payload = token.split('.')[1]
  if (!payload) return null
  const normalized = payload.replace(/-/g, '+').replace(/_/g, '/')
  const padded = normalized.padEnd(Math.ceil(normalized.length / 4) * 4, '=')
  return JSON.parse(atob(padded))
}

export const useAuthStore = defineStore('auth', () => {
  const user = ref(null)
  const accessToken = ref(localStorage.getItem(TOKEN_STORAGE_KEY))
  const loading = ref(false)
  const errorMessage = ref('')
  const profileSaving = ref(false)

  const isAuthenticated = computed(() => Boolean(accessToken.value && user.value))

  function setSession(tokenResponse) {
    accessToken.value = tokenResponse.accessToken
    localStorage.setItem(TOKEN_STORAGE_KEY, tokenResponse.accessToken)

    const payload = decodeJwtPayload(tokenResponse.accessToken)
    user.value = {
      userId: payload?.userId,
      loginId: payload?.sub,
      nickname: payload?.nickname || payload?.sub,
      role: payload?.role,
    }
    return user.value
  }

  async function loginWithKakaoCode(code, redirectUri) {
    loading.value = true
    errorMessage.value = ''
    try {
      const { data } = await authApi.kakaoLogin({ code, redirectUri })
      setSession(data)
      return await fetchMe()
    } catch (error) {
      errorMessage.value = error.userMessage || '카카오 로그인에 실패했습니다.'
      throw error
    } finally {
      loading.value = false
    }
  }

  async function logout() {
    localStorage.removeItem(TOKEN_STORAGE_KEY)
    accessToken.value = null
    user.value = null
  }

  async function fetchMe() {
    if (!user.value && accessToken.value) {
      setSession({ accessToken: accessToken.value })
    }
    if (accessToken.value) {
      try {
        const { data } = await userApi.me()
        user.value = {
          ...user.value,
          ...data,
        }
      } catch {
        // Keep the decoded token fallback so route guards do not log users out on transient API errors.
      }
    }
    return user.value
  }

  async function updateNickname(nickname) {
    profileSaving.value = true
    errorMessage.value = ''
    try {
      const { data } = await userApi.updateNickname({ nickname })
      user.value = {
        ...user.value,
        ...data,
      }
      return user.value
    } catch (error) {
      errorMessage.value = error.userMessage || '닉네임 변경에 실패했습니다.'
      throw error
    } finally {
      profileSaving.value = false
    }
  }

  return {
    user,
    accessToken,
    loading,
    errorMessage,
    profileSaving,
    isAuthenticated,
    setSession,
    loginWithKakaoCode,
    logout,
    fetchMe,
    updateNickname,
  }
})

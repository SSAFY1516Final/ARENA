import { api } from './axios'

export const authApi = {
  kakaoLogin(payload) {
    return api.post('/api/auth/kakao', payload)
  },
  logout() {
    return api.post('/api/auth/logout')
  },
}

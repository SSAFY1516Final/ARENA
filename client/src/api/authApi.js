import { api } from './axios'

export const authApi = {
  signup(payload) {
    return api.post('/api/auth/signup', payload)
  },
  login(payload) {
    return api.post('/api/auth/login', payload)
  },
  kakaoLogin(payload) {
    return api.post('/api/auth/kakao', payload)
  },
  logout() {
    return api.post('/api/auth/logout')
  },
}

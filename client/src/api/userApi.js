import { api } from './axios'

export const userApi = {
  me() {
    return api.get('/api/users/me')
  },
  updateNickname(payload) {
    return api.patch('/api/users/me/nickname', payload)
  },
}

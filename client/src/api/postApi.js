import { api } from './axios'

export const postApi = {
  list(params) {
    return api.get('/api/posts', { params })
  },
  detail(postId) {
    return api.get(`/api/posts/${postId}`)
  },
  vote(postId, payload) {
    return api.post(`/api/posts/${postId}/votes`, payload)
  },
  remove(postId) {
    return api.delete(`/api/posts/${postId}`)
  },
}

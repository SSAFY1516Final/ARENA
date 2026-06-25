import { api } from './axios'

export const commentApi = {
  create(postId, payload) {
    return api.post(`/api/posts/${postId}/comments`, payload)
  },
  remove(commentId) {
    return api.delete(`/api/comments/${commentId}`)
  },
}

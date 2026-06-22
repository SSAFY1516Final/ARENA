import { api } from './axios'

export const debateApi = {
  list() {
    return api.get('/api/debates')
  },
  detail(debateId) {
    return api.get(`/api/debates/${debateId}`)
  },
  create(payload) {
    return api.post('/api/debates', payload)
  },
  nextTurn(debateId) {
    return api.post(`/api/debates/${debateId}/turns`)
  },
  stop(debateId) {
    return api.post(`/api/debates/${debateId}/stop`)
  },
  share(debateId, payload) {
    return api.post(`/api/debates/${debateId}/share`, payload)
  },
}

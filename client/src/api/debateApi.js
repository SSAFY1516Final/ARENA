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
  generateRoundCandidates(payload) {
    return api.post('/api/ai/round-candidates', payload)
  },
  getRoundCandidateRun(runId) {
    return api.get(`/api/ai/round-candidates/runs/${runId}`)
  },
  nextTurn(debateId) {
    return api.post(`/api/debates/${debateId}/turns`)
  },
  generateInitialTurns(debateId) {
    return api.post(`/api/debates/${debateId}/turns/batch`)
  },
  stop(debateId, payload = null) {
    return api.post(`/api/debates/${debateId}/stop`, payload)
  },
  share(debateId, payload) {
    return api.post(`/api/debates/${debateId}/share`, payload)
  },
  remove(debateId) {
    return api.delete(`/api/debates/${debateId}`)
  },
}

import axios from 'axios'

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
})

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('arena_access_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status
    const messageByStatus = {
      400: '입력값을 확인해주세요.',
      401: '로그인이 필요합니다.',
      403: '요청 권한이 없습니다.',
      409: '이미 사용 중인 닉네임입니다.',
      502: 'AI 서버 응답에 실패했습니다. 잠시 후 다시 시도해주세요.',
    }

    error.userMessage = messageByStatus[status] || '요청 처리 중 오류가 발생했습니다.'
    return Promise.reject(error)
  },
)

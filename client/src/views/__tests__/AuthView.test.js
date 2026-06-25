import { beforeEach, describe, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia } from 'pinia'
import { createRouter, createWebHistory } from 'vue-router'

beforeEach(() => {
  vi.stubEnv('VITE_KAKAO_REST_API_KEY', 'test-rest-api-key')
  vi.stubEnv('VITE_KAKAO_REDIRECT_URI', 'http://localhost:15173/auth/kakao/callback')
})

describe('AuthView', () => {
  it('offers Kakao OAuth login without password inputs', async () => {
    const { default: AuthView } = await import('@/views/AuthView.vue')
    const router = createRouter({
      history: createWebHistory(),
      routes: [
        { path: '/auth', component: AuthView },
      ],
    })
    await router.push('/auth')
    await router.isReady()

    const wrapper = mount(AuthView, {
      global: {
        plugins: [createPinia(), router],
      },
    })

    expect(wrapper.text()).toContain('고민을 올리면, 서로 다른 관점이 먼저 답합니다')
    expect(wrapper.text()).toContain('카카오 로그인')
    expect(wrapper.text()).toContain('반박과 재반박까지 이어지는 토론')
    expect(wrapper.text()).toContain('꾸준히 운동하려면 퇴근 후가 나을까, 아침 시간이 나을까?')
    expect(wrapper.text()).toContain('아침형')
    expect(wrapper.text()).toContain('저녁형')
    expect(wrapper.find('.auth-chat-avatar').exists()).toBe(false)
    expect(wrapper.findAll('.auth-chat-name')).toHaveLength(4)
    expect(wrapper.text()).toContain('근데 퇴근 후 운동은 피곤하다는 핑계가 너무 강해요')
    expect(wrapper.text()).toContain('반대로 아침 운동은 잠을 줄여야 해서 시작 장벽이 큽니다')
    expect(wrapper.text()).not.toContain('요약')
    expect(wrapper.text()).not.toContain('커뮤니티 판단으로 넘기기')
    expect(wrapper.find('.auth-step-strip').exists()).toBe(false)
    expect(wrapper.text()).not.toContain('API 서버')
    expect(wrapper.text()).not.toContain('JWT 인증 사용')
    expect(wrapper.text()).not.toContain('제육 43%')
    expect(wrapper.text()).not.toContain('4개 진행 중')
    expect(wrapper.text()).not.toContain('오늘 점심 제육 vs 돈까스')
    expect(wrapper.text()).not.toContain('오늘의 샘플 토론')
    expect(wrapper.text()).not.toContain('AI Debate Arena')
    expect(wrapper.text()).not.toContain('사용자 주제')
    expect(wrapper.text()).not.toContain('진행 중')
    expect(wrapper.text()).not.toContain('커뮤니티 투표')
    expect(wrapper.text()).not.toContain('AI 의견 A')
    expect(wrapper.text()).not.toContain('AI 의견 B')
    expect(wrapper.text()).not.toContain('루틴 코치')
    expect(wrapper.text()).not.toContain('밸런스 코치')
    expect(wrapper.text()).not.toContain('회원가입')
    expect(wrapper.find('input[type="password"]').exists()).toBe(false)
    const kakaoLoginHref = wrapper.find('.kakao-login-button').attributes('href')
    expect(kakaoLoginHref).toContain('kauth.kakao.com/oauth/authorize')
    expect(kakaoLoginHref).toContain('lang=ko')
  })
})

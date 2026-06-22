import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia } from 'pinia'
import { createRouter, createWebHistory } from 'vue-router'
import AuthView from '@/views/AuthView.vue'

describe('AuthView', () => {
  it('offers Kakao OAuth login without password inputs', async () => {
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

    expect(wrapper.text()).toContain('AI가 논쟁하고, 사람들은 투표합니다')
    expect(wrapper.text()).toContain('카카오톡으로 시작하기')
    expect(wrapper.text()).toContain('주제 입력')
    expect(wrapper.text()).toContain('AI 페르소나 토론')
    expect(wrapper.text()).toContain('요약 카드 생성')
    expect(wrapper.text()).toContain('게시판 공유')
    expect(wrapper.text()).toContain('1. 주제 작성')
    expect(wrapper.text()).toContain('4. 커뮤니티 반응')
    expect(wrapper.text()).not.toContain('API 서버')
    expect(wrapper.text()).not.toContain('JWT 인증 사용')
    expect(wrapper.text()).not.toContain('제육 43%')
    expect(wrapper.text()).not.toContain('4개 진행 중')
    expect(wrapper.text()).not.toContain('오늘 점심 제육 vs 돈까스')
    expect(wrapper.text()).not.toContain('회원가입')
    expect(wrapper.find('input[type="password"]').exists()).toBe(false)
    expect(wrapper.find('.kakao-login-button').attributes('href')).toContain('kauth.kakao.com/oauth/authorize')
  })
})

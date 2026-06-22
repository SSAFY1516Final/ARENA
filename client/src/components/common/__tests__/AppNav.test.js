import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia } from 'pinia'
import { createRouter, createWebHistory } from 'vue-router'
import AppNav from '@/components/common/AppNav.vue'
import { useAuthStore } from '@/stores/authStore'

function createJwt(payload) {
  return `header.${btoa(JSON.stringify(payload))}.signature`
}

describe('AppNav', () => {
  it('renders the signed-in user as a profile chip separate from menu links', async () => {
    const router = createRouter({
      history: createWebHistory(),
      routes: [
        { path: '/auth', component: { template: '<div />' } },
        { path: '/new', component: { template: '<div />' } },
        { path: '/debates', component: { template: '<div />' } },
        { path: '/posts', component: { template: '<div />' } },
      ],
    })
    const pinia = createPinia()
    const auth = useAuthStore(pinia)
    auth.setSession({
      tokenType: 'Bearer',
      accessToken: createJwt({ userId: 7, sub: 'kakao_12345', role: 'USER' }),
    })

    const wrapper = mount(AppNav, {
      global: {
        plugins: [pinia, router],
      },
    })

    expect(wrapper.find('.profile-chip').exists()).toBe(true)
    expect(wrapper.find('.profile-avatar').text()).toBe('k')
    expect(wrapper.find('.profile-chip').text()).toContain('kakao_12345')
    expect(wrapper.find('.profile-chip').classes()).not.toContain('router-link-active')
    expect(wrapper.get('a.nav-create-link[href="/new"]').text()).toContain('새 토론')
  })
})

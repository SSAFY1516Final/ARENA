import { describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import { createPinia } from 'pinia'
import { createRouter, createWebHistory } from 'vue-router'
import AppNav from '@/components/common/AppNav.vue'
import { useAuthStore } from '@/stores/authStore'
import { userApi } from '@/api/userApi'

vi.mock('@/api/userApi', () => ({
  userApi: {
    me: vi.fn(),
    updateNickname: vi.fn(),
  },
}))

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
    expect(wrapper.find('a.nav-create-link[href="/new"]').exists()).toBe(false)
    expect(wrapper.get('a.nav-action-link[href="/new"]').text()).toContain('토론 만들기')
    expect(wrapper.get('button.logout-button').text()).toContain('로그아웃')
  })

  it('opens a compact nickname editor from the profile chip', async () => {
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

    await wrapper.get('.profile-chip').trigger('click')

    expect(wrapper.get('#nickname-editor').exists()).toBe(true)
    expect(wrapper.get('input#nickname-input').element.value).toBe('kakao_12345')
  })

  it('saves a changed nickname and closes the editor', async () => {
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
    userApi.updateNickname.mockResolvedValue({
      data: {
        userId: 7,
        loginId: 'kakao_12345',
        nickname: '새닉네임',
      },
    })

    const wrapper = mount(AppNav, {
      global: {
        plugins: [pinia, router],
      },
    })

    await wrapper.get('.profile-chip').trigger('click')
    await wrapper.get('input#nickname-input').setValue('새닉네임')
    await wrapper.get('#nickname-editor').trigger('submit')
    await flushPromises()

    expect(userApi.updateNickname).toHaveBeenCalledWith({ nickname: '새닉네임' })
    expect(auth.user.nickname).toBe('새닉네임')
    expect(wrapper.find('#nickname-editor').exists()).toBe(false)
    expect(wrapper.get('.profile-chip').text()).toContain('새닉네임')
  })

  it('logs out and returns to the auth page from the profile area', async () => {
    const router = createRouter({
      history: createWebHistory(),
      routes: [
        { path: '/auth', component: { template: '<div />' } },
        { path: '/new', component: { template: '<div />' } },
        { path: '/debates', component: { template: '<div />' } },
        { path: '/posts', component: { template: '<div />' } },
      ],
    })
    await router.push('/debates')
    await router.isReady()
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

    await wrapper.get('button.logout-button').trigger('click')
    await flushPromises()

    expect(auth.isAuthenticated).toBe(false)
    expect(localStorage.getItem('arena_access_token')).toBeNull()
    expect(router.currentRoute.value.path).toBe('/auth')
  })
})

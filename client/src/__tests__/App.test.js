import { describe, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia } from 'pinia'
import { createRouter, createWebHistory } from 'vue-router'
import { NConfigProvider, NDialogProvider, NLoadingBarProvider, NMessageProvider } from 'naive-ui'
import App from '@/App.vue'

vi.mock('@/components/common/AppNav.vue', () => ({
  default: {
    template: '<nav data-test="app-nav" />',
  },
}))

describe('App', () => {
  it('wraps the application with Naive UI providers', async () => {
    const router = createRouter({
      history: createWebHistory(),
      routes: [
        { path: '/debates', name: 'my-debates', component: { template: '<main>내 토론</main>' } },
      ],
    })
    await router.push('/debates')
    await router.isReady()

    const wrapper = mount(App, {
      global: {
        plugins: [createPinia(), router],
        stubs: {
          RouterView: true,
        },
      },
    })

    expect(wrapper.findComponent(NConfigProvider).exists()).toBe(true)
    expect(wrapper.findComponent(NMessageProvider).exists()).toBe(true)
    expect(wrapper.findComponent(NDialogProvider).exists()).toBe(true)
    expect(wrapper.findComponent(NLoadingBarProvider).exists()).toBe(true)
  })
})

import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import { createPinia } from 'pinia'
import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '@/views/HomeView.vue'
import { debateApi } from '@/api/debateApi'

vi.mock('@/api/debateApi', () => ({
  debateApi: {
    create: vi.fn(),
  },
}))

describe('HomeView', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    debateApi.create.mockResolvedValue({
      data: { debateId: 11, topic: '오후 집중력을 기준으로 제육 vs 돈까스', mode: 'PRACTICAL', status: 'ACTIVE' },
    })
  })

  it('shows a sample debate without wireframe explanation copy', async () => {
    const router = createRouter({
      history: createWebHistory(),
      routes: [
        { path: '/', component: HomeView },
        { path: '/posts', component: { template: '<div />' } },
      ],
    })

    const wrapper = mount(HomeView, {
      global: {
        plugins: [createPinia(), router],
      },
    })

    expect(wrapper.text()).toContain('샘플 토론')
    expect(wrapper.text()).not.toContain('토론 미리보기')
    expect(wrapper.text()).not.toContain('입력한 주제로 생성될 대화 예시')
    expect(wrapper.text()).not.toContain('예시')
  })

  it('places compact mode choices above the long topic field and keeps the start action on the top right', () => {
    const router = createRouter({
      history: createWebHistory(),
      routes: [
        { path: '/', component: HomeView },
        { path: '/posts', component: { template: '<div />' } },
      ],
    })

    const wrapper = mount(HomeView, {
      global: {
        plugins: [createPinia(), router],
      },
    })

    const panel = wrapper.get('.start-panel')
    const topbar = panel.get('.start-panel__topbar')

    expect(wrapper.find('a[href="/posts"]').exists()).toBe(false)
    expect(topbar.get('.mode-grid.mode-grid--compact').exists()).toBe(true)
    expect(topbar.get('button[type="submit"]').text()).toContain('토론 시작')
    expect(panel.get('input#topic').classes()).toContain('input--topic')

    const topbarText = topbar.text()
    expect(topbarText.indexOf('실용 판정')).toBeLessThan(topbarText.indexOf('토론 시작'))
    expect(panel.text().indexOf('실용 판정')).toBeLessThan(panel.text().indexOf('토론 주제'))
  })

  it('starts a debate from the entered topic', async () => {
    const router = createRouter({
      history: createWebHistory(),
      routes: [
        { path: '/', component: HomeView },
        { path: '/debates/:debateId', component: { template: '<div />' } },
      ],
    })

    const wrapper = mount(HomeView, {
      global: {
        plugins: [createPinia(), router],
      },
    })

    await wrapper.get('form.start-panel').trigger('submit')
    await flushPromises()

    expect(debateApi.create).toHaveBeenCalledWith({
      topic: '오늘 점심 제육 vs 돈까스',
      mode: 'PRACTICAL',
    })
    expect(router.currentRoute.value.path).toBe('/debates/11')
  })
})

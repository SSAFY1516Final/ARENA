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

  it('shows a clean topic workspace instead of a mode-based setup', async () => {
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

    expect(wrapper.find('.workspace-layout').exists()).toBe(true)
    expect(wrapper.find('.workspace-setup').exists()).toBe(true)
    expect(wrapper.find('.workspace-main').exists()).toBe(true)
    expect(wrapper.text()).toContain('무엇을 비교할까요?')
    expect(wrapper.text()).toContain('상황/조건')
    expect(wrapper.text()).toContain('세부 조건')
    expect(wrapper.text()).toContain('추천 주제')
    expect(wrapper.text()).toContain('하나를 골라 시작하세요')
    expect(wrapper.find('.pipeline-stage-list').exists()).toBe(false)
    expect(wrapper.find('.selected-candidate-panel').exists()).toBe(false)
    expect(wrapper.find('.pipeline-summary').exists()).toBe(false)
    expect(wrapper.find('.candidate-score-row').exists()).toBe(false)
    expect(wrapper.text()).not.toContain('실용 판정')
    expect(wrapper.text()).not.toContain('예능 배틀')
    expect(wrapper.text()).not.toContain('참신도')
    expect(wrapper.text()).not.toContain('적합도')
    expect(wrapper.text()).not.toContain('토론성')
    expect(wrapper.text()).not.toContain('실제 구현')
    expect(wrapper.text()).not.toContain('샘플 토론')
    expect(wrapper.text()).not.toContain('고민을 꺼내면')
  })

  it('places topic, condition, and detail inputs before candidate selection', () => {
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

    const panel = wrapper.get('.workspace-setup')

    expect(wrapper.find('.pipeline-candidate-grid').exists()).toBe(true)
    expect(panel.get('button[type="submit"]').text()).toContain('토론 시작')
    expect(panel.get('input#topic').classes()).toContain('input--topic')
    expect(panel.get('input#condition').exists()).toBe(true)
    expect(panel.get('textarea#details').exists()).toBe(true)

    const panelText = panel.text()
    expect(panelText.indexOf('토론 주제')).toBeLessThan(panelText.indexOf('토론 시작'))
    expect(panelText.indexOf('상황/조건')).toBeLessThan(panelText.indexOf('토론 시작'))
  })

  it('starts a debate from the selected pipeline candidate', async () => {
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

    const candidateButtons = wrapper.findAll('.pipeline-candidate-card')
    expect(candidateButtons.length).toBeGreaterThanOrEqual(3)
    await candidateButtons[1].trigger('click')
    await wrapper.get('form.workspace-setup').trigger('submit')
    await flushPromises()

    expect(debateApi.create).toHaveBeenCalledWith({
      topic: expect.stringContaining('제육'),
      mode: 'PRACTICAL',
    })
    expect(debateApi.create.mock.calls[0][0].topic).not.toBe('오늘 점심 제육 vs 돈까스')
    expect(router.currentRoute.value.path).toBe('/debates/11')
  })
})

import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import { createPinia } from 'pinia'
import { createRouter, createWebHistory } from 'vue-router'
import MyDebatesView from '@/views/MyDebatesView.vue'
import { debateApi } from '@/api/debateApi'

vi.mock('@/api/debateApi', () => ({
  debateApi: {
    list: vi.fn(),
  },
}))

describe('MyDebatesView', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    debateApi.list.mockResolvedValue({
      data: [
        { debateId: 10, topic: '오늘 점심 제육 vs 돈까스', mode: 'PRACTICAL', status: 'ACTIVE', summaryCard: 'ACTIVE', shareBody: '', updatedAt: '2026-06-11T12:00:00' },
      ],
    })
  })

  it('shows my debate topics as cards and provides a new debate button', async () => {
    const router = createRouter({
      history: createWebHistory(),
      routes: [
        { path: '/debates', component: MyDebatesView },
        { path: '/new', component: { template: '<div>새 토론</div>' } },
        { path: '/debates/:debateId', component: { template: '<div>토론방</div>' } },
      ],
    })
    await router.push('/debates')
    await router.isReady()

    const wrapper = mount(MyDebatesView, {
      global: {
        plugins: [createPinia(), router],
      },
    })
    await flushPromises()

    expect(wrapper.text()).toContain('내 토론')
    expect(wrapper.text()).toContain('오늘 점심 제육 vs 돈까스')
    expect(wrapper.findAll('.my-debate-card')).toHaveLength(1)
    expect(wrapper.findAll('a.my-debate-card')).toHaveLength(1)
    expect(wrapper.find('.share-toggle').exists()).toBe(false)
    expect(wrapper.find('.share-form').exists()).toBe(false)
    expect(wrapper.text()).not.toContain('토론방')
    expect(wrapper.text()).not.toContain('공유하기')

    const newDebateLink = wrapper.get('a[href="/new"]')
    expect(newDebateLink.text()).toContain('새 토론')
  })
})

import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import { createPinia } from 'pinia'
import { createRouter, createWebHistory } from 'vue-router'
import { NButton, NCard, NGrid, NGridItem } from 'naive-ui'
import MyDebatesView from '@/views/MyDebatesView.vue'
import { debateApi } from '@/api/debateApi'

vi.mock('@/api/debateApi', () => ({
  debateApi: {
    list: vi.fn(),
    remove: vi.fn(),
  },
}))

function createRouterForView(extraRoutes = []) {
  return createRouter({
    history: createWebHistory(),
    routes: [
      { path: '/debates', component: MyDebatesView },
      { path: '/new', component: { template: '<div>new</div>' } },
      { path: '/debates/:debateId', component: { template: '<div>room</div>' } },
      { path: '/debates/:debateId/result', component: { template: '<div>result</div>' } },
      ...extraRoutes,
    ],
  })
}

async function mountMyDebates(router = createRouterForView()) {
  await router.push('/debates')
  await router.isReady()

  const wrapper = mount(MyDebatesView, {
    global: {
      plugins: [createPinia(), router],
    },
  })
  await flushPromises()
  return { wrapper, router }
}

describe('MyDebatesView', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    debateApi.list.mockResolvedValue({
      data: [
        {
          debateId: 10,
          originalTopic: 'Lunch',
          topic: 'Which lunch standard is stronger?',
          mode: 'PRACTICAL',
          status: 'ACTIVE',
          sideALabel: '제육',
          sideBLabel: '돈까스',
          roundCount: 3,
          coolCount: 2,
          hotCount: 1,
          updatedAt: '2026-06-11T12:00:00',
        },
      ],
    })
  })

  it('shows my debate topics from the backend only', async () => {
    debateApi.list.mockResolvedValue({
      data: [
        {
          debateId: 10,
          originalTopic: 'Lunch',
          topic: 'Which lunch standard is stronger?',
          mode: 'PRACTICAL',
          status: 'ACTIVE',
          sideALabel: '제육',
          sideBLabel: '돈까스',
          roundCount: 3,
          coolCount: 2,
          hotCount: 1,
          updatedAt: '2026-06-11T12:00:00',
        },
        {
          debateId: 11,
          topic: 'Commute timing',
          mode: 'PRACTICAL',
          status: 'STOPPED',
          updatedAt: '2026-06-12T12:00:00',
        },
        {
          debateId: 12,
          topic: 'Trip lodging',
          mode: 'PRACTICAL',
          status: 'SHARED',
          updatedAt: '2026-06-13T12:00:00',
        },
      ],
    })

    const { wrapper } = await mountMyDebates()

    expect(wrapper.find('.debates-page').exists()).toBe(true)
    expect(wrapper.find('.debates-header').exists()).toBe(true)
    expect(wrapper.find('.debates-board-card').exists()).toBe(true)
    expect(wrapper.findComponent(NGrid).exists()).toBe(true)
    expect(wrapper.findAllComponents(NGridItem)).toHaveLength(3)
    expect(wrapper.findAllComponents(NCard)).toHaveLength(4)
    expect(wrapper.findComponent(NButton).exists()).toBe(true)
    expect(wrapper.findAll('.my-debate-card')).toHaveLength(3)
    expect(wrapper.text()).toContain('Lunch')
    expect(wrapper.text()).toContain('진행 라운드')
    expect(wrapper.text()).toContain('3/5')
    expect(wrapper.text()).toContain('제육 2 : 1 돈까스')
    expect(wrapper.text()).toContain('이어가기')
    expect(wrapper.text()).toContain('Commute timing')
    expect(wrapper.text()).toContain('Trip lodging')
    expect(wrapper.text()).not.toContain('9001')
    expect(wrapper.findAll('button.my-debate-card__delete')).toHaveLength(3)
  })

  it('shows an empty state without temporary preview debates when the user has no debates yet', async () => {
    debateApi.list.mockResolvedValue({ data: [] })

    const { wrapper } = await mountMyDebates()

    expect(wrapper.find('.empty-state').exists()).toBe(true)
    expect(wrapper.findAll('.my-debate-card')).toHaveLength(0)
    expect(wrapper.text()).toContain('아직 저장된 토론이 없습니다')
    expect(wrapper.text()).not.toContain('9001')
    expect(wrapper.text()).not.toContain('오늘 점심')
    expect(debateApi.remove).not.toHaveBeenCalled()
  })

  it('opens an active debate from the recent list in the debate room', async () => {
    const { wrapper, router } = await mountMyDebates()

    await wrapper.get('.my-debate-card').trigger('click')
    await flushPromises()

    expect(router.currentRoute.value.path).toBe('/debates/10')
    expect(router.currentRoute.value.query).toEqual({})
  })

  it('opens a stopped debate from the recent list in review mode', async () => {
    debateApi.list.mockResolvedValue({
      data: [
        {
          debateId: 10,
          originalTopic: 'Lunch',
          topic: 'Which lunch standard is stronger?',
          mode: 'PRACTICAL',
          status: 'STOPPED',
          updatedAt: '2026-06-11T12:00:00',
        },
      ],
    })
    const { wrapper, router } = await mountMyDebates()

    await wrapper.get('.my-debate-card').trigger('click')
    await flushPromises()

    expect(router.currentRoute.value.path).toBe('/debates/10/result')
    expect(router.currentRoute.value.query).toEqual({ from: 'recent' })
  })

  it('deletes a recent debate without opening the debate room', async () => {
    debateApi.remove.mockResolvedValue({ status: 204 })
    const { wrapper, router } = await mountMyDebates()

    await wrapper.get('button.my-debate-card__delete').trigger('click')
    await flushPromises()

    expect(debateApi.remove).toHaveBeenCalledWith(10)
    expect(router.currentRoute.value.path).toBe('/debates')
    expect(wrapper.findAll('.my-debate-card')).toHaveLength(0)
  })
})

import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import { createPinia } from 'pinia'
import { createRouter, createWebHistory } from 'vue-router'
import DebateRoomView from '@/views/DebateRoomView.vue'
import { useDebateStore } from '@/stores/debateStore'
import { debateApi } from '@/api/debateApi'

vi.mock('@/api/debateApi', () => ({
  debateApi: {
    detail: vi.fn(),
    nextTurn: vi.fn(),
    stop: vi.fn(),
    share: vi.fn(),
  },
}))

function mockDebateDetail(status = 'ACTIVE') {
  debateApi.detail.mockResolvedValue({
    data: {
      debate: { debateId: 999, topic: '오늘 점심 제육 vs 돈까스', mode: 'PRACTICAL', status },
      messages: [],
      summary: status === 'ACTIVE' ? null : { summaryText: '선택 기준 요약' },
    },
  })
}

describe('DebateRoomView', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockDebateDetail('ACTIVE')
    debateApi.stop.mockResolvedValue({
      data: { debateId: 999, status: 'STOPPED', summary: { summaryText: '선택 기준 요약' } },
    })
    debateApi.share.mockResolvedValue({
      data: { postId: 77, debateId: 10, title: '오늘 점심 제육 vs 돈까스', voteOptionA: '냉정파', voteOptionB: '열정파' },
    })
  })

  it('uses finish wording instead of result selection or stop wording', async () => {
    const router = createRouter({
      history: createWebHistory(),
      routes: [
        { path: '/debates/:debateId', component: DebateRoomView },
        { path: '/debates', component: { template: '<div />' } },
        { path: '/posts/:postId', component: { template: '<div />' } },
      ],
    })
    await router.push('/debates/999')
    await router.isReady()

    const wrapper = mount(DebateRoomView, {
      global: {
        plugins: [createPinia(), router],
      },
    })
    await flushPromises()

    expect(wrapper.text()).toContain('종료하기')
    expect(wrapper.text()).toContain('토론 진행')
    expect(wrapper.text()).not.toContain('공유하기')
    expect(wrapper.text()).not.toContain('게시판')
    expect(wrapper.text()).not.toContain('한 라운드 더')
    expect(wrapper.text()).not.toContain('결과 선택')
    expect(wrapper.text()).not.toContain('토론 멈추기')
  })

  it('does not present vote percentages as debate persona scores', async () => {
    const router = createRouter({
      history: createWebHistory(),
      routes: [
        { path: '/debates/:debateId', component: DebateRoomView },
        { path: '/debates', component: { template: '<div />' } },
        { path: '/posts/:postId', component: { template: '<div />' } },
      ],
    })
    await router.push('/debates/999')
    await router.isReady()

    const wrapper = mount(DebateRoomView, {
      global: {
        plugins: [createPinia(), router],
      },
    })
    await flushPromises()

    expect(wrapper.text()).not.toContain('58 : 42')
    expect(wrapper.text()).not.toContain('돈까스 안정성 / 제육 만족감')
    expect(wrapper.text()).not.toContain('게시판 공유 후 사용자 투표 비율이 표시됩니다.')
  })

  it('shows a compact debate status rail with primary controls', async () => {
    const router = createRouter({
      history: createWebHistory(),
      routes: [
        { path: '/debates/:debateId', component: DebateRoomView },
        { path: '/debates', component: { template: '<div />' } },
        { path: '/posts/:postId', component: { template: '<div />' } },
      ],
    })
    await router.push('/debates/999')
    await router.isReady()

    const wrapper = mount(DebateRoomView, {
      global: {
        plugins: [createPinia(), router],
      },
    })
    await flushPromises()

    expect(wrapper.find('.debate-room-layout').exists()).toBe(true)
    expect(wrapper.find('.debate-status-rail').exists()).toBe(true)
    expect(wrapper.text()).toContain('토론 컨트롤')
    expect(wrapper.text()).toContain('발화')
    expect(wrapper.text()).toContain('다음 발화')
    expect(wrapper.text()).toContain('종료')
    expect(wrapper.text()).not.toContain('게시판 보기')
  })

  it('returns to my debates when finishing an active debate', async () => {
    const router = createRouter({
      history: createWebHistory(),
      routes: [
        { path: '/debates/:debateId', component: DebateRoomView },
        { path: '/debates', component: { template: '<div>내 토론</div>' } },
        { path: '/posts/:postId', component: { template: '<div />' } },
      ],
    })
    await router.push('/debates/999')
    await router.isReady()

    const wrapper = mount(DebateRoomView, {
      global: {
        plugins: [createPinia(), router],
      },
    })

    await wrapper.get('button.finish-button').trigger('click')
    await flushPromises()
    expect(router.currentRoute.value.path).toBe('/debates')
  })

  it('allows sharing only from a finished debate and moves to the created post detail', async () => {
    mockDebateDetail('STOPPED')
    const router = createRouter({
      history: createWebHistory(),
      routes: [
        { path: '/debates/:debateId', component: DebateRoomView },
        { path: '/debates', component: { template: '<div>내 토론</div>' } },
        { path: '/posts/:postId', component: { template: '<div>게시글 상세</div>' } },
      ],
    })
    await router.push('/debates/10')
    await router.isReady()

    const pinia = createPinia()
    const debateStore = useDebateStore(pinia)
    debateStore.currentDebate = {
      debateId: 10,
      topic: '오늘 점심 제육 vs 돈까스',
      mode: 'PRACTICAL',
      status: 'STOPPED',
      peakReached: true,
      shareBody: '',
    }

    const wrapper = mount(DebateRoomView, {
      global: {
        plugins: [pinia, router],
      },
    })

    expect(wrapper.text()).toContain('공유하기')
    expect(wrapper.text()).toContain('토론 컨트롤')
    expect(wrapper.text()).not.toContain('종료하기')

    await flushPromises()
    await wrapper.get('button.share-toggle').trigger('click')
    await wrapper.get('form.share-form').trigger('submit')
    await flushPromises()

    expect(router.currentRoute.value.path).toMatch(/^\/posts\/\d+$/)
  })
})

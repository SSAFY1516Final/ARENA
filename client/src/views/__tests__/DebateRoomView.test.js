import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
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

function createMessages(count = 6) {
  return Array.from({ length: count }, (_, index) => ({
    messageId: index + 1,
    speaker: index % 2 === 0 ? 'COOL_HEADED' : 'PASSIONATE',
    roundNo: Math.floor(index / 2) + 1,
    content: `${index % 2 === 0 ? '제육' : '돈까스'} 의견 ${index + 1}`,
    peakReached: index >= 5,
  }))
}

function mockDebateDetail(status = 'ACTIVE', messages = createMessages()) {
  debateApi.detail.mockResolvedValue({
    data: {
      debate: { debateId: 999, topic: '오늘 점심 제육 vs 돈까스', mode: 'PRACTICAL', status },
      messages,
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
      data: { postId: 77, debateId: 10, title: '오늘 점심 제육 vs 돈까스', voteOptionA: '제육', voteOptionB: '돈까스' },
    })
    let nextTurnIndex = 0
    debateApi.nextTurn.mockImplementation(() => {
      const message = createMessages(6)[nextTurnIndex] || {
        messageId: nextTurnIndex + 1,
        speaker: nextTurnIndex % 2 === 0 ? 'COOL_HEADED' : 'PASSIONATE',
        roundNo: Math.floor(nextTurnIndex / 2) + 1,
        content: `추가 의견 ${nextTurnIndex + 1}`,
        peakReached: nextTurnIndex >= 5,
      }
      nextTurnIndex += 1
      return Promise.resolve({ data: message })
    })
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  it('shows the completed debate choice flow after automatic messages', async () => {
    vi.useFakeTimers()
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
    await vi.advanceTimersByTimeAsync(3600)

    expect(wrapper.text()).toContain('판단하기')
    expect(wrapper.text()).toContain('제육')
    expect(wrapper.text()).toContain('돈까스')
    expect(wrapper.text()).toContain('이 주제로 더 듣기')
    expect(wrapper.text()).not.toContain('멈추고 요약')
    expect(wrapper.text()).not.toContain('진행 상태')
    expect(wrapper.find('.debate-status-rail').exists()).toBe(false)
    expect(wrapper.text()).not.toContain('Round')
    expect(wrapper.text()).not.toContain('공유하기')
    expect(wrapper.text()).not.toContain('다음 발화')
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

  it('keeps primary actions at the bottom of the chat panel', async () => {
    vi.useFakeTimers()
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
    await vi.advanceTimersByTimeAsync(3600)

    expect(wrapper.find('.debate-room-layout').exists()).toBe(true)
    expect(wrapper.find('.debate-status-rail').exists()).toBe(false)
    expect(wrapper.find('.debate-progress-bar').exists()).toBe(false)
    expect(wrapper.text()).toContain('이 주제로 더 듣기')
    expect(wrapper.text()).toContain('판단하기')
    expect(wrapper.text()).not.toContain('멈추고 요약')
    expect(wrapper.text()).not.toContain('토론 컨트롤')
    expect(wrapper.text()).not.toContain('게시판 보기')
  })

  it('automatically appends debate messages one by one from an empty debate', async () => {
    vi.useFakeTimers()
    mockDebateDetail('ACTIVE', [])
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
    await flushPromises()

    expect(wrapper.text()).toContain('토론 진행 중')

    for (let index = 0; index < 6; index += 1) {
      await vi.advanceTimersByTimeAsync(1000)
      await flushPromises()
    }

    expect(debateApi.nextTurn).toHaveBeenCalledTimes(6)
    expect(wrapper.findAll('.debate-message')).toHaveLength(6)
    expect(wrapper.text()).toContain('판단하기')
  })

  it('stays in the room and exposes sharing after stopping an active debate', async () => {
    vi.useFakeTimers()
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
    await flushPromises()
    await vi.advanceTimersByTimeAsync(3600)

    await wrapper.get('button.finish-button').trigger('click')
    await flushPromises()
    expect(router.currentRoute.value.path).toBe('/debates/999')
    expect(wrapper.text()).toContain('어느 쪽이 더 설득됐나요?')

    await wrapper.findAll('.debate-choice')[0].trigger('click')
    await flushPromises()
    expect(wrapper.text()).toContain('다른 세부주제 보기')
    expect(wrapper.text()).toContain('토론 마무리')

    const finishButton = wrapper.findAll('button').find((button) => button.text() === '토론 마무리')
    await finishButton.trigger('click')
    await flushPromises()
    expect(wrapper.text()).toContain('요약이 준비됐습니다')
    expect(wrapper.text()).toContain('게시판에 공유')
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
    await flushPromises()

    expect(wrapper.text()).toContain('게시판에 공유')
    expect(wrapper.text()).not.toContain('진행 상태')
    expect(wrapper.text()).not.toContain('멈추고 요약')

    await wrapper.get('button.share-toggle').trigger('click')
    await wrapper.get('form.share-form').trigger('submit')
    await flushPromises()

    expect(router.currentRoute.value.path).toMatch(/^\/posts\/\d+$/)
  })
})

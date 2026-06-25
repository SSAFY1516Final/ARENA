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
    generateInitialTurns: vi.fn(),
    stop: vi.fn(),
    share: vi.fn(),
  },
}))

function createMessages(count = 10) {
  const shortContents = [
    '제육은 만족감이 큽니다.',
    '돈까스는 실패가 적습니다.',
    '제육은 보상감이 있어요.',
    '돈까스는 속도가 빠릅니다.',
    '제육은 오늘 기분에 맞습니다.',
    '돈까스는 오후 일정에 안전합니다.',
  ]
  return Array.from({ length: count }, (_, index) => ({
    messageId: index + 1,
    speaker: index % 2 === 0 ? 'COOL_HEADED' : 'PASSIONATE',
    roundNo: 1,
    content: shortContents[index % shortContents.length],
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
        roundNo: 1,
        content: '짧은 추가 의견입니다.',
        peakReached: nextTurnIndex >= 5,
      }
      nextTurnIndex += 1
      return Promise.resolve({ data: message })
    })
    debateApi.generateInitialTurns.mockResolvedValue({ data: createMessages(10) })
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  it('uses persisted side labels instead of inferred faction names', async () => {
    vi.useFakeTimers()
    debateApi.detail.mockResolvedValueOnce({
      data: {
        debate: {
          debateId: 999,
          originalTopic: 'Lunch',
          topic: 'Which lunch standard is stronger?',
          mode: 'PRACTICAL',
          status: 'ACTIVE',
          sideALabel: 'Choose A',
          sideBLabel: 'Choose B',
        },
        messages: createMessages(2),
        summary: null,
      },
    })
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
    await vi.advanceTimersByTimeAsync(4200)
    await flushPromises()

    expect(wrapper.text()).toContain('Choose A')
    expect(wrapper.text()).toContain('Choose B')
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
    await vi.advanceTimersByTimeAsync(22000)

    expect(wrapper.text()).not.toContain('내 선택')
    expect(wrapper.text()).toContain('어느 쪽 의견에 더 마음이 가나요?')
    expect(wrapper.text()).not.toContain('판단하기')
    expect(wrapper.text()).toContain('제육')
    expect(wrapper.text()).toContain('돈까스')
    expect(wrapper.find('.debate-room-page').exists()).toBe(true)
    expect(wrapper.find('.debate-topic-card').exists()).toBe(true)
    expect(wrapper.find('.debate-room-title').text()).toBe('오늘 점심 제육 vs 돈까스')
    expect(wrapper.find('.debate-topic-card .tag-row').exists()).toBe(false)
    expect(wrapper.find('.debate-chat-card').exists()).toBe(true)
    expect(wrapper.find('.debate-message-stream').exists()).toBe(true)
    expect(wrapper.text()).not.toContain('이 주제로 더 듣기')
    expect(wrapper.text()).not.toContain('토론 진행하기')
    expect(wrapper.text()).not.toContain('토론 대화')
    expect(wrapper.find('.chat-panel__header').exists()).toBe(false)
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

  it('opens recent debates directly on the result and summary step', async () => {
    const router = createRouter({
      history: createWebHistory(),
      routes: [
        { path: '/debates/:debateId/result', component: { template: '<div>result</div>' } },
        { path: '/debates/:debateId', component: DebateRoomView },
        { path: '/debates', component: { template: '<div />' } },
        { path: '/posts/:postId', component: { template: '<div />' } },
      ],
    })
    await router.push('/debates/999?from=recent')
    await router.isReady()

    const wrapper = mount(DebateRoomView, {
      global: {
        plugins: [createPinia(), router],
      },
    })
    await flushPromises()

    expect(debateApi.nextTurn).not.toHaveBeenCalled()
    expect(wrapper.findAll('.debate-message')).toHaveLength(10)
    expect(wrapper.find('.typing-message').exists()).toBe(false)
    expect(wrapper.find('.debate-choice').exists()).toBe(false)
    expect(wrapper.find('.share-toggle').exists()).toBe(true)
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
    await vi.advanceTimersByTimeAsync(22000)

    expect(wrapper.find('.debate-room-layout').exists()).toBe(true)
    expect(wrapper.find('.debate-chat-card').exists()).toBe(true)
    expect(wrapper.find('.debate-status-rail').exists()).toBe(false)
    expect(wrapper.find('.debate-progress-bar').exists()).toBe(false)
    expect(wrapper.text()).not.toContain('이 주제로 더 듣기')
    expect(wrapper.text()).toContain('어느 쪽 의견에 더 마음이 가나요?')
    expect(wrapper.text()).not.toContain('내 선택')
    expect(wrapper.text()).not.toContain('판단하기')
    expect(wrapper.text()).not.toContain('멈추고 요약')
    expect(wrapper.text()).not.toContain('토론 컨트롤')
    expect(wrapper.text()).not.toContain('게시판 보기')
  })

  it('shows the selected detailed topic as the title and the original question as a subtitle', async () => {
    mockDebateDetail(
      'ACTIVE',
      createMessages(),
    )
    debateApi.detail.mockResolvedValue({
      data: {
        debate: {
          debateId: 999,
          originalTopic: '오늘 점심 제육 vs 돈까스',
          topic: '오늘 점심 제육 vs 돈까스, 지금 바로 선택해야 한다면 무엇이 더 나은가',
          roundTitle: '지금 바로 선택하는 점심 안정성',
          mode: 'PRACTICAL',
          status: 'ACTIVE',
        },
        messages: createMessages(),
        summary: null,
      },
    })
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

    expect(wrapper.get('.debate-room-title').text()).toBe(
      '지금 바로 선택하는 점심 안정성',
    )
    expect(wrapper.get('.debate-room-detail-topic').text()).toBe(
      '오늘 점심 제육 vs 돈까스, 지금 바로 선택해야 한다면 무엇이 더 나은가',
    )
    expect(wrapper.get('.debate-room-original-topic').text()).toContain('오늘 점심 제육 vs 돈까스')
    expect(wrapper.get('.debate-room-original-topic').text()).not.toContain('원본 질문:')
  })

  it('infers the original question when old backend data duplicated the selected topic', async () => {
    debateApi.detail.mockResolvedValue({
      data: {
        debate: {
          debateId: 999,
          originalTopic: '오늘 점심 제육 vs 돈까스, 지금 바로 선택해야 한다면 무엇이 더 나은가',
          topic: '오늘 점심 제육 vs 돈까스, 지금 바로 선택해야 한다면 무엇이 더 나은가',
          mode: 'PRACTICAL',
          status: 'ACTIVE',
        },
        messages: createMessages(),
        summary: null,
      },
    })
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

    expect(wrapper.get('.debate-room-title').text()).toContain('지금 바로 선택해야 한다면')
    expect(wrapper.get('.debate-room-original-topic').text()).toContain('오늘 점심 제육 vs 돈까스')
    expect(wrapper.get('.debate-room-original-topic').text()).not.toContain('원본 질문:')
    expect(wrapper.get('.debate-room-original-topic').text()).not.toContain('지금 바로 선택해야 한다면')
  })

  it('automatically appends debate messages one by one from an empty debate', async () => {
    vi.useFakeTimers()
    mockDebateDetail('ACTIVE', [])
    const router = createRouter({
      history: createWebHistory(),
      routes: [
        { path: '/debates/:debateId', component: DebateRoomView },
        { path: '/new', component: { template: '<div>새 토론</div>' } },
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
    expect(wrapper.find('.debate-room-start-loading').exists()).toBe(false)
    expect(wrapper.find('.typing-message').exists()).toBe(true)
    expect(wrapper.text()).toContain('입력중')
    expect(wrapper.find('.debate-empty-panel').exists()).toBe(false)
    expect(wrapper.text()).not.toContain('토론을 준비하고 있습니다')

    await vi.advanceTimersByTimeAsync(1500)
    await flushPromises()

    expect(wrapper.findAll('.bubble p')).toHaveLength(0)
    expect(wrapper.find('.typing-message').exists()).toBe(true)

    for (let index = 0; index < 10; index += 1) {
      await vi.advanceTimersByTimeAsync(2000)
      await flushPromises()
    }

    expect(debateApi.generateInitialTurns).toHaveBeenCalledTimes(1)
    expect(debateApi.nextTurn).not.toHaveBeenCalled()
    expect(wrapper.findAll('.bubble p')).toHaveLength(10)
    expect(wrapper.text()).toContain('어느 쪽 의견에 더 마음이 가나요?')
    expect(wrapper.text()).not.toContain('내 선택')
    expect(wrapper.text()).not.toContain('판단하기')
  })

  it('shows the debate preparation loading inside the room for newly started debates', async () => {
    vi.useFakeTimers()
    mockDebateDetail('ACTIVE', [])
    let resolveInitialTurns
    debateApi.generateInitialTurns.mockReturnValue(new Promise((resolve) => {
      resolveInitialTurns = () => resolve({ data: createMessages(10) })
    }))
    const router = createRouter({
      history: createWebHistory(),
      routes: [
        { path: '/debates/:debateId/result', component: { template: '<div>결과</div>' } },
        { path: '/debates/:debateId', component: DebateRoomView },
        { path: '/new', component: { template: '<div>새 토론</div>' } },
        { path: '/debates', component: { template: '<div>내 토론</div>' } },
        { path: '/posts/:postId', component: { template: '<div />' } },
      ],
    })
    await router.push('/debates/999?starting=1')
    await router.isReady()

    const wrapper = mount(DebateRoomView, {
      global: {
        plugins: [createPinia(), router],
      },
    })
    await flushPromises()

    expect(wrapper.find('.debate-room-start-loading').exists()).toBe(true)
    expect(wrapper.text()).toContain('토론 대결을 준비중입니다')
    expect(wrapper.text()).toContain('1분 정도 소요될 수 있습니다')
    expect(wrapper.find('.typing-message').exists()).toBe(false)

    resolveInitialTurns()
    await flushPromises()

    for (let index = 0; index < 10; index += 1) {
      await vi.advanceTimersByTimeAsync(2000)
      await flushPromises()
    }

    expect(wrapper.find('.debate-room-start-loading').exists()).toBe(false)
    expect(wrapper.find('.debate-choice').exists()).toBe(true)
  })

  it('keeps the preparation loading visible while the initial debate generation is still pending', async () => {
    vi.useFakeTimers()
    mockDebateDetail('ACTIVE', [])
    let resolveInitialTurns
    debateApi.generateInitialTurns.mockReturnValue(new Promise((resolve) => {
      resolveInitialTurns = () => resolve({ data: createMessages(10) })
    }))
    const router = createRouter({
      history: createWebHistory(),
      routes: [
        { path: '/debates/:debateId/result', component: { template: '<div>result</div>' } },
        { path: '/debates/:debateId', component: DebateRoomView },
        { path: '/new', component: { template: '<div>new</div>' } },
        { path: '/debates', component: { template: '<div>debates</div>' } },
        { path: '/posts/:postId', component: { template: '<div />' } },
      ],
    })
    await router.push('/debates/999?starting=1')
    await router.isReady()

    const wrapper = mount(DebateRoomView, {
      global: {
        plugins: [createPinia(), router],
      },
    })
    await flushPromises()

    expect(wrapper.find('.debate-room-start-loading').exists()).toBe(true)

    await vi.advanceTimersByTimeAsync(5000)
    await flushPromises()

    expect(wrapper.find('.debate-room-start-loading').exists()).toBe(true)

    resolveInitialTurns()
    await flushPromises()
  })

  it('shows the typing indicator while revealing generated batch turns', async () => {
    vi.useFakeTimers()
    mockDebateDetail('ACTIVE', [])
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

    await vi.advanceTimersByTimeAsync(100)
    await flushPromises()

    expect(debateApi.generateInitialTurns).toHaveBeenCalledTimes(1)
    expect(debateApi.nextTurn).not.toHaveBeenCalled()
    expect(wrapper.find('.typing-message').exists()).toBe(true)
  })

  it('keeps preparation loading for direct empty debates until batch generation returns', async () => {
    vi.useFakeTimers()
    mockDebateDetail('ACTIVE', [])
    let resolveInitialTurns
    debateApi.generateInitialTurns.mockReturnValue(new Promise((resolve) => {
      resolveInitialTurns = () => resolve({ data: createMessages(10) })
    }))
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

    expect(wrapper.find('.debate-room-start-loading').exists()).toBe(true)
    expect(wrapper.find('.typing-message').exists()).toBe(false)

    await vi.advanceTimersByTimeAsync(3000)
    await flushPromises()

    expect(wrapper.find('.debate-room-start-loading').exists()).toBe(true)
    expect(wrapper.findAll('.debate-message')).toHaveLength(0)

    resolveInitialTurns()
    await flushPromises()

    await vi.advanceTimersByTimeAsync(500)
    await flushPromises()

    expect(wrapper.find('.debate-room-start-loading').exists()).toBe(false)
    expect(wrapper.find('.typing-message').exists()).toBe(true)
  })

  it('polls saved debate detail until backend background generation finishes', async () => {
    vi.useFakeTimers()
    debateApi.detail
      .mockResolvedValueOnce({
        data: {
          debate: { debateId: 999, topic: '오늘 점심 제육 vs 돈까스', mode: 'PRACTICAL', status: 'ACTIVE' },
          messages: [],
          summary: null,
        },
      })
      .mockResolvedValueOnce({
        data: {
          debate: { debateId: 999, topic: '오늘 점심 제육 vs 돈까스', mode: 'PRACTICAL', status: 'ACTIVE' },
          messages: [],
          summary: null,
        },
      })
      .mockResolvedValueOnce({
        data: {
          debate: { debateId: 999, topic: '오늘 점심 제육 vs 돈까스', mode: 'PRACTICAL', status: 'ACTIVE' },
          messages: createMessages(10),
          summary: null,
        },
      })
    debateApi.generateInitialTurns.mockResolvedValue({
      data: {
        status: 'GENERATING',
        messages: [],
      },
    })
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

    expect(debateApi.generateInitialTurns).toHaveBeenCalledTimes(1)
    expect(wrapper.find('.debate-room-start-loading').exists()).toBe(true)

    await vi.advanceTimersByTimeAsync(2000)
    await flushPromises()
    expect(debateApi.detail).toHaveBeenCalledTimes(2)
    expect(wrapper.findAll('.debate-message')).toHaveLength(0)

    await vi.advanceTimersByTimeAsync(2000)
    await flushPromises()
    expect(debateApi.detail).toHaveBeenCalledTimes(3)

    for (let index = 0; index < 10; index += 1) {
      await vi.advanceTimersByTimeAsync(2000)
      await flushPromises()
    }

    expect(wrapper.find('.debate-room-start-loading').exists()).toBe(false)
    expect(wrapper.findAll('.debate-message')).toHaveLength(10)
    expect(wrapper.text()).toContain('어느 쪽 의견에 더 마음이 가나요?')
  })

  it('does not reveal partial stored messages before completing the generated batch', async () => {
    vi.useFakeTimers()
    const partialMessages = createMessages(6)
    mockDebateDetail('ACTIVE', partialMessages)
    let resolveInitialTurns
    debateApi.generateInitialTurns.mockReturnValue(new Promise((resolve) => {
      resolveInitialTurns = () => resolve({ data: createMessages(10) })
    }))
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

    expect(debateApi.generateInitialTurns).toHaveBeenCalledTimes(1)
    expect(wrapper.find('.debate-room-start-loading').exists()).toBe(true)
    expect(wrapper.findAll('.debate-message')).toHaveLength(0)

    resolveInitialTurns()
    await flushPromises()

    await vi.advanceTimersByTimeAsync(21000)
    await flushPromises()

    expect(wrapper.findAll('.debate-message')).toHaveLength(10)
  })

  it('generates messages for a new round even when previous round messages are included in detail', async () => {
    vi.useFakeTimers()
    const previousRoundMessages = createMessages(10).map((message) => ({
      ...message,
      roundNo: 1,
      content: `previous ${message.content}`,
    }))
    const currentRoundMessages = createMessages(10).map((message) => ({
      ...message,
      messageId: 200 + message.messageId,
      roundNo: 2,
      content: `current ${message.content}`,
    }))
    debateApi.detail.mockResolvedValueOnce({
      data: {
        debate: {
          debateId: 44,
          originalTopic: '내일 뭐입을까',
          topic: '비 오는 날 옷 선택',
          roundTitle: '활동성 기준',
          mode: 'PRACTICAL',
          status: 'ACTIVE',
        },
        messages: previousRoundMessages,
        rounds: [
          {
            roundNo: 1,
            debateId: 40,
            selectedSide: 'COOL_HEADED',
            title: '기온 기준',
            topic: '기온 기준 질문',
            description: '기온 기준 설명',
          },
          {
            roundNo: 2,
            debateId: 44,
            title: '활동성 기준',
            topic: '비 오는 날 옷 선택',
            description: '활동성 기준 설명',
          },
        ],
        summary: null,
      },
    })
    debateApi.generateInitialTurns.mockResolvedValueOnce({ data: currentRoundMessages })
    const router = createRouter({
      history: createWebHistory(),
      routes: [
        { path: '/debates/:debateId', component: DebateRoomView },
        { path: '/debates', component: { template: '<div />' } },
        { path: '/posts/:postId', component: { template: '<div />' } },
      ],
    })
    await router.push('/debates/44?starting=1')
    await router.isReady()

    const wrapper = mount(DebateRoomView, {
      global: {
        plugins: [createPinia(), router],
      },
    })
    await flushPromises()

    expect(debateApi.generateInitialTurns).toHaveBeenCalledTimes(1)
    expect(wrapper.text()).not.toContain('어느 쪽 의견에 더 마음이 가나요?')

    await flushPromises()

    await vi.advanceTimersByTimeAsync(22000)
    await flushPromises()

    expect(wrapper.text()).toContain('current 제육은 만족감이 큽니다.')
    expect(wrapper.text()).not.toContain('previous 제육은 만족감이 큽니다.')
  })

  it('limits the initial active debate to five short turns per side', async () => {
    vi.useFakeTimers()
    mockDebateDetail('ACTIVE', createMessages(10))
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

    expect(wrapper.findAll('.debate-message')).toHaveLength(0)
    expect(wrapper.find('.typing-message').exists()).toBe(true)

    await vi.advanceTimersByTimeAsync(2100)
    await flushPromises()

    expect(wrapper.findAll('.debate-message').length).toBeGreaterThan(0)
    expect(wrapper.findAll('.debate-message').length).toBeLessThan(10)

    await vi.advanceTimersByTimeAsync(20000)
    await flushPromises()

    expect(wrapper.findAll('.debate-message')).toHaveLength(10)
    expect(wrapper.text()).not.toContain('6개 의견')
    expect(wrapper.text()).not.toContain('의견 7')
    expect(wrapper.text()).not.toContain('의견 6')
    wrapper.findAll('.bubble p').forEach((message) => {
      expect(message.text().length).toBeLessThanOrEqual(32)
      expect(message.text()).not.toContain('...')
    })
  })

  it('keeps stored server messages instead of replacing them with local preview messages', async () => {
    vi.useFakeTimers()
    const storedMessages = [
      {
        messageId: 101,
        speaker: 'COOL_HEADED',
        roundNo: 1,
        content:
          '예전에 들어간 너무 긴 더미 발화입니다. 한 문단이 길게 이어져서 실제 토론 화면에서 말풍선이 비정상적으로 커지는 문제가 있었습니다.',
      },
      {
        messageId: 102,
        speaker: 'PASSIONATE',
        roundNo: 2,
        content:
          '이 메시지도 오래된 더미 발화입니다. 지금 화면에서는 서버에 저장된 메시지를 그대로 확인해야 합니다.',
      },
      ...createMessages(8).map((message, index) => ({
        ...message,
        messageId: 103 + index,
        roundNo: 1,
      })),
    ]
    mockDebateDetail('ACTIVE', storedMessages)
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

    await vi.advanceTimersByTimeAsync(4200)
    await flushPromises()

    expect(wrapper.findAll('.bubble p').map((message) => message.text())).toEqual([
      storedMessages[0].content,
      storedMessages[1].content,
    ])
    expect(wrapper.text()).not.toContain('제육은 만족감이 큽니다.')
  })

  it('does not fill unavailable generated turns with local preview messages', async () => {
    vi.useFakeTimers()
    mockDebateDetail('ACTIVE', [])
    debateApi.generateInitialTurns.mockRejectedValue(new Error('AI unavailable'))
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
    await vi.advanceTimersByTimeAsync(10000)
    await flushPromises()

    expect(wrapper.findAll('.debate-message')).toHaveLength(0)
    expect(wrapper.text()).toContain('AI response generation failed. Please try again.')
    expect(wrapper.text()).not.toContain('제육은 보상감이 있어요.')
  })

  it('opens the result page quickly while the backend stop request continues', async () => {
    vi.useFakeTimers()
    debateApi.stop.mockReturnValue(new Promise((resolve) => {
      window.setTimeout(() => resolve({
        data: {
          debateId: 999,
          status: 'STOPPED',
          selectedSide: 'COOL_HEADED',
          selectedRoundNo: 1,
          summary: { summaryText: '선택 기준 요약' },
        },
      }), 5000)
    }))
    const router = createRouter({
      history: createWebHistory(),
      routes: [
        { path: '/debates/:debateId/result', component: { template: '<div>result</div>' } },
        { path: '/debates/:debateId', component: DebateRoomView },
        { path: '/new', component: { template: '<div>새 토론</div>' } },
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
    await vi.advanceTimersByTimeAsync(22000)

    expect(router.currentRoute.value.path).toBe('/debates/999')
    expect(wrapper.text()).toContain('어느 쪽 의견에 더 마음이 가나요?')
    expect(wrapper.find('button.finish-button').exists()).toBe(false)

    await wrapper.findAll('.debate-choice')[0].trigger('click')
    await vi.advanceTimersByTimeAsync(300)
    await flushPromises()
    expect(debateApi.stop).toHaveBeenCalledWith('999', {
      selectedSide: 'COOL_HEADED',
      selectedRoundNo: 1,
    })
    expect(wrapper.text()).not.toContain('토론 더 진행하기')
    expect(wrapper.text()).not.toContain('게시글 등록')
    expect(wrapper.text()).not.toContain('결과 페이지로 이동하고 있습니다.')
    expect(router.currentRoute.value.path).toBe('/debates/999/result')
    expect(router.currentRoute.value.query.choice).toBe('COOL_HEADED')
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

    expect(wrapper.text()).toContain('게시글 등록')
    expect(wrapper.text()).toContain('토론 더 진행하기')
    expect(wrapper.text()).not.toContain('종료하기')
    expect(wrapper.text()).not.toContain('진행 상태')
    expect(wrapper.text()).not.toContain('멈추고 요약')

    await wrapper.get('button.share-toggle').trigger('click')
    await flushPromises()

    expect(wrapper.find('form.share-form').exists()).toBe(false)
    expect(router.currentRoute.value.path).toMatch(/^\/posts\/\d+$/)
  })
})

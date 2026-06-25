import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import { createPinia } from 'pinia'
import { createRouter, createWebHistory } from 'vue-router'
import DebateResultView from '@/views/DebateResultView.vue'
import { debateApi } from '@/api/debateApi'

vi.mock('@/api/debateApi', () => ({
  debateApi: {
    detail: vi.fn(),
    share: vi.fn(),
  },
}))

function messages() {
  const contents = [
    '제육은 만족감이 큽니다.',
    '돈까스는 실패가 적습니다.',
    '제육은 보상감이 있어요.',
    '돈까스는 속도가 빠릅니다.',
    '제육은 오늘 기분에 맞습니다.',
    '돈까스는 오후 일정에 안전합니다.',
    '제육은 매운맛으로 집중을 깨웁니다.',
    '돈까스는 바삭한 식감이 확실합니다.',
    '제육은 밥과의 조합이 강합니다.',
    '돈까스는 누구에게나 설명하기 쉽습니다.',
  ]

  return contents.map((content, index) => ({
    messageId: index + 1,
    speaker: index % 2 === 0 ? 'COOL_HEADED' : 'PASSIONATE',
    roundNo: 1,
    content,
    peakReached: index >= 5,
  }))
}

function legacyRoundNoMessages() {
  return messages().map((message, index) => ({
    ...message,
    roundNo: Math.floor(index / 2) + 1,
  }))
}

function mockDetail() {
  debateApi.detail.mockResolvedValue({
    data: {
      debate: {
        debateId: 10,
        candidateRunId: 100,
        selectedCandidateId: 12,
        originalTopic: '오늘 점심 제육 vs 돈까스',
        topic: '오늘 점심 제육 vs 돈까스, 지금 바로 선택해야 한다면 무엇이 더 나은가',
        roundTitle: '점심 안정성',
        basicConditions: '같은 예산과 점심시간 기준',
        debateAxis: '안정성 vs 만족감',
        sideALabel: '제육',
        sideBLabel: '돈까스',
        mode: 'PRACTICAL',
        status: 'STOPPED',
        selectedSide: 'COOL_HEADED',
        selectedRoundNo: 1,
      },
      messages: messages(),
      rounds: [
        {
          roundNo: 1,
          debateId: 10,
          title: '점심 안정성',
          topic: '오늘 점심 제육 vs 돈까스, 지금 바로 선택해야 한다면 무엇이 더 나은가',
          description: '안정성 vs 만족감',
          selectedSide: 'COOL_HEADED',
        },
      ],
      summary: {
        summaryText: '제육파를 선택했습니다.',
      },
    },
  })
}

function mockSharedDetail() {
  debateApi.detail.mockResolvedValue({
    data: {
      debate: {
        debateId: 10,
        candidateRunId: 100,
        selectedCandidateId: 12,
        originalTopic: '오늘 점심 제육 vs 돈까스',
        topic: '오늘 점심 제육 vs 돈까스, 지금 바로 선택해야 한다면 무엇이 더 나은가',
        roundTitle: '점심 안정성',
        basicConditions: '같은 예산과 점심시간 기준',
        debateAxis: '안정성 vs 만족감',
        sideALabel: '제육',
        sideBLabel: '돈까스',
        mode: 'PRACTICAL',
        status: 'SHARED',
        selectedSide: 'COOL_HEADED',
        selectedRoundNo: 1,
      },
      messages: messages(),
      rounds: [
        {
          roundNo: 1,
          debateId: 10,
          title: '점심 안정성',
          topic: '오늘 점심 제육 vs 돈까스, 지금 바로 선택해야 한다면 무엇이 더 나은가',
          description: '안정성 vs 만족감',
          selectedSide: 'COOL_HEADED',
        },
      ],
      summary: {
        summaryText: '제육파를 선택했습니다.',
      },
    },
  })
}

function mockEmptyDetail() {
  debateApi.detail.mockResolvedValue({
    data: {
      debate: {
        debateId: 10,
        originalTopic: '오늘 점심 제육 vs 돈까스',
        topic: '오늘 점심 제육 vs 돈까스, 지금 바로 선택해야 한다면 무엇이 더 나은가',
        roundTitle: '점심 안정성',
        basicConditions: '같은 예산과 점심시간 기준',
        debateAxis: '안정성 vs 만족감',
        sideALabel: '제육',
        sideBLabel: '돈까스',
        mode: 'PRACTICAL',
        status: 'STOPPED',
      },
      messages: [],
      rounds: [
        {
          roundNo: 1,
          debateId: 10,
          title: '점심 안정성',
          topic: '오늘 점심 제육 vs 돈까스, 지금 바로 선택해야 한다면 무엇이 더 나은가',
          description: '안정성 vs 만족감',
        },
      ],
      summary: null,
    },
  })
}

async function mountResult(path = '/debates/10/result?choice=COOL_HEADED') {
  const router = createRouter({
    history: createWebHistory(),
    routes: [
      { path: '/debates/:debateId/result', component: DebateResultView },
      { path: '/new', component: { template: '<div>새 토론</div>' } },
      { path: '/posts/:postId', component: { template: '<div>게시글 상세</div>' } },
    ],
  })
  await router.push(path)
  await router.isReady()

  const wrapper = mount(DebateResultView, {
    global: {
      plugins: [createPinia(), router],
    },
  })
  await flushPromises()

  return { wrapper, router }
}

describe('DebateResultView', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    window.localStorage.clear()
    mockDetail()
    debateApi.share.mockResolvedValue({
      data: {
        postId: 77,
        debateId: 10,
        title: '오늘 점심 제육 vs 돈까스',
      },
    })
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  it('shows the original question and keeps the selected round review focused on title and choice', async () => {
    const { wrapper } = await mountResult()

    const resultHeader = wrapper.get('.result-summary-card')
    expect(resultHeader.get('h1').text()).toBe('오늘 점심 제육 vs 돈까스')
    expect(resultHeader.find('.result-choice-score').exists()).toBe(false)
    const sidePanel = wrapper.get('.result-side-panel')
    expect(sidePanel.get('.result-score-card .result-choice-score').text()).toBe('제육파 1 : 0 돈까스파')
    expect(sidePanel.text().indexOf('선택 현황')).toBeLessThan(sidePanel.text().indexOf('라운드 선택'))
    expect(resultHeader.find('.result-summary-metrics').exists()).toBe(false)
    expect(resultHeader.text()).not.toContain('내 선택')
    expect(resultHeader.text()).not.toContain('토론 분량')

    expect(wrapper.find('.result-hero-card').exists()).toBe(true)
    expect(wrapper.find('.result-content-grid').exists()).toBe(true)
    expect(wrapper.find('.result-transcript-card').exists()).toBe(true)
    expect(wrapper.find('.result-round-sidebar').exists()).toBe(true)
    expect(wrapper.find('.result-main-panel').exists()).toBe(true)
    expect(wrapper.find('.result-side-panel').exists()).toBe(true)
    expect(wrapper.find('.result-side-panel .result-action-card').exists()).toBe(false)
    expect(wrapper.find('.result-side-panel .result-actions').exists()).toBe(true)
    expect(wrapper.findAll('.result-round-card')).toHaveLength(1)
    expect(wrapper.find('.result-round-card--selected').exists()).toBe(true)
    expect(wrapper.find('.result-round-card--cool').exists()).toBe(true)
    expect(wrapper.find('.result-round-card').text()).toContain('1라운드')
    expect(wrapper.find('.result-round-card').text()).toContain('점심 안정성')
    expect(wrapper.find('.result-round-card').text()).not.toContain('개 발화')

    const selectedRoundHead = wrapper.get('.result-selected-round__head')
    expect(selectedRoundHead.text()).toContain('1라운드')
    expect(selectedRoundHead.get('h2').text()).toBe('점심 안정성')
    expect(selectedRoundHead.find('.result-round-description').text()).toContain(
      '오늘 점심 제육 vs 돈까스, 지금 바로 선택해야 한다면 무엇이 더 나은가',
    )
    expect(selectedRoundHead.text()).not.toContain('내 선택')
    expect(selectedRoundHead.text()).not.toContain('10개 발화')
    expect(selectedRoundHead.get('.result-selected-side-pill').text()).toBe('제육파')
    expect(selectedRoundHead.get('.result-selected-side-pill').classes()).toContain('result-selected-side-pill--cool')
    expect(wrapper.find('.result-side-panel .result-summary-note').exists()).toBe(false)
    expect(wrapper.find('.result-selected-round .result-summary-note').exists()).toBe(true)
    expect(wrapper.find('.result-selected-round').text().indexOf('요약')).toBeLessThan(
      wrapper.find('.result-selected-round').text().indexOf('제육은 만족감이 큽니다.'),
    )

    expect(wrapper.findAll('.debate-message')).toHaveLength(10)
    expect(wrapper.text()).not.toContain('10개 발화')
    expect(wrapper.text()).toContain('제육은 만족감이 큽니다.')
    expect(wrapper.text()).toContain('돈까스는 누구에게나 설명하기 쉽습니다.')
    expect(wrapper.text()).toContain('다음 라운드 진행')
    expect(wrapper.text()).not.toContain('새로운 토론')
    expect(wrapper.text()).toContain('토론 공유하기')
    expect(wrapper.find('.result-post-body-input').exists()).toBe(false)
    expect(wrapper.find('.result-actions').text().indexOf('토론 공유하기')).toBeLessThan(
      wrapper.find('.result-actions').text().indexOf('다음 라운드 진행'),
    )
  })

  it('shows an empty round state instead of a blank result when no messages are stored', async () => {
    mockEmptyDetail()

    const { wrapper } = await mountResult()

    expect(wrapper.text()).toContain('아직 저장된 토론 발화가 없습니다')
    expect(wrapper.text()).toContain('공유할 토론 내용이 아직 없습니다.')
    expect(wrapper.find('.result-share-button').attributes('disabled')).toBeDefined()
  })

  it('keeps sharing disabled while the stop summary is still being saved', async () => {
    debateApi.detail.mockResolvedValueOnce({
      data: {
        debate: {
          debateId: 10,
          originalTopic: '오늘 점심 제육 vs 돈까스',
          topic: '오늘 점심 제육 vs 돈까스, 지금 바로 선택해야 한다면 무엇이 더 나은가',
          mode: 'PRACTICAL',
          status: 'ACTIVE',
        },
        messages: messages(),
        summary: null,
      },
    })

    const { wrapper } = await mountResult()

    const shareButton = wrapper.find('.result-share-button')
    expect(shareButton.attributes('disabled')).toBeDefined()
    expect(shareButton.text()).toContain('결과 정리 중')
  })

  it('refreshes the result until the stop summary is saved', async () => {
    vi.useFakeTimers()
    debateApi.detail
      .mockResolvedValueOnce({
        data: {
          debate: {
            debateId: 10,
            originalTopic: '오늘 점심 제육 vs 돈까스',
            topic: '오늘 점심 제육 vs 돈까스, 지금 바로 선택해야 한다면 무엇이 더 나은가',
            mode: 'PRACTICAL',
            status: 'ACTIVE',
          },
          messages: messages(),
          summary: null,
        },
      })
      .mockResolvedValueOnce({
        data: {
          debate: {
            debateId: 10,
            originalTopic: '오늘 점심 제육 vs 돈까스',
            topic: '오늘 점심 제육 vs 돈까스, 지금 바로 선택해야 한다면 무엇이 더 나은가',
            roundTitle: '점심 안정성',
            sideALabel: '제육',
            sideBLabel: '돈까스',
            mode: 'PRACTICAL',
            status: 'STOPPED',
            selectedSide: 'COOL_HEADED',
            selectedRoundNo: 1,
          },
          messages: messages(),
          summary: {
            summaryText: '제육파를 선택했습니다.',
          },
        },
      })

    const { wrapper } = await mountResult()

    expect(wrapper.find('.result-share-button').attributes('disabled')).toBeDefined()
    expect(wrapper.find('.result-share-button').text()).toContain('결과 정리 중')

    await vi.advanceTimersByTimeAsync(1500)
    await flushPromises()

    expect(debateApi.detail).toHaveBeenCalledTimes(2)
    expect(wrapper.find('.result-share-button').attributes('disabled')).toBeUndefined()
    expect(wrapper.find('.result-share-button').text()).toContain('토론 공유하기')
    expect(wrapper.text()).toContain('제육파를 선택했습니다.')
  })

  it('shows an error state when the result cannot be loaded', async () => {
    debateApi.detail.mockRejectedValue(new Error('network failed'))

    const { wrapper } = await mountResult()

    expect(wrapper.text()).toContain('결과를 불러오지 못했습니다')
    expect(wrapper.text()).toContain('다시 불러오기')
  })

  it('renders legacy round numbers as selectable round cards', async () => {
    debateApi.detail.mockResolvedValueOnce({
      data: {
        debate: {
          debateId: 10,
          originalTopic: '오늘 점심 제육 vs 돈까스',
          topic: '오늘 점심 제육 vs 돈까스, 지금 바로 선택해야 한다면 무엇이 더 나은가',
          mode: 'PRACTICAL',
          status: 'STOPPED',
        },
        messages: legacyRoundNoMessages(),
        summary: {
          summaryText: '제육파를 선택했습니다.',
        },
      },
    })

    const { wrapper } = await mountResult()

    expect(wrapper.findAll('.result-round-card')).toHaveLength(5)
    expect(wrapper.findAll('.debate-message')).toHaveLength(2)
    expect(wrapper.text()).toContain('1라운드')
    expect(wrapper.text()).not.toContain('2개 발화')
    expect(wrapper.text()).toContain('제육은 만족감이 큽니다.')

    await wrapper.findAll('.result-round-card')[4].trigger('click')
    await flushPromises()

    expect(wrapper.text()).toContain('5라운드')
    expect(wrapper.findAll('.debate-message')).toHaveLength(2)
    expect(wrapper.text()).toContain('돈까스는 누구에게나 설명하기 쉽습니다.')
  })

  it('disables starting the next round after five rounds are filled', async () => {
    debateApi.detail.mockResolvedValueOnce({
      data: {
        debate: {
          debateId: 10,
          originalTopic: '오늘 점심 제육 vs 돈까스',
          topic: '5라운드 토론',
          mode: 'PRACTICAL',
          status: 'STOPPED',
          selectedSide: 'COOL_HEADED',
          selectedRoundNo: 5,
        },
        messages: legacyRoundNoMessages(),
        rounds: Array.from({ length: 5 }, (_, index) => ({
          roundNo: index + 1,
          debateId: 10 + index,
          selectedSide: index % 2 === 0 ? 'COOL_HEADED' : 'PASSIONATE',
          title: `${index + 1}라운드 주제`,
          topic: `${index + 1}라운드 질문`,
          description: `${index + 1}라운드 설명`,
        })),
        summary: {
          summaryText: '5라운드를 모두 진행했습니다.',
        },
      },
    })

    const { wrapper, router } = await mountResult('/debates/10/result?choice=COOL_HEADED')

    expect(wrapper.findAll('.result-round-card')).toHaveLength(5)
    expect(wrapper.find('.result-new-button').exists()).toBe(false)
    expect(wrapper.find('.result-share-button').exists()).toBe(true)
    expect(router.currentRoute.value.path).toBe('/debates/10/result')
  })

  it('uses per-round detail titles when the result contains multiple detailed topics', async () => {
    debateApi.detail.mockResolvedValueOnce({
      data: {
        debate: {
          debateId: 11,
          candidateRunId: 100,
          originalTopic: '오늘 점심 제육 vs 돈까스',
          topic: 'Which lunch tradeoff matters more?',
          roundTitle: 'Speed vs comfort',
          sideALabel: '제육',
          sideBLabel: '돈까스',
          mode: 'PRACTICAL',
          status: 'STOPPED',
          selectedSide: 'PASSIONATE',
          selectedRoundNo: 2,
        },
        messages: [
          ...messages().map((message) => ({
            ...message,
            messageId: message.messageId,
            roundNo: 1,
            content: `R1 ${message.content}`,
          })),
          ...messages().map((message) => ({
            ...message,
            messageId: 100 + message.messageId,
            roundNo: 2,
            content: `R2 ${message.content}`,
          })),
        ],
        rounds: [
          {
            roundNo: 1,
            debateId: 10,
            title: 'Stability vs thrill',
            topic: 'Which lunch standard is stronger?',
            description: 'Risk versus satisfaction',
            selectedSide: 'COOL_HEADED',
            summary: {
              summaryText: '제육파 1라운드 요약입니다.',
            },
          },
          {
            roundNo: 2,
            debateId: 11,
            title: 'Speed vs comfort',
            topic: 'Which lunch tradeoff matters more?',
            description: 'Speed versus comfort',
            selectedSide: 'PASSIONATE',
            summary: {
              summaryText: '돈까스파 2라운드 요약입니다.',
            },
          },
        ],
        summary: {
          summaryText: '돈까스파를 선택했습니다.',
        },
      },
    })

    const { wrapper } = await mountResult('/debates/11/result?choice=PASSIONATE')

    expect(wrapper.get('.result-score-card .result-choice-score').text()).toBe('제육파 1 : 1 돈까스파')
    expect(wrapper.findAll('.result-round-card')).toHaveLength(2)
    expect(wrapper.findAll('.result-round-card')[0].find('.result-round-card__choice').classes()).toContain(
      'result-round-card__choice--right',
    )
    expect(wrapper.findAll('.result-round-card')[0].text()).toContain('1라운드')
    expect(wrapper.findAll('.result-round-card')[0].text()).toContain('Stability vs thrill')
    expect(wrapper.findAll('.result-round-card')[0].text()).toContain('제육파')
    expect(wrapper.findAll('.result-round-card')[0].classes()).toContain('result-round-card--cool')
    expect(wrapper.findAll('.result-round-card')[1].text()).toContain('2라운드')
    expect(wrapper.findAll('.result-round-card')[1].text()).toContain('Speed vs comfort')
    expect(wrapper.findAll('.result-round-card')[1].text()).toContain('돈까스파')
    expect(wrapper.findAll('.result-round-card')[1].classes()).toContain('result-round-card--hot')
    expect(wrapper.get('.result-selected-round__head h2').text()).toBe('Speed vs comfort')
    expect(wrapper.get('.result-selected-side-pill').text()).toBe('돈까스파')
    expect(wrapper.get('.result-selected-side-pill').classes()).toContain('result-selected-side-pill--hot')
    expect(wrapper.get('.result-summary-note').text()).toContain('돈까스파 2라운드 요약입니다.')
    expect(wrapper.get('.result-summary-note').text()).not.toContain('제육파 1라운드 요약입니다.')
    expect(wrapper.text()).toContain('R2 돈까스는 누구에게나 설명하기 쉽습니다.')
    expect(wrapper.text()).not.toContain('R1 돈까스는 누구에게나 설명하기 쉽습니다.')

    await wrapper.findAll('.result-round-card')[0].trigger('click')
    await flushPromises()

    expect(wrapper.get('.result-selected-round__head h2').text()).toBe('Stability vs thrill')
    expect(wrapper.get('.result-selected-side-pill').text()).toBe('제육파')
    expect(wrapper.get('.result-selected-side-pill').classes()).toContain('result-selected-side-pill--cool')
    expect(wrapper.get('.result-summary-note').text()).toContain('제육파 1라운드 요약입니다.')
    expect(wrapper.get('.result-summary-note').text()).not.toContain('돈까스파 2라운드 요약입니다.')
    expect(wrapper.text()).toContain('R1 돈까스는 누구에게나 설명하기 쉽습니다.')
    expect(wrapper.text()).not.toContain('R2 돈까스는 누구에게나 설명하기 쉽습니다.')
  })

  it('does not color unselected rounds with the current selected side fallback', async () => {
    debateApi.detail.mockResolvedValueOnce({
      data: {
        debate: {
          debateId: 11,
          originalTopic: '오늘 점심 제육 vs 돈까스',
          topic: 'Which lunch tradeoff matters more?',
          sideALabel: '제육',
          sideBLabel: '돈까스',
          mode: 'PRACTICAL',
          status: 'STOPPED',
          selectedSide: 'PASSIONATE',
          selectedRoundNo: 2,
        },
        messages: [
          ...messages().map((message) => ({
            ...message,
            roundNo: 1,
            content: `R1 ${message.content}`,
          })),
          ...messages().map((message) => ({
            ...message,
            messageId: 100 + message.messageId,
            roundNo: 2,
            content: `R2 ${message.content}`,
          })),
        ],
        rounds: [
          {
            roundNo: 1,
            debateId: 10,
            title: 'Stability vs thrill',
            selectedSide: 'COOL_HEADED',
          },
          {
            roundNo: 2,
            debateId: 11,
            title: 'Unfinished or missing choice',
          },
        ],
        summary: null,
      },
    })

    const { wrapper } = await mountResult('/debates/11/result?choice=PASSIONATE')
    const roundCards = wrapper.findAll('.result-round-card')

    expect(wrapper.get('.result-score-card .result-choice-score').text()).toBe('제육파 1 : 0 돈까스파')
    expect(roundCards[1].text()).toContain('선택 기록 없음')
    expect(roundCards[1].classes()).toContain('result-round-card--neutral')
    expect(roundCards[1].text()).not.toContain('돈까스파')
  })

  it('restores the selected side from local storage when opened from my debates', async () => {
    window.localStorage.setItem('arena.debate.choice.10', 'PASSIONATE')
    debateApi.detail.mockResolvedValueOnce({
      data: {
        debate: {
          debateId: 10,
          originalTopic: '오늘 점심 제육 vs 돈까스',
          topic: '오늘 점심 제육 vs 돈까스, 지금 바로 선택해야 한다면 무엇이 더 나은가',
          roundTitle: '점심 안정성',
          sideALabel: '제육',
          sideBLabel: '돈까스',
          mode: 'PRACTICAL',
          status: 'STOPPED',
        },
        messages: messages(),
        rounds: [
          {
            roundNo: 1,
            debateId: 10,
            title: '점심 안정성',
            topic: '오늘 점심 제육 vs 돈까스, 지금 바로 선택해야 한다면 무엇이 더 나은가',
            description: '안정성 vs 만족감',
          },
        ],
        summary: {
          summaryText: '돈까스파를 선택했습니다.',
        },
      },
    })

    const { wrapper } = await mountResult('/debates/10/result?from=recent')

    expect(wrapper.text()).not.toContain('내 선택')
    expect(wrapper.text()).toContain('돈까스파')
    expect(wrapper.find('.result-round-card--hot').exists()).toBe(true)
  })

  it('uses the server-selected side and persisted side labels when result is reloaded', async () => {
    debateApi.detail.mockResolvedValueOnce({
      data: {
        debate: {
          debateId: 10,
          originalTopic: 'Lunch',
          topic: 'Which lunch standard is stronger?',
          mode: 'PRACTICAL',
          status: 'STOPPED',
          sideALabel: 'Choose A',
          sideBLabel: 'Choose B',
          selectedSide: 'PASSIONATE',
          selectedRoundNo: 1,
        },
        messages: messages(),
        summary: {
          summaryText: 'summary',
        },
      },
    })

    const { wrapper } = await mountResult('/debates/10/result?from=recent')

    expect(wrapper.text()).toContain('Choose B')
    expect(wrapper.text()).toContain('1')
  })

  it('falls back to the available round when an old saved selected round is no longer valid', async () => {
    debateApi.detail.mockResolvedValueOnce({
      data: {
        debate: {
          debateId: 10,
          originalTopic: 'Lunch',
          topic: 'Which lunch standard is stronger?',
          mode: 'PRACTICAL',
          status: 'STOPPED',
          selectedSide: 'COOL_HEADED',
          selectedRoundNo: 5,
        },
        messages: messages(),
        summary: {
          summaryText: 'summary',
        },
      },
    })

    const { wrapper } = await mountResult('/debates/10/result?from=recent')

    expect(wrapper.text()).toContain('1라운드')
    expect(wrapper.text()).toContain('제육은 보상감이 있어요.')
    expect(wrapper.text()).not.toContain('5라운드')
  })

  it('starts the next round from the original topic', async () => {
    const { wrapper, router } = await mountResult()

    await wrapper.get('.result-new-button').trigger('click')
    await flushPromises()

    expect(router.currentRoute.value.path).toBe('/new')
    expect(router.currentRoute.value.query).toEqual({
      topic: '오늘 점심 제육 vs 돈까스',
      candidateRunId: '100',
    })
  })

  it('registers a post from the selected round and opens the post detail', async () => {
    const { wrapper, router } = await mountResult()

    await wrapper.get('.result-share-button').trigger('click')
    await flushPromises()

    expect(wrapper.get('.result-share-modal').attributes('role')).toBe('dialog')
    await wrapper.get('.result-post-body-input').setValue('내 생각은 제육 쪽 설득력이 더 컸다.')
    await wrapper.get('.result-share-submit-button').trigger('click')
    await flushPromises()

    expect(debateApi.share).toHaveBeenCalledWith('10', {
      title: '오늘 점심 제육 vs 돈까스',
      voteOptionA: '제육파',
      voteOptionB: '돈까스파',
      isPublic: true,
      roundNo: 1,
      body: '내 생각은 제육 쪽 설득력이 더 컸다.',
    })
    expect(debateApi.share.mock.calls[0][1].body).not.toContain('돈까스는 누구에게나 설명하기 쉽습니다.')
    expect(router.currentRoute.value.path).toBe('/posts/77')
  })

  it('allows sharing another post after the debate is already shared', async () => {
    mockSharedDetail()
    const { wrapper } = await mountResult('/debates/10/result?from=recent')

    const shareButton = wrapper.get('.result-share-button')
    expect(shareButton.attributes('disabled')).toBeUndefined()

    await shareButton.trigger('click')
    await wrapper.get('.result-post-body-input').setValue('두 번째 공유 본문')
    await wrapper.get('.result-share-submit-button').trigger('click')
    await flushPromises()

    expect(debateApi.share).toHaveBeenCalledWith('10', expect.objectContaining({
      roundNo: 1,
      body: '두 번째 공유 본문',
    }))
  })
})

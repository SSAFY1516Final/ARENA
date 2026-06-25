import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import { createPinia } from 'pinia'
import { createRouter, createWebHistory } from 'vue-router'
import { NButton, NCard, NInput } from 'naive-ui'
import HomeView from '@/views/HomeView.vue'

const mockDebateApi = vi.hoisted(() => ({
  create: vi.fn(),
  generateRoundCandidates: vi.fn(),
  getRoundCandidateRun: vi.fn(),
}))

vi.mock('@/api/debateApi', () => ({
  debateApi: mockDebateApi,
}))

describe('HomeView', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.useRealTimers()
    mockDebateApi.create.mockResolvedValue({
      data: {
        debateId: 11,
        originalTopic: '오늘 점심 제육 vs 돈까스',
        topic: '오후 집중력을 기준으로 제육 vs 돈까스',
        mode: 'PRACTICAL',
        status: 'ACTIVE',
      },
    })
    mockDebateApi.generateRoundCandidates.mockResolvedValue({
      data: {
        candidateRunId: 100,
        topicFrame: {
          normalizedBigTopic: 'Lunch choice',
          sideA: 'Choose A',
          sideB: 'Choose B',
          basicConditions: 'Same budget and lunch break.',
          isDebatable: true,
        },
        roundCandidates: [
          {
            candidateId: 11,
            roundId: 'R1',
            title: 'Stability vs thrill',
            coreQuestion: 'Which lunch standard is stronger?',
            debateAxis: 'Risk versus satisfaction',
            sideAFrame: 'A lowers risk.',
            sideBFrame: 'B gives satisfaction.',
          },
          {
            candidateId: 12,
            roundId: 'R2',
            title: 'Speed vs comfort',
            coreQuestion: 'Which lunch tradeoff matters more?',
            debateAxis: 'Speed versus comfort',
            sideAFrame: 'A is faster.',
            sideBFrame: 'B is more comfortable.',
          },
          {
            candidateId: 13,
            roundId: 'R3',
            title: 'Routine recovery',
            coreQuestion: 'Which choice is easier to recover from?',
            debateAxis: 'Recovery after regret',
            sideAFrame: 'A recovers faster.',
            sideBFrame: 'B leaves less regret.',
          },
          {
            candidateId: 14,
            roundId: 'R4',
            title: 'Cost vs mood',
            coreQuestion: 'Which lunch choice balances cost and mood better?',
            debateAxis: 'Cost versus mood',
            sideAFrame: 'A is cost efficient.',
            sideBFrame: 'B improves mood.',
          },
          {
            candidateId: 15,
            roundId: 'R5',
            title: 'Familiar vs special',
            coreQuestion: 'Which lunch choice is better today?',
            debateAxis: 'Familiarity versus specialness',
            sideAFrame: 'A is familiar.',
            sideBFrame: 'B feels special.',
          },
        ],
        warnings: [],
      },
    })
  })

  it('shows only the simple topic generator before recommendations are created', async () => {
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
    expect(wrapper.find('.new-template-header').exists()).toBe(true)
    expect(wrapper.find('.new-workbench-card').exists()).toBe(true)
    expect(wrapper.find('.new-wizard-steps').exists()).toBe(false)
    expect(wrapper.find('.new-candidate-card').exists()).toBe(false)
    expect(wrapper.find('.new-preview-card').exists()).toBe(false)
    expect(wrapper.findAllComponents(NCard).length).toBe(1)
    expect(wrapper.findAllComponents(NInput).length).toBe(1)
    const topicInput = wrapper.getComponent(NInput)
    expect(topicInput.props('type')).toBe('textarea')
    expect(topicInput.props('autosize')).toEqual({ minRows: 1, maxRows: 5 })
    expect(wrapper.get('textarea#topic').element.value).toBe('')
    expect(wrapper.get('textarea#topic').attributes('placeholder')).toBe('예: 오늘 점심 제육 vs 돈까스')
    expect(wrapper.findComponent(NButton).exists()).toBe(true)
    expect(wrapper.text()).toContain('토론 생성하기')
    expect(wrapper.text()).toContain('토론 만들기')
    expect(wrapper.text()).toContain('생성하기')
    expect(wrapper.text()).not.toContain('무엇을 비교할까요?')
    expect(wrapper.text()).not.toContain('상황/조건')
    expect(wrapper.text()).not.toContain('세부 조건')
    expect(wrapper.text()).not.toContain('추천 세부 주제')
    expect(wrapper.text()).not.toContain('하나를 골라 시작하세요')
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

  it('starts with an empty topic when no topic query is provided', async () => {
    const router = createRouter({
      history: createWebHistory(),
      routes: [
        { path: '/', component: HomeView },
      ],
    })

    const wrapper = mount(HomeView, {
      global: {
        plugins: [createPinia(), router],
      },
    })

    expect(wrapper.get('textarea#topic').element.value).toBe('')
    expect(wrapper.text()).toContain('0/120')
    expect(mockDebateApi.generateRoundCandidates).not.toHaveBeenCalled()
  })

  it('shows a loading state while recommendations are being generated', async () => {
    let resolveCandidates
    mockDebateApi.generateRoundCandidates.mockReturnValue(new Promise((resolve) => {
      resolveCandidates = resolve
    }))
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
    expect(wrapper.find('.pipeline-candidate-grid').exists()).toBe(false)
    expect(panel.get('textarea#topic').exists()).toBe(true)
    await panel.get('textarea#topic').setValue('오늘 점심 제육 vs 돈까스')

    await panel.get('form').trigger('submit')

    expect(wrapper.find('.new-generation-loading').exists()).toBe(true)
    expect(panel.get('button[type="submit"]').attributes('disabled')).toBeDefined()
    expect(wrapper.find('.pipeline-candidate-grid').exists()).toBe(false)

    resolveCandidates({
      data: {
        candidateRunId: 100,
        topicFrame: { normalizedBigTopic: 'Lunch choice', isDebatable: true },
        roundCandidates: [
          { candidateId: 11, roundId: 'R1', title: 'Stability vs thrill', coreQuestion: 'Which lunch standard is stronger?' },
          { candidateId: 12, roundId: 'R2', title: 'Speed vs comfort', coreQuestion: 'Which lunch tradeoff matters more?' },
          { candidateId: 13, roundId: 'R3', title: 'Routine recovery', coreQuestion: 'Which choice is easier to recover from?' },
          { candidateId: 14, roundId: 'R4', title: 'Cost vs mood', coreQuestion: 'Which lunch choice balances cost and mood better?' },
          { candidateId: 15, roundId: 'R5', title: 'Familiar vs special', coreQuestion: 'Which lunch choice is better today?' },
        ],
        warnings: [],
      },
    })
    await flushPromises()

    expect(wrapper.find('.new-generation-loading').exists()).toBe(false)
    expect(wrapper.find('.pipeline-candidate-grid').exists()).toBe(true)
    expect(wrapper.findAll('.pipeline-candidate-card')).toHaveLength(5)
    expect(panel.get('textarea#topic').attributes('disabled')).toBeDefined()
    expect(panel.classes()).toContain('new-workbench-card--locked')
  })
  it('defaults to the first candidate but starts from the user-selected candidate', async () => {
    vi.useFakeTimers()
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

    await wrapper.get('textarea#topic').setValue('오늘 점심 제육 vs 돈까스')
    await wrapper.get('.workspace-setup form').trigger('submit')
    await vi.advanceTimersByTimeAsync(5000)
    await flushPromises()
    const candidateButtons = wrapper.findAll('.pipeline-candidate-card')
    expect(candidateButtons).toHaveLength(5)
    expect(candidateButtons[0].classes()).toContain('pipeline-candidate-card--selected')
    await candidateButtons[1].trigger('click')
    expect(wrapper.findAll('.pipeline-candidate-card')[1].classes()).toContain('pipeline-candidate-card--selected')
    await wrapper.get('.pipeline-board button').trigger('click')
    await flushPromises()

    expect(mockDebateApi.create).toHaveBeenCalledWith({
      originalTopic: '오늘 점심 제육 vs 돈까스',
      topic: 'Which lunch tradeoff matters more?',
      mode: 'PRACTICAL',
      sideALabel: 'Choose A',
      sideBLabel: 'Choose B',
      debateAxis: 'Speed versus comfort',
      sideAFrame: 'A is faster.',
      sideBFrame: 'B is more comfortable.',
      selectedRoundId: 'R2',
      roundTitle: 'Speed vs comfort',
      basicConditions: 'Same budget and lunch break.',
      candidateRunId: 100,
      selectedCandidateId: 12,
    })
    expect(mockDebateApi.create.mock.calls[0][0].topic).not.toBe('오늘 점심 제육 vs 돈까스')
    expect(router.currentRoute.value.path).toBe('/debates/11')
    expect(router.currentRoute.value.query.starting).toBe('1')
  })

  it('does not show the debate preparation loading on the candidate screen', async () => {
    vi.useFakeTimers()
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

    await wrapper.get('textarea#topic').setValue('오늘 점심 제육 vs 돈까스')
    await wrapper.get('.workspace-setup form').trigger('submit')
    await vi.advanceTimersByTimeAsync(5000)
    await flushPromises()

    await wrapper.get('.pipeline-board button').trigger('click')
    await flushPromises()

    expect(wrapper.find('.debate-start-loading').exists()).toBe(false)
    expect(router.currentRoute.value.path).toBe('/debates/11')
    expect(router.currentRoute.value.query.starting).toBe('1')
  })

  it('reopens detailed topic candidates from a debate question query', async () => {
    const router = createRouter({
      history: createWebHistory(),
      routes: [
        { path: '/', component: HomeView },
      ],
    })
    await router.push('/?topic=오늘%20점심%20제육%20vs%20돈까스&candidates=1')
    await router.isReady()

    const wrapper = mount(HomeView, {
      global: {
        plugins: [createPinia(), router],
      },
    })
    await flushPromises()

    expect(wrapper.find('.pipeline-candidate-grid').exists()).toBe(true)
    expect(wrapper.findAll('.pipeline-candidate-card')).toHaveLength(5)
    expect(mockDebateApi.generateRoundCandidates).toHaveBeenCalledWith({
      topic: '오늘 점심 제육 vs 돈까스',
      mode: 'PRACTICAL',
      candidateCount: 5,
    })
  })

  it('reuses a previous candidate run without requesting generation again', async () => {
    mockDebateApi.getRoundCandidateRun.mockResolvedValue({
      data: {
        candidateRunId: 100,
        topicFrame: {
          normalizedBigTopic: 'Lunch choice',
          sideA: 'Choose A',
          sideB: 'Choose B',
          basicConditions: 'Same budget and lunch break.',
          isDebatable: true,
        },
        roundCandidates: [
          {
            candidateId: 11,
            roundId: 'R1',
            title: 'Stability vs thrill',
            coreQuestion: 'Which lunch standard is stronger?',
            debateAxis: 'Risk versus satisfaction',
            sideAFrame: 'A lowers risk.',
            sideBFrame: 'B gives satisfaction.',
          },
        ],
        warnings: [],
      },
    })
    const router = createRouter({
      history: createWebHistory(),
      routes: [
        { path: '/', component: HomeView },
      ],
    })
    await router.push('/?topic=오늘%20점심%20제육%20vs%20돈까스&candidateRunId=100')
    await router.isReady()

    const wrapper = mount(HomeView, {
      global: {
        plugins: [createPinia(), router],
      },
    })
    await flushPromises()

    expect(mockDebateApi.getRoundCandidateRun).toHaveBeenCalledWith(100)
    expect(mockDebateApi.generateRoundCandidates).not.toHaveBeenCalled()
    expect(wrapper.find('.pipeline-candidate-grid').exists()).toBe(true)
    expect(wrapper.text()).toContain('Stability vs thrill')
  })

  it('disables detailed topics already used in the same candidate run', async () => {
    mockDebateApi.getRoundCandidateRun.mockResolvedValue({
      data: {
        candidateRunId: 100,
        topicFrame: {
          normalizedBigTopic: 'Lunch choice',
          sideA: 'Choose A',
          sideB: 'Choose B',
          basicConditions: 'Same budget and lunch break.',
          isDebatable: true,
        },
        roundCandidates: [
          {
            candidateId: 11,
            roundId: 'R1',
            title: 'Stability vs thrill',
            coreQuestion: 'Which lunch standard is stronger?',
          },
          {
            candidateId: 12,
            roundId: 'R2',
            title: 'Speed vs comfort',
            coreQuestion: 'Which lunch tradeoff matters more?',
          },
          {
            candidateId: 13,
            roundId: 'R3',
            title: 'Routine recovery',
            coreQuestion: 'Which choice is easier to recover from?',
          },
        ],
        usedCandidateIds: [11, 12],
        warnings: [],
      },
    })
    const router = createRouter({
      history: createWebHistory(),
      routes: [
        { path: '/', component: HomeView },
        { path: '/debates/:debateId', component: { template: '<div />' } },
      ],
    })
    await router.push('/?topic=오늘%20점심%20제육%20vs%20돈까스&candidateRunId=100')
    await router.isReady()

    const wrapper = mount(HomeView, {
      global: {
        plugins: [createPinia(), router],
      },
    })
    await flushPromises()

    const candidateCards = wrapper.findAll('.pipeline-candidate-card')
    expect(candidateCards).toHaveLength(3)
    expect(candidateCards[0].classes()).toContain('pipeline-candidate-card--disabled')
    expect(candidateCards[0].attributes('aria-disabled')).toBe('true')
    expect(candidateCards[1].classes()).toContain('pipeline-candidate-card--disabled')
    expect(candidateCards[1].attributes('aria-disabled')).toBe('true')
    expect(candidateCards[2].classes()).toContain('pipeline-candidate-card--selected')

    await candidateCards[0].trigger('click')
    expect(wrapper.findAll('.pipeline-candidate-card')[2].classes()).toContain('pipeline-candidate-card--selected')

    await wrapper.get('.pipeline-board button').trigger('click')
    await flushPromises()

    expect(mockDebateApi.create).toHaveBeenCalledWith(expect.objectContaining({
      topic: 'Which choice is easier to recover from?',
      candidateRunId: 100,
      selectedCandidateId: 13,
      selectedRoundId: 'R3',
    }))
  })

  it('generates candidates from the backend and starts with the selected core question', async () => {
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

    await wrapper.get('textarea#topic').setValue('오늘 점심 제육 vs 돈까스')
    await wrapper.get('.workspace-setup form').trigger('submit')
    await flushPromises()

    expect(mockDebateApi.generateRoundCandidates).toHaveBeenCalledWith({
      topic: expect.any(String),
      mode: 'PRACTICAL',
      candidateCount: 5,
    })
    expect(wrapper.text()).toContain('Stability vs thrill')
    expect(wrapper.text()).toContain('Which lunch standard is stronger?')

    await wrapper.findAll('.pipeline-candidate-card')[1].trigger('click')
    await wrapper.get('.pipeline-board button').trigger('click')
    await flushPromises()

    expect(mockDebateApi.create).toHaveBeenCalledWith({
      originalTopic: expect.any(String),
      topic: 'Which lunch tradeoff matters more?',
      mode: 'PRACTICAL',
      sideALabel: 'Choose A',
      sideBLabel: 'Choose B',
      debateAxis: 'Speed versus comfort',
      sideAFrame: 'A is faster.',
      sideBFrame: 'B is more comfortable.',
      selectedRoundId: 'R2',
      roundTitle: 'Speed vs comfort',
      basicConditions: 'Same budget and lunch break.',
      candidateRunId: 100,
      selectedCandidateId: 12,
    })
    expect(router.currentRoute.value.path).toBe('/debates/11')
  })
})

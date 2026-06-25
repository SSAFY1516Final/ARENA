import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useDebateStore } from '@/stores/debateStore'
import { debateApi } from '@/api/debateApi'

vi.mock('@/api/debateApi', () => ({
  debateApi: {
    list: vi.fn(),
    detail: vi.fn(),
    create: vi.fn(),
    nextTurn: vi.fn(),
    generateInitialTurns: vi.fn(),
    stop: vi.fn(),
    share: vi.fn(),
    remove: vi.fn(),
    generateRoundCandidates: vi.fn(),
    getRoundCandidateRun: vi.fn(),
  },
}))

describe('debateStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('uses backend debate APIs for create, turn, stop, and share', async () => {
    const store = useDebateStore()
    debateApi.create.mockResolvedValue({
      data: {
        debateId: 1,
        originalTopic: '오늘 점심 제육 vs 돈까스',
        topic: '오늘 점심 제육 vs 돈까스, 지금 바로 선택해야 한다면 무엇이 더 나은가',
        mode: 'PRACTICAL',
        status: 'ACTIVE',
      },
    })
    debateApi.nextTurn.mockResolvedValue({ data: { messageId: 10, speaker: 'COOL_HEADED', roundNo: 1, content: '제육이 안정적입니다.', peakReached: false } })
    debateApi.stop.mockResolvedValue({ data: { debateId: 1, status: 'STOPPED', summary: { summaryText: '선택 기준 요약' } } })
    debateApi.share.mockResolvedValue({ data: { postId: 5, debateId: 1, title: '오늘 점심 제육 vs 돈까스', voteOptionA: '제육', voteOptionB: '돈까스' } })

    const debate = await store.createDebate({
      originalTopic: '오늘 점심 제육 vs 돈까스',
      topic: '오늘 점심 제육 vs 돈까스, 지금 바로 선택해야 한다면 무엇이 더 나은가',
      mode: 'PRACTICAL',
    })
    expect(debate.status).toBe('ACTIVE')
    expect(debate.originalTopic).toBe('오늘 점심 제육 vs 돈까스')

    const message = await store.generateNextTurn(debate.debateId)
    expect(message.roundNo).toBe(1)
    expect(store.messages).toHaveLength(1)

    const stopped = await store.stopDebate(debate.debateId)
    expect(stopped.status).toBe('STOPPED')
    expect(store.summary.summaryText).toContain('선택 기준')

    const post = await store.shareDebate(debate.debateId, {
      title: '오늘 점심 제육 vs 돈까스',
      voteOptionA: '제육',
      voteOptionB: '돈까스',
      isPublic: true,
    })
    expect(post.postId).toBe(5)
    expect(store.currentDebate.status).toBe('SHARED')
  })

  it('loads my debates and a debate detail from backend data', async () => {
    const store = useDebateStore()
    debateApi.list.mockResolvedValue({
      data: [{
        debateId: 2,
        originalTopic: '오타니 10명 vs 북극곰',
        topic: '오타니 10명 vs 북극곰',
        candidateRunId: 100,
        selectedCandidateId: 11,
        mode: 'ENTERTAINMENT',
        status: 'ACTIVE',
        summaryCard: 'ACTIVE',
        shareBody: '',
        updatedAt: '2026-06-11T12:00:00',
      }],
    })
    debateApi.detail.mockResolvedValue({
      data: {
        debate: { debateId: 2, originalTopic: '오타니 10명 vs 북극곰', topic: '오타니 10명 vs 북극곰', mode: 'ENTERTAINMENT', status: 'ACTIVE' },
        messages: [{ id: 9, speaker: 'PASSIONATE', roundNo: 1, content: '상상력이 중요합니다.' }],
        rounds: [{ roundNo: 1, debateId: 2, selectedSide: 'COOL_HEADED', title: '신체 능력', topic: '오타니 vs 북극곰', description: '힘 vs 전략' }],
        summary: null,
      },
    })

    await store.fetchMyDebates()
    await store.fetchDebate(2)

    expect(store.myDebates).toHaveLength(1)
    expect(store.myDebates[0].candidateRunId).toBe(100)
    expect(store.myDebates[0].selectedCandidateId).toBe(11)
    expect(store.currentDebate.originalTopic).toContain('오타니')
    expect(store.currentDebate.topic).toContain('오타니')
    expect(store.messages[0].messageId).toBe(9)
    expect(store.rounds[0]).toEqual({
      roundNo: 1,
      debateId: 2,
      selectedSide: 'COOL_HEADED',
      title: '신체 능력',
      topic: '오타니 vs 북극곰',
      description: '힘 vs 전략',
      summary: null,
    })
  })

  it('deletes a debate from the backend and removes it from my debates', async () => {
    const store = useDebateStore()
    store.myDebates = [
      { debateId: 2, topic: 'delete me', status: 'ACTIVE' },
      { debateId: 3, topic: 'keep me', status: 'STOPPED' },
    ]
    store.currentDebate = { debateId: 2, topic: 'delete me', status: 'ACTIVE' }
    debateApi.remove.mockResolvedValue({ status: 204 })

    await store.deleteDebate(2)

    expect(debateApi.remove).toHaveBeenCalledWith(2)
    expect(store.myDebates.map((debate) => debate.debateId)).toEqual([3])
    expect(store.currentDebate.debateId).toBeNull()
  })

  it('maps initial turn generation status responses from the backend', async () => {
    const store = useDebateStore()
    store.currentDebate = { debateId: 3, topic: 'Lunch', status: 'ACTIVE' }
    debateApi.generateInitialTurns.mockResolvedValue({
      data: {
        status: 'GENERATING',
        messages: [
          { messageId: 1, speaker: 'COOL_HEADED', roundNo: 1, content: 'Stored first turn.', peakReached: false },
        ],
      },
    })

    const response = await store.generateInitialTurns(3)

    expect(response.status).toBe('GENERATING')
    expect(response.messages).toHaveLength(1)
    expect(store.messages[0].content).toBe('Stored first turn.')
    expect(store.turnLoading).toBe(false)
  })

  it('generates round candidates from the backend and stores them', async () => {
    const store = useDebateStore()
    debateApi.generateRoundCandidates.mockResolvedValue({
      data: {
        candidateRunId: 100,
        topicFrame: { normalizedBigTopic: 'Lunch choice', isDebatable: true },
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
        usedCandidateIds: [11],
        warnings: [],
      },
    })

    const response = await store.generateRoundCandidates({
      topic: 'lunch',
      mode: 'PRACTICAL',
      candidateCount: 5,
    })

    expect(debateApi.generateRoundCandidates).toHaveBeenCalledWith({
      topic: 'lunch',
      mode: 'PRACTICAL',
      candidateCount: 5,
    })
    expect(response.roundCandidates[0].coreQuestion).toContain('lunch')
    expect(store.candidateRunId).toBe(100)
    expect(store.roundCandidates[0].candidateId).toBe(11)
    expect(store.roundCandidates[0].title).toBe('Stability vs thrill')
    expect(store.usedCandidateIds).toEqual([11])
    expect(store.candidateGenerationLoading).toBe(false)
  })

  it('loads a previous candidate run without generating new candidates', async () => {
    const store = useDebateStore()
    debateApi.getRoundCandidateRun.mockResolvedValue({
      data: {
        candidateRunId: 100,
        topicFrame: { normalizedBigTopic: 'Lunch choice', isDebatable: true },
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
        usedCandidateIds: [11],
        warnings: [],
      },
    })

    const response = await store.fetchRoundCandidateRun(100)

    expect(debateApi.getRoundCandidateRun).toHaveBeenCalledWith(100)
    expect(debateApi.generateRoundCandidates).not.toHaveBeenCalled()
    expect(response.candidateRunId).toBe(100)
    expect(store.candidateRunId).toBe(100)
    expect(store.roundCandidates[0].candidateId).toBe(11)
    expect(store.usedCandidateIds).toEqual([11])
    expect(store.candidateGenerationLoading).toBe(false)
  })
})

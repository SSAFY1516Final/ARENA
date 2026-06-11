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
    stop: vi.fn(),
    share: vi.fn(),
  },
}))

describe('debateStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('uses backend debate APIs for create, turn, stop, and share', async () => {
    const store = useDebateStore()
    debateApi.create.mockResolvedValue({ data: { debateId: 1, topic: '오늘 점심 제육 vs 돈까스', mode: 'PRACTICAL', status: 'ACTIVE' } })
    debateApi.nextTurn.mockResolvedValue({ data: { messageId: 10, speaker: 'COOL_HEADED', roundNo: 1, content: '제육이 안정적입니다.', peakReached: false } })
    debateApi.stop.mockResolvedValue({ data: { debateId: 1, status: 'STOPPED', summary: { summaryText: '선택 기준 요약' } } })
    debateApi.share.mockResolvedValue({ data: { postId: 5, debateId: 1, title: '오늘 점심 제육 vs 돈까스', voteOptionA: '제육', voteOptionB: '돈까스' } })

    const debate = await store.createDebate({
      topic: '오늘 점심 제육 vs 돈까스',
      mode: 'PRACTICAL',
    })
    expect(debate.status).toBe('ACTIVE')

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
      data: [{ debateId: 2, topic: '오타니 10명 vs 북극곰', mode: 'ENTERTAINMENT', status: 'ACTIVE', summaryCard: 'ACTIVE', shareBody: '', updatedAt: '2026-06-11T12:00:00' }],
    })
    debateApi.detail.mockResolvedValue({
      data: {
        debate: { debateId: 2, topic: '오타니 10명 vs 북극곰', mode: 'ENTERTAINMENT', status: 'ACTIVE' },
        messages: [{ id: 9, speaker: 'PASSIONATE', roundNo: 1, content: '상상력이 중요합니다.' }],
        summary: null,
      },
    })

    await store.fetchMyDebates()
    await store.fetchDebate(2)

    expect(store.myDebates).toHaveLength(1)
    expect(store.currentDebate.topic).toContain('오타니')
    expect(store.messages[0].messageId).toBe(9)
  })
})

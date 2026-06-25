import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { usePostStore } from '@/stores/postStore'
import { postApi } from '@/api/postApi'

vi.mock('@/api/postApi', () => ({
  postApi: {
    list: vi.fn(),
    detail: vi.fn(),
    vote: vi.fn(),
    remove: vi.fn(),
  },
}))

describe('postStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('loads posts from the backend list response', async () => {
    const store = usePostStore()
    postApi.list.mockResolvedValue({
      data: {
        items: [{
          postId: 5,
          title: '제육 vs 돈까스',
          mode: 'PRACTICAL',
          summaryCard: 'AI 요약',
          shareBody: '세부주제\n점심 안정성\n\n상세설명\n점심 선택 기준\n\n본문\n내가 직접 쓴 공유 본문',
          commentCount: 0,
          voteOptionA: '제육',
          voteOptionB: '돈까스',
          voteCountA: 2,
          voteCountB: 1,
        }],
        page: 1,
        size: 10,
        totalCount: 1,
      },
    })

    await store.fetchPosts({ keyword: '제육', mode: 'PRACTICAL', sort: 'latest' })
    expect(store.posts).toHaveLength(1)
    expect(store.posts[0].title).toContain('제육')
    expect(store.posts[0].body).toBe('내가 직접 쓴 공유 본문')
    expect(store.posts[0].shareBody).toBe('내가 직접 쓴 공유 본문')
    expect(store.posts[0].voteA).toBe(2)
  })

  it('loads post detail and applies backend vote response', async () => {
    const store = usePostStore()
    postApi.detail.mockResolvedValue({
      data: {
        post: {
          postId: 5,
          title: '제육 vs 돈까스',
          mode: 'PRACTICAL',
          summaryCard: 'AI 요약',
          shareBody: '상세에서 보여야 하는 공유 본문',
          commentCount: 0,
          voteOptionA: '제육',
          voteOptionB: '돈까스',
          voteCountA: 2,
          voteCountB: 1,
          userVoteChoice: 'B',
          isOwner: true,
        },
        messages: [],
        comments: [],
      },
    })
    postApi.vote.mockResolvedValue({
      data: { postId: 5, voteOptionA: '제육', voteOptionB: '돈까스', voteCountA: 3, voteCountB: 1, voteRatioA: 75, voteRatioB: 25 },
    })

    await store.fetchPost(5)
    expect(store.postDetail.userVoteChoice).toBe('B')
    expect(store.postDetail.isOwner).toBe(true)

    const result = await store.vote(5, { choice: 'A' })

    expect(result.choice).toBe('A')
    expect(store.postDetail.body).toBe('상세에서 보여야 하는 공유 본문')
    expect(store.postDetail.userVoteChoice).toBe('A')
    expect(store.postDetail.voteA).toBe(3)
    expect(store.postDetail.voteB).toBe(1)
  })

  it('clears loaded post detail after deleting that post', async () => {
    const store = usePostStore()
    store.posts = [{ postId: 5, title: '제육 vs 돈까스' }]
    store.postDetail = { postId: 5, title: '제육 vs 돈까스' }
    postApi.remove.mockResolvedValue({})

    await store.deletePost(5)

    expect(postApi.remove).toHaveBeenCalledWith(5)
    expect(store.posts).toHaveLength(0)
    expect(store.postDetail).toBeNull()
  })

  it('does not add the same detail comment twice', () => {
    const store = usePostStore()
    store.postDetail = {
      postId: 5,
      comments: [{ commentId: 12, postId: 5, authorNickname: '나', content: '이미 있는 댓글' }],
    }

    store.addCommentToDetail({ commentId: 12, postId: 5, authorNickname: '나', content: '이미 있는 댓글' })

    expect(store.postDetail.comments).toHaveLength(1)
  })
})

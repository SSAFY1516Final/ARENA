import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import { createPinia } from 'pinia'
import { createRouter, createWebHistory } from 'vue-router'
import PostDetailView from '@/views/PostDetailView.vue'
import { postApi } from '@/api/postApi'

vi.mock('@/api/postApi', () => ({
  postApi: {
    detail: vi.fn(),
    vote: vi.fn(),
  },
}))

const detailResponse = {
  post: {
    postId: 5,
    title: '오늘 점심 제육 vs 돈까스',
    mode: 'PRACTICAL',
    summaryCard: 'AI가 생성한 토론 요약입니다.',
    shareBody: '내가 직접 작성한 게시글 본문입니다.',
    commentCount: 0,
    voteOptionA: '제육',
    voteOptionB: '돈까스',
    voteCountA: 42,
    voteCountB: 58,
  },
  messages: [{ id: 1, speaker: 'COOL_HEADED', roundNo: 1, content: '안정성을 택하면 돈까스, 지금의 만족을 택하면 제육입니다.' }],
  comments: [],
}

describe('PostDetailView', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    postApi.detail.mockResolvedValue({ data: detailResponse })
    postApi.vote.mockResolvedValue({
      data: { postId: 5, voteOptionA: '제육', voteOptionB: '돈까스', voteCountA: 43, voteCountB: 57, voteRatioA: 43, voteRatioB: 57 },
    })
  })

  it('shows the sharer body and moves voting below the debate log without a summary section', async () => {
    const router = createRouter({
      history: createWebHistory(),
      routes: [{ path: '/posts/:postId', component: PostDetailView }],
    })
    await router.push('/posts/5')
    await router.isReady()

    const wrapper = mount(PostDetailView, {
      global: {
        plugins: [createPinia(), router],
      },
    })
    await flushPromises()

    const text = wrapper.text()

    expect(text).toContain('내가 직접 작성한 게시글 본문입니다.')
    expect(text).not.toContain('AI가 생성한 토론 요약입니다.')
    expect(wrapper.find('.summary-detail').exists()).toBe(false)
    expect(text).not.toContain('핵심 주장')
    expect(text).not.toContain('선택 기준')
    expect(text.indexOf('공유 라운드 토론 로그')).toBeLessThan(text.indexOf('사용자 투표'))
  })

  it('hides vote percentages until the user votes', async () => {
    const router = createRouter({
      history: createWebHistory(),
      routes: [{ path: '/posts/:postId', component: PostDetailView }],
    })
    await router.push('/posts/101')
    await router.isReady()

    const wrapper = mount(PostDetailView, {
      global: {
        plugins: [createPinia(), router],
      },
    })
    await flushPromises()

    expect(wrapper.text()).toContain('사용자 투표')
    expect(wrapper.text()).toContain('투표하고 결과 보기')
    expect(wrapper.text()).not.toContain('42%')
    expect(wrapper.text()).not.toContain('58%')
  })

  it('shows the selected vote and blocks duplicate voting', async () => {
    const router = createRouter({
      history: createWebHistory(),
      routes: [{ path: '/posts/:postId', component: PostDetailView }],
    })
    await router.push('/posts/5')
    await router.isReady()

    const wrapper = mount(PostDetailView, {
      global: {
        plugins: [createPinia(), router],
      },
    })
    await flushPromises()

    const voteButtons = wrapper.findAll('.vote-choice')
    await voteButtons[0].trigger('click')
    await flushPromises()

    expect(wrapper.text()).toContain('내 선택: 제육')
    expect(wrapper.text()).toContain('43%')
    expect(wrapper.text()).toContain('57%')
    expect(voteButtons[0].attributes('disabled')).toBeDefined()
    expect(voteButtons[1].attributes('disabled')).toBeDefined()
  })
})

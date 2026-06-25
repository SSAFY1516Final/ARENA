import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createRouter, createWebHistory } from 'vue-router'
import PostDetailView from '@/views/PostDetailView.vue'
import { postApi } from '@/api/postApi'
import { commentApi } from '@/api/commentApi'
import { useAuthStore } from '@/stores/authStore'

vi.mock('@/api/postApi', () => ({
  postApi: {
    detail: vi.fn(),
    vote: vi.fn(),
    remove: vi.fn(),
  },
}))

vi.mock('@/api/commentApi', () => ({
  commentApi: {
    create: vi.fn(),
    remove: vi.fn(),
  },
}))

vi.mock('@/api/userApi', () => ({
  userApi: {
    me: vi.fn(),
    updateNickname: vi.fn(),
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
    isOwner: false,
  },
  round: {
    roundNo: 1,
    debateId: 11,
    selectedSide: 'COOL_HEADED',
    title: '점심 안정성',
    topic: '오늘 점심 제육 vs 돈까스, 지금 바로 선택해야 한다면 무엇이 더 나은가',
    description: '안정성 vs 만족감',
    summary: {
      summaryText: 'AI가 생성한 토론 요약입니다.',
    },
  },
  summary: {
    summaryText: 'AI가 생성한 토론 요약입니다.',
  },
  messages: [{ id: 1, speaker: 'COOL_HEADED', roundNo: 1, content: '안정성을 택하면 돈까스, 지금의 만족을 택하면 제육입니다.' }],
  comments: [],
}

function createTestPinia(authenticated = false) {
  const pinia = createPinia()
  setActivePinia(pinia)
  if (authenticated) {
    const authStore = useAuthStore()
    authStore.accessToken = 'test-token'
    authStore.user = {
      userId: 7,
      loginId: 'kakao_12345',
      nickname: '나',
      role: 'USER',
    }
  }
  return pinia
}

describe('PostDetailView', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    postApi.detail.mockResolvedValue({ data: detailResponse })
    postApi.vote.mockResolvedValue({
      data: { postId: 5, voteOptionA: '제육', voteOptionB: '돈까스', voteCountA: 43, voteCountB: 57, voteRatioA: 43, voteRatioB: 57 },
    })
    postApi.remove.mockResolvedValue({})
    commentApi.create.mockResolvedValue({
      data: {
        commentId: 99,
        postId: 5,
        authorNickname: '나',
        content: '엔터로 등록한 댓글',
        isOwner: true,
      },
    })
    commentApi.remove.mockResolvedValue({})
  })

  it('shows the sharer body, detailed topic, and summary around the debate log', async () => {
    const router = createRouter({
      history: createWebHistory(),
      routes: [{ path: '/posts/:postId', component: PostDetailView }],
    })
    await router.push('/posts/5')
    await router.isReady()

    const wrapper = mount(PostDetailView, {
      global: {
        plugins: [createTestPinia(), router],
      },
    })
    await flushPromises()

    const text = wrapper.text()

    expect(text).toContain('내가 직접 작성한 게시글 본문입니다.')
    expect(text).toContain('AI가 생성한 토론 요약입니다.')
    expect(wrapper.get('.post-share-body-box').text()).toContain('내가 직접 작성한 게시글 본문입니다.')
    expect(wrapper.get('.log-box .post-log-context').text()).toContain('세부주제')
    expect(wrapper.get('.log-box .post-log-context').text()).toContain('점심 안정성')
    expect(wrapper.get('.log-box .post-log-context').text()).toContain('지금 바로 선택해야 한다면')
    expect(wrapper.get('.log-box .post-log-summary').text()).toContain('AI가 생성한 토론 요약입니다.')
    expect(wrapper.find('.page-copy').exists()).toBe(false)
    expect(wrapper.find('.summary-detail').exists()).toBe(false)
    expect(text).not.toContain('핵심 주장')
    expect(text).not.toContain('선택 기준')
    expect(text.indexOf('내가 직접 작성한 게시글 본문입니다.')).toBeLessThan(text.indexOf('공유 라운드 토론 로그'))
    expect(text.indexOf('공유 라운드 토론 로그')).toBeLessThan(text.indexOf('점심 안정성'))
    expect(text.indexOf('점심 안정성')).toBeLessThan(text.indexOf('안정성을 택하면 돈까스'))
    expect(text.indexOf('안정성을 택하면 돈까스')).toBeLessThan(text.indexOf('AI가 생성한 토론 요약입니다.'))
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
        plugins: [createTestPinia(true), router],
      },
    })
    await flushPromises()

    expect(wrapper.text()).toContain('사용자 투표')
    expect(wrapper.text()).toContain('투표하고 결과 보기')
    expect(wrapper.text()).not.toContain('42%')
    expect(wrapper.text()).not.toContain('58%')
  })

  it('prompts anonymous users to log in before voting or commenting', async () => {
    const router = createRouter({
      history: createWebHistory(),
      routes: [
        { path: '/posts/:postId', component: PostDetailView },
        { path: '/auth', component: { template: '<div>로그인</div>' } },
      ],
    })
    await router.push('/posts/5')
    await router.isReady()

    const wrapper = mount(PostDetailView, {
      global: {
        plugins: [createTestPinia(), router],
      },
    })
    await flushPromises()

    expect(wrapper.text()).toContain('로그인 후 투표할 수 있습니다')
    expect(wrapper.text()).toContain('로그인 후 댓글을 작성할 수 있습니다')
    expect(wrapper.find('.vote-choice').exists()).toBe(false)
    expect(wrapper.find('.comment-form').exists()).toBe(false)

    await wrapper.get('.vote-login-button').trigger('click')
    await flushPromises()

    expect(postApi.vote).not.toHaveBeenCalled()
    expect(commentApi.create).not.toHaveBeenCalled()
    expect(router.currentRoute.value.path).toBe('/auth')
  })

  it('shows the selected vote and allows changing it', async () => {
    const router = createRouter({
      history: createWebHistory(),
      routes: [{ path: '/posts/:postId', component: PostDetailView }],
    })
    await router.push('/posts/5')
    await router.isReady()

    const wrapper = mount(PostDetailView, {
      global: {
        plugins: [createTestPinia(true), router],
      },
    })
    await flushPromises()

    const voteButtons = wrapper.findAll('.vote-choice')
    await voteButtons[0].trigger('click')
    await flushPromises()

    expect(wrapper.text()).toContain('내 선택: 제육')
    expect(wrapper.text()).toContain('43%')
    expect(wrapper.text()).toContain('57%')
    expect(wrapper.findAll('.vote-choice')[0].attributes('disabled')).toBeUndefined()
    expect(wrapper.findAll('.vote-choice')[1].attributes('disabled')).toBeUndefined()

    postApi.vote.mockResolvedValueOnce({
      data: { postId: 5, voteOptionA: '제육', voteOptionB: '돈까스', voteCountA: 42, voteCountB: 58, voteRatioA: 42, voteRatioB: 58 },
    })
    await wrapper.findAll('.vote-choice')[1].trigger('click')
    await flushPromises()

    expect(postApi.vote).toHaveBeenNthCalledWith(2, 5, { choice: 'B' })
    expect(wrapper.text()).toContain('내 선택: 돈까스')
  })

  it('submits comments with Enter and deletes owner comments', async () => {
    const ownerDetailResponse = JSON.parse(JSON.stringify(detailResponse))
    ownerDetailResponse.comments = [
      {
        commentId: 12,
        postId: 5,
        authorNickname: '나',
        content: '삭제할 댓글',
        isOwner: true,
      },
    ]
    postApi.detail.mockResolvedValueOnce({ data: ownerDetailResponse })
    const router = createRouter({
      history: createWebHistory(),
      routes: [{ path: '/posts/:postId', component: PostDetailView }],
    })
    await router.push('/posts/5')
    await router.isReady()

    const wrapper = mount(PostDetailView, {
      global: {
        plugins: [createTestPinia(true), router],
      },
    })
    await flushPromises()

    await wrapper.get('.comment-form textarea').setValue('엔터로 등록한 댓글')
    await wrapper.get('.comment-form textarea').trigger('keydown', { key: 'Enter' })
    await flushPromises()

    expect(commentApi.create).toHaveBeenCalledWith(5, { content: '엔터로 등록한 댓글' })
    expect(wrapper.text()).toContain('엔터로 등록한 댓글')

    await wrapper.get('.comment-delete-button').trigger('click')
    await flushPromises()

    expect(commentApi.remove).toHaveBeenCalledWith(12)
    expect(wrapper.text()).not.toContain('삭제할 댓글')
  })

  it('deletes an owner post and returns to the post list', async () => {
    const ownerDetailResponse = JSON.parse(JSON.stringify(detailResponse))
    ownerDetailResponse.post.isOwner = true
    postApi.detail.mockResolvedValueOnce({ data: ownerDetailResponse })
    const router = createRouter({
      history: createWebHistory(),
      routes: [
        { path: '/posts/:postId', component: PostDetailView },
        { path: '/posts', component: { template: '<div>게시글 목록</div>' } },
      ],
    })
    await router.push('/posts/5')
    await router.isReady()

    const wrapper = mount(PostDetailView, {
      global: {
        plugins: [createTestPinia(true), router],
      },
    })
    await flushPromises()

    await wrapper.get('.post-delete-button').trigger('click')
    await flushPromises()

    expect(postApi.remove).toHaveBeenCalledWith(5)
    expect(router.currentRoute.value.path).toBe('/posts')
  })

  it('returns to the post board from the back button regardless of history', async () => {
    const router = createRouter({
      history: createWebHistory(),
      routes: [
        { path: '/debates/:debateId/result', component: { template: '<div>토론 결과</div>' } },
        { path: '/posts/:postId', component: PostDetailView },
        { path: '/posts', component: { template: '<div>게시글 목록</div>' } },
      ],
    })
    await router.push('/debates/1/result')
    await router.push('/posts/5')
    await router.isReady()

    const wrapper = mount(PostDetailView, {
      global: {
        plugins: [createTestPinia(), router],
      },
    })
    await flushPromises()

    await wrapper.get('.detail-back-button').trigger('click')
    await flushPromises()

    expect(router.currentRoute.value.path).toBe('/posts')
  })
})

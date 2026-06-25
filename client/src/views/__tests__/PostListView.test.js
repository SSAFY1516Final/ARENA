import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import { createPinia } from 'pinia'
import { createRouter, createWebHistory } from 'vue-router'
import PostListView from '@/views/PostListView.vue'
import { postApi } from '@/api/postApi'

vi.mock('@/api/postApi', () => ({
  postApi: {
    list: vi.fn(),
  },
}))

describe('PostListView', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    postApi.list.mockResolvedValue({
      data: {
        items: [
          { postId: 5, title: '오늘 점심 제육 vs 돈까스', mode: 'PRACTICAL', summaryCard: '점심을 고르는 기준이 매번 달라져서 공유합니다.', commentCount: 2, voteOptionA: '제육', voteOptionB: '돈까스', voteCountA: 42, voteCountB: 58 },
        ],
        page: 1,
        size: 10,
        totalCount: 1,
      },
    })
  })

  it('shows shared body copy and vote ratios without the debate pill on board cards', async () => {
    const router = createRouter({
      history: createWebHistory(),
      routes: [
        { path: '/posts', component: PostListView },
        { path: '/posts/:postId', component: { template: '<div />' } },
        { path: '/new', component: { template: '<div />' } },
      ],
    })
    await router.push('/posts')
    await router.isReady()

    const wrapper = mount(PostListView, {
      global: {
        plugins: [createPinia(), router],
      },
    })
    await flushPromises()

    expect(wrapper.text()).toContain('공유된 토론')
    expect(wrapper.text()).toContain('토론 결과가 커뮤니티로 이어집니다')
    expect(wrapper.find('.post-toolbar').exists()).toBe(true)
    expect(wrapper.find('.new-debate-button-link').exists()).toBe(false)
    expect(wrapper.text()).not.toContain('새 토론 시작')
    expect(wrapper.text()).toContain('댓글 2')
    expect(wrapper.text()).toContain('점심을 고르는 기준이 매번 달라져서 공유합니다.')
    expect(wrapper.findAll('.tag')).toHaveLength(1)
    expect(wrapper.text()).toContain('제육 42%')
    expect(wrapper.text()).toContain('돈까스 58%')
    expect(wrapper.text()).not.toContain('사용자 투표')
    expect(wrapper.text()).not.toContain('상세에서 투표하기')
    expect(wrapper.text()).not.toContain('안정성을 택하면 돈까스, 지금의 만족을 택하면 제육입니다.')
    expect(wrapper.text()).not.toContain('71%')
    expect(wrapper.text()).not.toContain('29%')
  })
})

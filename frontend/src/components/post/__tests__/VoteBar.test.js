import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import VoteBar from '@/components/post/VoteBar.vue'

describe('VoteBar', () => {
  it('hides vote ratios on list cards before the user opens the post', () => {
    const wrapper = mount(VoteBar, {
      props: {
        optionA: '제육',
        optionB: '돈까스',
        voteA: 42,
        voteB: 58,
      },
    })

    expect(wrapper.text()).toContain('사용자 투표')
    expect(wrapper.text()).toContain('상세에서 투표하기')
    expect(wrapper.text()).not.toContain('42%')
    expect(wrapper.text()).not.toContain('58%')
  })
})

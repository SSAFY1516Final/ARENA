import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import DebateMessage from '@/components/debate/DebateMessage.vue'

describe('DebateMessage', () => {
  it('renders dialogue content without truncating it in the component', () => {
    const longContent =
      '이 선택은 가격과 만족도와 기다리는 시간과 모두의 취향까지 한 번에 비교해야 해서 생각보다 복잡하지만 지금 상황에서는 빠르게 결론을 내리는 편이 좋습니다.'

    const wrapper = mount(DebateMessage, {
      props: {
        message: {
          messageId: 1,
          speaker: 'COOL_HEADED',
          roundNo: 1,
          content: longContent,
        },
        sideLabels: {
          COOL_HEADED: '제육파',
          PASSIONATE: '돈까스파',
        },
        displayIndex: 1,
      },
    })

    const content = wrapper.get('.bubble p').text()
    expect(content).toBe(longContent)
    expect(content).not.toContain('...')
  })

  it('places the speaker name outside of the chat bubble', () => {
    const wrapper = mount(DebateMessage, {
      props: {
        message: {
          messageId: 2,
          speaker: 'PASSIONATE',
          roundNo: 1,
          content: '돈까스는 실패가 적습니다.',
        },
        sideLabels: {
          COOL_HEADED: '제육파',
          PASSIONATE: '돈까스파',
        },
        displayIndex: 2,
      },
    })

    expect(wrapper.get('.debate-message__name').text()).toBe('돈까스파')
    expect(wrapper.find('.bubble .debate-message__name').exists()).toBe(false)
  })
})

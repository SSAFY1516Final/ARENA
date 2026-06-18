import { describe, expect, it } from 'vitest'
import { groupMessagesByRound } from '@/utils/messageGroups'

describe('groupMessagesByRound', () => {
  it('groups messages by round number in display order', () => {
    const groups = groupMessagesByRound([
      { messageId: 1, roundNo: 1, content: '첫 주장' },
      { messageId: 2, roundNo: 1, content: '첫 반박' },
      { messageId: 3, roundNo: 2, content: '두 번째 주장' },
    ])

    expect(groups).toEqual([
      {
        roundNo: 1,
        messages: [
          { messageId: 1, roundNo: 1, content: '첫 주장' },
          { messageId: 2, roundNo: 1, content: '첫 반박' },
        ],
      },
      {
        roundNo: 2,
        messages: [{ messageId: 3, roundNo: 2, content: '두 번째 주장' }],
      },
    ])
  })
})

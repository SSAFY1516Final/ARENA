import { describe, expect, it } from 'vitest'
import { nextTurnSamples, sampleMessages } from '@/mocks/data'

describe('debate mock data', () => {
  it('provides three short sample turns per side without relying on UI truncation', () => {
    const messages = [...sampleMessages, ...nextTurnSamples]

    expect(messages).toHaveLength(6)
    expect(messages.filter((message) => message.speaker === 'COOL_HEADED')).toHaveLength(3)
    expect(messages.filter((message) => message.speaker === 'PASSIONATE')).toHaveLength(3)

    messages.forEach((message) => {
      expect(message.content.length).toBeLessThanOrEqual(32)
      expect(message.content).not.toContain('...')
    })
  })
})

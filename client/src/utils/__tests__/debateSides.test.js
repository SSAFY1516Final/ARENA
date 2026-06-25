import { describe, expect, it } from 'vitest'
import { debateSideLabels, displaySideLabel } from '@/utils/debateSides'

describe('debateSideLabels', () => {
  it('uses the two sides around vs as display names', () => {
    expect(debateSideLabels('오늘 점심 제육 vs 돈까스')).toEqual({
      COOL_HEADED: '제육파',
      PASSIONATE: '돈까스파',
    })
  })

  it('ignores condition text before a comma', () => {
    expect(debateSideLabels('점심시간 15분 남았을 때, 제육 vs 돈까스 주문을 누가 양보할지 갈린다')).toEqual({
      COOL_HEADED: '제육파',
      PASSIONATE: '돈까스파',
    })
  })

  it('shows sentence-like stored side labels as short factions', () => {
    expect(displaySideLabel('오타니 10명이 이긴다')).toBe('오타니 10명파')
    expect(displaySideLabel('북극곰이 이긴다')).toBe('북극곰파')
  })

  it('does not duplicate faction suffixes', () => {
    expect(displaySideLabel('오타니 10명파')).toBe('오타니 10명파')
    expect(displaySideLabel('A 진영')).toBe('A 진영')
  })
})

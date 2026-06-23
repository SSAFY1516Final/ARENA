import { describe, expect, it } from 'vitest'
import { debateSideLabels } from '@/utils/debateSides'

describe('debateSideLabels', () => {
  it('uses the two sides around vs as display names', () => {
    expect(debateSideLabels('오늘 점심 제육 vs 돈까스')).toEqual({
      COOL_HEADED: '제육',
      PASSIONATE: '돈까스',
    })
  })

  it('ignores condition text before a comma', () => {
    expect(debateSideLabels('점심시간 15분 남았을 때, 제육 vs 돈까스 주문을 누가 양보할지 갈린다')).toEqual({
      COOL_HEADED: '제육',
      PASSIONATE: '돈까스',
    })
  })
})

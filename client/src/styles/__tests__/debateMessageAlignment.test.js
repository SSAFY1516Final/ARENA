import { describe, expect, it } from 'vitest'
import { readFileSync } from 'node:fs'
import { join } from 'node:path'

const css = readFileSync(
  join(process.cwd(), 'src/styles/base.css'),
  'utf8',
)

function blockFor(selector) {
  const escapedSelector = selector.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  return [...css.matchAll(new RegExp(`${escapedSelector}\\s*\\{([^}]*)\\}`, 'g'))]
    .map((match) => match[1])
    .join('\n')
}

describe('debate message alignment styles', () => {
  it('keeps passionate-side bubbles on the right while their text stays left-aligned', () => {
    expect(blockFor('.debate-message.hot')).toMatch(/justify-self:\s*end/)
    expect(blockFor('.typing-row.hot')).toMatch(/justify-self:\s*end/)
    expect(blockFor('.debate-message.hot')).not.toMatch(/text-align:\s*right/)
    expect(blockFor('.typing-row.hot')).not.toMatch(/text-align:\s*right/)
    expect(blockFor('.debate-message.hot .debate-message__body')).toMatch(/align-items:\s*flex-end/)
    expect(blockFor('.typing-row.hot .debate-message__body')).toMatch(/align-items:\s*flex-end/)
    expect(blockFor('.bubble p')).toMatch(/text-align:\s*left/)
  })
})

describe('topic input styles', () => {
  it('keeps the topic textarea vertically comfortable and ready to grow', () => {
    expect(blockFor('.input--topic')).toMatch(/--n-line-height-textarea:\s*1\.45/)
    expect(blockFor('.input--topic')).toMatch(/--n-padding-left:\s*20px/)
    expect(blockFor('.input--topic')).toMatch(/--n-padding-right:\s*20px/)
    expect(blockFor('.input--topic.n-input--textarea .n-input-wrapper')).toMatch(/min-height:\s*64px/)
    expect(blockFor('.input--topic.n-input--textarea .n-input-wrapper')).toMatch(/align-items:\s*center/)
    expect(blockFor('.input--topic.n-input--textarea .n-input__textarea-el')).toMatch(/overflow-wrap:\s*anywhere/)
  })
})

const SIDE_CONNECTORS = /\s+(?:vs|VS|Vs|대)\s+/
const TRAILING_PARTICLE = /(이|가|은|는|을|를|과|와|로|으로|에서)$/

function cleanToken(token) {
  return token
    .replace(/[,.!?]/g, '')
    .replace(TRAILING_PARTICLE, '')
    .trim()
}

export function normalizeSideLabel(label = '') {
  const normalized = String(label)
    .replace(/[,。.!?！？]+$/g, '')
    .replace(/\s+/g, ' ')
    .trim()

  if (!normalized) return ''

  const nounLike = normalized
    .replace(/(이|가)\s*(이긴다|승리한다|우세하다)$/u, '')
    .replace(/(이|가)\s*더\s*(좋다|강하다|적합하다|낫다|유리하다)$/u, '')
    .replace(/(을|를)\s*(선택해야\s*한다|골라야\s*한다|선택한다|고른다)$/u, '')
    .replace(/\s*(이긴다|승리한다|우세하다)$/u, '')
    .replace(/\s*더\s*(좋다|강하다|적합하다|낫다|유리하다)$/u, '')
    .trim()

  return nounLike || normalized
}

export function displaySideLabel(label = '') {
  const name = normalizeSideLabel(label)
  if (!name) return ''
  if (name.endsWith('진영') || name.endsWith('파')) return name
  return `${name}파`
}

function leftSideName(text) {
  const tokens = text.split(/\s+/).map(cleanToken).filter(Boolean)
  if (!tokens.length) return 'A 진영'
  const last = tokens[tokens.length - 1]
  const previous = tokens[tokens.length - 2]
  if (previous && /\d/.test(last)) {
    return `${previous} ${last}`
  }
  return last
}

function rightSideName(text) {
  const first = text.split(/\s+/).map(cleanToken).find(Boolean)
  return first || 'B 진영'
}

export function debateSideLabels(topic = '') {
  const normalized = topic.includes(',') ? topic.split(',').pop().trim() : topic.trim()
  const parts = normalized.split(SIDE_CONNECTORS)

  if (parts.length < 2) {
    return {
      COOL_HEADED: 'A 진영',
      PASSIONATE: 'B 진영',
    }
  }

  return {
    COOL_HEADED: displaySideLabel(leftSideName(parts[0])),
    PASSIONATE: displaySideLabel(rightSideName(parts[1])),
  }
}

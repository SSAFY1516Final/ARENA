const SIDE_CONNECTORS = /\s+(?:vs|VS|Vs|대)\s+/
const TRAILING_PARTICLE = /(이|가|은|는|을|를|과|와|로|으로|에|에서)$/

function cleanToken(token) {
  return token
    .replace(/[,.!?]/g, '')
    .replace(TRAILING_PARTICLE, '')
    .trim()
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
    COOL_HEADED: leftSideName(parts[0]),
    PASSIONATE: rightSideName(parts[1]),
  }
}

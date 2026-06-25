<template>
  <main class="page debate-result-page">
    <section v-if="isLoading" class="result-feedback-card" role="status" aria-live="polite">
      <span class="section-label">토론 결과</span>
      <h1>결과를 불러오고 있습니다</h1>
      <p>토론 기록과 선택 라운드를 정리하는 중입니다.</p>
    </section>

    <section v-else-if="fetchError" class="result-feedback-card result-feedback-card--error" role="alert">
      <span class="section-label">토론 결과</span>
      <h1>결과를 불러오지 못했습니다</h1>
      <p>{{ fetchError }}</p>
      <button class="debate-action-button debate-action-button--primary" type="button" @click="loadDebateResult">
        다시 불러오기
      </button>
    </section>

    <template v-else>
      <div class="result-topbar">
        <button
          class="debate-action-button debate-action-button--secondary result-exit-button"
          type="button"
          @click="exitToMyDebates"
        >
          나가기
        </button>
      </div>
      <section class="result-summary-card result-hero-card">
        <div class="result-topic-copy">
          <span class="section-label">토론 결과</span>
          <h1>{{ resultHeaderTitle }}</h1>
        </div>
      </section>

      <section class="result-layout result-content-grid">
        <section class="result-main-panel result-selected-round result-transcript-panel result-transcript-card">
          <div class="result-selected-round__head">
            <div class="result-selected-round__copy">
              <span class="section-label">{{ selectedRoundNo }}라운드 토론 기록</span>
              <h2>{{ selectedDetailTitle }}</h2>
              <p v-if="selectedDetailDescription" class="result-round-description">{{ selectedDetailDescription }}</p>
              <div class="result-round-context">
                <span class="result-selected-side-pill" :class="selectedSidePillClass">{{ selectedRoundSideLabel }}</span>
              </div>
            </div>
          </div>
          <div v-if="summaryText || isSummaryPending" class="result-summary-note">
            <span class="section-label">요약</span>
            <p>{{ summaryText || '요약 중...' }}</p>
          </div>
          <div v-if="selectedRoundMessages.length" class="message-list debate-message-stream">
            <DebateMessage
              v-for="(message, index) in selectedRoundMessages"
              :key="message.messageId"
              :message="message"
              :side-labels="sideLabels"
              :display-index="index + 1"
            />
          </div>
          <div v-else class="result-empty-round">
            <strong>아직 저장된 토론 발화가 없습니다</strong>
            <p>공유할 토론 내용이 아직 없습니다.</p>
          </div>
        </section>

        <aside class="result-side-panel result-round-sidebar">
          <div v-if="choiceScoreTotal > 0" class="result-side-card result-score-card">
            <span class="section-label">선택 현황</span>
            <strong class="result-choice-score">{{ choiceScoreText }}</strong>
          </div>

          <div class="result-side-card result-round-panel">
            <span class="section-label">라운드 선택</span>
            <div v-if="rounds.length" class="result-round-grid">
              <button
                v-for="round in rounds"
                :key="round.roundNo"
                class="result-round-card"
                :class="[
                  { 'result-round-card--selected': selectedRoundNo === round.roundNo },
                  roundCardToneClass(round),
                ]"
                type="button"
                @click="selectRound(round.roundNo)"
              >
                <strong class="result-round-card__round">{{ round.roundNo }}라운드</strong>
                <span class="result-round-card__title">{{ round.title || selectedDetailTitle }}</span>
                <span class="result-round-card__choice result-round-card__choice--right">{{ sideLabelForRound(round) }}</span>
              </button>
            </div>
            <p v-else class="result-round-empty">저장된 라운드가 없습니다.</p>
          </div>

          <div class="result-actions result-actions--floating">
            <button
              class="debate-action-button debate-action-button--primary result-share-button"
              type="button"
              :disabled="!canShareSelectedRound"
              @click="openShareModal"
            >
              {{ debateStore.currentDebate.status === 'ACTIVE' ? '결과 정리 중' : '토론 공유하기' }}
            </button>
            <button
              v-if="!isNextRoundDisabled"
              class="debate-action-button debate-action-button--secondary result-new-button"
              type="button"
              @click="goNewDebate"
            >
              다음 라운드 진행
            </button>
            <p v-if="!selectedRoundMessages.length" class="result-action-note">공유할 토론 내용이 아직 없습니다.</p>
          </div>
        </aside>
      </section>

      <section
        v-if="shareModalOpen"
        class="result-share-modal-backdrop"
        @click.self="closeShareModal"
      >
        <form class="result-share-modal" role="dialog" aria-modal="true" aria-labelledby="share-modal-title" @submit.prevent="shareSelectedRound">
          <div class="result-share-modal__head">
            <span class="section-label">토론 공유</span>
            <h2 id="share-modal-title">게시글 본문 작성</h2>
          </div>
          <label class="result-post-body-field">
            <span class="section-label">게시글 본문</span>
            <textarea
              v-model="postBody"
              class="result-post-body-input"
              rows="7"
              placeholder="토론을 보고 든 생각을 적어보세요."
            />
          </label>
          <div class="result-share-modal__actions">
            <button class="debate-action-button debate-action-button--secondary" type="button" @click="closeShareModal">
              취소
            </button>
            <button class="debate-action-button debate-action-button--primary result-share-submit-button" type="button" :disabled="!canShareSelectedRound" @click="shareSelectedRound">
              게시글 등록
            </button>
          </div>
        </form>
      </section>
    </template>
  </main>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import DebateMessage from '@/components/debate/DebateMessage.vue'
import { useDebateStore } from '@/stores/debateStore'
import { debateSideLabels, displaySideLabel } from '@/utils/debateSides'

const route = useRoute()
const router = useRouter()
const debateStore = useDebateStore()
const selectedRoundNo = ref(1)
const storedChoice = ref('')
const postBody = ref('')
const shareModalOpen = ref(false)
const isLoading = ref(true)
const fetchError = ref('')
const RESULT_REFRESH_INTERVAL_MS = 1500
let resultRefreshTimer = null

const originalTopic = computed(() => {
  const storedTopic = debateStore.currentDebate.originalTopic || ''
  const selectedTopic = debateStore.currentDebate.topic || ''
  if (storedTopic && storedTopic !== selectedTopic) return storedTopic
  if (!selectedTopic.includes(',')) return storedTopic
  return selectedTopic.split(',')[0].trim()
})
const sideLabels = computed(() => {
  if (debateStore.currentDebate.sideALabel && debateStore.currentDebate.sideBLabel) {
    return {
      COOL_HEADED: displaySideLabel(debateStore.currentDebate.sideALabel),
      PASSIONATE: displaySideLabel(debateStore.currentDebate.sideBLabel),
    }
  }
  return debateSideLabels(originalTopic.value || debateStore.currentDebate.topic)
})
const roomTopic = computed(() => debateStore.currentDebate.topic || '토론 결과')
const resultHeaderTitle = computed(() => {
  return originalTopic.value || debateStore.currentDebate.originalTopic || debateStore.currentDebate.topic || '토론 결과'
})
const frontendSelectedSide = computed(() => {
  if (route.query.choice === 'PASSIONATE') return 'PASSIONATE'
  if (route.query.choice === 'COOL_HEADED') return 'COOL_HEADED'
  if (storedChoice.value === 'PASSIONATE') return 'PASSIONATE'
  if (storedChoice.value === 'COOL_HEADED') return 'COOL_HEADED'
  return ''
})
const serverSelectedSide = computed(() => {
  if (debateStore.currentDebate.selectedSide === 'PASSIONATE') return 'PASSIONATE'
  if (debateStore.currentDebate.selectedSide === 'COOL_HEADED') return 'COOL_HEADED'
  return ''
})
const selectedSide = computed(() => {
  if (debateStore.currentDebate.status === 'ACTIVE' && frontendSelectedSide.value) {
    return frontendSelectedSide.value
  }
  return serverSelectedSide.value || frontendSelectedSide.value
})
const isPendingFrontendSelection = computed(() => debateStore.currentDebate.status === 'ACTIVE' && Boolean(frontendSelectedSide.value))
const selectedRoundSide = computed(() => sideForRound(selectedRound.value))
const selectedRoundSideLabel = computed(() => sideLabels.value[selectedRoundSide.value] || '선택 기록 없음')
const selectedSidePillClass = computed(() => `result-selected-side-pill--${toneForSide(selectedRoundSide.value)}`)
const roundMetadataByNo = computed(() => {
  return new Map(debateStore.rounds.map((round) => [normalizeRoundNo(round.roundNo), round]))
})
const rounds = computed(() => {
  const roundMap = new Map()
  debateStore.messages.forEach((message) => {
    const roundNo = normalizeRoundNo(message.roundNo)
    if (!roundMap.has(roundNo)) {
      roundMap.set(roundNo, [])
    }
    roundMap.get(roundNo).push(message)
  })
  debateStore.rounds.forEach((round) => {
    const roundNo = normalizeRoundNo(round.roundNo)
    if (!roundMap.has(roundNo)) {
      roundMap.set(roundNo, [])
    }
  })
  return [...roundMap.entries()]
    .sort(([left], [right]) => left - right)
    .map(([roundNo, messages]) => ({
      roundNo,
      messages,
      ...(roundMetadataByNo.value.get(roundNo) || {}),
    }))
})
const availableRoundNos = computed(() => rounds.value.map((round) => round.roundNo))
const selectedRound = computed(() => {
  return rounds.value.find((round) => round.roundNo === selectedRoundNo.value) || rounds.value[0] || null
})
const hasExplicitRoundSelection = computed(() => rounds.value.some((round) => normalizedSide(round.selectedSide)))
const selectedRoundMessages = computed(() => {
  return selectedRound.value?.messages || []
})
const choiceScore = computed(() => {
  return rounds.value.reduce((score, round) => {
    const side = sideForRound(round)
    if (side === 'COOL_HEADED') {
      score.cool += 1
    }
    if (side === 'PASSIONATE') {
      score.hot += 1
    }
    return score
  }, { cool: 0, hot: 0 })
})
const choiceScoreTotal = computed(() => choiceScore.value.cool + choiceScore.value.hot)
const choiceScoreText = computed(() => `${sideLabels.value.COOL_HEADED} ${choiceScore.value.cool} : ${choiceScore.value.hot} ${sideLabels.value.PASSIONATE}`)
const shareBody = computed(() => {
  return postBody.value.trim()
})
const selectedDetailTitle = computed(() => (
  selectedRound.value?.title ||
  debateStore.currentDebate.roundTitle ||
  debateStore.currentDebate.topic ||
  resultHeaderTitle.value
))
const selectedDetailDescription = computed(() => {
  if (selectedRound.value?.topic && selectedRound.value.topic !== selectedDetailTitle.value) {
    return selectedRound.value.topic
  }
  if (selectedRound.value?.description) {
    return selectedRound.value.description
  }
  const detailTopic = debateStore.currentDebate.topic || ''
  if (detailTopic && detailTopic !== selectedDetailTitle.value) {
    return detailTopic
  }
  return debateStore.currentDebate.debateAxis || debateStore.currentDebate.basicConditions || ''
})
const selectedRoundSummary = computed(() => selectedRound.value?.summary || null)
const summaryText = computed(() => selectedRoundSummary.value?.summaryText || debateStore.summary?.summaryText || '')
const isSummaryPending = computed(() => !summaryText.value && debateStore.currentDebate.status === 'ACTIVE')
const canShareSelectedRound = computed(() => {
  return Boolean(
    selectedRoundMessages.value.length
    && ['STOPPED', 'SHARED'].includes(debateStore.currentDebate.status),
  )
})
const isNextRoundDisabled = computed(() => rounds.value.length >= 5)

onMounted(() => {
  storedChoice.value = window.localStorage.getItem(`arena.debate.choice.${route.params.debateId}`) || ''
  loadDebateResult()
})

onBeforeUnmount(() => {
  clearResultRefreshTimer()
})

async function loadDebateResult(options = {}) {
  const silent = options?.silent === true
  clearResultRefreshTimer()
  if (!silent) {
    isLoading.value = true
    fetchError.value = ''
  }

  try {
    await debateStore.fetchDebate(route.params.debateId)
    const persistedRoundNo = persistedSelectedRoundNo()
    const fallbackRoundNo = isPendingFrontendSelection.value
      ? availableRoundNos.value[availableRoundNos.value.length - 1] || 1
      : availableRoundNos.value[0] || 1
    selectedRoundNo.value = persistedRoundNo && availableRoundNos.value.includes(persistedRoundNo)
      ? persistedRoundNo
      : fallbackRoundNo
    fetchError.value = ''
  } catch {
    if (!silent) {
      fetchError.value = '잠시 후 다시 시도하거나 내 토론 목록에서 토론을 다시 선택해주세요.'
    }
  } finally {
    if (!silent) {
      isLoading.value = false
    }
    scheduleResultRefresh()
  }
}

function scheduleResultRefresh() {
  if (fetchError.value || debateStore.currentDebate.status !== 'ACTIVE') {
    return
  }
  resultRefreshTimer = window.setTimeout(() => {
    loadDebateResult({ silent: true })
  }, RESULT_REFRESH_INTERVAL_MS)
}

function clearResultRefreshTimer() {
  if (resultRefreshTimer) {
    window.clearTimeout(resultRefreshTimer)
    resultRefreshTimer = null
  }
}

function normalizeRoundNo(roundNo) {
  const normalized = Number(roundNo)
  return Number.isFinite(normalized) && normalized > 0 ? normalized : 1
}

function persistedSelectedRoundNo() {
  const normalized = Number(debateStore.currentDebate.selectedRoundNo)
  return Number.isFinite(normalized) && normalized > 0 ? normalized : null
}

function normalizedSide(side) {
  if (side === 'PASSIONATE') return 'PASSIONATE'
  if (side === 'COOL_HEADED') return 'COOL_HEADED'
  return ''
}

function toneForSide(side) {
  if (side === 'PASSIONATE') return 'hot'
  if (side === 'COOL_HEADED') return 'cool'
  return 'neutral'
}

function sideForRound(round) {
  const explicitSide = normalizedSide(round?.selectedSide)
  if (explicitSide) {
    return explicitSide
  }
  if (
    debateStore.currentDebate.status === 'ACTIVE' &&
    frontendSelectedSide.value &&
    round?.roundNo === selectedRoundNo.value
  ) {
    return frontendSelectedSide.value
  }
  if (!hasExplicitRoundSelection.value && round?.roundNo === selectedRoundNo.value) {
    return selectedSide.value
  }
  return ''
}

function sideLabelForRound(round) {
  return sideLabels.value[sideForRound(round)] || '선택 기록 없음'
}

function roundCardToneClass(round) {
  return `result-round-card--${toneForSide(sideForRound(round))}`
}

function selectRound(roundNo) {
  selectedRoundNo.value = normalizeRoundNo(roundNo)
}

function goNewDebate() {
  if (isNextRoundDisabled.value) {
    return
  }

  const query = {
    topic: originalTopic.value || debateStore.currentDebate.originalTopic || debateStore.currentDebate.topic,
  }
  if (debateStore.currentDebate.candidateRunId) {
    query.candidateRunId = String(debateStore.currentDebate.candidateRunId)
  } else {
    query.candidates = '1'
  }

  router.push({
    path: '/new',
    query,
  })
}

function exitToMyDebates() {
  router.replace('/debates')
}

function openShareModal() {
  if (!canShareSelectedRound.value) {
    return
  }
  shareModalOpen.value = true
}

function closeShareModal() {
  shareModalOpen.value = false
}

async function shareSelectedRound() {
  if (!canShareSelectedRound.value) {
    return
  }

  const post = await debateStore.shareDebate(route.params.debateId, {
    title: resultHeaderTitle.value,
    voteOptionA: sideLabels.value.COOL_HEADED,
    voteOptionB: sideLabels.value.PASSIONATE,
    isPublic: true,
    roundNo: selectedRoundNo.value,
    body: shareBody.value,
  })
  closeShareModal()
  router.push(`/posts/${post.postId}`)
}
</script>

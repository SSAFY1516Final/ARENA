<template>
  <main class="page debate-room-page">
    <section class="topic-header debate-topic-card">
      <div class="debate-topic-copy">
        <h1 class="debate-room-title">{{ roomTopic }}</h1>
        <p v-if="roomDetailTopic" class="debate-room-detail-topic">{{ roomDetailTopic }}</p>
        <p v-if="originalTopic" class="debate-room-original-topic">{{ originalTopic }}</p>
      </div>
    </section>

    <section class="debate-room-layout">
      <div class="chat-panel debate-chat-card">
        <header v-if="chatMeta" class="chat-panel__header">
          <span>{{ chatMeta }}</span>
        </header>

        <div v-if="debateStore.loading && !visibleMessages.length" class="loading-state">
          토론을 불러오는 중입니다.
        </div>

        <div v-else class="message-list debate-message-stream">
          <div v-if="showInitialGenerationLoading" class="debate-room-start-loading" role="status" aria-live="polite">
            <strong>토론 대결을 준비중입니다</strong>
            <span>AI가 첫 발화를 준비하고 있어요. 1분 정도 소요될 수 있습니다. 화면을 떠나도 서버에서 계속 저장됩니다.</span>
            <div class="new-generation-loading__progress" aria-hidden="true">
              <span></span>
            </div>
          </div>
          <DebateMessage
            v-if="!showInitialGenerationLoading"
            v-for="(message, index) in visibleMessages"
            :key="message.messageId"
            :message="message"
            :side-labels="sideLabels"
            :display-index="index + 1"
          />
          <article
            v-if="!showInitialGenerationLoading && displayTypingSpeaker"
            class="typing-row typing-message"
            :class="{ hot: displayTypingSpeaker === 'PASSIONATE' }"
            aria-live="polite"
          >
            <div class="debate-message__body">
              <strong class="debate-message__name">{{ sideLabels[displayTypingSpeaker] }}</strong>
              <div class="bubble typing-indicator" :class="{ hot: displayTypingSpeaker === 'PASSIONATE' }">
                <span class="typing-dot"></span>
                <span class="typing-dot"></span>
                <span class="typing-dot"></span>
                <span class="typing-label">입력중</span>
              </div>
            </div>
          </article>
        </div>

        <div class="chat-actions">
          <button
            v-if="autoRunning"
            class="button button--full"
            type="button"
            disabled
          >
            토론 진행 중
          </button>
        </div>

        <section
          v-if="showDecisionPanel"
          class="debate-flow-panel"
          :class="{
            'debate-flow-panel--choice': flowStep === 'pick',
            'debate-flow-panel--chosen': flowStep === 'pick' && selectedSide,
          }"
        >
          <div v-if="flowStep === 'pick'" class="flow-step">
            <div>
              <h2>어느 쪽 의견에 더 마음이 가나요?</h2>
            </div>
            <div class="debate-choice-grid">
              <button
                type="button"
                class="debate-choice"
                :class="{
                  selected: selectedSide === 'COOL_HEADED',
                  'is-dimmed': selectedSide && selectedSide !== 'COOL_HEADED',
                }"
                :disabled="isChoosingSide"
                @click="pickSide('COOL_HEADED')"
              >
                <strong>{{ sideLabels.COOL_HEADED }}</strong>
              </button>
              <button
                type="button"
                class="debate-choice debate-choice--hot"
                :class="{
                  selected: selectedSide === 'PASSIONATE',
                  'is-dimmed': selectedSide && selectedSide !== 'PASSIONATE',
                }"
                :disabled="isChoosingSide"
                @click="pickSide('PASSIONATE')"
              >
                <strong>{{ sideLabels.PASSIONATE }}</strong>
              </button>
            </div>
          </div>

          <div v-else class="flow-step">
            <div class="flow-actions debate-result-actions">
              <button class="debate-action-button debate-action-button--secondary" type="button" @click="goMoreDebates">
                토론 더 진행하기
              </button>
              <button class="debate-action-button debate-action-button--primary share-toggle" type="button" @click="shareDebate">
                게시글 등록
              </button>
            </div>
          </div>
        </section>

        <p v-if="debateStore.currentDebate.shareBody" class="shared-body-preview">
          {{ debateStore.currentDebate.shareBody }}
        </p>
      </div>
    </section>
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
const visibleMessages = ref([])
const autoRunning = ref(false)
const initialGenerationPending = ref(false)
const typingSpeaker = ref('')
const targetTurnCount = ref(10)
const selectedSide = ref('')
const isChoosingSide = ref(false)
const flowStep = ref('chat')
const turnGenerationError = ref('')
let revealTimer = null
let unmounted = false
const MESSAGE_TYPING_DELAY_MS = 2000
const GENERATION_POLL_INTERVAL_MS = 2000
const CHOICE_TRANSITION_DELAY_MS = 250

const openedFromRecentDebates = computed(() => route.query.from === 'recent')
const originalTopic = computed(() => {
  const storedTopic = debateStore.currentDebate.originalTopic || ''
  const selectedTopic = debateStore.currentDebate.topic || ''
  if (storedTopic && storedTopic !== selectedTopic) return storedTopic
  return inferredOriginalTopic.value || storedTopic
})
const inferredOriginalTopic = computed(() => {
  const topic = debateStore.currentDebate.topic || ''
  if (!topic.includes(',')) return ''
  return topic.split(',')[0].trim()
})
const sideLabelSource = computed(() => originalTopic.value || debateStore.currentDebate.topic)
const sideLabels = computed(() => {
  if (debateStore.currentDebate.sideALabel && debateStore.currentDebate.sideBLabel) {
    return {
      COOL_HEADED: displaySideLabel(debateStore.currentDebate.sideALabel),
      PASSIONATE: displaySideLabel(debateStore.currentDebate.sideBLabel),
    }
  }
  return debateSideLabels(sideLabelSource.value)
})
const roomTopic = computed(() => (
  debateStore.currentDebate.roundTitle ||
  debateStore.currentDebate.topic ||
  '토론 주제를 불러오는 중입니다'
))
const roomDetailTopic = computed(() => {
  const topic = debateStore.currentDebate.topic || ''
  if (!debateStore.currentDebate.roundTitle || !topic || topic === debateStore.currentDebate.roundTitle) {
    return ''
  }
  return topic
})
const selectedSideLabel = computed(() => (selectedSide.value ? sideLabels.value[selectedSide.value] : '선택한 진영'))
const summaryText = computed(
  () => debateStore.summary?.summaryText || `${selectedSideLabel.value} 쪽을 선택했습니다. 토론 내용을 게시판에 공유할 수 있습니다.`,
)
const chatMeta = computed(() => {
  if (turnGenerationError.value) return turnGenerationError.value
  if (debateStore.currentDebate.status !== 'ACTIVE') return '토론 종료'
  if (flowStep.value !== 'chat') return ''
  if (autoRunning.value || debateStore.turnLoading) return '의견 생성 중'
  return ''
})
const showDecisionPanel = computed(() => flowStep.value !== 'chat')
const showInitialGenerationLoading = computed(() => (
  flowStep.value === 'chat' &&
  debateStore.currentDebate.status === 'ACTIVE' &&
  !turnGenerationError.value &&
  initialGenerationPending.value &&
  !visibleMessages.value.length
))
const displayTypingSpeaker = computed(() => {
  if (typingSpeaker.value) return typingSpeaker.value
  if (showInitialGenerationLoading.value) return ''
  if (
    flowStep.value === 'chat' &&
    debateStore.currentDebate.status === 'ACTIVE' &&
    !autoRunning.value &&
    !debateStore.turnLoading &&
    !openedFromRecentDebates.value &&
    !visibleMessages.value.length
  ) {
    return 'COOL_HEADED'
  }
  return ''
})
const currentRoundNo = computed(() => {
  const currentDebateId = Number(debateStore.currentDebate.debateId)
  const currentRound = debateStore.rounds.find((round) => Number(round.debateId) === currentDebateId)
  return currentRound?.roundNo || null
})
const debateMessages = computed(() => {
  if (!currentRoundNo.value) {
    return debateStore.messages
  }
  return debateStore.messages.filter((message) => Number(message.roundNo) === Number(currentRoundNo.value))
})

onMounted(async () => {
  await debateStore.fetchDebate(route.params.debateId)
  visibleMessages.value = []
  turnGenerationError.value = ''

  if (openedFromRecentDebates.value) {
    visibleMessages.value = debateMessages.value.slice(0, targetTurnCount.value)
    flowStep.value = 'summary'
    return
  }

  if (debateStore.currentDebate.status !== 'ACTIVE') {
    visibleMessages.value = [...debateMessages.value]
    flowStep.value = 'summary'
    return
  }

  if (debateMessages.value.length < targetTurnCount.value) {
    await runAutoDebate()
    return
  }
  await revealStoredMessages()
  openDecisionStepIfReady()
})

onBeforeUnmount(() => {
  unmounted = true
  if (revealTimer) {
    clearTimeout(revealTimer)
  }
})

function wait(ms) {
  return new Promise((resolve) => {
    revealTimer = window.setTimeout(resolve, ms)
  })
}

async function revealNextMessage(delay = MESSAGE_TYPING_DELAY_MS, showTyping = true) {
  if (visibleMessages.value.length >= targetTurnCount.value) return
  if (visibleMessages.value.length >= debateMessages.value.length) return

  const nextMessage = debateMessages.value[visibleMessages.value.length]
  if (nextMessage) {
    if (showTyping) {
      typingSpeaker.value = nextMessage.speaker
      if (delay > 0) await wait(delay)
      typingSpeaker.value = ''
    }
    visibleMessages.value = [...visibleMessages.value, nextMessage]
    openDecisionStepIfReady()
  }
}

async function revealStoredMessages() {
  while (
    visibleMessages.value.length < debateMessages.value.length &&
    visibleMessages.value.length < targetTurnCount.value
  ) {
    await revealNextMessage(MESSAGE_TYPING_DELAY_MS)
  }
}

async function waitForInitialGenerationToComplete() {
  while (!unmounted && debateMessages.value.length < targetTurnCount.value) {
    await wait(GENERATION_POLL_INTERVAL_MS)
    if (unmounted) return false
    try {
      await debateStore.fetchDebate(route.params.debateId)
    } catch {
      turnGenerationError.value = 'AI response generation failed. Please try again.'
      return false
    }
  }
  return debateMessages.value.length >= targetTurnCount.value
}

async function runAutoDebate() {
  if (autoRunning.value || debateStore.currentDebate.status !== 'ACTIVE') {
    return
  }

  autoRunning.value = true
  try {
    if (debateMessages.value.length < targetTurnCount.value) {
      initialGenerationPending.value = true
      try {
        const generation = await debateStore.generateInitialTurns(route.params.debateId)
        if (generation.status !== 'COMPLETE' || debateMessages.value.length < targetTurnCount.value) {
          const completed = await waitForInitialGenerationToComplete()
          if (!completed) return
        }
      } catch (error) {
        turnGenerationError.value = 'AI response generation failed. Please try again.'
        return
      } finally {
        initialGenerationPending.value = false
      }
    }
    await revealStoredMessages()
  } finally {
    initialGenerationPending.value = false
    typingSpeaker.value = ''
    autoRunning.value = false
    openDecisionStepIfReady()
  }
}

function openDecisionStepIfReady() {
  if (
    flowStep.value === 'chat' &&
    debateStore.currentDebate.status === 'ACTIVE' &&
    !autoRunning.value &&
    visibleMessages.value.length >= targetTurnCount.value
  ) {
    flowStep.value = 'pick'
  }
}

async function pickSide(side) {
  if (isChoosingSide.value) return
  isChoosingSide.value = true
  selectedSide.value = side
  turnGenerationError.value = ''
  window.localStorage.setItem(`arena.debate.choice.${route.params.debateId}`, side)
  const selectedRoundNo = currentRoundNo.value || visibleMessages.value.find((message) => message.roundNo)?.roundNo || 1
  let navigatingToResult = false
  const stopAttempt = debateStore.stopDebate(route.params.debateId, {
    selectedSide: side,
    selectedRoundNo,
  }).catch(() => {
    if (!navigatingToResult) {
      selectedSide.value = ''
      turnGenerationError.value = '결과를 저장하지 못했습니다. 잠시 후 다시 선택해주세요.'
    }
    return null
  })

  try {
    await wait(CHOICE_TRANSITION_DELAY_MS)
    if (turnGenerationError.value) return

    navigatingToResult = true
    await router.push({
      path: `/debates/${route.params.debateId}/result`,
      query: { choice: side },
    })
    void stopAttempt
  } catch {
    selectedSide.value = ''
    turnGenerationError.value = '결과를 저장하지 못했습니다. 잠시 후 다시 선택해주세요.'
  } finally {
    isChoosingSide.value = false
  }
}

function goMoreDebates() {
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

async function shareDebate() {
  const post = await debateStore.shareDebate(route.params.debateId, {
    title: debateStore.currentDebate.topic,
    voteOptionA: sideLabels.value.COOL_HEADED,
    voteOptionB: sideLabels.value.PASSIONATE,
    isPublic: true,
  })
  router.push(`/posts/${post.postId}`)
}
</script>

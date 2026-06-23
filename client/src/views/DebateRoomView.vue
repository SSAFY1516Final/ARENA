<template>
  <main class="page">
    <section class="topic-header">
      <div>
        <h1>{{ debateStore.currentDebate.topic }}</h1>
        <div class="tag-row">
          <span class="tag tag--teal">{{ sideLabels.COOL_HEADED }}</span>
          <span class="tag tag--blue">{{ sideLabels.PASSIONATE }}</span>
        </div>
      </div>
    </section>

    <section class="debate-room-layout">
      <div class="chat-panel">
        <header class="chat-panel__header">
          <div>
            <strong>{{ sideLabels.COOL_HEADED }} vs {{ sideLabels.PASSIONATE }}</strong>
            <span>{{ chatMeta }}</span>
          </div>
        </header>

        <div v-if="debateStore.loading && !visibleMessages.length" class="loading-state">
          토론을 불러오는 중입니다.
        </div>

        <div v-else class="message-list">
          <DebateMessage
            v-for="(message, index) in visibleMessages"
            :key="message.messageId"
            :message="message"
            :side-labels="sideLabels"
            :display-index="index + 1"
          />
          <div v-if="autoRunning || debateStore.turnLoading" class="typing-indicator" aria-live="polite">
            <span></span>
            <span></span>
            <span></span>
          </div>
          <div v-if="!visibleMessages.length && !autoRunning" class="empty-state empty-state--compact">
            <h2>토론을 준비하고 있습니다</h2>
            <p>{{ sideLabels.COOL_HEADED }}와 {{ sideLabels.PASSIONATE }} 입장이 차례로 올라옵니다.</p>
          </div>
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
          <button
            v-if="canContinueDebate"
            class="button button--ghost"
            type="button"
            :disabled="debateStore.turnLoading || autoRunning"
            @click="continueRound"
          >
            이 주제로 더 듣기
          </button>
          <button
            v-if="canStopDebate"
            class="button finish-button"
            type="button"
            :disabled="debateStore.stopLoading || autoRunning"
            @click="openDecisionStep"
          >
            판단하기
          </button>
        </div>

        <section v-if="showDecisionPanel" class="debate-flow-panel">
          <div v-if="flowStep === 'pick'" class="flow-step">
            <div>
              <span class="section-label">내 선택</span>
              <h2>어느 쪽이 더 설득됐나요?</h2>
            </div>
            <div class="debate-choice-grid">
              <button
                type="button"
                class="debate-choice"
                :class="{ selected: selectedSide === 'COOL_HEADED' }"
                @click="pickSide('COOL_HEADED')"
              >
                <strong>{{ sideLabels.COOL_HEADED }}</strong>
                <span>이쪽 주장이 더 설득력 있었습니다.</span>
              </button>
              <button
                type="button"
                class="debate-choice debate-choice--hot"
                :class="{ selected: selectedSide === 'PASSIONATE' }"
                @click="pickSide('PASSIONATE')"
              >
                <strong>{{ sideLabels.PASSIONATE }}</strong>
                <span>이쪽 주장이 더 설득력 있었습니다.</span>
              </button>
            </div>
          </div>

          <div v-else-if="flowStep === 'next'" class="flow-step">
            <div>
              <span class="section-label">다음 진행</span>
              <h2>{{ selectedSideLabel }} 쪽으로 선택했습니다</h2>
              <p>다른 세부주제를 골라 이어가거나, 여기서 토론을 마무리할 수 있습니다.</p>
            </div>
            <div class="flow-actions">
              <button class="button button--ghost" type="button" @click="goNewDebate">다른 세부주제 보기</button>
              <button class="button" type="button" :disabled="debateStore.stopLoading" @click="finishDebate">
                토론 마무리
              </button>
            </div>
          </div>

          <div v-else class="flow-step">
            <div>
              <span class="section-label">마무리</span>
              <h2>요약이 준비됐습니다</h2>
              <p>{{ summaryText }}</p>
            </div>
            <div class="flow-actions">
              <button class="button button--ghost" type="button" @click="goMyDebates">내 토론에 저장</button>
              <button class="button share-toggle" type="button" @click="toggleShareForm">게시판에 공유</button>
            </div>
          </div>
        </section>

        <form v-if="shareFormOpen" class="share-form share-form--room" @submit.prevent="shareDebate">
          <button class="button button--full" type="submit">게시글 공유하기</button>
        </form>

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
import { debateSideLabels } from '@/utils/debateSides'

const route = useRoute()
const router = useRouter()
const debateStore = useDebateStore()
const shareFormOpen = ref(false)
const visibleMessages = ref([])
const autoRunning = ref(false)
const targetTurnCount = ref(6)
const selectedSide = ref('')
const flowStep = ref('chat')
let revealTimer = null

const sideLabels = computed(() => debateSideLabels(debateStore.currentDebate.topic))
const selectedSideLabel = computed(() => (selectedSide.value ? sideLabels.value[selectedSide.value] : '선택한 진영'))
const summaryText = computed(
  () => debateStore.summary?.summaryText || `${selectedSideLabel.value} 쪽을 선택했습니다. 토론 내용을 게시판에 공유할 수 있습니다.`,
)
const chatMeta = computed(() => {
  if (debateStore.currentDebate.status !== 'ACTIVE') return '토론 종료'
  if (flowStep.value !== 'chat') return '선택 대기'
  if (autoRunning.value || debateStore.turnLoading) return '의견 생성 중'
  return `${visibleMessages.value.length}개 의견`
})
const canContinueDebate = computed(
  () =>
    flowStep.value === 'chat' &&
    debateStore.currentDebate.status === 'ACTIVE' &&
    !autoRunning.value &&
    visibleMessages.value.length >= targetTurnCount.value,
)
const canStopDebate = computed(
  () =>
    flowStep.value === 'chat' &&
    debateStore.currentDebate.status === 'ACTIVE' &&
    !autoRunning.value &&
    visibleMessages.value.length > 0,
)
const showDecisionPanel = computed(() => flowStep.value !== 'chat')

onMounted(async () => {
  await debateStore.fetchDebate(route.params.debateId)
  visibleMessages.value = []

  if (debateStore.currentDebate.status !== 'ACTIVE') {
    visibleMessages.value = [...debateStore.messages]
    flowStep.value = 'summary'
    return
  }

  await revealStoredMessages()

  if (debateStore.messages.length < targetTurnCount.value) {
    runAutoDebate()
  }
})

onBeforeUnmount(() => {
  if (revealTimer) {
    clearTimeout(revealTimer)
  }
})

function wait(ms) {
  return new Promise((resolve) => {
    revealTimer = window.setTimeout(resolve, ms)
  })
}

async function revealNextMessage(delay = 520) {
  if (visibleMessages.value.length >= debateStore.messages.length) return
  if (delay > 0) await wait(delay)

  const nextMessage = debateStore.messages[visibleMessages.value.length]
  if (nextMessage) {
    visibleMessages.value = [...visibleMessages.value, nextMessage]
  }
}

async function revealStoredMessages() {
  while (visibleMessages.value.length < debateStore.messages.length) {
    await revealNextMessage(520)
  }
}

async function runAutoDebate() {
  if (autoRunning.value || debateStore.currentDebate.status !== 'ACTIVE') {
    return
  }

  autoRunning.value = true
  try {
    while (
      debateStore.currentDebate.status === 'ACTIVE' &&
      debateStore.messages.length < targetTurnCount.value
    ) {
      await wait(780)
      const beforeCount = debateStore.messages.length
      try {
        await debateStore.generateNextTurn(route.params.debateId)
      } catch {
        break
      }
      if (debateStore.messages.length <= beforeCount) break
      await revealNextMessage(160)
    }
  } finally {
    autoRunning.value = false
  }
}

async function continueRound() {
  selectedSide.value = ''
  targetTurnCount.value += 2
  flowStep.value = 'chat'
  await runAutoDebate()
}

function openDecisionStep() {
  flowStep.value = 'pick'
}

function pickSide(side) {
  selectedSide.value = side
  flowStep.value = 'next'
}

async function finishDebate() {
  await debateStore.stopDebate(route.params.debateId)
  flowStep.value = 'summary'
}

function goNewDebate() {
  router.push('/new')
}

function goMyDebates() {
  router.push('/debates')
}

function toggleShareForm() {
  shareFormOpen.value = !shareFormOpen.value
}

async function shareDebate() {
  const post = await debateStore.shareDebate(route.params.debateId, {
    title: debateStore.currentDebate.topic,
    voteOptionA: sideLabels.value.COOL_HEADED,
    voteOptionB: sideLabels.value.PASSIONATE,
    isPublic: true,
  })
  shareFormOpen.value = false
  router.push(`/posts/${post.postId}`)
}
</script>

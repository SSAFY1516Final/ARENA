<template>
  <main class="page">
    <section class="topic-header">
      <div>
        <span class="eyebrow">Debate Room</span>
        <h1>{{ debateStore.currentDebate.topic }}</h1>
        <div class="tag-row">
          <span class="tag tag--teal">{{ modeLabel }}</span>
          <span class="tag tag--blue">{{ statusLabel }}</span>
        </div>
      </div>
    </section>

    <section class="debate-room-layout">
      <div class="chat-panel">
        <header class="chat-panel__header">
          <div>
            <strong>AI 페르소나 토론</strong>
            <span>{{ chatMeta }}</span>
          </div>
          <span v-if="debateStore.currentDebate.peakReached" class="tag tag--amber">핵심 쟁점 도달</span>
        </header>

        <div v-if="debateStore.loading && !debateStore.messages.length" class="loading-state">
          토론을 불러오는 중입니다.
        </div>

        <div v-else-if="!debateStore.messages.length" class="empty-state empty-state--compact">
          <h2>아직 발화가 없습니다</h2>
          <p>냉정파와 열정파가 차례로 의견을 내며 흐름을 만듭니다.</p>
        </div>

        <div v-else class="message-list">
          <section v-for="group in messageGroups" :key="group.roundNo" class="round-group">
            <div v-if="group.roundNo > 1" class="round-divider">
              <span>Round {{ group.roundNo }}</span>
            </div>
            <DebateMessage
              v-for="message in group.messages"
              :key="message.messageId"
              :message="message"
            />
          </section>
        </div>
        <p v-if="debateStore.currentDebate.shareBody" class="shared-body-preview">
          {{ debateStore.currentDebate.shareBody }}
        </p>
      </div>

      <aside class="debate-status-rail">
        <div>
          <span class="section-label">토론 진행</span>
          <h2>토론 컨트롤</h2>
        </div>

        <div class="status-metric-grid">
          <div class="status-metric">
            <span>발화</span>
            <strong>{{ debateStore.messages.length }}개</strong>
          </div>
          <div class="status-metric">
            <span>상태</span>
            <strong>{{ statusLabel }}</strong>
          </div>
        </div>

        <div class="status-note">
          <span>모드</span>
          <strong>{{ modeLabel }}</strong>
          <p>{{ debateStore.currentDebate.peakReached ? '핵심 쟁점에 도달했습니다.' : '다음 발화를 생성해 흐름을 이어가세요.' }}</p>
        </div>

        <div class="rail-actions">
          <button
            v-if="canGenerateTurn"
            class="button button--full"
            type="button"
            :disabled="debateStore.turnLoading"
            @click="nextTurn"
          >
            다음 발화
          </button>
          <button
            v-if="canGenerateTurn"
            class="button button--ghost button--full finish-button"
            type="button"
            :disabled="debateStore.stopLoading"
            @click="finishDebate"
          >
            종료하기
          </button>
          <button v-if="canShareDebate" class="button button--full share-toggle" type="button" @click="toggleShareForm">
            공유하기
          </button>
        </div>

        <form v-if="shareFormOpen" class="share-form share-form--room" @submit.prevent="shareDebate">
          <button class="button button--full" type="submit">게시글 공유</button>
        </form>
      </aside>
    </section>
  </main>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import DebateMessage from '@/components/debate/DebateMessage.vue'
import { useDebateStore } from '@/stores/debateStore'
import { groupMessagesByRound } from '@/utils/messageGroups'

const route = useRoute()
const router = useRouter()
const debateStore = useDebateStore()
const shareFormOpen = ref(false)

const messageGroups = computed(() => groupMessagesByRound(debateStore.messages))
const canGenerateTurn = computed(() => debateStore.currentDebate.status === 'ACTIVE')
const canShareDebate = computed(() => debateStore.currentDebate.status !== 'ACTIVE')
const modeLabel = computed(() =>
  debateStore.currentDebate.mode === 'PRACTICAL' ? '실용 판정' : '예능 배틀',
)
const statusLabel = computed(() => {
  if (debateStore.currentDebate.status === 'ACTIVE') return '진행 중'
  if (debateStore.currentDebate.status === 'STOPPED') return '요약 완료'
  if (debateStore.currentDebate.status === 'SHARED') return '공유됨'
  return debateStore.currentDebate.status
})
const chatMeta = computed(() => {
  const count = debateStore.messages.length
  return count ? `${count}개 발화` : '첫 발화를 기다리는 중'
})

onMounted(() => {
  debateStore.fetchDebate(route.params.debateId)
})

async function nextTurn() {
  await debateStore.generateNextTurn(route.params.debateId)
}

async function finishDebate() {
  await debateStore.stopDebate(route.params.debateId)
  router.push('/debates')
}

function toggleShareForm() {
  shareFormOpen.value = !shareFormOpen.value
}

async function shareDebate() {
  const post = await debateStore.shareDebate(route.params.debateId, {
    title: debateStore.currentDebate.topic,
    voteOptionA: '냉정파',
    voteOptionB: '열정파',
    isPublic: true,
  })
  shareFormOpen.value = false
  router.push(`/posts/${post.postId}`)
}
</script>

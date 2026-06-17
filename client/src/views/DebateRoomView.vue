<template>
  <main class="page">
    <section class="topic-header">
      <div>
        <h1>{{ debateStore.currentDebate.topic }}</h1>
        <div class="tag-row">
          <span class="tag tag--teal">{{ modeLabel }}</span>
          <span class="tag tag--blue">{{ debateStore.currentDebate.status }}</span>
        </div>
      </div>
    </section>

    <section class="debate-layout">
      <div class="chat-panel">
        <div class="message-list">
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
        <div class="chat-actions">
          <button
            v-if="canGenerateTurn"
            class="button button--ghost"
            type="button"
            :disabled="debateStore.turnLoading"
            @click="nextTurn"
          >
            토론 진행
          </button>
          <button v-if="canShareDebate" class="button button--ghost share-toggle" type="button" @click="toggleShareForm">
            공유하기
          </button>
          <button
            v-if="canGenerateTurn"
            class="button finish-button"
            type="button"
            :disabled="debateStore.stopLoading"
            @click="finishDebate"
          >
            종료하기
          </button>
        </div>
        <form v-if="shareFormOpen" class="share-form share-form--room" @submit.prevent="shareDebate">
          <button class="button button--full" type="submit">게시글 공유</button>
        </form>
        <p v-if="debateStore.currentDebate.shareBody" class="shared-body-preview">
          {{ debateStore.currentDebate.shareBody }}
        </p>
      </div>
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

<template>
  <main class="page debates-page">
    <section class="debates-header">
      <div>
        <h1>내 토론</h1>
      </div>
      <RouterLink class="new-debate-button-link" to="/new">
        <NButton class="new-debate-button" type="primary" size="large" strong>
          <template #icon>
            <span class="new-debate-button__icon" aria-hidden="true">+</span>
          </template>
          새 토론 시작
        </NButton>
      </RouterLink>
    </section>

    <NCard class="debates-board-card" :bordered="false">
      <div class="debates-board-header">
        <div>
          <span class="section-label">Recent debates</span>
          <h2>최근 토론</h2>
        </div>
      </div>

      <section v-if="debateStore.loading" class="loading-state">
        내 토론을 불러오는 중입니다.
      </section>

      <section v-else-if="!displayDebates.length" class="empty-state">
        <h2>아직 저장된 토론이 없습니다</h2>
        <p>새 토론을 시작하면 이곳에서 다시 확인할 수 있습니다.</p>
      </section>

      <NGrid
        v-else
        cols="1 m:2"
        :x-gap="14"
        :y-gap="14"
        responsive="screen"
        class="my-debate-grid"
        aria-label="내 토론 목록"
      >
        <NGridItem
          v-for="debate in displayDebates"
          :key="debate.debateId"
        >
          <NCard
            class="my-debate-card"
            hoverable
            :bordered="false"
            role="button"
            tabindex="0"
            @click="openDebate(debate)"
            @keydown.enter="openDebate(debate)"
          >
            <div class="my-debate-card__body">
              <div>
                <h2>{{ debateTitle(debate) }}</h2>
              </div>
              <div class="my-debate-card__actions">
                <button
                  class="my-debate-card__delete"
                  type="button"
                  :disabled="deletingDebateId === debate.debateId"
                  :aria-label="`${debateTitle(debate)} 삭제`"
                  @click.stop="deleteDebate(debate)"
                >
                  ×
                </button>
              </div>
            </div>
          </NCard>
        </NGridItem>
      </NGrid>
    </NCard>
  </main>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { NButton, NCard, NGrid, NGridItem } from 'naive-ui'
import { useDebateStore } from '@/stores/debateStore'

const debateStore = useDebateStore()
const router = useRouter()
const deletingDebateId = ref(null)
const displayDebates = computed(() => debateStore.myDebates)

onMounted(() => {
  debateStore.fetchMyDebates()
})

function openDebate(debate) {
  if (debate.status === 'ACTIVE') {
    router.push(`/debates/${debate.debateId}`)
    return
  }
  router.push({
    path: `/debates/${debate.debateId}/result`,
    query: { from: 'recent' },
  })
}

function debateTitle(debate) {
  return debate.originalTopic || debate.topic
}

async function deleteDebate(debate) {
  deletingDebateId.value = debate.debateId
  try {
    await debateStore.deleteDebate(debate.debateId)
  } finally {
    deletingDebateId.value = null
  }
}
</script>

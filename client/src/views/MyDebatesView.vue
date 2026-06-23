<template>
  <main class="page">
    <section class="list-header my-debates-header">
      <div>
        <span class="eyebrow">My Arena</span>
        <h1>내 토론</h1>
        <p class="page-copy">내가 만든 토론 주제를 카드로 확인하고 이어서 진행합니다.</p>
      </div>
      <RouterLink class="button" to="/new">토론 만들기</RouterLink>
    </section>

    <section v-if="debateStore.loading" class="loading-state">
      내 토론을 불러오는 중입니다.
    </section>

    <section v-else class="my-debate-grid" aria-label="내 토론 목록">
      <RouterLink
        v-for="debate in displayDebates"
        :key="debate.debateId"
        class="my-debate-card"
        :class="{ 'my-debate-card--mock': debate.isMock }"
        :to="debate.isMock ? '/new' : `/debates/${debate.debateId}`"
      >
        <div class="tag-row">
          <span class="tag tag--blue">{{ statusLabel(debate.status) }}</span>
        </div>
        <h2>{{ debate.topic }}</h2>
        <p>{{ debate.shareBody || debate.summaryCard }}</p>
        <span class="card-link-label">{{ debate.isMock ? '토론 시작하기' : '이어가기' }}</span>
      </RouterLink>
    </section>
  </main>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import { mockMyDebates } from '@/mocks/data'
import { useDebateStore } from '@/stores/debateStore'

const debateStore = useDebateStore()
const displayDebates = computed(() => debateStore.myDebates.length ? debateStore.myDebates : mockMyDebates)

onMounted(() => {
  debateStore.fetchMyDebates()
})

function statusLabel(status) {
  if (status === 'ACTIVE') return '진행 중'
  if (status === 'STOPPED') return '요약 완료'
  if (status === 'SHARED') return '공유됨'
  return status
}
</script>

<template>
  <main class="page">
    <section class="list-header my-debates-header">
      <div>
        <span class="eyebrow">My Arena</span>
        <h1>내 토론</h1>
        <p class="page-copy">내가 만든 토론 주제를 카드로 확인하고 이어서 진행합니다.</p>
      </div>
      <RouterLink class="button" to="/new">새 토론</RouterLink>
    </section>

    <section v-if="debateStore.loading" class="loading-state">
      내 토론을 불러오는 중입니다.
    </section>

    <section v-else-if="debateStore.myDebates.length" class="my-debate-grid" aria-label="내 토론 목록">
      <RouterLink
        v-for="debate in debateStore.myDebates"
        :key="debate.debateId"
        class="my-debate-card"
        :to="`/debates/${debate.debateId}`"
      >
        <div class="tag-row">
          <span class="tag" :class="debate.mode === 'PRACTICAL' ? 'tag--teal' : 'tag--amber'">
            {{ debate.mode === 'PRACTICAL' ? '실용 판정' : '예능 배틀' }}
          </span>
          <span class="tag tag--blue">{{ statusLabel(debate.status) }}</span>
        </div>
        <h2>{{ debate.topic }}</h2>
        <p>{{ debate.shareBody || debate.summaryCard }}</p>
        <span class="card-link-label">이어가기</span>
      </RouterLink>
    </section>

    <section v-else class="empty-state">
      <h2>아직 만든 토론이 없습니다</h2>
      <p>새 토론을 만들면 이곳에서 이어서 진행할 수 있습니다.</p>
      <RouterLink class="button" to="/new">첫 토론 만들기</RouterLink>
    </section>
  </main>
</template>

<script setup>
import { onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import { useDebateStore } from '@/stores/debateStore'

const debateStore = useDebateStore()

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

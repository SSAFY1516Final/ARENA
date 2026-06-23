<template>
  <main class="page summary-layout">
    <section>
      <span class="eyebrow">STOPPED</span>
      <h1>토론을 종료하면 요약 카드가 생성됩니다</h1>
      <p class="page-copy">
        핵심 주장, 하이라이트, 남은 쟁점을 확인한 뒤 게시판 공유 정보를 입력합니다.
      </p>

      <article class="summary-card">
        <div class="tag-row">
          <span class="tag tag--amber">요약 생성 완료</span>
          <span class="tag tag--teal">토론</span>
        </div>
        <h2>제육은 만족감, 돈까스는 안정성</h2>
        <p>{{ summary.summaryText }}</p>
        <div class="criteria">{{ summary.decisionCriteria }}</div>
      </article>
    </section>

    <aside class="share-card">
      <h2>게시판 공유 설정</h2>
      <label class="field-label" for="share-title">게시글 제목</label>
      <input id="share-title" v-model="shareForm.title" class="input" />
      <div class="inline-fields">
        <input v-model="shareForm.voteOptionA" class="input" placeholder="투표 선택지 A" />
        <input v-model="shareForm.voteOptionB" class="input" placeholder="투표 선택지 B" />
      </div>
      <label class="toggle-row">
        <span>공개 게시글로 공유</span>
        <input v-model="shareForm.isPublic" type="checkbox" />
      </label>
      <div class="actions">
        <button class="button" type="button" @click="share">게시판에 공유</button>
        <RouterLink class="button button--ghost" :to="`/debates/${route.params.debateId}`">
          토론으로 돌아가기
        </RouterLink>
      </div>
    </aside>
  </main>
</template>

<script setup>
import { computed, onMounted, reactive } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { useDebateStore } from '@/stores/debateStore'

const route = useRoute()
const router = useRouter()
const debateStore = useDebateStore()

const shareForm = reactive({
  title: '오늘 점심 제육 vs 돈까스',
  voteOptionA: '제육',
  voteOptionB: '돈까스',
  isPublic: true,
})

const summary = computed(() => debateStore.summary || {
  summaryText: '선택 기준은 안정성과 만족감의 차이입니다.',
  decisionCriteria: '선택 기준: 오후 일정이 중요하면 돈까스, 지금의 만족이 중요하면 제육',
})
onMounted(async () => {
  await debateStore.fetchDebate(route.params.debateId)
  if (!debateStore.summary) {
    await debateStore.stopDebate(route.params.debateId)
  }
})

async function share() {
  const post = await debateStore.shareDebate(route.params.debateId, shareForm)
  router.push(`/posts/${post.postId}`)
}
</script>

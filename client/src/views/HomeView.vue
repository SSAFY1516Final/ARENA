<template>
  <main class="page workspace-page">
    <section class="workspace-header">
      <div>
        <span class="eyebrow">Debate Workspace</span>
        <h1>무엇을 비교할까요?</h1>
        <p class="page-copy">주제와 모드를 정하면 AI 토론방에서 바로 발화를 이어갈 수 있습니다.</p>
      </div>
    </section>

    <section class="workspace-layout">
      <form class="workspace-setup" @submit.prevent="startDebate">
        <div class="workspace-setup__section">
          <span class="section-label">토론 설정</span>
          <h2>새 토론 만들기</h2>
          <p>실제 선택은 기준 중심으로, 예능 배틀은 더 과감한 관점으로 진행합니다.</p>
        </div>

        <div class="workspace-setup__modes">
          <div class="mode-grid mode-grid--compact">
            <ModeCard
              mode="PRACTICAL"
              label="실용 판정"
              description="실제 선택 기준"
              :selected="mode === 'PRACTICAL'"
              @select="mode = $event"
            />
            <ModeCard
              mode="ENTERTAINMENT"
              label="예능 배틀"
              description="상상 대결"
              :selected="mode === 'ENTERTAINMENT'"
              @select="mode = $event"
            />
          </div>
        </div>

        <div class="workspace-setup__topic">
          <div class="field-header">
            <label class="field-label" for="topic">토론 주제</label>
            <span>{{ topic.length }}/120</span>
          </div>
          <input
            id="topic"
            v-model="topic"
            class="input input--topic"
            maxlength="120"
            placeholder="예: 오늘 점심 제육 vs 돈까스"
            required
          />
        </div>

        <button class="button button--full workspace-setup__submit" type="submit">토론 시작</button>
      </form>

      <section class="workspace-main">
        <article class="workspace-preview">
          <div class="workspace-preview__header">
            <div>
              <span class="section-label">AI 토론 미리보기</span>
              <h2>{{ topic || '토론 주제를 입력하세요' }}</h2>
            </div>
            <span class="tag tag--blue">Round 2</span>
          </div>
          <DebateMessage
            v-for="message in sampleMessages"
            :key="message.messageId"
            :message="message"
          />
          <div class="workspace-preview__footer">
            <span>토론을 생성하면 이 화면에서 다음 발화, 종료, 요약 공유까지 이어집니다.</span>
          </div>
        </article>

        <div class="workspace-signal-grid">
          <RouterLink class="workspace-signal-card" to="/posts">
            <span class="section-label">최근 공유된 토론</span>
            <strong>퇴근 후 운동 vs 휴식</strong>
            <p>댓글 8 · 투표 24</p>
          </RouterLink>
          <RouterLink class="workspace-signal-card" to="/posts">
            <span class="section-label">투표가 진행 중</span>
            <strong>제육 58% / 돈까스 42%</strong>
            <p>토론 결과를 공유하면 게시판 투표로 이어집니다.</p>
          </RouterLink>
        </div>
      </section>
    </section>
  </main>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import ModeCard from '@/components/common/ModeCard.vue'
import DebateMessage from '@/components/debate/DebateMessage.vue'
import { sampleMessages } from '@/mocks/data'
import { useDebateStore } from '@/stores/debateStore'

const router = useRouter()
const debateStore = useDebateStore()
const topic = ref('오늘 점심 제육 vs 돈까스')
const mode = ref('PRACTICAL')

async function startDebate() {
  const debate = await debateStore.createDebate({
    topic: topic.value,
    mode: mode.value,
  })
  router.push(`/debates/${debate.debateId}`)
}
</script>

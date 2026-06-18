<template>
  <main class="page page--hero">
    <section class="hero-copy">
      <span class="eyebrow">AI 논쟁 커뮤니티</span>
      <h1>선택 고민을 두 AI가 대신 논쟁합니다</h1>
      <p>
        실용적인 결정은 기준 중심으로, 상상 배틀은 더 재미있게. 주제만 입력하면
        냉정파와 열정파가 번갈아 토론합니다.
      </p>

      <form class="start-panel" @submit.prevent="startDebate">
        <div class="start-panel__topbar">
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

          <button class="button start-panel__submit" type="submit">토론 시작</button>
        </div>

        <div class="start-panel__topic">
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
      </form>

      <div class="hero-metrics" aria-label="ARENA 주요 흐름">
        <div>
          <strong>01</strong>
          <span>주제 입력</span>
        </div>
        <div>
          <strong>02</strong>
          <span>AI 토론</span>
        </div>
        <div>
          <strong>03</strong>
          <span>공유와 투표</span>
        </div>
      </div>
    </section>

    <aside class="preview-card">
      <div class="preview-card__header">
        <span class="eyebrow">샘플 토론</span>
      </div>
      <div class="tag-row">
        <span class="tag tag--teal">실용 판정</span>
      </div>
      <h2>오늘 점심 제육 vs 돈까스</h2>
      <DebateMessage
        v-for="message in sampleMessages"
        :key="message.messageId"
        :message="message"
      />
      <div class="preview-card__footer">
        <span>토론 종료 후 요약 카드와 투표 게시글로 이어집니다.</span>
      </div>
    </aside>
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

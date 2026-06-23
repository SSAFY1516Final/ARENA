<template>
  <main class="page workspace-page">
    <section class="workspace-header">
      <div>
        <span class="eyebrow">ARENA</span>
        <h1>무엇을 비교할까요?</h1>
        <p class="page-copy">상황을 조금만 적어두면 바로 토론하기 좋은 주제로 정리해드립니다.</p>
      </div>
    </section>

    <section class="workspace-layout">
      <form class="workspace-setup" @submit.prevent="startDebate">
        <div class="workspace-setup__section">
          <span class="section-label">토론 만들기</span>
          <h2>주제와 상황을 입력하세요</h2>
          <p>원하는 방향이 있으면 세부 조건에 짧게 남겨주세요.</p>
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

        <div class="pipeline-input-grid">
          <div class="field">
            <label class="field-label" for="condition">상황/조건</label>
            <input
              id="condition"
              v-model="condition"
              class="input"
              maxlength="80"
              placeholder="예: 점심시간 15분 남았을 때"
            />
          </div>

          <div class="field">
            <label class="field-label" for="details">세부 조건</label>
            <textarea
              id="details"
              v-model="details"
              class="input textarea"
              maxlength="180"
              rows="4"
              placeholder="예: 맛 비교보다 기다리는 사람, 주문 책임, 분위기 싸움 중심"
            ></textarea>
          </div>
        </div>

        <button class="button button--full workspace-setup__submit" type="submit">
          이 주제로 토론 시작
        </button>
      </form>

      <section class="workspace-main">
        <article class="pipeline-board">
          <div class="workspace-preview__header">
            <div>
              <span class="section-label">추천 주제</span>
              <h2>하나를 골라 시작하세요</h2>
            </div>
            <span class="tag tag--blue">{{ candidateCards.length }}개</span>
          </div>

          <div class="pipeline-candidate-grid">
            <button
              v-for="candidate in candidateCards"
              :key="candidate.id"
              class="pipeline-candidate-card"
              :class="{ 'pipeline-candidate-card--selected': selectedCandidateId === candidate.id }"
              type="button"
              @click="selectedCandidateId = candidate.id"
            >
              <strong>{{ candidate.candidateTitle }}</strong>
              <p>{{ candidate.scene }}</p>
            </button>
          </div>
        </article>

        <article class="pipeline-preview">
          <span class="section-label">선택한 주제</span>
          <h2>{{ selectedCandidate.candidateTitle }}</h2>
          <p class="pipeline-preview__note">{{ selectedCandidate.scene }}</p>
          <button class="button" type="button" @click="startDebate">이 주제로 토론 시작</button>
        </article>
      </section>
    </section>
  </main>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useDebateStore } from '@/stores/debateStore'

const router = useRouter()
const debateStore = useDebateStore()
const topic = ref('오늘 점심 제육 vs 돈까스')
const condition = ref('점심시간 15분 남았을 때')
const details = ref('맛 비교보다 기다리는 사람, 주문 책임, 분위기 싸움 중심')
const selectedCandidateId = ref(1)

const candidateCards = computed(() => {
  const baseTopic = topic.value.trim() || '입력한 주제'
  const baseCondition = condition.value.trim() || '주어진 상황'
  return [
    {
      id: 1,
      candidateTitle: `${baseCondition}, ${baseTopic} 주문을 누가 양보할지 갈린다`,
      scene: `${baseCondition} 모두가 빨리 정해야 하는데 한쪽 선택이 계속 밀리면서 분위기가 굳는다.`,
    },
    {
      id: 2,
      candidateTitle: `${baseTopic} 때문에 단체 주문 총대를 누가 멜지 싸운다`,
      scene: `${baseCondition} 주문을 정리하던 사람이 메뉴 변경을 계속 받아주다가 총대 부담을 떠안는다.`,
    },
    {
      id: 3,
      candidateTitle: `${baseTopic} 선택이 늦어져 쉬는 시간이 날아가도 괜찮은지 갈린다`,
      scene: `${baseCondition} 메뉴 논쟁이 길어져 밥 먹는 시간보다 기다리는 시간이 더 길어진다.`,
    },
  ]
})

const selectedCandidate = computed(() => {
  return candidateCards.value.find((candidate) => candidate.id === selectedCandidateId.value) || candidateCards.value[0]
})

async function startDebate() {
  const debate = await debateStore.createDebate({
    topic: selectedCandidate.value.candidateTitle,
    mode: 'PRACTICAL',
  })
  router.push(`/debates/${debate.debateId}`)
}
</script>

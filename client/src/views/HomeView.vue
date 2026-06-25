<template>
  <main class="page workspace-page">
    <section class="workspace-header new-template-header">
      <div>
        <h1>토론 생성하기</h1>
        <p class="page-copy">주제 입력 후 선택지를 고르면 AI 토론이 바로 시작됩니다.</p>
      </div>
    </section>

    <section class="workspace-layout new-generator-shell">
      <section class="workspace-main new-generator-stage">
        <NCard
          class="workspace-setup new-workbench-card"
          :class="{ 'new-workbench-card--locked': isTopicInputLocked }"
          :bordered="false"
        >
          <form class="workspace-setup-form" @submit.prevent="generateCandidates">
            <div class="workspace-setup__section">
              <span class="section-label">토론 만들기</span>
              <h2>주제를 입력하세요</h2>
            </div>

            <div class="workspace-setup__topic">
              <div class="field-header">
                <label class="field-label" for="topic">토론 주제</label>
                <span>{{ topic.length }}/120</span>
              </div>
              <NInput
                v-model:value="topic"
                type="textarea"
                :autosize="{ minRows: 1, maxRows: 5 }"
                class="input input--topic"
                maxlength="120"
                placeholder="예: 오늘 점심 제육 vs 돈까스"
                :disabled="isTopicInputLocked"
                :input-props="{ id: 'topic', required: true }"
                @keydown.enter.exact.prevent="submitTopicFromKeyboard"
              />
            </div>

            <NButton
              class="workspace-setup__submit"
              type="primary"
              attr-type="submit"
              block
              strong
              :loading="isGeneratingCandidates"
              :disabled="isGeneratingCandidates || hasGeneratedCandidates"
            >
              {{ generateButtonLabel }}
            </NButton>
          </form>
        </NCard>

        <NCard v-if="isGeneratingCandidates" class="new-generation-loading" :bordered="false">
          <div class="new-generation-loading__head">
            <div>
              <h2>토론 세부 주제를 생성중...</h2>
              <p>생성까지 30초 정도 걸릴 수 있어요.</p>
            </div>
          </div>

          <div class="new-generation-loading__progress" aria-hidden="true">
            <span></span>
          </div>
        </NCard>

        <NCard v-if="hasGeneratedCandidates && !isGeneratingCandidates" class="pipeline-board" :bordered="false">
          <div class="workspace-preview__header">
            <div>
              <span class="section-label">추천 세부 주제</span>
              <h2>선택된 세부 주제로 토론을 시작합니다.</h2>
            </div>
          </div>

          <div class="pipeline-candidate-grid">
            <NCard
              v-for="candidate in candidateCards"
              :key="candidate.id"
              class="pipeline-candidate-card new-candidate-card"
              :class="{
                'pipeline-candidate-card--selected': selectedCandidateId === candidate.id,
                'pipeline-candidate-card--disabled': isCandidateUsed(candidate),
              }"
              :bordered="false"
              :hoverable="!isCandidateUsed(candidate)"
              role="button"
              :aria-disabled="isCandidateUsed(candidate) ? 'true' : 'false'"
              :tabindex="isCandidateUsed(candidate) ? -1 : 0"
              @click="selectCandidate(candidate)"
              @keydown.enter="selectCandidate(candidate)"
              @keydown.space.prevent="selectCandidate(candidate)"
            >
              <strong>{{ candidate.candidateTitle }}</strong>
              <p>{{ candidate.scene }}</p>
            </NCard>
          </div>

          <div class="pipeline-start-action">
            <NButton type="primary" strong :loading="isStartingDebate" :disabled="isStartingDebate || !selectedCandidate" @click="startDebate">
              이 주제로 시작하기
            </NButton>
          </div>
        </NCard>
      </section>
    </section>
  </main>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NButton, NCard, NInput } from 'naive-ui'
import { useDebateStore } from '@/stores/debateStore'

const router = useRouter()
const route = useRoute()
const debateStore = useDebateStore()
const initialTopic = typeof route.query.topic === 'string' ? route.query.topic : ''
const topic = ref(initialTopic)
const selectedCandidateId = ref(null)
const hasGeneratedCandidates = ref(false)
const isGeneratingCandidates = ref(false)
const isStartingDebate = ref(false)
const isTopicInputLocked = computed(() => isGeneratingCandidates.value || hasGeneratedCandidates.value)
const routeCandidateRunId = computed(() => {
  const rawRunId = Array.isArray(route.query.candidateRunId)
    ? route.query.candidateRunId[0]
    : route.query.candidateRunId
  const runId = Number(rawRunId)
  return Number.isFinite(runId) && runId > 0 ? runId : null
})

const generateButtonLabel = computed(() => {
  if (isGeneratingCandidates.value) return '생성 중입니다'
  if (hasGeneratedCandidates.value) return '생성 완료'
  return '생성하기'
})

const candidateCards = computed(() => {
  return debateStore.roundCandidates.map((candidate, index) => ({
    id: candidate.candidateId ? String(candidate.candidateId) : candidate.roundId || String(index + 1),
    candidateId: candidate.candidateId || null,
    candidateTitle: candidate.title,
    scene: candidate.coreQuestion,
    coreQuestion: candidate.coreQuestion,
    debateAxis: candidate.debateAxis || '',
    sideAFrame: candidate.sideAFrame || '',
    sideBFrame: candidate.sideBFrame || '',
    selectedRoundId: candidate.roundId || String(index + 1),
    roundTitle: candidate.title || '',
  }))
})

const usedCandidateIdSet = computed(() => new Set((debateStore.usedCandidateIds || []).map((candidateId) => String(candidateId))))
const availableCandidateCards = computed(() => candidateCards.value.filter((candidate) => !isCandidateUsed(candidate)))

const selectedCandidate = computed(() => {
  return availableCandidateCards.value.find((candidate) => candidate.id === selectedCandidateId.value) || availableCandidateCards.value[0] || null
})

async function generateCandidates() {
  if (isGeneratingCandidates.value || hasGeneratedCandidates.value) return
  if (!topic.value.trim()) return

  hasGeneratedCandidates.value = false
  isGeneratingCandidates.value = true
  try {
    await debateStore.generateRoundCandidates({
      topic: topic.value.trim(),
      mode: 'PRACTICAL',
      candidateCount: 5,
    })
    selectedCandidateId.value = firstAvailableCandidateId()
    hasGeneratedCandidates.value = candidateCards.value.length > 0
  } finally {
    isGeneratingCandidates.value = false
  }
}

function submitTopicFromKeyboard() {
  if (isTopicInputLocked.value || !topic.value.trim()) return
  generateCandidates()
}

async function loadExistingCandidateRun(runId) {
  if (isGeneratingCandidates.value || hasGeneratedCandidates.value) return

  isGeneratingCandidates.value = true
  try {
    await debateStore.fetchRoundCandidateRun(runId)
    selectedCandidateId.value = firstAvailableCandidateId()
    hasGeneratedCandidates.value = candidateCards.value.length > 0
  } finally {
    isGeneratingCandidates.value = false
  }
}

function isCandidateUsed(candidate) {
  return Boolean(candidate?.candidateId && usedCandidateIdSet.value.has(String(candidate.candidateId)))
}

function firstAvailableCandidateId() {
  return availableCandidateCards.value[0]?.id || null
}

function selectCandidate(candidate) {
  if (isCandidateUsed(candidate)) return
  selectedCandidateId.value = candidate.id
}

async function startDebate() {
  if (isStartingDebate.value || !selectedCandidate.value) return

  isStartingDebate.value = true
  try {
    const debate = await debateStore.createDebate({
      originalTopic: topic.value.trim(),
      topic: selectedCandidate.value.coreQuestion || selectedCandidate.value.candidateTitle,
      mode: 'PRACTICAL',
      sideALabel: debateStore.topicFrame?.sideA || '',
      sideBLabel: debateStore.topicFrame?.sideB || '',
      debateAxis: selectedCandidate.value.debateAxis || '',
      sideAFrame: selectedCandidate.value.sideAFrame || '',
      sideBFrame: selectedCandidate.value.sideBFrame || '',
      selectedRoundId: selectedCandidate.value.selectedRoundId || '',
      roundTitle: selectedCandidate.value.roundTitle || '',
      basicConditions: debateStore.topicFrame?.basicConditions || '',
      candidateRunId: debateStore.candidateRunId || null,
      selectedCandidateId: selectedCandidate.value.candidateId || null,
    })
    await router.push({
      path: `/debates/${debate.debateId}`,
      query: { starting: '1' },
    })
  } finally {
    isStartingDebate.value = false
  }
}

onMounted(() => {
  if (routeCandidateRunId.value) {
    loadExistingCandidateRun(routeCandidateRunId.value)
    return
  }
  if (route.query.candidates === '1') {
    generateCandidates()
  }
})
</script>

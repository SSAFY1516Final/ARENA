import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { debateApi } from '@/api/debateApi'

function mapDebate(debate) {
  return {
    debateId: debate.debateId,
    originalTopic: debate.originalTopic || '',
    topic: debate.topic,
    sideALabel: debate.sideALabel || '',
    sideBLabel: debate.sideBLabel || '',
    debateAxis: debate.debateAxis || '',
    sideAFrame: debate.sideAFrame || '',
    sideBFrame: debate.sideBFrame || '',
    selectedRoundId: debate.selectedRoundId || '',
    roundTitle: debate.roundTitle || '',
    basicConditions: debate.basicConditions || '',
    candidateRunId: debate.candidateRunId || null,
    selectedCandidateId: debate.selectedCandidateId || null,
    mode: debate.mode,
    status: debate.status,
    selectedSide: debate.selectedSide || '',
    selectedRoundNo: debate.selectedRoundNo || null,
    summaryCard: debate.summaryCard || debate.status,
    shareBody: debate.shareBody || '',
    roundCount: Number(debate.roundCount) || 0,
    coolCount: Number(debate.coolCount) || 0,
    hotCount: Number(debate.hotCount) || 0,
    updatedAt: debate.updatedAt,
  }
}

function mapMessage(message) {
  return {
    messageId: message.messageId || message.id,
    speaker: message.speaker,
    roundNo: message.roundNo,
    content: message.content,
    peakReached: Boolean(message.peakReached),
  }
}

function mapRound(round) {
  return {
    roundNo: Number(round.roundNo) || 1,
    debateId: round.debateId || null,
    selectedSide: round.selectedSide || '',
    title: round.title || '',
    topic: round.topic || '',
    description: round.description || '',
    summary: round.summary || null,
  }
}

function normalizeInitialTurnGenerationResponse(data) {
  if (Array.isArray(data)) {
    return {
      status: data.length >= 10 ? 'COMPLETE' : 'GENERATING',
      messages: data,
    }
  }
  return {
    status: data?.status || 'GENERATING',
    messages: data?.messages || [],
  }
}

export const useDebateStore = defineStore('debate', () => {
  const currentDebate = ref({
    debateId: null,
    originalTopic: '',
    topic: '',
    sideALabel: '',
    sideBLabel: '',
    debateAxis: '',
    sideAFrame: '',
    sideBFrame: '',
    selectedRoundId: '',
    roundTitle: '',
    basicConditions: '',
    candidateRunId: null,
    selectedCandidateId: null,
    mode: 'PRACTICAL',
    status: 'ACTIVE',
    selectedSide: '',
    selectedRoundNo: null,
    shareBody: '',
  })
  const messages = ref([])
  const rounds = ref([])
  const summary = ref(null)
  const topicFrame = ref(null)
  const roundCandidates = ref([])
  const usedCandidateIds = ref([])
  const candidateRunId = ref(null)
  const turnLoading = ref(false)
  const stopLoading = ref(false)
  const myDebates = ref([])
  const loading = ref(false)
  const deleteLoading = ref(false)
  const candidateGenerationLoading = ref(false)
  const initialTurnGenerationStatus = ref('')

  const isStopped = computed(() => currentDebate.value?.status === 'STOPPED')
  const isShared = computed(() => currentDebate.value?.status === 'SHARED')

  async function createDebate(payload) {
    const { data } = await debateApi.create(payload)
    currentDebate.value = mapDebate(data)
    messages.value = []
    rounds.value = []
    summary.value = null
    myDebates.value = [currentDebate.value, ...myDebates.value]
    return currentDebate.value
  }

  async function generateRoundCandidates(payload) {
    candidateGenerationLoading.value = true
    try {
      const { data } = await debateApi.generateRoundCandidates(payload)
      applyRoundCandidateResponse(data)
      return data
    } finally {
      candidateGenerationLoading.value = false
    }
  }

  async function fetchRoundCandidateRun(runId) {
    candidateGenerationLoading.value = true
    try {
      const { data } = await debateApi.getRoundCandidateRun(runId)
      applyRoundCandidateResponse(data)
      return data
    } finally {
      candidateGenerationLoading.value = false
    }
  }

  function applyRoundCandidateResponse(data) {
    candidateRunId.value = data.candidateRunId || null
    topicFrame.value = data.topicFrame || null
    roundCandidates.value = data.roundCandidates || []
    usedCandidateIds.value = data.usedCandidateIds || []
  }

  async function fetchMyDebates() {
    loading.value = true
    try {
      const { data } = await debateApi.list()
      myDebates.value = data.map(mapDebate)
      return myDebates.value
    } finally {
      loading.value = false
    }
  }

  async function fetchDebate(debateId) {
    const { data } = await debateApi.detail(debateId)
    currentDebate.value = mapDebate(data.debate)
    messages.value = (data.messages || []).map(mapMessage)
    rounds.value = (data.rounds || []).map(mapRound)
    summary.value = data.summary || null
    return currentDebate.value
  }

  async function generateNextTurn(debateId) {
    if (currentDebate.value?.status !== 'ACTIVE') {
      return null
    }

    turnLoading.value = true
    try {
      const { data } = await debateApi.nextTurn(debateId || currentDebate.value.debateId)
      const message = mapMessage(data)
      messages.value.push(message)
      currentDebate.value.peakReached = message.peakReached
      return message
    } finally {
      turnLoading.value = false
    }
  }

  async function generateInitialTurns(debateId) {
    if (currentDebate.value?.status !== 'ACTIVE') {
      return { status: 'COMPLETE', messages: [] }
    }

    turnLoading.value = true
    try {
      const { data } = await debateApi.generateInitialTurns(debateId || currentDebate.value.debateId)
      const response = normalizeInitialTurnGenerationResponse(data)
      messages.value = response.messages.map(mapMessage)
      initialTurnGenerationStatus.value = response.status
      currentDebate.value.peakReached = response.status === 'COMPLETE' || messages.value.some((message) => message.peakReached)
      return {
        status: response.status,
        messages: messages.value,
      }
    } finally {
      turnLoading.value = false
    }
  }

  async function stopDebate(debateId, payload = null) {
    stopLoading.value = true
    try {
      const { data } = await debateApi.stop(debateId || currentDebate.value.debateId, payload)
      currentDebate.value = {
        ...currentDebate.value,
        debateId: data.debateId,
        status: data.status,
        selectedSide: data.selectedSide || payload?.selectedSide || currentDebate.value.selectedSide || '',
        selectedRoundNo: data.selectedRoundNo || payload?.selectedRoundNo || currentDebate.value.selectedRoundNo || null,
      }
      summary.value = data.summary
      return data
    } finally {
      stopLoading.value = false
    }
  }

  async function shareDebate(debateId, payload) {
    const { data } = await debateApi.share(debateId, payload)
    currentDebate.value = {
      ...currentDebate.value,
      debateId: Number(debateId),
      status: 'SHARED',
      shareBody: summary.value?.summaryText || '',
    }

    const debateIndex = myDebates.value.findIndex((debate) => String(debate.debateId) === String(debateId))
    const nextDebate = {
      debateId: Number(debateId),
      originalTopic: currentDebate.value.originalTopic,
      topic: data.title || currentDebate.value.topic,
      sideALabel: currentDebate.value.sideALabel,
      sideBLabel: currentDebate.value.sideBLabel,
      debateAxis: currentDebate.value.debateAxis,
      sideAFrame: currentDebate.value.sideAFrame,
      sideBFrame: currentDebate.value.sideBFrame,
      selectedRoundId: currentDebate.value.selectedRoundId,
      roundTitle: currentDebate.value.roundTitle,
      basicConditions: currentDebate.value.basicConditions,
      candidateRunId: currentDebate.value.candidateRunId,
      selectedCandidateId: currentDebate.value.selectedCandidateId,
      selectedSide: currentDebate.value.selectedSide,
      selectedRoundNo: currentDebate.value.selectedRoundNo,
      mode: currentDebate.value.mode,
      status: 'SHARED',
      summaryCard: summary.value?.summaryText || '공유됨',
      shareBody: summary.value?.summaryText || '',
      updatedAt: new Date().toISOString(),
    }

    if (debateIndex >= 0) {
      myDebates.value = myDebates.value.map((debate, index) =>
        index === debateIndex ? { ...debate, ...nextDebate } : debate,
      )
    } else {
      myDebates.value = [nextDebate, ...myDebates.value]
    }

    return data
  }

  async function deleteDebate(debateId) {
    deleteLoading.value = true
    try {
      await debateApi.remove(debateId)
      myDebates.value = myDebates.value.filter((debate) => String(debate.debateId) !== String(debateId))
      if (String(currentDebate.value?.debateId) === String(debateId)) {
        currentDebate.value = {
          debateId: null,
          originalTopic: '',
          topic: '',
          sideALabel: '',
          sideBLabel: '',
          debateAxis: '',
          sideAFrame: '',
          sideBFrame: '',
          selectedRoundId: '',
          roundTitle: '',
          basicConditions: '',
          candidateRunId: null,
          selectedCandidateId: null,
          mode: 'PRACTICAL',
          status: 'ACTIVE',
          selectedSide: '',
          selectedRoundNo: null,
          shareBody: '',
        }
        messages.value = []
        rounds.value = []
        summary.value = null
      }
    } finally {
      deleteLoading.value = false
    }
  }

  return {
    currentDebate,
    messages,
    rounds,
    summary,
    topicFrame,
    roundCandidates,
    usedCandidateIds,
    candidateRunId,
    myDebates,
    turnLoading,
    stopLoading,
    deleteLoading,
    candidateGenerationLoading,
    initialTurnGenerationStatus,
    loading,
    isStopped,
    isShared,
    createDebate,
    generateRoundCandidates,
    fetchRoundCandidateRun,
    fetchMyDebates,
    fetchDebate,
    generateNextTurn,
    generateInitialTurns,
    stopDebate,
    shareDebate,
    deleteDebate,
  }
})

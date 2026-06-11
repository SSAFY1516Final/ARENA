import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { debateApi } from '@/api/debateApi'

function mapDebate(debate) {
  return {
    debateId: debate.debateId,
    topic: debate.topic,
    mode: debate.mode,
    status: debate.status,
    summaryCard: debate.summaryCard || debate.status,
    shareBody: debate.shareBody || '',
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

export const useDebateStore = defineStore('debate', () => {
  const currentDebate = ref({
    debateId: null,
    topic: '',
    mode: 'PRACTICAL',
    status: 'ACTIVE',
    shareBody: '',
  })
  const messages = ref([])
  const summary = ref(null)
  const turnLoading = ref(false)
  const stopLoading = ref(false)
  const myDebates = ref([])
  const loading = ref(false)

  const isStopped = computed(() => currentDebate.value?.status === 'STOPPED')
  const isShared = computed(() => currentDebate.value?.status === 'SHARED')

  async function createDebate(payload) {
    const { data } = await debateApi.create(payload)
    currentDebate.value = mapDebate(data)
    messages.value = []
    summary.value = null
    myDebates.value = [currentDebate.value, ...myDebates.value]
    return currentDebate.value
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

  async function stopDebate(debateId) {
    stopLoading.value = true
    try {
      const { data } = await debateApi.stop(debateId || currentDebate.value.debateId)
      currentDebate.value = {
        ...currentDebate.value,
        debateId: data.debateId,
        status: data.status,
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
      topic: data.title || currentDebate.value.topic,
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

  return {
    currentDebate,
    messages,
    summary,
    myDebates,
    turnLoading,
    stopLoading,
    loading,
    isStopped,
    isShared,
    createDebate,
    fetchMyDebates,
    fetchDebate,
    generateNextTurn,
    stopDebate,
    shareDebate,
  }
})

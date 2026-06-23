<template>
  <article class="debate-message" :class="{ hot: message.speaker === 'PASSIONATE' }">
    <div class="bubble" :class="{ hot: message.speaker === 'PASSIONATE' }">
      <div class="bubble__meta">
        <strong>{{ sideLabel }}</strong>
        <span>의견 {{ displayIndex }}</span>
      </div>
      <p>{{ message.content }}</p>
    </div>
  </article>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  message: {
    type: Object,
    required: true,
  },
  sideLabels: {
    type: Object,
    required: true,
  },
  displayIndex: {
    type: Number,
    required: true,
  },
})

const sideLabel = computed(() => props.sideLabels[props.message.speaker] || 'A 진영')
</script>

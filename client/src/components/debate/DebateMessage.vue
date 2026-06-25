<template>
  <article class="debate-message" :class="{ hot: message.speaker === 'PASSIONATE' }">
    <div class="debate-message__body">
      <strong class="debate-message__name">{{ sideLabel }}</strong>
      <div class="bubble" :class="{ hot: message.speaker === 'PASSIONATE' }">
        <p>{{ message.content }}</p>
      </div>
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

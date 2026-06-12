<template>
  <article class="debate-message" :class="{ hot: message.speaker === 'PASSIONATE' }">
    <div class="avatar" :class="{ hot: message.speaker === 'PASSIONATE' }">
      {{ persona.shortLabel }}
    </div>
    <div class="bubble" :class="{ hot: message.speaker === 'PASSIONATE' }">
      <div class="bubble__meta">
        <strong>{{ persona.label }}</strong>
        <span>Round {{ message.roundNo }}</span>
      </div>
      <p>{{ message.content }}</p>
    </div>
  </article>
</template>

<script setup>
import { computed } from 'vue'
import { personas } from '@/mocks/data'

const props = defineProps({
  message: {
    type: Object,
    required: true,
  },
})

const persona = computed(() => personas[props.message.speaker] || personas.COOL_HEADED)
</script>

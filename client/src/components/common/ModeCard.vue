<template>
  <button class="mode-card" :class="{ selected }" type="button" @click="$emit('select', mode)">
    <span class="mode-card__label">{{ label }}</span>
    <span class="mode-card__description">{{ description }}</span>
  </button>
</template>

<script setup>
defineProps({
  mode: {
    type: String,
    required: true,
  },
  label: {
    type: String,
    required: true,
  },
  description: {
    type: String,
    required: true,
  },
  selected: {
    type: Boolean,
    default: false,
  },
})

defineEmits(['select'])
</script>

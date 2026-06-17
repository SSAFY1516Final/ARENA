<template>
  <div class="vote-widget">
    <span class="vote-widget__title">사용자 투표</span>
    <div class="vote-widget__prompt">
      <span>{{ optionA }} / {{ optionB }}</span>
      <strong>상세에서 투표하기</strong>
    </div>
  </div>
</template>

<script setup>
defineProps({
  optionA: {
    type: String,
    required: true,
  },
  optionB: {
    type: String,
    required: true,
  },
  voteA: {
    type: Number,
    default: 0,
  },
  voteB: {
    type: Number,
    default: 0,
  },
})
</script>

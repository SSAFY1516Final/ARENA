<template>
  <AppNav v-if="!authRouteNames.includes(route.name)" />
  <RouterView />
</template>

<script setup>
import { onMounted } from 'vue'
import { RouterView, useRoute } from 'vue-router'
import AppNav from '@/components/common/AppNav.vue'
import { useAuthStore } from '@/stores/authStore'

const route = useRoute()
const auth = useAuthStore()
const authRouteNames = ['auth', 'kakao-callback']

onMounted(() => {
  auth.fetchMe()
})
</script>

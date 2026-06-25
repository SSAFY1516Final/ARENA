<template>
  <main class="page auth-callback-page">
    <section class="empty-state">
      <span class="eyebrow">Kakao OAuth</span>
      <h1>{{ title }}</h1>
      <p>{{ message }}</p>
      <RouterLink v-if="hasError" class="button" to="/auth">다시 로그인</RouterLink>
    </section>
  </main>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const hasError = ref(false)

const title = computed(() => (hasError.value ? '로그인에 실패했습니다' : '카카오 로그인 처리 중'))
const message = computed(() =>
  hasError.value ? authStore.errorMessage || '다시 시도해주세요.' : '잠시만 기다려주세요.',
)

onMounted(async () => {
  const code = route.query.code
  if (!code) {
    hasError.value = true
    return
  }

  try {
    const redirectUri = `${window.location.origin}/auth/kakao/callback`
    await authStore.loginWithKakaoCode(String(code), redirectUri)
    await router.replace('/debates')
  } catch {
    hasError.value = true
  }
})
</script>

<template>
  <header class="app-nav">
    <RouterLink class="brand" to="/debates">
      <span class="brand-mark" aria-hidden="true"></span>
      <span>ARENA</span>
    </RouterLink>

    <nav class="nav-links" aria-label="주요 메뉴">
      <RouterLink to="/posts">게시판</RouterLink>
      <RouterLink to="/debates">내 토론</RouterLink>
      <RouterLink class="nav-create-link" to="/new">새 토론</RouterLink>
    </nav>

    <div class="nav-user">
      <div v-if="auth.isAuthenticated" class="profile-chip" aria-label="현재 로그인 사용자">
        <span class="profile-avatar" aria-hidden="true">{{ auth.user.nickname.slice(0, 1) }}</span>
        <span>{{ auth.user.nickname }}</span>
      </div>
      <RouterLink v-else class="login-link" to="/auth">로그인</RouterLink>
    </div>
  </header>
</template>

<script setup>
import { RouterLink } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'

const auth = useAuthStore()
</script>

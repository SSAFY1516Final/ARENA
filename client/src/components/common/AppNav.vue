<template>
  <header class="app-nav">
    <RouterLink class="brand" to="/debates">
      <span class="brand-mark" aria-hidden="true"></span>
      <span>ARENA</span>
    </RouterLink>

    <nav class="nav-links" aria-label="주요 메뉴">
      <RouterLink to="/posts">게시판</RouterLink>
      <RouterLink to="/debates">내 토론</RouterLink>
    </nav>

    <div class="nav-user">
      <RouterLink v-if="auth.isAuthenticated" class="nav-action-link" to="/new">토론 만들기</RouterLink>
      <div v-if="auth.isAuthenticated" class="profile-menu">
        <button
          class="profile-chip"
          type="button"
          :aria-expanded="isNicknameEditorOpen"
          aria-controls="nickname-editor"
          @click="toggleNicknameEditor"
        >
          <span class="profile-avatar" aria-hidden="true">{{ displayNickname.slice(0, 1) }}</span>
          <span>{{ displayNickname }}</span>
        </button>

        <form
          v-if="isNicknameEditorOpen"
          id="nickname-editor"
          class="nickname-editor"
          @submit.prevent="saveNickname"
        >
          <label for="nickname-input">닉네임</label>
          <div class="nickname-editor-row">
            <input
              id="nickname-input"
              v-model.trim="nicknameInput"
              type="text"
              maxlength="20"
              autocomplete="nickname"
              placeholder="2~20자"
            />
            <button type="submit" :disabled="!canSaveNickname">
              {{ auth.profileSaving ? '저장 중' : '저장' }}
            </button>
          </div>
          <p v-if="nicknameError" class="nickname-editor-error">{{ nicknameError }}</p>
          <p v-else class="nickname-editor-help">한글, 영문, 숫자, 밑줄만 사용할 수 있습니다.</p>
        </form>
      </div>
      <button v-if="auth.isAuthenticated" class="logout-button" type="button" @click="logout">로그아웃</button>
      <RouterLink v-else class="login-link" to="/auth">로그인</RouterLink>
    </div>
  </header>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'

const auth = useAuthStore()
const router = useRouter()
const isNicknameEditorOpen = ref(false)
const nicknameInput = ref('')
const nicknameError = ref('')

const displayNickname = computed(() => auth.user?.nickname || auth.user?.loginId || '사용자')
const normalizedNickname = computed(() => nicknameInput.value.trim())
const canSaveNickname = computed(() => {
  return (
    normalizedNickname.value.length >= 2 &&
    normalizedNickname.value.length <= 20 &&
    normalizedNickname.value !== displayNickname.value &&
    !auth.profileSaving
  )
})

watch(displayNickname, (nickname) => {
  if (!isNicknameEditorOpen.value) {
    nicknameInput.value = nickname
  }
}, { immediate: true })

function toggleNicknameEditor() {
  isNicknameEditorOpen.value = !isNicknameEditorOpen.value
  nicknameError.value = ''
  nicknameInput.value = displayNickname.value
}

async function saveNickname() {
  nicknameError.value = ''
  const nickname = normalizedNickname.value
  if (!canSaveNickname.value) {
    return
  }

  try {
    await auth.updateNickname(nickname)
    isNicknameEditorOpen.value = false
  } catch (error) {
    nicknameError.value = error.userMessage || '닉네임 변경에 실패했습니다.'
  }
}

async function logout() {
  await auth.logout()
  isNicknameEditorOpen.value = false
  router.push('/auth')
}
</script>

<template>
  <main class="auth-shell">
    <section class="auth-landing">
      <header class="auth-topbar">
        <RouterLink class="auth-brand" to="/auth">
          <span class="brand-mark" aria-hidden="true"></span>
          <span>ARENA</span>
        </RouterLink>
        <a
          class="kakao-login-button auth-header-login"
          :class="{ 'kakao-login-button--disabled': !isKakaoConfigured }"
          :href="isKakaoConfigured ? kakaoAuthorizeUrl : undefined"
          :aria-disabled="!isKakaoConfigured"
          @click="handleKakaoLoginClick"
        >
          <svg class="kakao-login-button__icon" viewBox="0 0 24 22" aria-hidden="true">
            <path
              d="M12 1.5C5.92 1.5 1 5.32 1 10.03c0 3.05 2.07 5.72 5.18 7.23l-.86 3.16c-.08.28.24.5.48.34l3.78-2.5c.78.12 1.59.19 2.42.19 6.08 0 11-3.82 11-8.52S18.08 1.5 12 1.5Z"
              fill="currentColor"
            />
          </svg>
          <span>카카오 로그인</span>
        </a>
      </header>

      <section class="auth-gate">
        <section class="auth-intro">
          <h1>고민을 올리면, 서로 다른 관점이 먼저 답합니다</h1>
          <p>
            주제만 던지면 서로 다른 관점의 AI가 근거를 나눠 말하고,
            반박과 재반박까지 이어지는 토론을 바로 확인할 수 있습니다.
          </p>

          <p v-if="!isKakaoConfigured" class="auth-config-warning">
            카카오 REST API 키를 설정하면 실제 로그인이 가능합니다.
          </p>
        </section>

        <aside class="auth-preview-card" aria-label="ARENA 샘플 토론">
          <div class="auth-preview-topbar">
            <span>운동 루틴 고민</span>
          </div>

          <div class="auth-topic-prompt">
            <p>꾸준히 운동하려면 퇴근 후가 나을까, 아침 시간이 나을까?</p>
          </div>

          <div
            v-for="message in previewMessages"
            :key="message.id"
            class="auth-chat-row"
            :class="`auth-chat-row--${message.side}`"
          >
            <div class="auth-chat-message">
              <span class="auth-chat-name">{{ message.name }}</span>
              <div class="auth-debate-bubble" :class="`auth-debate-bubble--${message.side}`">
                <p>{{ message.text }}</p>
              </div>
            </div>
          </div>
        </aside>
      </section>
    </section>
  </main>
</template>

<script setup>
import { computed } from 'vue'
import { RouterLink } from 'vue-router'

const kakaoRestApiKey = import.meta.env.VITE_KAKAO_REST_API_KEY || ''
const kakaoRedirectUri = import.meta.env.VITE_KAKAO_REDIRECT_URI || `${window.location.origin}/auth/kakao/callback`
const isKakaoConfigured = computed(() => Boolean(kakaoRestApiKey) && !kakaoRestApiKey.startsWith('your-'))
const previewMessages = [
  {
    id: 1,
    side: 'left',
    name: '아침형',
    text: '지속 가능성을 보면 아침 운동이 유리해요. 야근이나 약속 같은 변수가 적고, 하루의 첫 결정을 이미 끝낼 수 있습니다.',
  },
  {
    id: 2,
    side: 'right',
    name: '저녁형',
    text: '퇴근 후 운동은 스트레스를 바로 해소해요. 보상감이 커서 운동을 의무가 아니라 하루를 닫는 루틴으로 만들 수 있습니다.',
  },
  {
    id: 3,
    side: 'left',
    name: '아침형',
    text: '근데 퇴근 후 운동은 피곤하다는 핑계가 너무 강해요. 하루가 밀리면 운동이 제일 먼저 빠지는 선택지가 됩니다.',
  },
  {
    id: 4,
    side: 'right',
    name: '저녁형',
    text: '반대로 아침 운동은 잠을 줄여야 해서 시작 장벽이 큽니다. 꾸준함은 시간대보다 회복 가능한 루틴인지가 더 중요해요.',
  },
]
const kakaoAuthorizeUrl = computed(() => {
  const params = new URLSearchParams({
    client_id: kakaoRestApiKey,
    redirect_uri: kakaoRedirectUri,
    response_type: 'code',
    lang: 'ko',
  })
  return `https://kauth.kakao.com/oauth/authorize?${params.toString()}`
})

const handleKakaoLoginClick = (event) => {
  if (!isKakaoConfigured.value) {
    event.preventDefault()
  }
}
</script>

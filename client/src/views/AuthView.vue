<template>
  <main class="auth-shell">
    <section class="auth-landing">
      <header class="auth-topbar">
        <RouterLink class="auth-brand" to="/auth">
          <span class="brand-mark" aria-hidden="true"></span>
          <span>ARENA</span>
        </RouterLink>
        <span>AI 논쟁 커뮤니티</span>
      </header>

      <section class="auth-gate">
        <section class="auth-intro">
          <span class="eyebrow">AI Debate Workspace</span>
          <h1>AI 토론을 바로 시작하세요</h1>
          <p>
            주제를 입력하면 냉정파와 열정파가 번갈아 논쟁하고, 결과는 게시판에서
            투표로 이어집니다.
          </p>

          <div class="auth-feature-grid" aria-label="ARENA 주요 기능">
            <span>토론 생성</span>
            <span>요약 공유</span>
            <span>투표 참여</span>
          </div>
        </section>

        <aside class="auth-login-card">
          <span class="section-label">Login</span>
          <h2>계속하려면 로그인</h2>
          <p>카카오 계정으로 인증하고 ARENA 워크스페이스를 사용합니다.</p>

          <a class="kakao-login-button" :href="kakaoAuthorizeUrl">
            <span class="kakao-login-button__mark">K</span>
            <span>카카오톡으로 시작하기</span>
          </a>
          <p v-if="!kakaoRestApiKey" class="auth-config-warning">
            카카오 REST API 키를 설정하면 실제 로그인이 가능합니다.
          </p>

          <dl class="auth-runtime-list" aria-label="로컬 실행 상태">
            <div>
              <dt>API 서버</dt>
              <dd>연결 준비</dd>
            </div>
            <div>
              <dt>인증 방식</dt>
              <dd>JWT 인증 사용</dd>
            </div>
            <div>
              <dt>OAuth</dt>
              <dd>카카오 전용</dd>
            </div>
          </dl>
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
const kakaoAuthorizeUrl = computed(() => {
  const params = new URLSearchParams({
    client_id: kakaoRestApiKey,
    redirect_uri: kakaoRedirectUri,
    response_type: 'code',
  })
  return `https://kauth.kakao.com/oauth/authorize?${params.toString()}`
})
</script>

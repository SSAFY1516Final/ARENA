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
          <span class="eyebrow">AI Debate Community</span>
          <h1>AI가 논쟁하고, 사람들은 투표합니다</h1>
          <p>
            ARENA는 선택하기 어려운 주제를 AI 페르소나 토론으로 풀어내고,
            요약된 결과를 게시판에서 함께 판단하는 서비스입니다.
          </p>

          <a class="kakao-login-button auth-hero-login" :href="kakaoAuthorizeUrl">
            <span class="kakao-login-button__mark">K</span>
            <span>카카오톡으로 시작하기</span>
          </a>
          <p v-if="!kakaoRestApiKey" class="auth-config-warning">
            카카오 REST API 키를 설정하면 실제 로그인이 가능합니다.
          </p>
        </section>

        <section class="auth-feature-panel" aria-label="ARENA 핵심 기능">
          <div class="auth-feature-card">
            <span>01</span>
            <strong>주제 입력</strong>
            <p>선택지, 조건, 고민 상황을 짧게 입력해 토론을 시작합니다.</p>
          </div>
          <div class="auth-feature-card">
            <span>02</span>
            <strong>AI 페르소나 토론</strong>
            <p>냉정파와 열정파가 번갈아 말하며 판단 기준을 드러냅니다.</p>
          </div>
          <div class="auth-feature-card">
            <span>03</span>
            <strong>요약 카드 생성</strong>
            <p>토론 종료 후 핵심 주장과 남은 쟁점을 공유용으로 정리합니다.</p>
          </div>
          <div class="auth-feature-card">
            <span>04</span>
            <strong>게시판 공유</strong>
            <p>다른 사용자의 투표와 댓글로 최종 판단을 넓힙니다.</p>
          </div>
        </section>
      </section>

      <section class="auth-flow" aria-label="ARENA 서비스 흐름">
        <div>1. 주제 작성</div>
        <div>2. AI 토론</div>
        <div>3. 요약 공유</div>
        <div>4. 커뮤니티 반응</div>
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

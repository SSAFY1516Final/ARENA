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

      <section class="auth-intro">
        <span class="eyebrow">AI Debate Workspace</span>
        <h1>고민을 꺼내면<br />논쟁이 시작됩니다</h1>
        <p>
          두 AI 페르소나가 선택지를 밀고 당기고, 사람들은 게시판에서 투표합니다.
          ARENA는 결정과 재미 사이를 빠르게 정리하는 토론 공간입니다.
        </p>

        <a class="kakao-login-button" :href="kakaoAuthorizeUrl">
          <span class="kakao-login-button__mark">K</span>
          <span>카카오톡으로 시작하기</span>
        </a>
        <p v-if="!kakaoRestApiKey" class="auth-config-warning">
          카카오 REST API 키를 설정하면 실제 로그인이 가능합니다.
        </p>
      </section>

      <section class="auth-product" aria-label="ARENA 시작 화면">
        <article class="auth-product__board">
          <div class="auth-product__top">
            <span class="tag tag--teal">실용 판정</span>
            <strong>오늘 점심 제육 vs 돈까스</strong>
          </div>
          <div class="auth-product__message">
            <span>냉정파</span>
            <p>오후 일정이 빡빡하면 실패 확률이 낮은 선택이 더 안전합니다.</p>
          </div>
          <div class="auth-product__message auth-product__message--hot">
            <span>열정파</span>
            <p>하지만 점심은 보상입니다. 오늘을 버티게 하는 맛도 기준이 됩니다.</p>
          </div>
        </article>

        <aside class="auth-product__side">
          <div class="auth-mini-card">
            <span>사용자 투표</span>
            <strong>제육 43%</strong>
            <div class="auth-mini-bar"><i></i></div>
          </div>
          <div class="auth-mini-card">
            <span>내 토론</span>
            <strong>4개 진행 중</strong>
          </div>
          <div class="auth-mini-card">
            <span>로그인</span>
            <strong>카카오 전용</strong>
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
const kakaoAuthorizeUrl = computed(() => {
  const params = new URLSearchParams({
    client_id: kakaoRestApiKey,
    redirect_uri: kakaoRedirectUri,
    response_type: 'code',
  })
  return `https://kauth.kakao.com/oauth/authorize?${params.toString()}`
})
</script>

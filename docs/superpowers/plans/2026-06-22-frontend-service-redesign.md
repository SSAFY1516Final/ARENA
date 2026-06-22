# Frontend Service Redesign Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Redesign the Vue frontend so ARENA feels like an actual AI debate service, centered on debate creation and debate-room operation rather than landing-page mockups.

**Architecture:** Keep the current Vue 3, Pinia, router, and API contracts. Modify only view structure, copy, tests, and global CSS. `/new` becomes the main debate workspace, `/debates/:id` becomes a debate operation screen with a compact status rail, `/auth` becomes a concise Kakao service entry screen, and `/posts` receives light visual alignment.

**Tech Stack:** Vue 3 SFC, Vite, Pinia, Vue Router, Vitest, @vue/test-utils, CSS.

---

## File Structure

- Modify `client/src/views/AuthView.vue`: Replace promotional hero copy with concise service entry and a compact product preview.
- Modify `client/src/views/HomeView.vue`: Convert the current hero/sample layout into a debate workspace with a left setup panel, central preview, and light community signals.
- Modify `client/src/views/DebateRoomView.vue`: Add a compact status/action rail beside the debate log.
- Modify `client/src/views/PostListView.vue`: Keep API behavior and filters, adjust copy and card metadata toward a service list.
- Modify `client/src/components/common/AppNav.vue`: Align nav labels and primary action with the debate-first flow.
- Modify `client/src/styles/base.css`: Add responsive workspace/debate-room layout styles and reduce landing-page decoration.
- Modify `client/src/views/__tests__/AuthView.test.js`: Assert concise Kakao-only entry and no password inputs.
- Modify `client/src/views/__tests__/HomeView.test.js`: Assert workspace structure instead of sample hero copy.
- Modify `client/src/views/__tests__/PostListView.test.js`: Keep board behavior assertion and update empty/copy expectations if needed.
- Create or modify `client/src/views/__tests__/DebateRoomView.test.js`: Assert status rail and debate action availability.

## Task 1: Update Tests for Approved Screen Structure

**Files:**
- Modify: `client/src/views/__tests__/AuthView.test.js`
- Modify: `client/src/views/__tests__/HomeView.test.js`
- Modify: `client/src/views/__tests__/DebateRoomView.test.js`
- Modify: `client/src/views/__tests__/PostListView.test.js`

- [ ] **Step 1: Update `AuthView` test expectations**

Replace the existing test body assertions with:

```js
expect(wrapper.text()).toContain('AI 토론을 바로 시작하세요')
expect(wrapper.text()).toContain('카카오톡으로 시작하기')
expect(wrapper.text()).toContain('실제 토론 흐름')
expect(wrapper.text()).not.toContain('회원가입')
expect(wrapper.find('input[type="password"]').exists()).toBe(false)
expect(wrapper.find('.kakao-login-button').attributes('href')).toContain('kauth.kakao.com/oauth/authorize')
```

- [ ] **Step 2: Update `HomeView` structure test**

Replace the sample-debate oriented assertions with:

```js
expect(wrapper.find('.workspace-layout').exists()).toBe(true)
expect(wrapper.find('.workspace-setup').exists()).toBe(true)
expect(wrapper.find('.workspace-main').exists()).toBe(true)
expect(wrapper.text()).toContain('무엇을 비교할까요?')
expect(wrapper.text()).toContain('최근 공유된 토론')
expect(wrapper.text()).toContain('투표가 진행 중')
expect(wrapper.text()).not.toContain('샘플 토론')
expect(wrapper.text()).not.toContain('고민을 꺼내면')
```

Keep the existing submit test, but change the form selector from `form.start-panel` to `form.workspace-setup`.

- [ ] **Step 3: Add or update `DebateRoomView` assertions**

Ensure the test mounts `DebateRoomView` with a mocked debate store state and asserts:

```js
expect(wrapper.find('.debate-room-layout').exists()).toBe(true)
expect(wrapper.find('.debate-status-rail').exists()).toBe(true)
expect(wrapper.text()).toContain('토론 컨트롤')
expect(wrapper.text()).toContain('발화')
expect(wrapper.text()).toContain('다음 발화')
expect(wrapper.text()).toContain('종료')
```

- [ ] **Step 4: Keep `PostListView` behavior test and add copy alignment**

Add these expectations after posts load:

```js
expect(wrapper.text()).toContain('공유된 토론')
expect(wrapper.text()).toContain('토론 결과가 커뮤니티로 이어집니다')
expect(wrapper.text()).toContain('댓글 2')
```

- [ ] **Step 5: Run frontend tests and confirm expected failures**

Run:

```bash
cd client
npm test -- --run
```

Expected: tests fail because the implementation still uses the previous hero/sample structure.

## Task 2: Implement Debate-First View Markup

**Files:**
- Modify: `client/src/views/AuthView.vue`
- Modify: `client/src/views/HomeView.vue`
- Modify: `client/src/views/DebateRoomView.vue`
- Modify: `client/src/views/PostListView.vue`
- Modify: `client/src/components/common/AppNav.vue`

- [ ] **Step 1: Update `AuthView.vue` copy and preview structure**

Change the main heading to:

```vue
<h1>AI 토론을 바로 시작하세요</h1>
```

Use a shorter paragraph:

```vue
<p>
  주제를 입력하면 냉정파와 열정파가 번갈아 논쟁하고, 결과는 게시판에서 투표로 이어집니다.
</p>
```

Rename the preview label to `실제 토론 흐름` and remove copy that reads like a marketing landing page.

- [ ] **Step 2: Convert `HomeView.vue` to workspace structure**

Use this top-level shape while keeping `topic`, `mode`, `startDebate`, and `ModeCard` behavior:

```vue
<main class="page workspace-page">
  <section class="workspace-header">
    <div>
      <span class="eyebrow">Debate Workspace</span>
      <h1>무엇을 비교할까요?</h1>
      <p class="page-copy">주제와 모드를 정하면 AI 토론방에서 바로 발화를 이어갈 수 있습니다.</p>
    </div>
  </section>

  <section class="workspace-layout">
    <form class="workspace-setup" @submit.prevent="startDebate">
      <!-- ModeCard grid, topic input, submit button -->
    </form>
    <section class="workspace-main">
      <!-- debate preview and community signal cards -->
    </section>
  </section>
</main>
```

- [ ] **Step 3: Update `DebateRoomView.vue` layout**

Wrap the existing chat panel in:

```vue
<section class="debate-room-layout">
  <div class="chat-panel">...</div>
  <aside class="debate-status-rail">...</aside>
</section>
```

The rail must show:

```vue
<h2>토론 컨트롤</h2>
<span>{{ debateStore.messages.length }}개 발화</span>
<span>{{ statusLabel }}</span>
```

Move or duplicate primary actions into the rail without changing method names: `nextTurn`, `finishDebate`, `toggleShareForm`, `shareDebate`.

- [ ] **Step 4: Update `PostListView.vue` copy**

Change page copy to:

```vue
<p class="page-copy">토론 결과가 커뮤니티로 이어집니다. 공유된 판단 기준과 투표 흐름을 확인하세요.</p>
```

Keep `postStore.fetchPosts(filters)` and all routes unchanged.

- [ ] **Step 5: Update `AppNav.vue` primary action label**

Change `새 토론 만들기` to `새 토론` in the nav primary link, leaving `to="/new"` unchanged.

## Task 3: Implement Service-Like CSS

**Files:**
- Modify: `client/src/styles/base.css`

- [ ] **Step 1: Add workspace layout styles**

Add CSS for:

```css
.workspace-page {}
.workspace-header {}
.workspace-layout {}
.workspace-setup {}
.workspace-main {}
.workspace-preview {}
.workspace-signal-grid {}
.workspace-signal-card {}
```

Use a two-column desktop layout: `300px minmax(0, 1fr)`. On mobile, stack columns.

- [ ] **Step 2: Add debate room rail styles**

Add CSS for:

```css
.debate-room-layout {}
.debate-status-rail {}
.status-metric {}
.rail-actions {}
```

Use `grid-template-columns: minmax(0, 1fr) 260px` on desktop and one column on mobile.

- [ ] **Step 3: Reduce landing-page decoration**

Update `.auth-landing`, `.auth-intro`, `.auth-product`, and related classes so the auth screen reads as a compact service entry, not a decorative hero.

- [ ] **Step 4: Ensure responsive behavior**

In the existing media query section, ensure `.workspace-layout`, `.debate-room-layout`, `.workspace-signal-grid`, `.auth-landing`, and `.app-nav` do not overflow at widths below `760px`.

## Task 4: Verify and Commit

**Files:**
- Test all changed frontend files.

- [ ] **Step 1: Run frontend tests**

Run:

```bash
cd client
npm test -- --run
```

Expected: all tests pass.

- [ ] **Step 2: Run frontend production build**

Run:

```bash
cd client
npm run build
```

Expected: Vite build succeeds.

- [ ] **Step 3: Browser-check key routes**

With the existing dev server running on `http://localhost:15173`, check:

```text
http://localhost:15173/auth
http://localhost:15173/new
http://localhost:15173/posts
```

Expected: no blank page, no console errors, key text visible.

- [ ] **Step 4: Check git diff**

Run:

```bash
git diff --check
git status --short
```

Expected: no whitespace errors; only intended frontend and plan files changed.

- [ ] **Step 5: Commit**

Use:

```bash
git add client/src docs/superpowers/plans/2026-06-22-frontend-service-redesign.md
git commit -m "design : 프론트 서비스형 화면 개선"
```

## Self-Review

- Spec coverage: `/auth`, `/new`, `/debates/:debateId`, `/posts`, nav, CSS, tests, and browser verification are covered.
- Placeholder scan: no unfinished placeholder markers are present.
- Type consistency: existing `topic`, `mode`, `startDebate`, `nextTurn`, `finishDebate`, `shareDebate`, `statusLabel`, and Pinia store names are reused.

# Post and Result Flow Improvements Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Improve ARENA debate creation, result, post detail, voting, comment, and deletion UX while persisting per-user post vote state in the backend.

**Architecture:** Keep existing Vue/Pinia/Spring/MyBatis boundaries. Frontend changes stay in the affected views and stores; backend changes extend existing post/comment mappers and DTOs without introducing a new service layer.

**Tech Stack:** Vue 3, Vite, Pinia, Vitest, Spring Boot, MyBatis, MySQL.

---

### Task 1: Frontend Interaction Tests

**Files:**
- Modify: `client/src/views/__tests__/HomeView.test.js`
- Modify: `client/src/views/__tests__/DebateResultView.test.js`
- Modify: `client/src/views/__tests__/PostDetailView.test.js`
- Modify: `client/src/stores/__tests__/postStore.test.js`

- [ ] Add failing tests for Enter-submit topic generation, instant selected-side display, summary loading copy, share-body placement, Enter-submit comments, owner delete actions, back/exit buttons, and vote changes.
- [ ] Run targeted Vitest tests and verify failures are behavior failures, not syntax errors.

### Task 2: Backend Persistence Tests

**Files:**
- Modify: `server/src/test/java/com/ssafy/arena/service/PostServiceTest.java`
- Modify: `server/src/main/java/com/ssafy/arena/dto/post/PostDetailResponse.java`
- Modify: `server/src/main/java/com/ssafy/arena/dto/post/PostListItem.java`
- Modify: `server/src/main/java/com/ssafy/arena/dto/comment/CommentResponse.java`

- [ ] Add failing service tests for detail responses containing current user's vote choice and owner flags.
- [ ] Run `./gradlew test --tests com.ssafy.arena.service.PostServiceTest` and verify RED.

### Task 3: Frontend Implementation

**Files:**
- Modify: `client/src/views/HomeView.vue`
- Modify: `client/src/views/DebateResultView.vue`
- Modify: `client/src/views/PostDetailView.vue`
- Modify: `client/src/stores/postStore.js`
- Modify: `client/src/stores/commentStore.js`
- Modify: `client/src/styles/base.css`

- [ ] Make topic/comment Enter submit and Shift+Enter newline.
- [ ] Prioritize locally stored selected side on result page and show “요약 중...” until summary exists.
- [ ] Move shared post body into a dedicated body box before the transcript.
- [ ] Add post/comment delete controls for owner responses.
- [ ] Add post detail back button and result exit button.
- [ ] Enable vote changes by removing disabled voting after first vote.
- [ ] Add guarded history handling for generated-candidate, debate room, and result flows.

### Task 4: Backend Implementation

**Files:**
- Modify: `server/src/main/java/com/ssafy/arena/controller/PostController.java`
- Modify: `server/src/main/java/com/ssafy/arena/service/PostService.java`
- Modify: `server/src/main/java/com/ssafy/arena/mapper/PostMapper.java`
- Modify: `server/src/main/java/com/ssafy/arena/mapper/CommentMapper.java`
- Modify: `server/src/main/resources/mapper/PostMapper.xml`
- Modify: `server/src/main/resources/mapper/CommentMapper.xml`
- Modify: DTO files from Task 2

- [ ] Allow post detail to receive optional authenticated principal.
- [ ] Query current user's vote choice from `post_votes`.
- [ ] Mark post/comment owner fields in detail response.
- [ ] Keep public anonymous detail behavior unchanged.

### Task 5: Verification

**Files:**
- No new files.

- [ ] Run targeted frontend tests.
- [ ] Run targeted backend tests.
- [ ] Run broader frontend and backend test suites if targeted tests pass.
- [ ] Rebuild/restart Docker app and client if source changes affect running containers.
- [ ] Verify login, topic Enter, result summary loading, share body placement, comment Enter, delete controls, back/exit navigation, and vote editing in browser.

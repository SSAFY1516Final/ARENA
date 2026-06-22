# Spring AI Base Reset Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Keep only the Spring AI foundation in the base PR while preserving the current choice-candidate pipeline on a separate experiment branch.

**Architecture:** The base branch keeps `AiClient`, `AiPipelineService`, `SpringAiClient`, prompt/parser support for the existing debate next-turn and summary flows, plus environment/docs. The topic-candidate pipeline, `/api/ai/topic-candidates`, and frontend candidate-selection UI are removed from the base PR and preserved on `ai-experiment-choice-pipeline`.

**Tech Stack:** Spring Boot, Spring AI ChatClient, Vue 3, Pinia, Vitest, Gradle.

---

### Task 1: Preserve Current Experiment Branch

**Files:**
- No source edits.

- [ ] **Step 1: Push the current GitHub PR head to an experiment branch**

Run from `/private/tmp/arena-github-pr`:

```bash
git branch ai-experiment-choice-pipeline
git push origin ai-experiment-choice-pipeline
```

Expected: GitHub has `ai-experiment-choice-pipeline` pointing at the current choice-candidate implementation.

### Task 2: Remove Topic Candidate Pipeline From Base

**Files:**
- Delete: `server/src/main/java/com/ssafy/arena/controller/AiController.java`
- Delete: `server/src/main/java/com/ssafy/arena/ai/topic/*`
- Delete: `server/src/main/java/com/ssafy/arena/dto/ai/TopicCandidate*.java`
- Delete: `server/src/test/java/com/ssafy/arena/controller/AiControllerTest.java`
- Delete: `server/src/test/java/com/ssafy/arena/ai/topic/TopicSuggestionUseCaseTest.java`
- Modify: `server/src/main/java/com/ssafy/arena/ai/AiPromptFactory.java`
- Modify: `server/src/main/java/com/ssafy/arena/ai/AiResponseParser.java`

- [ ] **Step 1: Delete the topic-candidate endpoint, use case, DTOs, and tests**

Use `rm` only for the exact files listed above.

- [ ] **Step 2: Remove topic-candidate prompt/parser methods**

Remove `topicCandidateSystemPrompt`, `topicCandidateUserPrompt`, and `parseTopicCandidates` because base no longer exposes a candidate pipeline.

### Task 3: Remove Frontend Candidate UI From Base

**Files:**
- Modify: `client/src/api/debateApi.js`
- Modify: `client/src/stores/debateStore.js`
- Modify: `client/src/stores/__tests__/debateStore.test.js`
- Modify: `client/src/views/HomeView.vue`
- Modify: `client/src/views/__tests__/HomeView.test.js`
- Modify: `client/src/styles/base.css`

- [ ] **Step 1: Remove `topicCandidates` API wrapper and store state/action**

Delete the frontend call to `/api/ai/topic-candidates` and related store state.

- [ ] **Step 2: Simplify HomeView to direct debate creation**

Keep topic/mode input and create debate behavior. Remove candidate generation controls/cards.

### Task 4: Update Docs To Say Spring AI Is Base Only

**Files:**
- Modify: `README.md`
- Modify: `docs/ai-design.md`
- Modify: `docs/api.md`

- [ ] **Step 1: Remove topic-candidates API docs from base**

Delete `/api/ai/topic-candidates` from README/API docs.

- [ ] **Step 2: Document experiment branch policy**

Add that the base branch only prepares Spring AI wiring; choice pipeline work lives in `ai-experiment-choice-pipeline` or future experiment branches.

### Task 5: Verify, Commit, Push

**Files:**
- All modified/deleted files above.

- [ ] **Step 1: Run backend tests**

```bash
cd server && ./gradlew test
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 2: Run frontend tests and build**

```bash
cd client && npm test -- --run && npm run build
```

Expected: all tests pass and Vite build succeeds.

- [ ] **Step 3: Commit and push**

```bash
git add -A
git commit -m "refactor : Spring AI 기초 환경만 유지"
git push origin codex/auth-frontend-sync
```

Expected: PR #1 updates with Spring AI base-only structure.

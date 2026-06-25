# Spring AI Round Candidates Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace `/new` mock candidate generation with a Spring AI-backed candidate API that persists prompts and results.

**Architecture:** Add a backend `/api/ai/round-candidates` endpoint backed by `AiRoundCandidateService`, prompt resources, parser support, and MyBatis logging tables. Update the Vue store and `/new` page to call that endpoint, then update existing API/AI handoff docs.

**Tech Stack:** Spring Boot 3.5, Spring AI, MyBatis, MySQL, JUnit 5, Mockito, Vue 3, Pinia, Vitest.

---

### Task 1: Backend Persistence Schema And Mappers

**Files:**
- Modify: `server/src/main/resources/db/schema.sql`
- Create: `server/src/main/java/com/ssafy/arena/domain/AiRoundCandidateRun.java`
- Create: `server/src/main/java/com/ssafy/arena/domain/AiPromptCallLog.java`
- Create: `server/src/main/java/com/ssafy/arena/mapper/AiRoundCandidateRunMapper.java`
- Create: `server/src/main/java/com/ssafy/arena/mapper/AiPromptCallLogMapper.java`
- Create: `server/src/main/resources/mapper/AiRoundCandidateRunMapper.xml`
- Create: `server/src/main/resources/mapper/AiPromptCallLogMapper.xml`

- [ ] **Step 1: Write mapper-facing domain and schema tests**

Add focused tests that instantiate `AiRoundCandidateRun` and `AiPromptCallLog` and verify field names expected by mapper XML. Run:

```powershell
cd server
.\gradlew.bat test --tests "*Ai*"
```

Expected: FAIL because classes/mappers do not exist.

- [ ] **Step 2: Add schema tables and mapper classes**

Add `ai_round_candidate_runs` and `ai_prompt_call_logs` tables exactly as specified in the design docs. Add mapper insert/update methods:

```java
void insert(AiRoundCandidateRun run);
void markSucceeded(Long id, String topicFrameJson, String finalResponseJson);
void markFailed(Long id, String topicFrameJson, String errorMessage);
```

```java
void insert(AiPromptCallLog log);
```

- [ ] **Step 3: Run backend tests**

Run:

```powershell
cd server
.\gradlew.bat test --tests "*Ai*"
```

Expected: PASS for new persistence compile coverage.

### Task 2: Prompt Loader, DTOs, And Parser

**Files:**
- Create: `server/src/main/resources/prompts/topic-frame-generator.txt`
- Create: `server/src/main/resources/prompts/topic-round-candidates-generator.txt`
- Create: `server/src/main/resources/prompts/topic-round-candidates-validator.txt`
- Create: `server/src/main/java/com/ssafy/arena/ai/PromptTemplateLoader.java`
- Modify: `server/src/main/java/com/ssafy/arena/ai/AiResponseParser.java`
- Create: `server/src/main/java/com/ssafy/arena/dto/ai/RoundCandidatesRequest.java`
- Create: `server/src/main/java/com/ssafy/arena/dto/ai/TopicFrameResponse.java`
- Create: `server/src/main/java/com/ssafy/arena/dto/ai/RoundCandidateResponse.java`
- Create: `server/src/main/java/com/ssafy/arena/dto/ai/RoundCandidatesResponse.java`
- Test: `server/src/test/java/com/ssafy/arena/ai/PromptTemplateLoaderTest.java`
- Modify test: `server/src/test/java/com/ssafy/arena/ai/AiResponseParserTest.java`

- [ ] **Step 1: Write failing loader and parser tests**

Test placeholder replacement, missing placeholder failure, fenced JSON extraction, candidate generator parsing, and validator topCandidates parsing.

Run:

```powershell
cd server
.\gradlew.bat test --tests "com.ssafy.arena.ai.PromptTemplateLoaderTest" --tests "com.ssafy.arena.ai.AiResponseParserTest"
```

Expected: FAIL because loader and parser methods do not exist.

- [ ] **Step 2: Add prompts, DTOs, loader, and parser methods**

Copy the three active prompt files from `C:\Users\SSAFY\Downloads\gd.zip` into `server/src/main/resources/prompts`. Implement JSON extraction and parsing methods:

```java
TopicFrameResponse parseTopicFrame(String content);
List<RoundCandidateResponse> parseGeneratedCandidates(String content);
List<RoundCandidateResponse> parseValidatedCandidates(String content);
```

- [ ] **Step 3: Run loader and parser tests**

Run the same Gradle command.

Expected: PASS.

### Task 3: Backend Candidate Service And Controller

**Files:**
- Create: `server/src/main/java/com/ssafy/arena/ai/AiRoundCandidateService.java`
- Create: `server/src/main/java/com/ssafy/arena/controller/AiController.java`
- Test: `server/src/test/java/com/ssafy/arena/ai/AiRoundCandidateServiceTest.java`

- [ ] **Step 1: Write failing service tests**

Cover success, `isDebatable=false` as `422`, provider/parser failure as `502`, and persistence calls for both success and failure.

Run:

```powershell
cd server
.\gradlew.bat test --tests "com.ssafy.arena.ai.AiRoundCandidateServiceTest"
```

Expected: FAIL because service does not exist.

- [ ] **Step 2: Implement service and controller**

Implement authenticated endpoint:

```http
POST /api/ai/round-candidates
```

Use `@AuthenticationPrincipal UserPrincipal user` and pass `user.getId()` into the service. Persist run and prompt logs around all provider calls.

- [ ] **Step 3: Run backend AI tests**

Run:

```powershell
cd server
.\gradlew.bat test --tests "*Ai*"
```

Expected: PASS.

### Task 4: Frontend API, Store, And HomeView

**Files:**
- Modify: `client/src/api/debateApi.js`
- Modify: `client/src/stores/debateStore.js`
- Modify: `client/src/stores/__tests__/debateStore.test.js`
- Modify: `client/src/views/HomeView.vue`
- Modify: `client/src/views/__tests__/HomeView.test.js`

- [ ] **Step 1: Write failing frontend tests**

Update store mock to include `generateRoundCandidates`. Assert it calls `/api/ai/round-candidates`, stores candidates, renders returned titles/core questions, starts debates with selected `coreQuestion`, and clears loading on API failure.

Run:

```powershell
cd client
npm test -- --run client/src/stores/__tests__/debateStore.test.js client/src/views/__tests__/HomeView.test.js
```

Expected: FAIL because frontend API/store action does not exist and HomeView still uses mock candidates.

- [ ] **Step 2: Implement frontend API/store/view**

Add `debateApi.generateRoundCandidates(payload)` and `debateStore.generateRoundCandidates(payload)`. Remove the local 5-second mock delay. Render `candidate.title` and `candidate.coreQuestion`; create debates with selected `candidate.coreQuestion`.

- [ ] **Step 3: Run frontend focused tests**

Run the same npm command.

Expected: PASS.

### Task 5: Existing Docs Update

**Files:**
- Modify: `docs/ai-design.md`
- Modify: `docs/api.md`
- Modify: `docs/codex-handoff.md`
- Modify: `docs/deliverables/api/arena-rest-api.md`
- Modify: `docs/deliverables/requirements/arena-requirements.md`

- [ ] **Step 1: Update docs**

Document `POST /api/ai/round-candidates`, DB prompt/result preservation, and the fact that `/new` now calls Spring AI instead of using a fixed mock delay.

- [ ] **Step 2: Search docs for stale mock wording**

Run:

```powershell
rg -n "목업|5초|fixed|topic-candidates|round-candidates|prompt" docs
```

Expected: Remaining mock references either describe historical plans or are updated to current behavior.

### Task 6: Full Verification

**Files:**
- All modified backend, frontend, and docs files.

- [ ] **Step 1: Run backend tests**

```powershell
cd server
.\gradlew.bat test
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 2: Run frontend tests and build**

```powershell
cd client
npm test -- --run
npm run build
```

Expected: all tests pass and Vite build succeeds.

- [ ] **Step 3: Review git diff**

```powershell
git diff --stat
git diff -- server client docs
```

Expected: changes are limited to Spring AI candidate generation, persistence, frontend wiring, and docs.


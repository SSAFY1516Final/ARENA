# Topic Candidate API Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a backend-only API that generates, validates, and ranks AI topic candidates before debate creation.

**Architecture:** Keep the external change small: one authenticated controller endpoint delegates to `TopicSuggestionUseCase`. The use case coordinates generator, validator, and ranker components; generation uses the existing Spring AI provider wrapper and parser patterns.

**Tech Stack:** Java 17, Spring Boot 3, Spring Security, Spring AI, Jackson, JUnit 5, Mockito.

---

### Task 1: DTO and Parser Tests

**Files:**
- Create: `server/src/main/java/com/ssafy/arena/dto/ai/TopicCandidateRequest.java`
- Create: `server/src/main/java/com/ssafy/arena/dto/ai/TopicCandidateItem.java`
- Create: `server/src/main/java/com/ssafy/arena/dto/ai/TopicCandidateResponse.java`
- Modify: `server/src/main/java/com/ssafy/arena/ai/AiResponseParser.java`
- Test: `server/src/test/java/com/ssafy/arena/ai/AiResponseParserTest.java`

- [ ] Add a failing parser test for candidate JSON.
- [ ] Implement DTO records and parser method.
- [ ] Verify AI parser tests pass.

### Task 2: Topic Suggestion Use Case

**Files:**
- Create: `server/src/main/java/com/ssafy/arena/ai/topic/TopicCandidateGenerator.java`
- Create: `server/src/main/java/com/ssafy/arena/ai/topic/TopicCandidateValidator.java`
- Create: `server/src/main/java/com/ssafy/arena/ai/topic/TopicCandidateRanker.java`
- Create: `server/src/main/java/com/ssafy/arena/ai/topic/TopicSuggestionUseCase.java`
- Test: `server/src/test/java/com/ssafy/arena/ai/topic/TopicSuggestionUseCaseTest.java`

- [ ] Add a failing use-case test that verifies invalid candidates are removed and remaining candidates are ranked.
- [ ] Implement generator, validator, ranker, and use case.
- [ ] Verify topic use-case tests pass.

### Task 3: Controller API

**Files:**
- Create: `server/src/main/java/com/ssafy/arena/controller/AiController.java`
- Test: `server/src/test/java/com/ssafy/arena/controller/AiControllerTest.java`

- [ ] Add a failing controller test for `POST /api/ai/topic-candidates`.
- [ ] Implement controller endpoint.
- [ ] Keep endpoint authenticated by existing default security rule.

### Task 4: Documentation and Verification

**Files:**
- Modify: `docs/ai-design.md`
- Modify: `docs/api.md`

- [ ] Document topic candidate pipeline and API.
- [ ] Run `cd server && ./gradlew test`.
- [ ] Run `git diff --check`.

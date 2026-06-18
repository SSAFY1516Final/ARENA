# AI Pipeline Separation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Split the current AI client into focused prompt, provider, parser, and pipeline units while preserving existing debate APIs.

**Architecture:** Keep `AiClient` as the interface used by `DebateService`. Move orchestration to `AiPipelineService`, Spring AI calls to `SpringAiClient`, prompt construction to `AiPromptFactory`, and JSON parsing to `AiResponseParser`.

**Tech Stack:** Java 17, Spring Boot 3, Spring AI ChatClient, Jackson, JUnit 5, Mockito.

---

### Task 1: Parser Tests

**Files:**
- Create: `server/src/test/java/com/ssafy/arena/ai/AiResponseParserTest.java`

- [ ] Verify next-turn JSON is parsed into `AiNextTurnResponse`.
- [ ] Verify summary JSON is parsed into `AiSummaryResponse`.
- [ ] Verify malformed JSON becomes `ApiException` with `BAD_GATEWAY`.

### Task 2: Pipeline Tests

**Files:**
- Create: `server/src/test/java/com/ssafy/arena/ai/AiPipelineServiceTest.java`

- [ ] Verify next-turn pipeline chooses speaker from round number.
- [ ] Verify next-turn pipeline delegates prompt, provider call, and parser.
- [ ] Verify summary pipeline delegates prompt, provider call, and parser.

### Task 3: Production Split

**Files:**
- Modify: `server/src/main/java/com/ssafy/arena/ai/AiClient.java`
- Create: `server/src/main/java/com/ssafy/arena/ai/AiPipelineService.java`
- Create: `server/src/main/java/com/ssafy/arena/ai/SpringAiClient.java`
- Create: `server/src/main/java/com/ssafy/arena/ai/AiPromptFactory.java`
- Create: `server/src/main/java/com/ssafy/arena/ai/AiResponseParser.java`

- [ ] Convert `AiClient` to an interface with `nextTurn` and `summarize`.
- [ ] Implement `AiPipelineService` as the primary `AiClient`.
- [ ] Move raw `ChatClient` calls to `SpringAiClient`.
- [ ] Move prompt text construction to `AiPromptFactory`.
- [ ] Move Jackson JSON parsing to `AiResponseParser`.

### Task 4: Verification

**Files:**
- Existing backend tests

- [ ] Run `cd server && ./gradlew test`.
- [ ] Run `git diff --check`.

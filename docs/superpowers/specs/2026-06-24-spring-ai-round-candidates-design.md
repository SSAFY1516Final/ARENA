# Spring AI Round Candidates Design

## Goal

Replace the `/new` page's local five-second mock candidate generation with a stable Spring AI-backed round candidate API. Keep the existing debate creation, live turn generation, summary, and sharing flows unchanged.

## Scope

This project implements only round candidate generation.

In scope:

- Generate five detailed debate round candidates from a user's original topic.
- Use the downloaded prompt package's active topic prompts:
  - `topic-frame-generator.txt`
  - `topic-round-candidates-generator.txt`
  - `topic-round-candidates-validator.txt`
- Add a backend API consumed by the existing `/new` page.
- Keep the selected candidate flowing into the existing `POST /api/debates` request.
- Preserve `originalTopic` as the user's input and use the selected candidate's `coreQuestion` as the debate `topic`.

Out of scope:

- The `debate-single-round-fast-generator.txt` 10-turn batch generation flow.
- New persistence for topic frames or round candidate metadata.
- Replacing the existing `/api/debates/{debateId}/turns` one-turn-at-a-time flow.
- Importing the prompt-lab experiment controllers, log UI, prompt editing API, or legacy generation modes.

## Architecture

The backend adds a focused candidate generation path beside the existing `AiClient` debate turn path.

```text
HomeView
  -> debateStore.generateRoundCandidates()
  -> debateApi.generateRoundCandidates()
  -> POST /api/ai/round-candidates
  -> AiRoundCandidateService
  -> SpringAiClient
  -> PromptTemplateLoader + AiResponseParser
```

The existing `DebateService.create()` contract remains unchanged:

```json
{
  "originalTopic": "오늘 점심 제육 vs 돈까스",
  "topic": "점심 메뉴를 고를 때 안정적인 만족을 택하는 편이 나은가, 즉각적인 끌림을 택하는 편이 나은가?",
  "mode": "PRACTICAL"
}
```

## Backend API

Add:

```http
POST /api/ai/round-candidates
Authorization: Bearer <JWT>
Content-Type: application/json
```

Request:

```json
{
  "topic": "오늘 점심 제육 vs 돈까스",
  "mode": "PRACTICAL",
  "candidateCount": 5
}
```

Response:

```json
{
  "topicFrame": {
    "normalizedBigTopic": "오늘 점심으로 제육과 돈까스 중 어느 쪽이 더 나은가?",
    "sideA": "제육이 더 낫다",
    "sideB": "돈까스가 더 낫다",
    "basicConditions": "평일 점심시간에 일반 직장인 또는 학생이 빠르게 메뉴를 고르는 상황을 기준으로 한다.",
    "isDebatable": true,
    "assumptions": [],
    "missingFields": [],
    "problemReason": null
  },
  "roundCandidates": [
    {
      "roundId": "R1",
      "title": "안정감 vs 즉각 만족",
      "coreQuestion": "점심 메뉴를 고를 때 안정적인 만족을 택하는 편이 나은가, 즉각적인 끌림을 택하는 편이 나은가?",
      "debateAxis": "실패 확률과 당장의 만족감 사이의 선택",
      "sideAFrame": "제육은 익숙한 맛과 든든함으로 실패 확률을 줄이는 선택이다.",
      "sideBFrame": "돈까스는 바삭한 식감과 즉각적인 만족감이 더 강한 선택이다."
    }
  ],
  "warnings": []
}
```

The endpoint remains authenticated because it spends provider quota.

## Backend Components

Create `server/src/main/resources/prompts/` and store the three active prompt files as UTF-8 resources. The prompt files should be copied from the downloaded package without semantic edits, except placeholder usage must match the renderer.

Add `PromptTemplateLoader`:

- Reads prompt resources from `classpath:/prompts`.
- Replaces `{{placeholder}}` values.
- Fails fast with `500` if a required prompt file or placeholder value is missing.

Add candidate DTOs under `server/src/main/java/com/ssafy/arena/dto/ai`:

- `RoundCandidatesRequest`
- `TopicFrameResponse`
- `RoundCandidateResponse`
- `RoundCandidatesResponse`

Extend `SpringAiClient` only if needed to support the current string prompt call. It already accepts a system and user prompt; candidate generation can pass a small stable system instruction and the rendered package prompt as the user prompt.

Extend `AiResponseParser`:

- Extract JSON from raw content before parsing, including fenced JSON if the model returns one.
- Parse topic frame, generated candidates, and validator output.
- Validate required fields before returning a response.

Add `AiRoundCandidateService`:

1. Render and call `topic-frame-generator.txt`.
2. Parse `TopicFrameResponse`.
3. If `isDebatable=false`, throw `422 Unprocessable Entity`.
4. Render and call `topic-round-candidates-generator.txt`.
5. Render and call `topic-round-candidates-validator.txt`.
6. Return exactly five candidates, or fail with `502 Bad Gateway` when the AI response is malformed.

The service should not persist prompt outputs in this first version.

## Frontend Flow

Update `/new` so it no longer creates local mock candidates or waits for a fixed timer.

Flow:

1. User enters a topic.
2. User clicks generate.
3. `HomeView` calls `debateStore.generateRoundCandidates({ topic, mode: 'PRACTICAL', candidateCount: 5 })`.
4. The page shows a loading state until the API returns.
5. Candidate cards render `title` and `coreQuestion`.
6. User selects a card.
7. Start debate calls the existing `createDebate` action with:
   - `originalTopic`: original user input
   - `topic`: selected candidate `coreQuestion`
   - `mode`: `PRACTICAL`

The page should unlock the topic input if generation fails so the user can revise and retry.

## Error Handling

Backend:

- `400 Bad Request`: blank topic, invalid mode, invalid candidate count.
- `422 Unprocessable Entity`: topic frame says the input is not debatable.
- `502 Bad Gateway`: provider failure, malformed JSON, missing required candidate fields, or fewer than five valid candidates.

Frontend:

- Show the existing Axios `userMessage` when present.
- Keep the user on `/new`.
- Reset candidate loading state.
- Preserve the original input for retry.

## Testing

Backend tests:

- `PromptTemplateLoaderTest` verifies placeholder replacement and missing placeholder failure.
- `AiResponseParserTest` parses fenced and unfenced candidate JSON.
- `AiRoundCandidateServiceTest` verifies the three-prompt flow, non-debatable `422`, and malformed JSON `502`.
- `AiControllerTest` verifies authenticated request mapping and response shape.

Frontend tests:

- `debateStore.test.js` verifies `generateRoundCandidates` calls the API and stores returned candidates.
- `HomeView.test.js` verifies candidate generation uses the API, renders five cards, starts a debate with selected `coreQuestion`, and clears loading on failure.

## Migration Notes

No database migration is required. Existing rows keep using `original_topic` and `topic`.

The downloaded package's `POST /api/debate/round-turns` and 10-turn batch generation are intentionally outside this spec. They require a separate follow-up spec after candidate generation is stable in the product.

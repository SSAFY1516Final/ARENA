# Spring AI 세부 라운드 후보 생성 설계

## 목표

`/new` 화면의 로컬 5초 목업 후보 생성을 안정적인 Spring AI 기반 세부 라운드 후보 생성 API로 교체한다. 기존 토론 생성, 실시간 발화 생성, 요약, 게시글 공유 흐름은 그대로 유지한다.

## 범위

이번 작업은 세부 라운드 후보 생성만 구현한다.

포함 범위:

- 사용자가 입력한 원본 주제에서 세부 토론 라운드 후보 5개를 생성한다.
- 다시 받은 프롬프트 패키지의 active topic prompt 3개를 사용한다.
  - `topic-frame-generator.txt`
  - `topic-round-candidates-generator.txt`
  - `topic-round-candidates-validator.txt`
- 기존 `/new` 화면이 호출할 백엔드 API를 추가한다.
- 사용자가 선택한 후보는 기존 `POST /api/debates` 요청으로 이어지게 한다.
- `originalTopic`에는 사용자 원본 입력을 보존하고, 선택 후보의 `coreQuestion`을 토론 세션의 `topic`으로 사용한다.

제외 범위:

- `debate-single-round-fast-generator.txt`를 이용한 10턴 일괄 생성 흐름.
- topic frame 또는 round candidate metadata의 DB 저장.
- 기존 `/api/debates/{debateId}/turns` 단일 발화 생성 흐름 교체.
- 프롬프트 랩 프로젝트의 실험용 컨트롤러, 로그 UI, 프롬프트 편집 API, legacy generation mode 이식.

## 아키텍처

백엔드는 기존 `AiClient` 토론 발화 경로 옆에 후보 생성 전용 경로를 추가한다.

```text
HomeView
  -> debateStore.generateRoundCandidates()
  -> debateApi.generateRoundCandidates()
  -> POST /api/ai/round-candidates
  -> AiRoundCandidateService
  -> SpringAiClient
  -> PromptTemplateLoader + AiResponseParser
```

기존 `DebateService.create()` 계약은 변경하지 않는다.

```json
{
  "originalTopic": "오늘 점심 제육 vs 돈까스",
  "topic": "점심 메뉴를 고를 때 안정적인 만족을 택하는 편이 나은가, 즉각적인 끌림을 택하는 편이 나은가?",
  "mode": "PRACTICAL"
}
```

## 백엔드 API

추가 API:

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

이 API는 provider quota를 사용하므로 인증이 필요하다.

## 백엔드 구성 요소

`server/src/main/resources/prompts/`를 만들고 active prompt 3개를 UTF-8 리소스로 저장한다. 프롬프트 파일은 내려받은 패키지에서 의미 변경 없이 복사한다. 단, placeholder 표기는 renderer가 처리할 수 있게 맞춘다.

`PromptTemplateLoader`를 추가한다.

- `classpath:/prompts`에서 prompt resource를 읽는다.
- `{{placeholder}}` 값을 치환한다.
- 필요한 prompt 파일 또는 placeholder 값이 없으면 빠르게 실패한다.

`server/src/main/java/com/ssafy/arena/dto/ai` 아래에 후보 생성 DTO를 추가한다.

- `RoundCandidatesRequest`
- `TopicFrameResponse`
- `RoundCandidateResponse`
- `RoundCandidatesResponse`

`SpringAiClient`는 현재 문자열 prompt 호출 방식을 최대한 유지한다. 후보 생성은 짧고 안정적인 system instruction과 렌더링된 패키지 prompt를 user prompt로 전달한다.

`AiResponseParser`를 확장한다.

- 모델이 fenced JSON을 반환해도 파싱할 수 있도록 raw content에서 JSON만 추출한다.
- topic frame, generated candidates, validator output을 파싱한다.
- 응답을 반환하기 전에 필수 필드와 후보 개수를 검증한다.

`AiRoundCandidateService`를 추가한다.

1. `topic-frame-generator.txt`를 렌더링하고 호출한다.
2. `TopicFrameResponse`로 파싱한다.
3. `isDebatable=false`이면 `422 Unprocessable Entity`를 반환한다.
4. `topic-round-candidates-generator.txt`를 렌더링하고 호출한다.
5. `topic-round-candidates-validator.txt`를 렌더링하고 호출한다.
6. 정확히 5개의 후보를 반환한다. AI 응답이 잘못됐거나 필수 필드가 부족하면 `502 Bad Gateway`로 실패한다.

첫 버전에서는 prompt 출력이나 후보 metadata를 DB에 저장하지 않는다.

## 프론트엔드 흐름

`/new`는 더 이상 로컬 목업 후보를 만들거나 고정 타이머를 기다리지 않는다.

흐름:

1. 사용자가 주제를 입력한다.
2. 사용자가 생성 버튼을 누른다.
3. `HomeView`가 `debateStore.generateRoundCandidates({ topic, mode: 'PRACTICAL', candidateCount: 5 })`를 호출한다.
4. API 응답이 올 때까지 loading state를 보여준다.
5. 후보 카드는 `title`과 `coreQuestion`을 렌더링한다.
6. 사용자가 후보 하나를 선택한다.
7. 토론 시작 버튼은 기존 `createDebate` action을 호출한다.
   - `originalTopic`: 사용자 원본 입력
   - `topic`: 선택 후보의 `coreQuestion`
   - `mode`: `PRACTICAL`

후보 생성이 실패하면 주제 입력 잠금을 해제하고, 사용자가 내용을 고쳐 다시 시도할 수 있게 한다.

## 에러 처리

백엔드:

- `400 Bad Request`: 빈 주제, 잘못된 mode, 잘못된 candidate count.
- `422 Unprocessable Entity`: topic frame이 토론 불가능 주제라고 판단한 경우.
- `502 Bad Gateway`: provider 실패, JSON 파싱 실패, 필수 후보 필드 누락, 유효 후보 5개 미만.

프론트엔드:

- Axios의 기존 `userMessage`가 있으면 그대로 보여준다.
- 사용자는 `/new`에 머무른다.
- 후보 생성 loading state를 초기화한다.
- 원본 입력은 유지해서 바로 재시도할 수 있게 한다.

## 테스트

백엔드 테스트:

- `PromptTemplateLoaderTest`: placeholder 치환과 누락 placeholder 실패를 검증한다.
- `AiResponseParserTest`: fenced JSON과 unfenced JSON 후보 응답을 파싱하는지 검증한다.
- `AiRoundCandidateServiceTest`: 3단계 prompt 흐름, 토론 불가능 주제의 `422`, 잘못된 JSON의 `502`를 검증한다.
- `AiControllerTest`: 인증된 요청 mapping과 응답 shape을 검증한다.

프론트엔드 테스트:

- `debateStore.test.js`: `generateRoundCandidates`가 API를 호출하고 반환 후보를 저장하는지 검증한다.
- `HomeView.test.js`: 후보 생성이 API를 사용하고, 후보 5개를 렌더링하고, 선택한 `coreQuestion`으로 토론을 시작하고, 실패 시 loading을 해제하는지 검증한다.

## 마이그레이션 메모

DB migration은 필요 없다. 기존 row는 계속 `original_topic`과 `topic`을 사용한다.

프롬프트 패키지의 `POST /api/debate/round-turns`와 10턴 일괄 생성은 이 설계의 범위 밖이다. 후보 생성이 서비스에서 안정화된 뒤 별도 후속 spec으로 다룬다.

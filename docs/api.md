# API 명세

## 공통

- Base URL: `http://localhost:8080`
- 인증 헤더: `Authorization: Bearer <JWT>`
- 응답 형식: JSON

## Auth

### POST `/api/auth/kakao`

카카오 authorization code를 서비스 JWT로 교환한다.

Request:

```json
{
  "code": "kakao-authorization-code",
  "redirectUri": "http://localhost:5173/auth/kakao/callback"
}
```

Response:

```json
{
  "tokenType": "Bearer",
  "accessToken": "jwt..."
}
```

## Debate

### GET `/api/debates`

내 토론 목록을 조회한다.

### POST `/api/debates`

토론을 생성한다.

Request:

```json
{
  "originalTopic": "오늘 점심 제육 vs 돈까스",
  "topic": "오늘 점심은 김밥과 라면 중 무엇이 나은가?",
  "mode": "PRACTICAL",
  "sideALabel": "제육을 선택해야 한다",
  "sideBLabel": "돈까스를 선택해야 한다"
}
```

`sideALabel`과 `sideBLabel`은 선택형 후보 생성의 `topicFrame.sideA/sideB`에서 전달한다. 값이 있으면 토론방의 발화자 표시와 대화 생성 프롬프트의 A/B 진영 이름으로 사용한다.

#### 2026-06-24 추가 생성 필드

선택형 세부 주제 생성에서 후보를 고른 뒤 토론을 만들 때는 후보의 프레임 정보를 함께 전달한다.

```json
{
  "originalTopic": "오늘 점심 제육 vs 돈까스",
  "topic": "지금 바로 선택해야 한다면 어떤 기준이 더 강한가?",
  "mode": "PRACTICAL",
  "sideALabel": "제육을 선택해야 한다",
  "sideBLabel": "돈까스를 선택해야 한다",
  "debateAxis": "실패 위험과 만족감 중 무엇을 우선할지",
  "sideAFrame": "제육은 익숙하고 든든해 실패 위험을 낮춘다.",
  "sideBFrame": "돈까스는 바삭한 만족감으로 기분 전환 가치가 크다."
}
```

서버는 `sideALabel`, `sideBLabel`, `debateAxis`, `sideAFrame`, `sideBFrame`을 `debate_sessions`에 저장한다. 이후 토론방 표시 이름과 Spring AI 대화 생성 프롬프트 입력으로 재사용한다.

### POST `/api/debates/{debateId}/turns`

AI에게 다음 발화를 요청한다. 서버는 `debate-single-round-fast-generator.txt` 프롬프트를 렌더링해 Spring AI provider를 호출하고, 생성된 메시지는 `debate_messages`에 저장한다. 렌더링된 프롬프트, raw 응답, 파싱 결과, latency는 `ai_debate_turn_logs`에 저장한다.

Response:

```json
{
  "speaker": "COOL_HEADED",
  "content": "AI 발화 내용",
  "peakReached": false
}
```

### POST `/api/debates/{debateId}/turns/batch`

새 토론 시작 시 사용하는 10턴 배치 생성 작업 시작 API다. 서버는 요청을 받으면 debateId 기준 중복 방지 작업 레지스트리를 확인하고, 아직 10개 발화가 저장되지 않았으면 백그라운드 executor에서 `debate-single-round-fast-generator.txt` 기반 생성 작업을 계속 진행한다. 따라서 사용자가 브라우저를 닫거나 다른 화면으로 이동해도 서버 작업은 계속 실행되고 결과는 `debate_messages`에 저장된다.

이미 10개 메시지가 저장된 토론이면 AI를 다시 호출하지 않고 `COMPLETE`와 DB 메시지 목록을 반환한다. 10개 미만이면 `GENERATING`과 현재 저장된 메시지 목록을 즉시 반환하며, 프론트는 준비중 화면을 유지하고 `GET /api/debates/{debateId}`를 polling해 저장 완료 여부를 확인한다.

라운드의 기준은 사용자가 선택한 세부주제다. 따라서 처음 선택한 세부주제에서 생성된 10개 발화는 모두 `roundNo: 1`인 1라운드에 속하고, 발화 순서는 `messageId` 오름차순으로 보존한다.

Response:

```json
{
  "status": "GENERATING",
  "messages": []
}
```

생성이 완료된 뒤 다시 호출하거나 detail polling으로 확인하면 다음처럼 10개 메시지가 저장되어 있다.

```json
{
  "status": "COMPLETE",
  "messages": [
    {
      "messageId": 101,
      "speaker": "COOL_HEADED",
      "roundNo": 1,
      "content": "AI 발화 내용",
      "peakReached": true
    }
  ]
}
```

### POST `/api/debates/{debateId}/stop`

토론을 종료하고 AI 요약을 생성한다.

#### 2026-06-24 추가 종료 필드

Request body는 선택 사항이다. 결과 페이지가 URL query나 localStorage 없이도 선택값을 복원하려면 사용자가 고른 진영과 라운드를 함께 보낸다.

```json
{
  "selectedSide": "COOL_HEADED",
  "selectedRoundNo": 1
}
```

Response:

```json
{
  "debateId": 10,
  "status": "STOPPED",
  "selectedSide": "COOL_HEADED",
  "selectedRoundNo": 1,
  "summary": {
    "summaryText": "요약 내용"
  }
}
```

### POST `/api/debates/{debateId}/share`

선택한 라운드의 토론 요약을 게시글로 공유한다. `STOPPED` 또는 이미 `SHARED` 상태인 토론에서 호출할 수 있으며, 같은 토론/라운드도 여러 번 공유할 수 있다. 게시글 제목은 서버에서 질문 원본으로 저장하고, 게시글 본문은 세부주제 제목, 세부주제 상세설명, 사용자가 입력한 본문으로 구성된다.

Request:

```json
{
  "title": "오늘 점심 선택 토론",
  "voteOptionA": "김밥",
  "voteOptionB": "라면",
  "isPublic": true,
  "roundNo": 1,
  "body": "내가 이 토론을 공유하며 덧붙이는 본문입니다."
}
```

### DELETE `/api/debates/{debateId}`

내 토론 목록에서 토론을 삭제한다. 토론 작성자만 삭제할 수 있다. 공유된 토론을 삭제하면 연결된 게시글, 댓글, 투표, 요약, 메시지도 함께 삭제된다.

Response: `204 No Content`

## Post

### GET `/api/posts`

공개 게시글 목록을 조회한다.

### GET `/api/posts/{postId}`

게시글 상세, 공유된 라운드의 토론 요약과 토론 로그, 댓글을 조회한다.

### POST `/api/posts/{postId}/votes`

게시글에 투표한다.

Request:

```json
{
  "choice": "A"
}
```

### POST `/api/posts/{postId}/comments`

댓글을 작성한다.

Request:

```json
{
  "content": "저는 라면이 더 낫다고 봅니다."
}
```

## Admin

### GET `/api/admin/users`

전체 사용자 목록을 조회한다.

### PATCH `/api/admin/users/{userId}/role`

사용자 권한을 변경한다.

Request:

```json
{
  "role": "ADMIN"
}
```

### PATCH `/api/admin/posts/{postId}/visibility`

게시글 공개 여부를 변경한다.

## AI

### POST `/api/ai/round-candidates`

사용자가 입력한 원본 주제를 Spring AI로 분석해 세부 라운드 후보 5개를 생성한다. 이 API는 GMS OpenAI-compatible provider quota를 사용하므로 JWT 인증이 필요하다.

Request:

```json
{
  "topic": "오늘 점심 제육 vs 돈까스",
  "mode": "PRACTICAL",
  "candidateCount": 5
}
```

서버는 후보 생성 run과 각 LLM 호출 단계의 렌더링된 프롬프트, raw 응답, 파싱 결과, 성공/실패 상태를 DB에 저장한다. API key, Authorization header, 환경 변수 값은 저장하지 않는다.

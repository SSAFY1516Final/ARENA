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

## AI

### POST `/api/ai/topic-candidates`

큰 주제와 조건을 바탕으로 토론 후보 주제 목록을 생성한다. 인증이 필요하다.

Request:

```json
{
  "topic": "점심 메뉴",
  "mode": "PRACTICAL",
  "conditions": ["1만원 이하", "오후 집중력"],
  "detailConditions": "회사 근처에서 빠르게 먹어야 함"
}
```

Response:

```json
{
  "items": [
    {
      "title": "오후 집중력을 기준으로 제육 vs 돈까스",
      "reason": "조건과 선택 갈등이 명확합니다.",
      "noveltyScore": 88,
      "fitScore": 94,
      "funScore": 72
    }
  ]
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
  "topic": "오늘 점심은 김밥과 라면 중 무엇이 나은가?",
  "mode": "PRACTICAL"
}
```

### POST `/api/debates/{debateId}/turns`

AI에게 다음 발화를 요청한다.

Response:

```json
{
  "speaker": "COOL_HEADED",
  "content": "AI 발화 내용",
  "peakReached": false
}
```

### POST `/api/debates/{debateId}/stop`

토론을 종료하고 AI 요약을 생성한다.

### POST `/api/debates/{debateId}/share`

토론 요약을 게시글로 공유한다.

Request:

```json
{
  "title": "오늘 점심 선택 토론",
  "voteOptionA": "김밥",
  "voteOptionB": "라면",
  "isPublic": true
}
```

## Post

### GET `/api/posts`

공개 게시글 목록을 조회한다.

### GET `/api/posts/{postId}`

게시글 상세, 토론 요약, 댓글을 조회한다.

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

# ARENA(아레나) REST API 설계서

## 1. 공통 정책

| 항목 | 정책 |
| --- | --- |
| 인증 방식 | Kakao OAuth 로그인 후 서비스 JWT Bearer Token 사용 |
| 요청 형식 | JSON |
| 응답 형식 | JSON |
| 시간 형식 | ISO-8601 |
| 권한 오류 | 401 Unauthorized, 403 Forbidden |
| 검증 오류 | 400 Bad Request |
| AI 서버 오류 | 502 Bad Gateway |

### 공통 에러 응답

```json
{
  "status": 400,
  "message": "Invalid request payload",
  "timestamp": "2026-06-22T10:30:00"
}
```

### 인증 정책

| API | 인증 |
| --- | --- |
| POST /api/auth/kakao | 불필요 |
| POST /api/auth/logout | 필요 |
| GET /api/posts | 불필요 |
| GET /api/posts/{postId} | 불필요 |
| /api/admin/** | ADMIN 필요 |
| 그 외 /api/** | USER 이상 필요 |

인증이 필요한 API는 다음 헤더를 전달한다.

```http
Authorization: Bearer {accessToken}
```

## 2. 인증 API

### POST /api/auth/kakao

Kakao authorization code를 서비스 JWT로 교환한다.

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
  "accessToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

Status:

| 상황 | 코드 |
| --- | --- |
| 성공 | 200 OK |
| Kakao 인증 실패 | 401 Unauthorized |
| 검증 실패 | 400 Bad Request |

### POST /api/auth/logout

JWT는 서버 세션을 보관하지 않으므로 서버 측 상태를 무효화하지 않는다. 클라이언트는 저장된 access token을 삭제해 로그아웃을 완료한다.

Status:

| 상황 | 코드 |
| --- | --- |
| 성공 | 204 No Content |
| 미인증 | 401 Unauthorized |

## 3. 사용자 API

### GET /api/users/me

현재 로그인한 사용자의 프로필을 조회한다.

Response:

```json
{
  "userId": 7,
  "loginId": "kakao_123456789",
  "nickname": "아레나유저"
}
```

Status:

| 상황 | 코드 |
| --- | --- |
| 성공 | 200 OK |
| 미인증 | 401 Unauthorized |

### PATCH /api/users/me/nickname

현재 로그인한 사용자의 닉네임을 수정한다.

Request:

```json
{
  "nickname": "아레나유저"
}
```

Validation:

| 항목 | 조건 |
| --- | --- |
| 길이 | 2자 이상 20자 이하 |
| 허용 문자 | 한글, 영문, 숫자, 밑줄 |
| 중복 | 다른 사용자가 사용 중인 닉네임 불가 |

Response:

```json
{
  "userId": 7,
  "loginId": "kakao_123456789",
  "nickname": "아레나유저"
}
```

Status:

| 상황 | 코드 |
| --- | --- |
| 성공 | 200 OK |
| 미인증 | 401 Unauthorized |
| 형식 오류 | 400 Bad Request |
| 닉네임 중복 | 409 Conflict |

## 4. 토론 API

### GET /api/debates

로그인 사용자의 토론 목록을 조회한다.

Response:

```json
[
  {
    "debateId": 10,
    "topic": "오늘 점심 제육 vs 돈까스",
    "mode": "PRACTICAL",
    "status": "ACTIVE",
    "summaryCard": "ACTIVE",
    "shareBody": "",
    "updatedAt": "2026-06-22T10:30:00"
  }
]
```

### GET /api/debates/{debateId}

로그인 사용자의 토론 상세, 발화 목록, 요약을 조회한다.

### POST /api/debates

로그인 사용자의 토론 세션을 생성한다.

현재 프론트 `/new` 화면은 주제, 상황/조건, 세부 조건을 입력받고 검증 통과 후보를 선택하는 목업 흐름을 제공한다. 실제 서버 요청에는 선택된 후보 제목을 `topic`으로 전달한다. `mode`는 기존 DB/API 호환을 위해 유지한다.

Request:

```json
{
  "topic": "점심시간 15분 남았을 때, 오늘 점심 제육 vs 돈까스 주문을 누가 양보할지 갈린다",
  "mode": "PRACTICAL"
}
```

Response:

```json
{
  "debateId": 10,
  "topic": "점심시간 15분 남았을 때, 오늘 점심 제육 vs 돈까스 주문을 누가 양보할지 갈린다",
  "mode": "PRACTICAL",
  "status": "ACTIVE"
}
```

Status:

| 상황 | 코드 |
| --- | --- |
| 성공 | 201 Created |
| 미인증 | 401 Unauthorized |
| 검증 실패 | 400 Bad Request |

### POST /api/debates/{debateId}/turns

다음 AI 토론 발화를 생성하고 저장한다.

Response:

```json
{
  "messageId": 31,
  "speaker": "COOL_HEADED",
  "roundNo": 3,
  "content": "오후 일정까지 고려하면 돈까스가 안정적입니다.",
  "peakReached": true
}
```

Status:

| 상황 | 코드 |
| --- | --- |
| 성공 | 200 OK |
| 미인증 | 401 Unauthorized |
| 작성자 아님 | 403 Forbidden |
| 토론 없음 | 404 Not Found |
| ACTIVE 상태 아님 | 409 Conflict |
| AI 서버 실패 | 502 Bad Gateway |

### POST /api/debates/{debateId}/stop

토론을 중단하고 요약을 생성한다.

Response:

```json
{
  "debateId": 10,
  "status": "STOPPED",
  "summary": {
    "coreArguments": "돈까스는 안정성, 제육은 만족감을 주장했습니다.",
    "highlight": "제육 쪽이 즉시 만족감을 강하게 주장한 장면",
    "decisionCriteria": "오후 집중력이 중요하면 돈까스, 현재 만족이 중요하면 제육",
    "remainingIssue": "매운맛과 식후 집중력",
    "summaryText": "두 선택지는 안정성과 만족감의 차이로 정리됩니다."
  }
}
```

### POST /api/debates/{debateId}/share

STOPPED 상태의 토론을 게시글로 공유한다.

Request:

```json
{
  "title": "오늘 점심 제육 vs 돈까스",
  "voteOptionA": "제육",
  "voteOptionB": "돈까스",
  "isPublic": true
}
```

Response:

```json
{
  "postId": 5,
  "debateId": 10,
  "title": "오늘 점심 제육 vs 돈까스",
  "voteOptionA": "제육",
  "voteOptionB": "돈까스"
}
```

## 5. 게시글 API

### GET /api/posts?page=1&size=10&keyword=제육&mode=PRACTICAL&sort=latest

공개 게시글 목록을 조회한다. 검색과 필터 파라미터는 모두 선택값이다.

Query:

| 이름 | 설명 |
| --- | --- |
| page | 1부터 시작하는 페이지 번호 |
| size | 페이지 크기 |
| keyword | 제목, 요약 카드, 토론 주제 검색어 |
| mode | PRACTICAL 또는 ENTERTAINMENT |
| sort | latest, comments, votes |

Response:

```json
{
  "items": [
    {
      "postId": 5,
      "title": "오늘 점심 제육 vs 돈까스",
      "mode": "PRACTICAL",
      "summaryCard": "안정성 vs 만족감으로 갈린 토론",
      "authorNickname": "토론러",
      "commentCount": 3,
      "voteOptionA": "제육",
      "voteOptionB": "돈까스",
      "voteCountA": 12,
      "voteCountB": 8,
      "voteRatioA": 60.0,
      "voteRatioB": 40.0,
      "createdAt": "2026-06-22T10:30:00"
    }
  ],
  "page": 1,
  "size": 10,
  "totalCount": 1
}
```

### GET /api/posts/{postId}

게시글, 요약, 전체 토론 로그, 댓글을 조회한다.

### DELETE /api/posts/{postId}

작성자 본인의 게시글을 삭제한다.

Status:

| 상황 | 코드 |
| --- | --- |
| 성공 | 204 No Content |
| 작성자 아님 | 403 Forbidden |
| 게시글 없음 | 404 Not Found |

## 6. 투표 API

### POST /api/posts/{postId}/votes

로그인 사용자가 게시글의 두 선택지 중 하나에 투표한다. 이미 투표한 경우 선택지를 변경한다.

Request:

```json
{
  "choice": "A"
}
```

Response:

```json
{
  "postId": 5,
  "voteOptionA": "제육",
  "voteOptionB": "돈까스",
  "voteCountA": 13,
  "voteCountB": 8,
  "voteRatioA": 61.9,
  "voteRatioB": 38.1
}
```

## 7. 댓글 API

### POST /api/posts/{postId}/comments

Request:

```json
{
  "content": "저라면 제육입니다."
}
```

Response:

```json
{
  "commentId": 12,
  "postId": 5,
  "authorNickname": "토론러",
  "content": "저라면 제육입니다.",
  "createdAt": "2026-06-22T10:40:00"
}
```

### DELETE /api/comments/{commentId}

작성자 본인의 댓글을 소프트 삭제한다.

## 8. 관리자 API

### GET /api/admin/users

전체 사용자 목록을 조회한다. ADMIN 권한이 필요하다.

### PATCH /api/admin/users/{userId}/role

사용자 권한을 변경한다. ADMIN 권한이 필요하다.

Request:

```json
{
  "role": "ADMIN"
}
```

### PATCH /api/admin/posts/{postId}/visibility

게시글 공개 여부를 변경한다. ADMIN 권한이 필요하다.

Request:

```json
{
  "isPublic": false
}
```

## 9. AI 생성 정책

AI 발화와 요약은 Spring Boot 내부의 Spring AI `ChatClient`가 OpenAI API를 호출해 생성한다. 브라우저가 직접 호출하는 별도 AI API는 제공하지 않는다. 토론 턴 생성과 토론 중단 API 안에서 서버가 내부적으로 Spring AI를 호출하고, 생성 결과만 서비스 데이터로 저장한다.

현재 `dev` 기준 Spring AI는 공통 호출 환경과 최소 계약만 유지한다. 선택형 주제 후보 생성 등 고도화 파이프라인은 `ai-experiment-choice-pipeline` 브랜치에 보존되어 있으며, 팀원별 실험 브랜치에서 비교 후 채택한다.

현재 `/new` 화면의 후보 생성, 검증/정렬, 후보 선택 단계는 데모용 프론트 목업이다. 실제 선택형 후보 생성 API를 본 브랜치에 확정하지 않은 이유는 팀원별 AI 파이프라인 실험 결과를 비교한 뒤 서버 계약을 고정하기 위해서다.

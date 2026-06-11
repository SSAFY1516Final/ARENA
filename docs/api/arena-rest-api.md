# ARENA(아레나) REST API 설계서

## 1. 공통 정책

| 항목 | 정책 |
| --- | --- |
| 인증 방식 | Spring Security + JWT Bearer Token |
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
  "timestamp": "2026-05-22T10:30:00"
}
```

### 인증 필요 API

| API | 인증 |
| --- | --- |
| POST /api/auth/signup | 불필요 |
| POST /api/auth/login | 불필요 |
| POST /api/auth/logout | 필요 |
| GET /api/posts | 불필요 |
| GET /api/posts/{postId} | 불필요 |
| /api/admin/** | ADMIN 권한 필요 |
| 그 외 /api/** | 필요 |

인증이 필요한 API는 다음 헤더를 전달한다.

```http
Authorization: Bearer {accessToken}
```

## 2. 인증 API

### POST /api/auth/signup

회원가입을 수행한다.

Request:

```json
{
  "loginId": "user01",
  "nickname": "토론러",
  "password": "password123!"
}
```

Response:

```json
{
  "userId": 1,
  "loginId": "user01",
  "nickname": "토론러"
}
```

Status:

| 상황 | 코드 |
| --- | --- |
| 성공 | 201 Created |
| loginId 또는 nickname 중복 | 409 Conflict |
| 검증 실패 | 400 Bad Request |

### POST /api/auth/login

로그인 ID와 비밀번호를 검증하고 JWT access token을 발급한다.

Request:

```json
{
  "loginId": "user01",
  "password": "password123!"
}
```

Response:

```json
{
  "tokenType": "Bearer",
  "accessToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

발급된 JWT에는 `sub`, `userId`, `role`, `iat`, `exp` 클레임이 포함된다. `role`은 `USER` 또는 `ADMIN`이다.

Status:

| 상황 | 코드 |
| --- | --- |
| 성공 | 200 OK |
| 인증 실패 | 401 Unauthorized |
| 검증 실패 | 400 Bad Request |

### POST /api/auth/logout

JWT는 서버 세션을 보관하지 않으므로 서버 측 상태를 무효화하지 않는다. 클라이언트는 저장된 access token을 삭제해 로그아웃을 완료한다.

Status:

| 상황 | 코드 |
| --- | --- |
| 성공 | 204 No Content |
| 미인증 | 401 Unauthorized |

## 3. 토론 API

### POST /api/debates

로그인 사용자의 토론 세션을 생성한다.

Request:

```json
{
  "topic": "오늘 점심 제육 vs 돈까스",
  "mode": "PRACTICAL"
}
```

Response:

```json
{
  "debateId": 10,
  "topic": "오늘 점심 제육 vs 돈까스",
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
    "coreArguments": "냉정파는 안정성, 열정파는 만족감을 주장했습니다.",
    "highlight": "열정파가 제육의 즉시 만족감을 강하게 주장한 장면",
    "decisionCriteria": "오후 집중력이 중요하면 돈까스, 현재 만족이 중요하면 제육",
    "remainingIssue": "매운맛과 식후 집중력",
    "summaryText": "두 선택지는 안정성과 만족감의 차이로 정리됩니다."
  }
}
```

Status:

| 상황 | 코드 |
| --- | --- |
| 성공 | 200 OK |
| 작성자 아님 | 403 Forbidden |
| 토론 없음 | 404 Not Found |
| ACTIVE 상태 아님 | 409 Conflict |
| AI 서버 실패 | 502 Bad Gateway |

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

Status:

| 상황 | 코드 |
| --- | --- |
| 성공 | 201 Created |
| 작성자 아님 | 403 Forbidden |
| 토론 없음 | 404 Not Found |
| STOPPED 상태 아님 | 409 Conflict |
| 이미 공유됨 | 409 Conflict |

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

## 4. 게시글 API

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
      "createdAt": "2026-05-15T10:30:00"
    }
  ],
  "page": 1,
  "size": 10,
  "totalCount": 1
}
```

정렬:

| sort | 기준 |
| --- | --- |
| latest | 게시글 생성일 내림차순 |
| comments | 댓글 수 내림차순, 생성일 내림차순 |
| votes | 전체 투표 수 내림차순, 생성일 내림차순 |

### GET /api/posts/{postId}

게시글, 요약, 전체 토론 로그, 댓글을 조회한다.

Response:

```json
{
  "post": {
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
    "createdAt": "2026-05-15T10:30:00"
  },
  "summary": {
    "summaryText": "두 선택지는 안정성과 만족감의 차이로 정리됩니다."
  },
  "messages": [
    {
      "speaker": "COOL_HEADED",
      "roundNo": 1,
      "content": "돈까스는 실패 확률이 낮습니다."
    }
  ],
  "comments": []
}
```

### DELETE /api/posts/{postId}

작성자 본인의 게시글을 삭제한다.

Status:

| 상황 | 코드 |
| --- | --- |
| 성공 | 204 No Content |
| 작성자 아님 | 403 Forbidden |
| 게시글 없음 | 404 Not Found |

## 5. 투표 API

### POST /api/posts/{postId}/votes

로그인 사용자가 게시글의 두 선택지 중 하나에 투표한다. 이미 투표한 경우 선택지를 변경한다.

Request:

```json
{
  "choice": "A"
}
```

Status:

| 상황 | 코드 |
| --- | --- |
| 성공 | 200 OK |
| 미인증 | 401 Unauthorized |
| 게시글 없음 | 404 Not Found |
| choice가 A/B가 아님 | 400 Bad Request |

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

Status:

| 상황 | 코드 |
| --- | --- |
| 성공 | 201 Created |
| 미인증 | 401 Unauthorized |
| 게시글 없음 | 404 Not Found |
| 검증 실패 | 400 Bad Request |

## 6. 댓글 API

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
  "createdAt": "2026-05-15T10:40:00"
}
```

### DELETE /api/comments/{commentId}

작성자 본인의 댓글을 소프트 삭제한다.

Status:

| 상황 | 코드 |
| --- | --- |
| 성공 | 204 No Content |
| 작성자 아님 | 403 Forbidden |
| 댓글 없음 | 404 Not Found |

## 7. 관리자 API

관리자 API는 `ADMIN` 권한을 가진 JWT로만 호출할 수 있다. `USER` 권한 토큰으로 호출하면 403 Forbidden을 반환한다.

### GET /api/admin/users

전체 사용자 목록을 조회한다.

Response:

```json
[
  {
    "userId": 1,
    "loginId": "admin",
    "nickname": "관리자",
    "role": "ADMIN",
    "createdAt": "2026-06-11T10:30:00"
  },
  {
    "userId": 2,
    "loginId": "user01",
    "nickname": "토론러",
    "role": "USER",
    "createdAt": "2026-06-11T10:35:00"
  }
]
```

### PATCH /api/admin/users/{userId}/role

사용자 권한을 변경한다. 관리자가 자기 자신의 권한을 `USER`로 낮추는 요청은 거부한다.

Request:

```json
{
  "role": "ADMIN"
}
```

Response:

```json
{
  "userId": 2,
  "loginId": "user01",
  "nickname": "토론러",
  "role": "ADMIN",
  "createdAt": "2026-06-11T10:35:00"
}
```

Status:

| 상황 | 코드 |
| --- | --- |
| 성공 | 200 OK |
| USER 권한 접근 | 403 Forbidden |
| 사용자 없음 | 404 Not Found |
| 자기 자신의 ADMIN 권한 해제 | 409 Conflict |
| 검증 실패 | 400 Bad Request |

### PATCH /api/admin/posts/{postId}/visibility

게시글 공개 여부를 변경한다. `false`로 변경된 게시글은 공개 목록/상세 조회 대상에서 제외된다.

Request:

```json
{
  "isPublic": false
}
```

Response:

```json
{
  "postId": 5,
  "isPublic": false
}
```

Status:

| 상황 | 코드 |
| --- | --- |
| 성공 | 200 OK |
| USER 권한 접근 | 403 Forbidden |
| 게시글 없음 | 404 Not Found |
| 검증 실패 | 400 Bad Request |

## 8. AI 생성 정책

AI 발화와 요약은 Spring Boot 내부의 Spring AI `ChatClient`가 OpenAI API를 호출해 생성한다.

브라우저가 직접 호출하는 별도 AI API는 제공하지 않는다. 토론 턴 생성과 토론 중단 API 안에서 서버가 내부적으로 Spring AI를 호출하고, 생성 결과만 서비스 데이터로 저장한다.

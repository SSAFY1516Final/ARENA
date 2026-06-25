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

현재 프론트 `/new` 화면은 원본 주제를 `POST /api/ai/round-candidates`로 전송하고, Spring AI가 생성한 세부 라운드 후보를 선택하는 흐름을 제공한다. 실제 토론 생성 요청에는 선택된 후보의 `coreQuestion`을 `topic`으로 전달한다. `mode`는 기존 DB/API 호환을 위해 유지한다.

2026-06-24 현재 `/new` 후보 선택 후 생성 요청은 원본 주제, 선택된 세부 질문, 진영 이름, 후보 프레임을 함께 보낸다.

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

현재 새 토론방 기본 흐름은 배치 생성 API를 사용하며, 이 단일 턴 API는 호환용으로 유지한다. 새 토론에서 라운드는 세부주제 기준이고, 처음 선택한 세부주제에서 나온 10개 발화는 모두 `roundNo: 1`에 속한다.

Response:

```json
{
  "messageId": 31,
  "speaker": "COOL_HEADED",
  "roundNo": 1,
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

### POST /api/debates/{debateId}/turns/batch

초기 10턴 토론 발화 생성을 서버 백그라운드 작업으로 시작한다. 10개 발화가 이미 저장되어 있으면 `COMPLETE`와 저장 메시지를 반환하고, 아직 부족하면 debateId 기준 중복 방지 레지스트리에 작업을 등록한 뒤 `GENERATING`을 즉시 반환한다. 사용자가 브라우저를 떠나도 시작된 서버 작업은 계속 실행된다.

Response:

```json
{
  "status": "GENERATING",
  "messages": []
}
```

완료 후 응답 예시는 다음과 같다.

```json
{
  "status": "COMPLETE",
  "messages": [
    {
      "messageId": 31,
      "speaker": "COOL_HEADED",
      "roundNo": 1,
      "content": "오후 일정까지 고려하면 돈까스가 안정적입니다.",
      "peakReached": true
    }
  ]
}
```

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

#### 2026-06-24 stop 선택값 보존

선택한 진영과 결과에서 열 라운드를 보존하려면 request body를 함께 보낸다. body가 없으면 기존처럼 요약만 생성한다.

```json
{
  "selectedSide": "COOL_HEADED",
  "selectedRoundNo": 1
}
```

응답에는 저장된 선택값이 함께 포함된다.

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

### POST /api/debates/{debateId}/share

선택한 라운드의 토론 요약을 게시글로 공유한다. `STOPPED` 또는 이미 `SHARED` 상태인 토론에서 호출할 수 있고, 하나의 토론에서도 라운드별 또는 같은 라운드별 게시글을 여러 번 생성할 수 있다.

Request:

```json
{
  "title": "오늘 점심 제육 vs 돈까스",
  "voteOptionA": "제육",
  "voteOptionB": "돈까스",
  "isPublic": true,
  "roundNo": 1,
  "body": "나는 오늘 상황에서는 실패 위험보다 만족감이 더 중요하다고 봤다."
}
```

`title`은 호환용으로 받을 수 있지만 서버는 게시글 제목을 질문 원본으로 저장한다. `summaryCard`는 공유 대상 라운드의 AI 요약 카드이며, `shareBody`는 세부주제 제목, 세부주제 상세설명, 사용자가 게시글 공유 모달에서 직접 작성한 본문만으로 저장된다. 토론 발화 전문은 `shareBody`에 붙이지 않는다.

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

### DELETE /api/debates/{debateId}

내 토론 목록에서 토론을 삭제한다. 요청자는 토론 작성자여야 한다. 삭제 대상 토론이 게시판에 공유된 상태라면 연결 게시글, 댓글, 투표를 먼저 삭제하고 이후 요약, 메시지, 토론 세션을 삭제한다.

Status:

| 상황 | 코드 |
| --- | --- |
| 성공 | 204 No Content |
| 미인증 | 401 Unauthorized |
| 작성자 아님 | 403 Forbidden |
| 토론 없음 | 404 Not Found |

## 5. 게시글 API

### GET /api/posts?page=1&size=10&keyword=제육&mode=PRACTICAL&sort=latest

공개 게시글 목록을 조회한다. 검색과 필터 파라미터는 모두 선택값이다.

Query:

| 이름 | 설명 |
| --- | --- |
| page | 1부터 시작하는 페이지 번호 |
| size | 페이지 크기 |
| keyword | 제목, 요약 카드, 사용자 작성 본문, 토론 주제 검색어 |
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
      "shareRoundNo": 1,
      "summaryCard": "안정성 vs 만족감으로 갈린 토론",
      "shareBody": "세부주제\n점심 안정성\n\n상세설명\n안정성 vs 만족감\n\n본문\n나는 오늘 상황에서는 실패 위험보다 만족감이 더 중요하다고 봤다.",
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

게시글, 공유된 라운드의 요약과 토론 로그, 댓글을 조회한다.

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

AI 발화와 요약은 Spring Boot 내부의 Spring AI `ChatClient`가 GMS OpenAI-compatible API를 호출해 생성한다. 브라우저가 직접 호출하는 별도 AI API는 제공하지 않는다. 토론 턴 생성과 토론 중단 API 안에서 서버가 내부적으로 Spring AI를 호출하고, 생성 결과만 서비스 데이터로 저장한다.

현재 `dev` 기준 Spring AI는 GMS `gpt-5.4-mini` 모델을 기본값으로 사용한다. 선택형 주제 후보 생성과 10턴 배치 토론 생성은 서버 API와 DB 로그 저장 흐름에 반영되어 있다.

현재 `/new` 화면의 후보 생성, 검증/정렬, 후보 선택 단계는 `POST /api/ai/round-candidates` 서버 API를 사용한다. 서버는 후보 생성 run과 prompt call log를 DB에 보존한다.

## AI

### POST `/api/ai/round-candidates`

원본 주제를 받아 Spring AI 기반 세부 라운드 후보 5개를 생성한다. `/new` 화면은 더 이상 고정 지연 목업 후보를 만들지 않고 이 API 응답을 사용한다.

Request:

```json
{
  "topic": "오늘 점심 제육 vs 돈까스",
  "mode": "PRACTICAL",
  "candidateCount": 5
}
```

후보 생성 run과 prompt call log는 DB에 보존한다. 비밀값은 저장하지 않는다.

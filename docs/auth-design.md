# 인증/인가 및 권한 설계

## 인증 흐름

```mermaid
sequenceDiagram
    participant U as User
    participant V as Vue Client
    participant K as Kakao OAuth
    participant B as Spring Boot API
    participant DB as MySQL

    U->>V: 카카오 로그인 클릭
    V->>K: authorize 요청
    K-->>V: authorization code redirect
    V->>B: POST /api/auth/kakao
    B->>K: code로 access token 교환
    B->>K: 사용자 프로필 조회
    B->>DB: 사용자 조회 또는 생성
    B-->>V: ARENA JWT 반환
    V->>B: Authorization: Bearer JWT
```

## JWT 설계

JWT payload에는 다음 정보를 포함한다.

| Claim | 설명 |
| --- | --- |
| `sub` | 서비스 로그인 ID |
| `userId` | 사용자 PK |
| `role` | USER 또는 ADMIN |
| `iat` | 발급 시각 |
| `exp` | 만료 시각 |

클라이언트는 JWT를 `Authorization: Bearer <token>` 형식으로 API 요청에 포함한다.

## 인가 정책

| 경로 | 권한 |
| --- | --- |
| `/api/auth/signup` | Public |
| `/api/auth/login` | Public |
| `/api/auth/kakao` | Public |
| `GET /api/posts/**` | Public |
| `/api/debates/**` | USER |
| `/api/posts/{id}/votes` | USER |
| `/api/posts/{id}/comments` | USER |
| `/api/admin/**` | ADMIN |

## 권한 모델

| Role | 설명 |
| --- | --- |
| USER | 일반 사용자. 토론 생성, AI 발화 요청, 게시글 공유, 투표/댓글 작성 가능 |
| ADMIN | 관리자. USER 권한에 더해 사용자/게시글 관리 가능 |

## 보안 고려사항

- Kakao access token은 백엔드에서만 사용하고 클라이언트에 저장하지 않는다.
- 서비스 API 인증은 Kakao token이 아니라 자체 JWT로 처리한다.
- JWT secret은 최소 32자 이상의 환경 변수로 관리한다.
- CORS는 로컬 개발 도메인과 필요한 헤더만 허용한다.
- 클라이언트 시크릿이 켜져 있으면 Kakao token 교환 시 `client_secret`을 함께 전달한다.

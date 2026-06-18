# ARENA - AI Debate Community

SSAFY 15기 서울 16반 관통 프로젝트 제출 저장소입니다. ARENA는 사용자가 선택하기 어려운 주제를 입력하면 두 AI 페르소나가 서로 다른 관점으로 토론하고, 토론 결과를 게시글로 공유해 다른 사용자의 투표와 의견을 받을 수 있는 서비스입니다.

## 프로젝트 개요

- 프로젝트명: ARENA
- 팀: Java_Seoul_16_Jaeyoung_Minyong
- 주제: AI 기반 선택 토론 커뮤니티
- 핵심 기능: 카카오 OAuth 로그인, JWT 인증, AI 토론 턴 생성, 토론 요약, 게시글 공유, 투표/댓글, 관리자 권한 관리
- 제출 범위: Spring Boot REST API, Spring Security + JWT, Spring AI 연동 설계 및 문서

## 기술 스택

| 영역 | 기술 |
| --- | --- |
| Server | Java 17, Spring Boot 3, Spring Security, Spring AI |
| Auth | Kakao OAuth 2.0, JWT |
| Persistence | MySQL 8, MyBatis |
| Infra | Docker Compose |
| Client | Vue 3, Vite, Pinia, Axios |
| Docs | Markdown, ERD/API/요구사항 문서 |

## 문서

- [요구사항 정의서](docs/requirements.md)
- [인증/인가 및 권한 설계](docs/auth-design.md)
- [Spring AI 기능 설계](docs/ai-design.md)
- [API 명세](docs/api.md)
- [프로젝트 구조 및 제출 체크리스트](docs/project-structure.md)

## 주요 기능

### 사용자 기능

- 카카오 OAuth 로그인
- JWT 발급 및 인증
- 내 토론 목록 조회
- 토론 생성 및 AI 발화 요청
- 토론 중단 후 AI 요약 생성
- 요약 결과 게시글 공유
- 게시글 투표 및 댓글 작성

### 관리자 기능

- 전체 사용자 조회
- 사용자 권한 변경
- 게시글 공개 여부 관리
- 사용자/콘텐츠 관리 기능 확장 기반 제공

### AI 기능

- Spring AI `ChatClient` 기반 토론 발화 생성
- 냉정파/열정파 페르소나를 번갈아 발화
- 토론 종료 시 핵심 주장, 하이라이트, 판단 기준, 남은 쟁점, 공유용 요약 생성

## 실행 환경 변수

실행 전 `.env` 또는 실행 환경에 다음 값을 설정합니다. 실제 키 값은 저장소에 커밋하지 않습니다.

```bash
JWT_SECRET=change-this-to-a-long-random-secret-key-32chars
JWT_EXPIRATION_SECONDS=86400

OPENAI_API_KEY=sk-...
OPENAI_MODEL=gpt-4o-mini

KAKAO_REST_API_KEY=...
KAKAO_CLIENT_SECRET=...
KAKAO_REDIRECT_URI=http://localhost:5173/auth/kakao/callback
```

## 실행 방법

Docker Compose를 사용하는 경우:

```bash
docker compose up -d --build
```

로컬 Spring Boot 실행 기준:

```bash
cd server
./gradlew bootRun
```

테스트:

```bash
cd server
./gradlew test

cd ../client
npm ci
npm test -- --run
```

프론트 개발 서버:

```bash
cd client
npm ci
npm run dev
```

## 프로젝트 구조

```text
.
├── client/              # Vue 3 + Vite 프론트엔드
├── server/              # Spring Boot 백엔드
├── docs/                # 요구사항, 인증/인가, AI, API 문서
├── docker-compose.yml   # MySQL + Spring Boot 실행
└── README.md
```

## Kakao OAuth 로컬 설정

Kakao Developers 콘솔에서 다음 설정이 필요합니다.

- 카카오 로그인 사용 설정: ON
- REST API 키의 카카오 로그인 리다이렉트 URI:
  - `http://localhost:5173/auth/kakao/callback`
- 웹 도메인:
  - `http://localhost:5173`
- 클라이언트 시크릿을 ON으로 둔 경우 백엔드 `KAKAO_CLIENT_SECRET`에 동일 값 설정

## API 요약

| Method | Path | 설명 | 인증 |
| --- | --- | --- | --- |
| POST | `/api/auth/kakao` | 카카오 인가 코드로 JWT 발급 | Public |
| POST | `/api/auth/login` | 로컬 로그인 | Public |
| POST | `/api/auth/signup` | 로컬 회원가입 | Public |
| POST | `/api/ai/topic-candidates` | 조건 기반 AI 주제 후보 생성 | USER |
| GET | `/api/debates` | 내 토론 목록 조회 | USER |
| POST | `/api/debates` | 토론 생성 | USER |
| POST | `/api/debates/{id}/turns` | AI 다음 발화 생성 | USER |
| POST | `/api/debates/{id}/stop` | 토론 종료 및 요약 생성 | USER |
| POST | `/api/debates/{id}/share` | 토론 요약 게시글 공유 | USER |
| GET | `/api/posts` | 공개 게시글 목록 | Public |
| POST | `/api/posts/{id}/votes` | 게시글 투표 | USER |
| POST | `/api/posts/{id}/comments` | 댓글 작성 | USER |
| GET | `/api/admin/users` | 사용자 목록 관리 | ADMIN |

상세 내용은 [API 명세](docs/api.md)를 참고합니다.

## 브랜치 전략

- `master`: 제출 및 최종 안정 버전
- `dev`: 문서/기능 통합 작업 브랜치
- 기능 작업 브랜치는 필요 시 `feat/*`, `fix/*`, `docs/*` 형식 사용

## 제출 체크리스트

- [x] 요구사항 정의서
- [x] 인증/인가 설계 문서
- [x] 사용자 권한 설계
- [x] Spring AI 기능 설계
- [x] README 정리
- [ ] 최종 소스코드 반영
- [ ] 실행 결과 캡처 또는 테스트 로그 정리

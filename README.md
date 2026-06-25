# ARENA - AI Debate Community

SSAFY 15기 서울 16반 관통 프로젝트 제출 저장소입니다. ARENA는 사용자가 선택하기 어려운 주제를 입력하면 두 AI 페르소나가 서로 다른 관점으로 토론하고, 토론 결과를 게시글로 공유해 다른 사용자의 투표와 의견을 받을 수 있는 서비스입니다.

## 제출 요약

| 항목 | 위치 | 상태 |
| --- | --- | --- |
| Spring Boot 백엔드 | `server/` | Spring Security, JWT, Kakao OAuth, MyBatis, Spring AI 베이스 환경 반영 |
| Vue 프론트엔드 | `client/` | Kakao 로그인, 닉네임 수정, 선택형 토론 생성 목업, 게시글 화면 흐름 반영 |
| 요구사항 정의서 | `docs/deliverables/requirements/arena-requirements.md` | 현재 서비스 방향 기준 보정 완료 |
| 유즈케이스 다이어그램 | `docs/deliverables/use-cases/arena-use-cases.md` | Actor, 주요 기능, Mermaid 원본 포함 |
| ERD | `docs/deliverables/erd/arena-erd.md` | Kakao OAuth 사용자 모델 및 JWT 인증 흐름 기준 보정 |
| WBS | `docs/deliverables/wbs/arena-wbs.xlsx` | 카카오 로그인, 관리자 기능, Spring AI 베이스 기준 보정 |
| 간트차트 | `docs/deliverables/gantt/arena-gantt.xlsx` | 현재 개발 흐름 기준 보정 |
| 화면설계서 | `docs/deliverables/screen-definition/figma-screen-definition.md` | Vue 화면 구조와 사용자 흐름 기준 보정 |
| API 설계서 | `docs/deliverables/api/arena-rest-api.md` | 현재 REST API 기준 보정 |
| 최종 검증 기록 | `docs/deliverables/test-report.md` | 테스트/빌드/브라우저 확인 결과 정리 |

전체 산출물 색인은 [docs/deliverables/README.md](docs/deliverables/README.md)를 참고합니다.

## 프로젝트 개요

- 프로젝트명: ARENA
- 팀: Java_Seoul_16_Jaeyoung_Minyong
- 주제: AI 기반 선택 토론 커뮤니티
- 인증 방식: Kakao OAuth 로그인 후 자체 JWT 발급
- 핵심 기능: 선택형 후보 기반 토론 생성, AI 토론 진행/요약, 게시글 공유, 투표/댓글, 관리자 권한 관리
- 제출 범위: Spring Boot REST API, Vue 3 프론트엔드, Spring Security + JWT, MyBatis, Spring AI 베이스 환경, 제출 산출물

## 기술 스택

| 영역 | 기술 |
| --- | --- |
| Server | Java 17, Spring Boot 3, Spring Security, Spring AI |
| Auth | Kakao OAuth 2.0, JWT |
| Persistence | MySQL 8, MyBatis |
| Infra | Docker Compose |
| Client | Vue 3, Vite, Pinia, Axios |
| Docs | Markdown, Mermaid, Excel, PNG |

## 주요 기능

### 사용자 기능

- 카카오 OAuth 로그인
- JWT 기반 API 인증
- 내 닉네임 조회 및 수정
- 내 토론 목록 조회
- 주제/상황/세부조건 기반 후보 선택형 토론 생성
- AI 발화 요청
- 토론 중단 후 AI 요약 생성
- 요약 결과 게시글 공유
- 공개 게시글 조회, 투표, 댓글 작성

### 관리자 기능

- 전체 사용자 조회
- 사용자 권한 변경
- 게시글 공개 여부 관리
- 사용자/콘텐츠 관리 기능 확장 기반 제공

### AI 기능

- Spring AI 연동을 위한 최소 베이스 환경 구성
- 토론 발화 생성 및 요약 생성을 담당하는 AI 클라이언트 추상화
- `/new` 화면은 선택형 후보 파이프라인을 목업으로 노출
- 실제 선택형 주제 후보 생성 등 고도화 파이프라인은 `ai-experiment-choice-pipeline` 브랜치에 별도 보존

## 실행 환경 변수

실행 전 `.env` 또는 실행 환경에 다음 값을 설정합니다. 실제 키 값은 저장소에 커밋하지 않습니다.

```bash
JWT_SECRET=change-this-to-a-long-random-secret-key-32chars
JWT_EXPIRATION_SECONDS=86400

GMS_KEY=...
GMS_BASE_URL=https://gms.ssafy.io/gmsapi/api.openai.com
GMS_COMPLETIONS_PATH=/v1/chat/completions
GMS_MODEL=gpt-5.4-mini
CORS_ALLOWED_ORIGINS=http://localhost:5173,http://127.0.0.1:5173,http://localhost:15173

KAKAO_REST_API_KEY=...
KAKAO_CLIENT_SECRET=...
KAKAO_REDIRECT_URI=http://localhost:5173/auth/kakao/callback
```

프론트 개발 서버를 `15173` 포트로 실행하는 경우 Kakao Developers에도 다음 값을 함께 등록합니다.

- Redirect URI: `http://localhost:15173/auth/kakao/callback`
- Web domain: `http://localhost:15173`

## 실행 방법

Docker Compose를 사용하는 경우:

```bash
docker compose up -d --build
```

백엔드만 로컬로 실행하는 경우:

```bash
cd server
./gradlew bootRun
```

프론트 개발 서버:

```bash
cd client
npm ci
npm run dev
```

테스트:

```bash
cd server
./gradlew test

cd ../client
npm ci
npm test -- --run
```

## 프로젝트 구조

```text
.
├── client/                 # Vue 3 + Vite 프론트엔드
├── server/                 # Spring Boot 백엔드
├── docs/                   # 설계 문서 및 제출 산출물
│   ├── auth-design.md      # 인증/인가 설계
│   ├── ai-design.md        # Spring AI 설계
│   ├── api.md              # API 명세
│   └── deliverables/       # 요구사항, ERD, WBS, 간트차트, 화면설계서
├── docker-compose.yml      # MySQL + Spring Boot 실행
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
| POST | `/api/auth/logout` | 클라이언트 토큰 폐기 흐름 | USER |
| GET | `/api/users/me` | 내 프로필 조회 | USER |
| PATCH | `/api/users/me/nickname` | 내 닉네임 수정 | USER |
| GET | `/api/debates` | 내 토론 목록 조회 | USER |
| POST | `/api/debates` | 토론 생성 | USER |
| POST | `/api/debates/{id}/turns` | AI 다음 발화 생성 | USER |
| POST | `/api/debates/{id}/stop` | 토론 종료 및 요약 생성 | USER |
| POST | `/api/debates/{id}/share` | 토론 요약 게시글 공유 | USER |
| GET | `/api/posts` | 공개 게시글 목록 | Public |
| POST | `/api/posts/{id}/votes` | 게시글 투표 | USER |
| POST | `/api/posts/{id}/comments` | 댓글 작성 | USER |
| GET | `/api/admin/users` | 사용자 목록 관리 | ADMIN |

상세 내용은 [docs/deliverables/api/arena-rest-api.md](docs/deliverables/api/arena-rest-api.md)를 참고합니다.

## 제출 체크리스트

- [x] 요구사항 정의서
- [x] 유즈케이스 문서 및 다이어그램
- [x] ERD
- [x] WBS
- [x] 간트차트
- [x] 화면설계서
- [x] 인증/인가 설계 문서
- [x] 사용자 권한 설계
- [x] Spring AI 베이스 설계
- [x] API 설계서
- [x] README 정리
- [x] 선택형 토론 생성 화면 목업 반영
- [x] 닉네임 수정 기능 문서 반영
- [x] 최종 실행 캡처 또는 테스트 로그 정리

## 브랜치 전략

- `master`: 제출 및 최종 안정 버전
- `dev`: 문서/기능 통합 작업 브랜치
- `ai-experiment-choice-pipeline`: 고도화 AI 파이프라인 실험 브랜치
- 기능 작업 브랜치는 필요 시 `feat/*`, `fix/*`, `docs/*` 형식 사용

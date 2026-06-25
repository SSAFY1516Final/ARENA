# ARENA

AI가 대신 싸워주는 선택형 토론 커뮤니티

ARENA는 혼자 결정하기 애매한 주제를 입력하면 AI가 서로 다른 관점으로 토론하고, 사용자는 그 과정을 보며 더 나은 선택을 할 수 있도록 돕는 서비스입니다. 토론이 끝난 뒤에는 결과를 게시판에 공유해 다른 사용자들의 투표와 댓글까지 받을 수 있습니다.

## 한 줄 소개

> "오늘 점심 뭐 먹지?" 같은 가벼운 고민부터 여행, 소비, 생활 선택까지 AI 토론과 커뮤니티 반응으로 결정 과정을 구조화하는 서비스

## 왜 만들었나

일상적인 선택은 단순해 보여도 막상 결정하려면 기준이 흩어지기 쉽습니다.

- 선택지가 많을수록 비교 기준이 흐려집니다.
- 주변 의견을 묻더라도 근거가 정리되지 않는 경우가 많습니다.
- AI에게 물어보면 답은 얻을 수 있지만, 찬반 관점의 충돌 과정은 잘 보이지 않습니다.

ARENA는 이 문제를 "AI 토론" 형식으로 풀었습니다. 한쪽 결론만 제시하는 대신 서로 다른 입장이 라운드별로 주장하고, 사용자는 그 흐름을 보며 판단할 수 있습니다.

## 핵심 사용자 흐름

1. 카카오 로그인으로 서비스에 입장합니다.
2. 고민 중인 큰 주제를 입력합니다.
3. AI가 토론하기 좋은 세부 질문 후보를 생성합니다.
4. 사용자는 후보 중 하나를 선택해 토론을 시작합니다.
5. 두 AI 페르소나가 서로 다른 입장에서 라운드별로 토론합니다.
6. 사용자는 토론을 중단하고 AI 요약 결과를 확인합니다.
7. 결과를 게시판에 공유해 투표와 댓글을 받을 수 있습니다.

## 주요 화면

| 화면 | 역할 |
| --- | --- |
| 메인/새 토론 | 사용자가 주제를 입력하고 AI가 생성한 세부 토론 후보를 선택 |
| 토론방 | 두 AI 페르소나의 라운드별 발화를 확인 |
| 결과 페이지 | 토론 요약, 선택 근거, 공유 진입점을 제공 |
| 내 토론 | 내가 만든 토론을 다시 열람하고 이어보기 |
| 게시판 | 공유된 토론 결과를 보고 투표와 댓글로 참여 |

## 핵심 기능

### AI 토론 생성

- 사용자가 입력한 주제를 기반으로 토론 가능한 세부 질문 후보를 생성합니다.
- 각 후보는 선택 기준이 분명한 형태로 제시되어 바로 토론으로 이어질 수 있습니다.
- AI 응답은 프롬프트 템플릿과 응답 파싱 계층을 통해 서비스 흐름에 맞게 정리됩니다.

### 라운드형 AI 토론

- 두 AI 페르소나가 서로 다른 선택지를 대표합니다.
- 토론은 라운드 단위로 진행되며, 사용자는 중간에 종료하고 요약을 받을 수 있습니다.
- 진행 라운드와 선택 현황을 통해 토론의 흐름을 쉽게 파악할 수 있습니다.

### 커뮤니티 공유

- 종료된 토론 결과는 게시글로 공유할 수 있습니다.
- 다른 사용자는 게시글에서 투표하고 댓글을 남길 수 있습니다.
- 개인의 선택 고민이 커뮤니티의 판단 데이터로 확장됩니다.

### 사용자 경험

- 카카오 OAuth 기반 로그인
- JWT 기반 API 인증
- 닉네임 수정
- 내 토론 목록 관리
- 반응형 Vue UI

## 기술 구성

| 영역 | 사용 기술 |
| --- | --- |
| Frontend | Vue 3, Vite, Pinia, Axios, Naive UI |
| Backend | Java 17, Spring Boot 3, Spring Security |
| AI | Spring AI 기반 AI 클라이언트, 프롬프트 템플릿, 응답 파싱 |
| Database | MySQL 8, MyBatis |
| Auth | Kakao OAuth, JWT |
| Infra | Docker Compose |
| Test | Vitest, JUnit |

## 아키텍처 개요

```text
User
  │
  ▼
Vue Client
  │  Kakao OAuth / JWT
  ▼
Spring Boot API
  ├─ Auth / User
  ├─ Debate
  ├─ AI Prompt Pipeline
  ├─ Post / Vote / Comment
  └─ Admin
  │
  ├─ MySQL
  └─ External AI API
```

상세 아키텍처와 다이어그램은 [docs/architecture](docs/architecture)를 참고합니다.

## 시연 포인트

발표나 데모에서는 아래 흐름으로 보면 서비스 의도가 가장 잘 드러납니다.

1. 로그인 후 새 토론 화면에서 고민 주제를 입력합니다.
2. AI가 생성한 세부 질문 후보를 확인합니다.
3. 후보를 선택해 토론방으로 이동합니다.
4. AI 양측의 발화를 보고 토론을 종료합니다.
5. 결과 페이지에서 요약과 선택 근거를 확인합니다.
6. 게시판에 공유된 토론에서 투표/댓글 참여 흐름을 확인합니다.
7. 내 토론에서 이전 토론이 관리되는 방식을 확인합니다.

## 프로젝트 구조

```text
.
├── client/                 # Vue 3 프론트엔드
├── server/                 # Spring Boot 백엔드
├── docs/                   # 설계 문서와 발표/제출 산출물
│   ├── architecture/       # 아키텍처, 기술 스택, AI 파이프라인 문서
│   └── deliverables/       # 요구사항, ERD, API, WBS, 간트차트 등
├── docker-compose.yml      # MySQL + API + Client 실행 구성
└── README.md
```

## 문서 바로가기

- [요구사항 정의서](docs/deliverables/requirements/arena-requirements.md)
- [API 설계서](docs/deliverables/api/arena-rest-api.md)
- [ERD](docs/deliverables/erd/arena-erd.md)
- [유즈케이스](docs/deliverables/use-cases/arena-use-cases.md)
- [화면설계서](docs/deliverables/screen-definition/figma-screen-definition.md)
- [최종 검증 기록](docs/deliverables/test-report.md)
- [산출물 목록](docs/deliverables/README.md)

## 실행 방법

전체 서비스를 Docker Compose로 실행할 수 있습니다.

```bash
docker compose up -d --build
```

기본 접속 주소:

- Client: `http://localhost:15173`
- API: `http://localhost:8080`
- MySQL: `localhost:3306`

실행에는 Kakao OAuth 키, JWT secret, AI API 키가 필요합니다. 실제 키 값은 저장소에 포함하지 않습니다.

## 팀과 제출 범위

- 프로젝트명: ARENA
- 팀: Java_Seoul_16_Jaeyoung_Minyong
- 주제: AI 기반 선택 토론 커뮤니티
- 제출 범위: Vue 프론트엔드, Spring Boot REST API, Kakao OAuth/JWT 인증, MySQL/MyBatis, Spring AI 기반 토론 생성 흐름, 설계 산출물

## 브랜치 기준

- `master`: 최종 제출 및 안정 버전
- `dev`: 통합 개발 브랜치
- 실험성 AI 파이프라인은 별도 브랜치에서 비교 개발 후 필요한 범위만 통합합니다.

# ARENA 아키텍처 구성 문서

## 목적

이 문서는 ARENA 서비스의 현재 구현 기준 아키텍처를 설명하고, 발표 자료에 넣을 수 있는 시스템 구성도를 정의한다. 사용자가 제공한 참고 이미지는 AWS 배포 아키텍처 형식이므로, 이 문서는 해당 배치 방식을 참고하되 현재 코드와 Docker 구성, DB 스키마, API 문서를 기준으로 작성했다.

## 전체 구조

ARENA는 Vue 3 프론트엔드, Spring Boot 백엔드, MySQL 데이터베이스, 외부 AI/GMS 및 Kakao OAuth 연동으로 구성된다.

| 영역 | 구성 요소 | 책임 |
| --- | --- | --- |
| 사용자/브라우저 | Chrome 등 웹 브라우저 | 화면 조작, Kakao OAuth 진입, JWT 보관, REST API 호출 |
| 프론트엔드 | Vue 3, Vite, Pinia, Vue Router, Axios, Naive UI | 토론 생성/진행/결과/게시판 UI, 상태 관리, API 호출 |
| 백엔드 | Spring Boot 3, Spring Security, JWT, MyBatis, Spring AI | 인증/인가, 토론/게시판 API, AI 프롬프트 렌더링, AI 응답 파싱, DB 저장 |
| 데이터베이스 | MySQL 8.4 | 사용자, 토론 세션, 메시지, 요약, 게시글, 댓글, 투표, AI 호출 로그 저장 |
| 외부 서비스 | Kakao OAuth, GMS OpenAI-compatible API | 소셜 로그인, GPT-5.4-mini 기반 AI 후보/토론/요약 생성 |

## Docker 로컬 실행 구성

현재 개발 환경은 Docker Compose 기준으로 다음 컨테이너를 사용한다.

| 컨테이너 | 내부 포트 | 로컬 포트 | 설명 |
| --- | --- | --- | --- |
| `arena-client` | `15173` | `15173` | Vite 개발 서버 |
| `arena-app` | `8080` | `18080` | Spring Boot REST API |
| `arena-mysql` | `3306` | `13306` | MySQL 8.4 |

`arena-app`은 `arena-mysql`의 healthcheck가 통과한 뒤 시작된다. 프론트엔드는 `VITE_API_BASE_URL=http://localhost:18080`로 백엔드에 접근한다.

## 백엔드 내부 계층

백엔드는 다음 계층으로 나뉜다.

| 계층 | 주요 클래스/패키지 | 설명 |
| --- | --- | --- |
| Controller | `AuthController`, `AiController`, `DebateController`, `PostController`, `CommentController`, `UserController`, `AdminController` | REST API 진입점 |
| Security | `SecurityConfig`, `JwtAuthenticationFilter`, `JwtTokenProvider` | Kakao 로그인 이후 발급된 JWT 검증, 보호 API 인증 |
| Service | `AuthService`, `DebateService`, `PostService`, `CommentService`, `UserService`, `AiRoundCandidateService` | 비즈니스 로직, 트랜잭션 경계 |
| AI Layer | `AiPipelineService`, `SpringAiClient`, `AiPromptFactory`, `PromptTemplateLoader`, `AiResponseParser` | 프롬프트 렌더링, GMS 호출, JSON 파싱, 재시도 |
| Mapper | `UserMapper`, `DebateMapper`, `PostMapper`, `AiPromptCallLogMapper`, `AiDebateTurnLogMapper` 등 | MyBatis 기반 DB 접근 |

## 주요 요청 흐름

### 1. 로그인 흐름

1. 사용자가 프론트엔드에서 Kakao 로그인 버튼을 누른다.
2. Kakao OAuth 인증 후 `http://localhost:15173/auth/kakao/callback`으로 돌아온다.
3. 프론트엔드는 authorization code를 `POST /api/auth/kakao`로 보낸다.
4. 백엔드는 Kakao API로 사용자 정보를 확인하고, ARENA 사용자 레코드를 생성하거나 조회한다.
5. 백엔드는 JWT를 발급하고, 프론트엔드는 이후 보호 API 호출에 사용한다.

### 2. 토론 후보 생성 흐름

1. 사용자가 `/new`에서 원본 주제를 입력한다.
2. 프론트엔드는 `POST /api/ai/round-candidates`를 호출한다.
3. 백엔드는 `topic-frame-generator.txt`, `topic-round-candidates-generator.txt`, `topic-round-candidates-validator.txt` 프롬프트를 순서대로 렌더링한다.
4. Spring AI `ChatClient`가 GMS OpenAI-compatible API를 호출한다.
5. 백엔드는 후보 생성 run, 프롬프트, raw 응답, 파싱 결과를 저장한다.
6. 프론트엔드는 검증된 세부 주제 후보 5개를 보여준다.

### 3. 토론 생성 및 진행 흐름

1. 사용자가 세부 주제를 선택하고 토론을 시작한다.
2. 프론트엔드는 `POST /api/debates`로 토론 세션을 생성한다.
3. 토론방 진입 후 `POST /api/debates/{debateId}/turns/batch`를 호출한다.
4. 백엔드는 debateId 기준 중복 방지 레지스트리를 확인하고, 필요한 경우 백그라운드 executor에 생성 작업을 등록한 뒤 `GENERATING`을 즉시 반환한다.
5. 백그라운드 작업은 `debate-single-round-fast-generator.txt`를 렌더링하고 GMS를 한 번 호출해 10개 발화를 생성한다.
6. 생성된 10개 발화는 `debate_messages`에 저장된다. 이때 DB의 `round_no`는 발화 순번이 아니라 세부 주제 기준 라운드 번호다.
7. 프론트엔드는 `GET /api/debates/{debateId}` polling으로 저장 완료를 확인하고, 저장된 메시지를 순차 표시한다.
6. 렌더링 프롬프트, raw 응답, 파싱 결과, latency는 `ai_debate_turn_logs`에 저장된다.
7. 프론트엔드는 저장된 메시지를 입력중 표시와 함께 순차적으로 보여준다.

### 4. 토론 종료, 결과, 공유 흐름

1. 사용자가 어느 파를 선택하면 `POST /api/debates/{debateId}/stop`이 호출된다.
2. 백엔드는 전체 메시지를 기반으로 요약 프롬프트를 렌더링하고 GMS를 호출한다.
3. 요약 결과는 `debate_summaries`에 저장되고, 토론 세션은 `STOPPED` 상태가 된다.
4. 결과 페이지는 라운드별 기록, 사용자 선택 스코어, 요약을 보여준다.
5. 사용자가 라운드를 선택해 공유하면 `POST /api/debates/{debateId}/share`가 호출되고, 서버가 `roundNo`에 해당하는 라운드 세션을 찾아 `posts`에 게시글을 생성한다. 같은 토론도 여러 번 공유할 수 있다.
6. 게시판에서는 투표, 댓글, 공개/비공개 관리 기능이 이어진다.

## 데이터 저장 구조

핵심 테이블은 다음과 같다.

| 테이블 | 저장 내용 |
| --- | --- |
| `users` | Kakao/로컬 사용자, 닉네임, 권한 |
| `debate_sessions` | 원본 주제, 선택 세부 주제, A/B 라벨, 라운드 후보 연결, 상태, 선택 결과 |
| `debate_messages` | AI 토론 발화, 발화자, 라운드 번호 |
| `debate_summaries` | 토론 종료 후 AI 요약 |
| `posts` | 공유 게시글, 공유 라운드 번호, 세부주제/상세설명/사용자 본문, 투표 선택지 |
| `comments` | 게시글 댓글 |
| `post_votes` | 사용자별 게시글 투표 |
| `ai_round_candidate_runs` | 후보 생성 실행 단위와 최종 응답 |
| `ai_round_candidates` | 후보 생성 결과 5개 |
| `ai_prompt_call_logs` | 후보 생성 단계별 프롬프트/응답/파싱 로그 |
| `ai_debate_turn_logs` | 토론 생성 프롬프트/응답/파싱 로그 |

## AI 생성 안정화 확장 지점

현재 토론 발화 배치 생성은 긴 동기 응답이 아니라 서버 백그라운드 작업으로 실행된다. 사용자가 브라우저를 떠나도 이미 시작된 서버 작업은 계속 진행되며, 내 토론에서 ACTIVE 토론으로 다시 들어오면 토론방이 저장된 메시지를 polling한다.

서버 재시작 이후까지 복구해야 한다면 향후 `generation_status` 또는 별도 작업 테이블을 추가한다. 현재 구현은 실행 중 서버 프로세스 안의 debateId 기준 작업 레지스트리로 중복 호출을 방지한다.

## 산출물

- 발표용 외부 아키텍처 SVG: `docs/architecture/arena-external-architecture.svg`
- 발표용 외부 아키텍처 PNG: `docs/architecture/arena-external-architecture.png`
- 발표용 외부 아키텍처 수정 원본: `docs/architecture/arena-external-architecture.source.json`
- 발표용 외부 아키텍처 생성 스크립트: `docs/architecture/render_arena_external_architecture.py`
- 아이콘 참고 출처: `docs/architecture/arena-external-architecture-icon-sources.md`
- Mermaid 원본: `docs/architecture/arena-architecture.mmd`
- SVG 구성도: `docs/architecture/arena-architecture.svg`
- 참고 이미지 스타일 SVG 구성도: `docs/architecture/arena-architecture-reference-style.svg`

발표자료에는 `arena-external-architecture.svg` 또는 `arena-external-architecture.png`를 우선 사용한다. 이 파일은 내부 클래스명, DB 테이블명, 상세 API, 프롬프트 파일명을 제외하고 외부 구성과 컨테이너 관계만 보여준다.

## 참고 이미지 반영 방향

사용자가 제공한 참고 이미지는 AWS 배포 구성도 형태였다. 핵심 특징은 다음과 같았다.

- 사용자 요청이 오른쪽에서 들어온다.
- 도메인/프록시/서버 박스를 중심에 크게 둔다.
- 서버 박스 안에 프론트엔드와 백엔드 런타임을 함께 배치한다.
- DB는 서버 바깥쪽 또는 별도 영역으로 두고 양방향 연결을 표시한다.
- 하단에는 Local, GitHub, 빌드/배포 흐름을 둔다.

ARENA는 현재 AWS Route53, EC2, RDS, S3, CodeDeploy, GitHub Actions가 아니라 Docker Compose 기반 개발/시연 환경이다. 따라서 참고 이미지의 배치 방식은 따르되, 구성요소 이름은 실제 구현 기준으로 다음처럼 치환했다.

| 참고 이미지 요소 | ARENA 치환 |
| --- | --- |
| Route53 / 도메인 | `localhost:15173`, `localhost:18080` |
| EC2 서버 박스 | Docker Compose Host |
| React | Vue 3 + Vite |
| Spring Boot/JPA | Spring Boot 3 + MyBatis + Spring Security + Spring AI |
| RDS MySQL | `arena-mysql` MySQL 8.4 컨테이너 |
| S3/CodeDeploy/GitHub Actions | 현재는 미구현, Local → GitHub → Docker Compose build 흐름으로 표현 |
| 외부 인증/AI | Kakao OAuth API, GMS OpenAI-compatible API |

발표 시에는 `arena-architecture-reference-style.svg`를 메인 아키텍처 그림으로 사용하고, 세부 계층 설명이 필요하면 `arena-architecture.svg`를 보조 그림으로 사용한다.

# ARENA 기술 스택 구성 문서

## 목적

이 문서는 ARENA 프로젝트의 현재 구현 기준 기술 스택을 정리하고, 발표 자료에 사용할 수 있는 기술 스택 구성도를 정의한다. 사용자가 제공한 참고 이미지처럼 큰 영역별 박스와 연결 흐름을 사용하되, AWS 배포 기술이 아닌 현재 프로젝트의 실제 기술을 기준으로 작성했다.

## 기술 스택 요약

| 구분 | 기술 | 현재 역할 |
| --- | --- | --- |
| Frontend | Vue 3.5, Vite 6, Pinia 3, Vue Router 4, Axios, Naive UI | SPA 화면, 라우팅, 전역 상태 관리, REST API 호출, UI 컴포넌트 |
| Backend | Java 17, Spring Boot 3.5, Spring Security, Spring Validation, MyBatis 3.0.5, Spring AI 1.0.0 | REST API, 인증/인가, 비즈니스 로직, DB 접근, AI 연동 |
| Authentication | Kakao OAuth, JWT, JJWT 0.12.6 | Kakao 로그인, 서비스 access token 발급/검증 |
| AI | Spring AI OpenAI starter, GMS OpenAI-compatible API, `gpt-5.4-mini` | 세부 주제 후보 생성, 10턴 토론 생성, 토론 요약 생성 |
| Database | MySQL 8.4, MySQL Connector/J, MyBatis Mapper XML | 사용자/토론/게시글/댓글/투표/AI 로그 저장 |
| Infra | Docker Compose, Node 22 Alpine, Gradle 8.14 JDK 17, Eclipse Temurin 17 JRE | 로컬 개발/시연 컨테이너 실행, 백엔드 빌드, 프론트 개발 서버 |
| Test | Vitest 4, Vue Test Utils, jsdom, JUnit 5, Spring Boot Test, Spring Security Test, MyBatis Test | 프론트 컴포넌트/스토어/라우터 테스트, 백엔드 서비스/AI/보안 테스트 |
| API Docs | Springdoc OpenAPI UI | REST API 문서 확인 |

## Frontend

프론트엔드는 `client/`에 위치하며 Vue 3 기반 SPA로 구성된다.

| 기술 | 버전/근거 | 사용 위치 |
| --- | --- | --- |
| Vue | `^3.5.16` | 화면 컴포넌트와 Composition API |
| Vite | `^6.3.5` | 개발 서버, 번들링 |
| Pinia | `^3.0.3` | 인증/토론/게시글/댓글 상태 관리 |
| Vue Router | `^4.5.1` | `/auth`, `/new`, `/debates/:id`, `/posts` 등 라우팅 |
| Axios | `^1.9.0` | Spring Boot REST API 호출 |
| Naive UI | `^2.44.1` | 버튼, 카드, 입력창 등 UI 컴포넌트 |
| Vitest | `^4.1.8` | 프론트 테스트 |
| Vue Test Utils | `^2.4.6` | Vue 컴포넌트 테스트 |

## Backend

백엔드는 `server/`에 위치하며 Spring Boot 기반 REST API 서버다.

| 기술 | 버전/근거 | 사용 위치 |
| --- | --- | --- |
| Java | 17 | Gradle toolchain, Docker JDK/JRE |
| Spring Boot | `3.5.0` | 백엔드 애플리케이션 |
| Spring Security | Boot starter | JWT 보호 API, 권한 검사 |
| Spring Validation | Boot starter | Request DTO 검증 |
| Spring Web MVC | Boot starter | REST Controller |
| MyBatis Spring Boot | `3.0.5` | Mapper 인터페이스/XML 기반 DB 접근 |
| Spring AI | BOM `1.0.0`, OpenAI starter | GMS OpenAI-compatible API 호출 |
| JJWT | `0.12.6` | JWT 생성/검증 |
| Lombok | compile/annotation processor | 도메인/DTO 보일러플레이트 축소 |
| Springdoc OpenAPI | `2.8.8` | Swagger UI |

## AI / Prompt Stack

AI 연동은 브라우저가 직접 외부 AI를 호출하지 않고, Spring Boot 내부 계층에서 처리한다.

| 구성 | 책임 |
| --- | --- |
| `PromptTemplateLoader` | `server/src/main/resources/prompts`의 프롬프트 파일 로드 및 변수 치환 |
| `AiPipelineService` | 프롬프트 렌더링, Spring AI 호출, 응답 재시도 |
| `SpringAiClient` | Spring AI `ChatClient` 기반 provider 호출 |
| `AiResponseParser` | JSON 응답 파싱 및 계약 검증 |
| GMS API | `https://gms.ssafy.io/gmsapi/api.openai.com`, 모델 `gpt-5.4-mini` |

AI 결과는 단순 응답으로 끝나지 않고 DB에 보존된다.

| 테이블 | 저장 내용 |
| --- | --- |
| `ai_round_candidate_runs` | 후보 생성 실행 단위 |
| `ai_round_candidates` | 후보 5개 |
| `ai_prompt_call_logs` | 후보 생성 단계별 프롬프트/응답/파싱 로그 |
| `ai_debate_turn_logs` | 토론 발화 생성 프롬프트/응답/파싱 로그 |

## Database

MySQL은 Docker Compose의 `arena-mysql` 컨테이너로 실행된다. schema는 `server/src/main/resources/db/schema.sql`을 기준으로 한다.

| 영역 | 주요 테이블 |
| --- | --- |
| 사용자 | `users` |
| 토론 | `debate_sessions`, `debate_messages`, `debate_summaries` |
| 게시판 | `posts`, `comments`, `post_votes` |
| AI 로그 | `ai_round_candidate_runs`, `ai_round_candidates`, `ai_prompt_call_logs`, `ai_debate_turn_logs` |

## Infra / Runtime

| 구성 | 이미지/포트 | 설명 |
| --- | --- | --- |
| `arena-client` | `node:22-alpine`, `15173:15173` | Vite 개발 서버 |
| `arena-app` | `gradle:8.14-jdk17` 빌드, `eclipse-temurin:17-jre` 실행, `18080:8080` | Spring Boot API |
| `arena-mysql` | `mysql:8.4`, `13306:3306` | MySQL DB |
| Volumes | `arena-mysql-data`, `arena-client-node-modules` | DB 데이터 및 node_modules 보존 |

## 참고 이미지 반영 방향

사용자가 제공한 참고 이미지는 아이콘 중심의 배포/기술 구성도였다. ARENA 기술 스택 구성도는 같은 방식으로 다음 흐름을 표현한다.

1. `User / Browser`에서 프론트 요청이 들어온다.
2. `Frontend Stack`은 Vue/Vite/Pinia/Router/Axios/Naive UI로 구성된다.
3. `Backend Stack`은 Java/Spring Boot/Security/MyBatis/Spring AI로 구성된다.
4. `Data & AI Stack`은 MySQL, AI 로그 테이블, Kakao OAuth, GMS API로 나뉜다.
5. 하단에는 Docker Compose와 테스트 스택을 배치한다.

## 산출물

- 기술 스택 문서: `docs/architecture/arena-tech-stack-ko.md`
- 발표용 16:9 SVG: `docs/architecture/arena-tech-stack-slide.svg`
- 발표용 고해상도 PNG: `docs/architecture/arena-tech-stack-slide.png`
- 수정 가능한 원본 데이터: `docs/architecture/arena-tech-stack-slide.source.json`
- SVG/PNG 재생성 스크립트: `docs/architecture/generate_arena_tech_stack_assets.py`
- 사용 아이콘 출처 목록: `docs/architecture/arena-tech-stack-icon-sources.md`
- 이전 참고 스타일 SVG: `docs/architecture/arena-tech-stack-reference-style.svg`

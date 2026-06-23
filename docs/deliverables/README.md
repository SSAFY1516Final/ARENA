# ARENA 제출 산출물

이 디렉터리는 GitLab `pjt_spring/java_seoul_16_jaeyoung_minyong` 레포의 기존 ARENA 산출물을 가져온 뒤, 현재 프로젝트 기준으로 보정한 제출 문서 모음입니다.

현재 기준은 다음과 같습니다.

- 로그인은 카카오 OAuth만 사용합니다.
- 카카오 로그인 성공 후 자체 JWT를 발급해 서비스 API 인증에 사용합니다.
- 로그인 사용자는 상단 프로필에서 닉네임을 수정할 수 있습니다.
- 백엔드는 Spring Boot, Spring Security, MyBatis, Spring AI 베이스 환경을 사용합니다.
- 프론트엔드는 Vue 3, Vite, Pinia, Axios 기반입니다.
- 토론 생성 화면은 선택형 후보 파이프라인 흐름을 목업으로 보여줍니다.
- Spring AI 고도화 파이프라인은 본 브랜치에 확정 구현하지 않고, 팀원들이 비교 개발할 수 있도록 실험 브랜치에 분리합니다.

## 산출물 목록

| 구분 | 파일 | 제출 기준 |
| --- | --- | --- |
| 요구사항 정의서 | [requirements/arena-requirements.md](requirements/arena-requirements.md) | 서비스 목표, 기능/비기능 요구사항, 제외 범위 정리 |
| 유즈케이스 문서 | [use-cases/arena-use-cases.md](use-cases/arena-use-cases.md) | 비회원/회원/관리자/Kakao OAuth/AI 연동 계층 기준 정리 |
| 유즈케이스 다이어그램 | [use-cases/diagrams/arena-use-case-simple.mmd](use-cases/diagrams/arena-use-case-simple.mmd), [use-cases/diagrams/arena-use-case-ko.png](use-cases/diagrams/arena-use-case-ko.png) | Mermaid 원본과 PNG 산출물 포함 |
| ERD | [erd/arena-erd.md](erd/arena-erd.md), [erd/arena-erd.png](erd/arena-erd.png) | 현재 사용자 인증 모델과 주요 도메인 테이블 기준 |
| WBS | [wbs/arena-wbs.xlsx](wbs/arena-wbs.xlsx) | 카카오 로그인, 관리자 기능, Spring AI 베이스 환경 기준 |
| 간트차트 | [gantt/arena-gantt.xlsx](gantt/arena-gantt.xlsx) | 현재 개발 흐름 기준 |
| 화면설계서 | [screen-definition/figma-screen-definition.md](screen-definition/figma-screen-definition.md) | Vue 화면 구조와 주요 사용자 흐름 기준 |
| API 설계서 | [api/arena-rest-api.md](api/arena-rest-api.md) | 현재 REST API와 권한 정책 기준 |
| 최종 검증 기록 | [test-report.md](test-report.md) | 테스트, 빌드, 브라우저 확인 결과 요약 |

## 현재 구현 하이라이트

| 영역 | 반영 내용 |
| --- | --- |
| 인증 | Kakao OAuth authorization code를 백엔드에서 교환하고 자체 JWT를 발급 |
| 회원 | `GET /api/users/me`, `PATCH /api/users/me/nickname`으로 현재 사용자 프로필 관리 |
| 토론 생성 | 주제, 상황/조건, 세부 조건을 입력하고 검증 통과 후보를 선택하는 화면 목업 |
| AI | Spring AI 호출 기반은 유지하고, 선택형 후보 생성/검증/린트 파이프라인은 실험 브랜치에서 비교 |
| 커뮤니티 | 중단된 토론을 게시글로 공유하고 투표/댓글로 의견 수집 |
| 관리자 | 사용자 권한과 게시글 공개 여부 관리 |

## 우선 확인 순서

1. 루트 [README.md](../../README.md)에서 프로젝트 개요, 실행 방법, 제출 체크리스트를 확인합니다.
2. [requirements/arena-requirements.md](requirements/arena-requirements.md)에서 현재 기능 범위를 확인합니다.
3. [api/arena-rest-api.md](api/arena-rest-api.md), [erd/arena-erd.md](erd/arena-erd.md), [use-cases/arena-use-cases.md](use-cases/arena-use-cases.md)로 구현과 설계를 대조합니다.
4. WBS, 간트차트, 화면설계서는 제출용 보조 산출물로 확인합니다.

## 참고 사항

- Markdown 문서가 최신 기준입니다.
- PNG, Excel 등 정적 산출물은 제출 편의를 위해 포함되어 있으며, 내용이 충돌할 경우 Markdown 문서를 우선합니다.
- 선택형 주제 후보 생성, 주제 검증기, 턴 선택기 같은 고도화 AI 파이프라인은 `ai-experiment-choice-pipeline` 브랜치에 보존되어 있습니다.
- 현재 `/new` 화면의 후보 생성/검증/정렬은 데모용 목업이며, 실제 AI 파이프라인 API로 교체할 수 있도록 기존 토론 생성 API 계약은 유지합니다.

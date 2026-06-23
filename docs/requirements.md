# 요구사항 정의서

## 목표

Spring Boot 기반 REST API에 Spring Security, JWT, Kakao OAuth, Spring AI를 적용해 인증/인가가 가능한 AI 토론 커뮤니티 서비스를 구현한다.

## 사용자 요구사항

| ID | 요구사항 | 우선순위 |
| --- | --- | --- |
| REQ-USER-001 | 사용자는 카카오 계정으로 로그인할 수 있다. | Must |
| REQ-USER-002 | 로그인 성공 시 서비스 JWT를 발급받는다. | Must |
| REQ-USER-003 | 사용자는 토론 주제, 상황/조건, 세부 조건을 입력하고 후보를 선택해 토론을 생성할 수 있다. | Must |
| REQ-USER-004 | 사용자는 AI에게 다음 발화를 요청할 수 있다. | Must |
| REQ-USER-005 | 사용자는 토론을 종료하고 요약 결과를 받을 수 있다. | Must |
| REQ-USER-006 | 사용자는 요약 결과를 게시글로 공유할 수 있다. | Should |
| REQ-USER-007 | 사용자는 공개 게시글에 투표와 댓글을 남길 수 있다. | Should |
| REQ-USER-008 | 사용자는 본인 닉네임을 조회하고 수정할 수 있다. | Should |

## 관리자 요구사항

| ID | 요구사항 | 우선순위 |
| --- | --- | --- |
| REQ-ADMIN-001 | 관리자는 전체 사용자 목록을 조회할 수 있다. | Must |
| REQ-ADMIN-002 | 관리자는 사용자 권한을 USER 또는 ADMIN으로 변경할 수 있다. | Must |
| REQ-ADMIN-003 | 관리자는 게시글 공개 여부를 변경할 수 있다. | Should |

## AI 요구사항

| ID | 요구사항 | 우선순위 |
| --- | --- | --- |
| REQ-AI-001 | AI는 선택된 세부 토론 주제와 이전 발화 로그를 입력으로 받는다. | Must |
| REQ-AI-002 | AI는 서로 다른 관점의 두 페르소나를 번갈아 발화한다. | Must |
| REQ-AI-003 | AI 응답은 서버에서 파싱 가능한 JSON 구조로 반환되어야 한다. | Must |
| REQ-AI-004 | 토론 종료 시 공유 가능한 요약 정보를 생성한다. | Must |
| REQ-AI-005 | 선택형 후보 생성/검증/린트 파이프라인은 실험 브랜치에서 비교 후 확정한다. | Should |

## 비기능 요구사항

- JWT는 stateless 방식으로 처리한다.
- API는 역할 기반 접근 제어를 적용한다.
- OpenAI/Kakao 키는 환경 변수로만 관리한다.
- DB 접근은 MyBatis Mapper를 통해 수행한다.
- 로컬 실행은 Docker Compose 기반으로 재현 가능해야 한다.

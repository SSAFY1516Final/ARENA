# ARENA(아레나) 설계서

## 1. 개요

ARENA(아레나)는 사용자의 선택 고민과 상상 논쟁을 AI 토론으로 풀어주는 한국어 웹 커뮤니티 서비스입니다. 사용자는 실제 선택이 필요한 주제나 재미 위주의 대결 주제를 입력하고, 두 AI 페르소나의 논쟁을 실시간 채팅처럼 확인합니다. 토론이 충분히 유용하거나 재미있어진 시점에 사용자가 직접 멈추고, 요약 결과를 게시판에 공유할 수 있습니다.

이 서비스는 단순히 하나의 정답을 반환하는 챗봇이 아닙니다. 사용자가 결론을 받기 전에 서로 다른 관점을 비교하고, 그 과정을 콘텐츠처럼 소비하고 공유할 수 있게 만드는 것이 핵심입니다.

## 2. MVP 목표

MVP는 AI 프롬프트 데모가 아니라 회원, 게시판, 댓글, AI 토론 기능이 결합된 완성형 웹서비스를 목표로 합니다.

프로젝트 일정은 2026-05-15부터 2026-06-26까지이며, 계획, 설계, 구현, 테스트, 발표 준비까지 포함합니다.

MVP 포함 범위는 다음과 같습니다.

- 회원가입
- 로그인/JWT 인증
- 토론 주제 생성
- 토론 모드 선택
- AI 토론 턴 생성
- 토론 메시지 저장
- 사용자의 토론 중단
- 중단된 토론 요약 생성
- 게시판 공유
- 게시글 상세 조회
- 댓글 작성과 삭제

## 3. 사용자와 사용 사례

### 주요 사용자

- 사소하지만 은근히 결정을 미루는 사용자
- 밈성 질문이나 상상 대결을 즐기는 사용자
- 친구나 커뮤니티에 공유할 만한 토론 결과를 만들고 싶은 사용자

### 대표 사용 사례

- “제육 vs 돈까스”: 실용 판정 모드
- “아이폰 vs 갤럭시”: 실용성이 있지만 취향과 정체성도 섞인 주제
- “오타니 10명 vs 북극곰”: 예능 배틀 모드
- “선물 A vs 선물 B”: 감정 기준이 중요한 실용 판정 모드

## 4. 제품 결정 사항

| 항목 | 결정 |
| --- | --- |
| 제품 형태 | 웹앱 MVP |
| 모드 처리 | 토론 시작 전 사용자가 모드 선택 |
| 모드 | 실용 판정, 예능 배틀 |
| 페르소나 | MVP 고정 페르소나: 냉정파, 열정파 |
| 토론 형식 | 실시간 채팅형 |
| 종료 방식 | 사용자가 계속할지 멈출지 결정 |
| 기본 결과 | 강제 승자 판정이 아닌 요약 |
| 공유 | 중단된 토론을 게시판에 공유 |
| 게시판 표시 | 요약 카드 목록과 상세 페이지 |
| 댓글 | 일반 자유 댓글 |

## 5. 페르소나 설계

### 냉정파

냉정파는 현실성 중심으로 주장합니다. 주요 판단 기준은 다음과 같습니다.

- 실행 가능성
- 비용
- 리스크
- 효율
- 장기 결과
- 숨은 단점과 트레이드오프

### 열정파

열정파는 만족감 중심으로 주장합니다. 주요 판단 기준은 다음과 같습니다.

- 재미
- 감정적 보상
- 취향
- 추진력
- 몰입감
- 지금의 만족

사용자 커스텀 페르소나는 MVP 범위에서 제외합니다. 다만 추후 확장을 고려해 AI 프롬프트 계층은 서비스 로직과 분리합니다.

## 6. 토론 모드

### 실용 판정

실제 선택이 필요한 주제에 사용합니다. 토론은 유용하고 기준 중심이어야 합니다. 요약은 사용자가 각 선택지가 무엇을 최적화하는지 이해하도록 도와야 합니다.

### 예능 배틀

상상 대결, 밈성 질문, 말싸움형 주제에 사용합니다. 토론은 템포, 과장, 캐릭터성, 공유 가능성을 우선합니다. 요약은 가장 웃기거나 강한 장면을 살려야 합니다.

## 7. 기술 스택

| 영역 | 기술 |
| --- | --- |
| 화면 | JSP, HTML, CSS, Vanilla JavaScript |
| 메인 백엔드 | Java 17, Spring Boot 3.x |
| 인증/인가 | Spring Security, JWT |
| 데이터베이스 | MySQL |
| 영속성 | MyBatis Mapper XML |
| API 문서 | Swagger UI, springdoc-openapi |
| 보일러플레이트 | Lombok |
| AI 연동 | Spring AI |
| AI 제공자 | OpenAI API |

OpenAI API 키는 서비스 운영자가 제공합니다. 사용자는 AI 키를 입력하거나 관리하지 않습니다.

## 8. 아키텍처

```text
브라우저
  -> Spring Boot 3.x 메인 애플리케이션
      -> JSP 화면
      -> CSS / Vanilla JavaScript 정적 파일
      -> Spring Security + JWT
      -> Controller
      -> Service
      -> MyBatis Mapper XML
      -> MySQL
      -> Spring AI 연동 계층
          -> OpenAI API
```

Spring Boot는 서비스 상태, 회원, 인증/인가, 게시판, 댓글, 모든 데이터 저장을 담당합니다. AI 생성은 Spring AI 연동 계층이 담당하며, OpenAI API 키는 서버 환경변수로만 관리합니다.

## 9. 주요 흐름

1. 사용자가 회원가입 또는 로그인을 합니다.
2. 사용자가 토론 주제를 입력합니다.
3. 사용자가 실용 판정 또는 예능 배틀 모드를 선택합니다.
4. Spring이 토론 세션을 생성합니다.
5. 브라우저가 토론방을 엽니다.
6. 브라우저가 Spring에 다음 턴 생성을 요청합니다.
7. Spring이 현재 토론 문맥을 Spring AI에 전달합니다.
8. Spring AI가 다음 페르소나 발화와 선택적 정점 신호를 반환합니다.
9. Spring이 발화를 저장합니다.
10. 브라우저가 발화를 화면에 표시합니다.
11. 사용자가 한 라운드 더 진행하거나 토론을 멈춥니다.
12. 멈추면 Spring이 Spring AI에 요약 생성을 요청합니다.
13. Spring이 요약을 저장합니다.
14. 사용자가 멈춘 토론을 게시판에 공유합니다.
15. 다른 사용자가 검색과 필터로 게시글을 탐색합니다.
16. 다른 사용자가 게시글의 두 선택지 중 하나에 투표하고 비율을 확인합니다.
17. 다른 사용자가 게시글을 읽고 댓글을 남깁니다.

## 10. 화면

| 화면 | 주요 내용 | 로그인 필요 |
| --- | --- | --- |
| 홈 | 주제 입력, 모드 선택 | 아니오 |
| 회원가입 | 계정 생성 폼 | 아니오 |
| 로그인 | 로그인 폼 | 아니오 |
| 토론방 | 실시간 토론과 조작 버튼 | 예 |
| 토론 종료 화면 | 요약, 공유 버튼, 투표 선택지 입력 | 예 |
| 게시판 목록 | 공유된 토론 카드, 검색, 모드 필터, 정렬, 투표 비율 | 아니오 |
| 게시글 상세 | 요약, 전체 로그, 투표, 댓글 | 읽기 아니오 / 투표와 댓글 예 |

## 11. 데이터 모델

### users

- id
- login_id
- nickname
- password_hash
- created_at
- updated_at

### debate_sessions

- id
- user_id
- topic
- mode: PRACTICAL 또는 ENTERTAINMENT
- status: ACTIVE, STOPPED, SHARED
- peak_reached
- created_at
- stopped_at

### debate_messages

- id
- debate_session_id
- speaker: COOL_HEADED, PASSIONATE, SYSTEM
- round_no
- content
- created_at

### debate_summaries

- id
- debate_session_id
- core_arguments
- highlight
- decision_criteria
- remaining_issue
- summary_text
- created_at

### posts

- id
- debate_session_id
- user_id
- title
- summary_card
- vote_option_a
- vote_option_b
- is_public
- created_at
- updated_at

### comments

- id
- post_id
- user_id
- content
- created_at
- updated_at
- deleted_at

### post_votes

- id
- post_id
- user_id
- choice: A 또는 B
- created_at
- updated_at

## 12. Spring Boot API

### 인증

- `POST /api/auth/signup`
- `POST /api/auth/login`

### 토론

- `POST /api/debates`
- `POST /api/debates/{id}/turns`
- `POST /api/debates/{id}/stop`
- `POST /api/debates/{id}/share`

### 게시글

- `GET /api/posts`
- `GET /api/posts/{id}`
- `DELETE /api/posts/{id}`

### 투표

- `POST /api/posts/{id}/votes`

### 댓글

- `POST /api/posts/{id}/comments`
- `DELETE /api/comments/{id}`

## 13. Spring AI 생성 정책

Spring AI는 토론 턴 생성과 토론 요약 생성에 사용합니다. 브라우저가 직접 호출하는 별도 AI API는 제공하지 않으며, Spring 서버가 토론 문맥과 이전 메시지를 조합해 OpenAI API를 호출합니다.

생성 결과는 서버가 JSON 형태로 파싱한 뒤 토론 메시지와 요약 테이블에 저장합니다.

## 14. 보안

- Spring Security가 JWT Bearer Token 인증을 검증합니다.
- 비밀번호는 BCrypt로 저장합니다.
- 게시판 목록과 게시글 상세 읽기는 공개합니다.
- 토론 생성은 로그인이 필요합니다.
- 토론 공유는 로그인이 필요합니다.
- 댓글 작성은 로그인이 필요합니다.
- 게시글 삭제는 작성자만 가능합니다.
- 댓글 삭제는 작성자만 가능합니다.
- OpenAI API 키는 Spring 서버 환경변수에만 존재합니다.
- OpenAI API 키는 브라우저에 전달하지 않습니다.

## 15. 제한 정책

- 주제 길이 제한
- 댓글 길이 제한
- 토론 세션당 최대 라운드 제한
- 사용자 또는 세션 기준 생성 요청 제한
- AI 응답 타임아웃 처리
- 데이터베이스 장애 시 일반화된 오류 응답

## 16. 오류 처리

- Spring AI 호출이 실패하면 Spring이 AI 생성 실패 응답을 반환합니다.
- 실패한 AI 턴은 정상 토론 메시지로 저장하지 않습니다.
- 요약 생성이 실패하면 멈춘 토론은 재시도 가능한 상태로 유지합니다.
- 공유가 실패하면 멈춘 토론은 비공개 상태로 유지합니다.
- 데이터베이스 오류는 서버에 기록하고 사용자에게는 일반 오류로 표시합니다.

## 17. 테스트 범위

- 회원가입, 로그인, JWT 인증
- 접근 제어
- 토론 생성
- 실용 판정/예능 배틀 턴 생성
- 토론 메시지 저장
- 토론 중단과 요약 생성
- 게시판 공유
- 게시글 목록, 검색, 필터, 상세 조회
- 게시글 투표와 투표 비율 표시
- 댓글 작성과 삭제
- 작성자 권한 검사
- AI 서버 장애 처리
- MyBatis mapper 통합 테스트

## 18. MVP 제외 범위

- 사용자 커스텀 페르소나
- 사용자별 AI API 키
- 결제 또는 크레딧 시스템
- 페르소나형 댓글
- 자동 모드 분류만으로 진행하는 흐름
- 네이티브 모바일 앱
- 프론트엔드 프레임워크
- JPA

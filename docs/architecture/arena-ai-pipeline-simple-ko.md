# ARENA AI 파이프라인 중심 아키텍처

## 목표

발표자가 한 장의 그림으로 ARENA의 전체 구조를 설명할 수 있게 한다.
중심은 개발환경이 아니라 **AI 토론 생성 파이프라인**이다.

> 사용자가 주제를 입력하면, ARENA가 AI를 호출해 세부 주제와 토론 발화를 만들고, 결과를 DB에 저장한 뒤 화면에 보여주는 구조

## 한 줄 구조

`User Browser` → `Vue Client` → `Spring Boot API` → `GMS AI API` → `MySQL` → `Result Page`

## 핵심 구성

| 영역 | 역할 |
| --- | --- |
| User Browser | 주제 입력, 세부 주제 선택, 토론 진행, 결과 확인 |
| Vue Client | 화면 표시와 REST API 호출 |
| Spring Boot API | 인증, 토론 생성 요청 처리, AI 파이프라인 실행, DB 저장 |
| GMS OpenAI-compatible API | 세부 주제 후보, 토론 발화, 요약 생성 |
| MySQL | 사용자 입력, AI 응답, 토론 발화, 요약, 게시글 저장 |
| Kakao OAuth | 소셜 로그인 |

## AI 파이프라인

```text
1. 주제 입력
   사용자가 원본 질문을 입력한다.

2. 세부 주제 후보 생성
   Spring Boot가 GMS AI를 호출해 토론 가능한 세부 주제 후보를 만든다.

3. 세부 주제 선택
   사용자가 후보 중 하나를 선택한다.

4. 한 라운드 토론 생성
   선택한 세부 주제를 기준으로 AI가 양쪽 진영의 발화 10개를 생성한다.

5. 토론 결과 저장
   생성된 발화, 사용자 선택, 요약, AI 호출 로그를 MySQL에 저장한다.

6. 결과 페이지 표시
   저장된 데이터를 기반으로 라운드별 토론 기록과 요약을 보여준다.
```

## 다이어그램 구성 방향

기존 하단의 `Development / Demo Environment` 영역은 제거하거나 작게 줄인다.
그 자리에 `AI Debate Pipeline`을 배치한다.

발표용 그림은 다음 흐름이 가장 크게 보여야 한다.

```text
Topic Input
→ Candidate Generation
→ Round Debate Generation
→ Summary / Choice
→ DB Save
→ Result Page
```

## 다이어그램에 넣을 문구

| 단계 | 표시 문구 |
| --- | --- |
| 입력 | Topic Input |
| 후보 생성 | Topic Candidates |
| 토론 생성 | Debate Turns |
| 요약/선택 | Summary / User Choice |
| 저장 | Save Debate Data |
| 결과 | Result Page |

## 저장 데이터 표현

DB 박스에는 상세 테이블명을 넣지 않는다.
아래 정도만 표시한다.

- User Input
- AI Raw Response
- Debate Turns
- Round Summary
- Posts

## 발표 설명 예시

ARENA는 Vue 화면에서 사용자가 입력한 주제를 Spring Boot API로 전달한다.
백엔드는 GMS OpenAI-compatible API를 호출해 세부 주제 후보와 토론 발화를 생성하고,
생성 결과와 사용자 선택을 MySQL에 저장한다.
결과 페이지는 AI 응답을 다시 생성하지 않고 저장된 토론 데이터와 요약을 조회해 보여준다.

## 제외할 내용

한 장짜리 발표 그림에서는 아래 내용은 넣지 않는다.

- 내부 클래스명
- DB 테이블명
- 프롬프트 파일명
- 상세 API URL 목록
- Docker 개발 흐름 상세
- GitHub / build / run 흐름

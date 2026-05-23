# ARENA(아레나)

## Docker Quickstart

ARENA는 Docker Compose로 Spring Boot 애플리케이션과 MySQL을 함께 실행할 수 있습니다.

### 1. 환경변수 파일 생성

프로젝트 루트에서 예시 파일을 복사해 `.env`를 만듭니다.

```bash
cp .env.example .env
```

`.env`에는 반드시 아래 값을 설정합니다.

```env
OPENAI_API_KEY=sk-your-openai-api-key
JWT_SECRET=change-this-to-a-long-random-secret-key-32chars
```

필수 값:

| 변수 | 설명 |
| --- | --- |
| `OPENAI_API_KEY` | Spring AI가 OpenAI API를 호출할 때 사용하는 API 키입니다. |
| `JWT_SECRET` | JWT access token 서명에 사용하는 시크릿입니다. 최소 32자 이상의 임의 문자열을 권장합니다. |

선택 값:

| 변수 | 기본값 | 설명 |
| --- | --- | --- |
| `OPENAI_MODEL` | `gpt-4o-mini` | Spring AI가 사용할 OpenAI 모델명입니다. |
| `JWT_EXPIRATION_SECONDS` | `86400` | JWT 만료 시간입니다. 기본값은 24시간입니다. |
| `MYSQL_ROOT_PASSWORD` | `arena-root-password` | Docker MySQL root 비밀번호입니다. |
| `MYSQL_PORT` | `3306` | 호스트에 노출할 MySQL 포트입니다. |
| `APP_PORT` | `8080` | 호스트에 노출할 Spring Boot 포트입니다. |

`.env`는 git에 포함하지 않습니다.

### 2. Docker 실행

```bash
docker compose up --build
```

실행 후 접속 주소:

| 항목 | 주소 |
| --- | --- |
| API 서버 | `http://localhost:8080` |
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| MySQL | `localhost:3306` |

처음 MySQL 컨테이너가 생성될 때 `src/main/resources/db/schema.sql`이 자동 실행되어 기본 테이블이 생성됩니다.

### 3. 인증 흐름

회원가입:

```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"loginId":"user01","nickname":"토론러","password":"password123!"}'
```

로그인 후 JWT 발급:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"loginId":"user01","password":"password123!"}'
```

인증이 필요한 API는 응답받은 토큰을 `Authorization` 헤더에 넣어 호출합니다.

```http
Authorization: Bearer {accessToken}
```

### 4. 종료

```bash
docker compose down
```

DB 데이터까지 초기화하려면 볼륨도 함께 삭제합니다.

```bash
docker compose down -v
```

ARENA(아레나)는 사용자의 선택 고민과 상상 논쟁을 두 AI 페르소나의 토론으로 풀어주는 AI 논쟁 커뮤니티 서비스입니다. 사용자는 고민 주제를 입력하고 토론 모드를 선택한 뒤, 냉정파와 열정파가 번갈아 제시하는 주장을 확인할 수 있습니다. 토론이 충분히 유용하거나 재미있어진 시점에는 사용자가 직접 토론을 멈추고, 요약된 결과를 게시판에 공유할 수 있습니다.

ARENA는 단순히 하나의 정답을 제시하는 챗봇이 아니라, 서로 다른 관점이 충돌하고 정리되는 과정을 콘텐츠로 제공합니다. 선택을 돕는 실용적인 토론과 공유하기 좋은 예능형 논쟁을 함께 다루며, 사용자들은 게시글을 통해 다른 사람의 고민과 논쟁 결과를 탐색하고 의견을 남길 수 있습니다.

## 서비스 배경

일상에는 진지하게 조사하기에는 작지만 계속 결정을 미루게 되는 선택이 많습니다. 점심 메뉴, 선물, 구매 후보, 여행지, 일정처럼 가볍지만 쉽게 결론이 나지 않는 문제가 대표적입니다. 반대로 인터넷 커뮤니티에는 실제 결론보다 토론 과정의 재미와 공유 가능성이 더 중요한 상상 논쟁도 많습니다.

ARENA는 이 두 흐름을 하나의 서비스 안에서 다룹니다. 사용자는 실제 결정을 돕는 근거 중심 토론을 만들 수도 있고, 재미와 몰입을 위한 캐릭터 중심 논쟁을 만들 수도 있습니다. 토론 결과는 요약되어 게시판에 공유되며, 다른 사용자는 검색, 필터, 댓글, 투표를 통해 논쟁에 참여합니다.

## 핵심 가치

- 선택지를 다양한 관점에서 빠르게 비교할 수 있습니다.
- AI 토론의 과정을 콘텐츠처럼 읽고 공유할 수 있습니다.
- 게시글 투표를 통해 다른 사용자들의 선택 비율을 확인할 수 있습니다.
- 검색과 필터를 통해 관심 있는 논쟁을 쉽게 찾을 수 있습니다.
- 댓글을 통해 토론 결과에 대한 추가 의견을 나눌 수 있습니다.

## 주요 기능

### AI 토론

사용자는 토론 주제와 모드를 선택해 AI 토론을 시작합니다. 토론은 냉정파와 열정파라는 두 페르소나가 번갈아 발화하는 방식으로 진행됩니다. 냉정파는 근거, 조건, 현실성을 중심으로 판단하고, 열정파는 감정, 재미, 직관, 몰입감을 중심으로 주장을 전개합니다.

### 토론 모드

ARENA는 실용 판정과 예능 배틀 두 가지 모드를 제공합니다.

| 모드 | 설명 |
| --- | --- |
| 실용 판정 | 실제 선택을 돕기 위해 장단점, 조건, 리스크, 판단 기준을 중심으로 토론합니다. |
| 예능 배틀 | 재미와 공유 가능성을 높이기 위해 캐릭터성, 과장, 반박의 흐름을 강조합니다. |

### 토론 요약

사용자가 토론을 멈추면 핵심 주장, 하이라이트, 선택 기준, 남은 쟁점을 중심으로 토론 결과가 요약됩니다. 요약은 게시글 공유의 기반이 되며, 게시판에서는 긴 토론을 읽기 전에 핵심 내용을 먼저 파악할 수 있습니다.

### 커뮤니티 게시판

요약된 토론은 게시글로 공유할 수 있습니다. 게시글에는 토론 주제, 요약 카드, 양측 선택지, 토론 로그, 댓글, 투표 결과가 함께 제공됩니다. 사용자는 다른 사용자의 고민과 논쟁을 탐색하고, 의견을 남기거나 선택지에 투표할 수 있습니다.

### 투표와 비율 표시

게시글에는 두 개의 선택지가 제공됩니다. 사용자는 둘 중 하나에 투표할 수 있으며, 게시글에서는 각 선택지의 득표 수와 비율을 확인할 수 있습니다. 이미 투표한 사용자가 다시 투표하면 기존 선택이 변경되는 방식으로 동작합니다.

### 검색과 필터

게시글 목록에서는 키워드 검색, 토론 모드 필터, 정렬 기능을 제공합니다. 최신순, 댓글순, 투표순 정렬을 통해 사용자는 최근 논쟁, 활발한 논쟁, 많은 선택을 받은 논쟁을 빠르게 찾을 수 있습니다.

## 사용자 흐름

1. 사용자가 회원가입 후 로그인합니다.
2. 토론 주제와 토론 모드를 선택합니다.
3. 냉정파와 열정파의 AI 토론을 확인합니다.
4. 토론이 충분히 진행되면 사용자가 토론을 멈춥니다.
5. 토론 결과가 핵심 내용 중심으로 요약됩니다.
6. 사용자가 요약된 토론을 게시판에 공유합니다.
7. 다른 사용자가 게시글을 검색하거나 필터링해 탐색합니다.
8. 다른 사용자가 두 선택지 중 하나에 투표하고 비율을 확인합니다.
9. 댓글을 통해 토론 결과에 대한 의견을 나눕니다.

## 주요 데이터

| 데이터 | 설명 |
| --- | --- |
| 회원 | 서비스 이용자의 계정과 인증 정보 |
| 토론 세션 | 사용자가 생성한 AI 토론 단위 |
| 토론 메시지 | 냉정파, 열정파, 시스템 발화 로그 |
| 토론 요약 | 중단된 토론의 핵심 요약 결과 |
| 게시글 | 게시판에 공유된 토론 콘텐츠 |
| 댓글 | 게시글에 남기는 사용자 의견 |
| 투표 | 게시글의 두 선택지에 대한 사용자 선택 |

## 기술 구성

| 구분 | 기술 |
| --- | --- |
| Language | Java 17 |
| Backend | Spring Boot 3.x |
| Security | Spring Security, JWT |
| Persistence | MyBatis Mapper XML |
| Database | MySQL |
| API Docs | Swagger UI, springdoc-openapi |
| Boilerplate | Lombok |
| AI Integration | Spring AI |
| AI Provider | OpenAI API |

## 구조 개요

```text
Browser
  -> Spring Boot Application
      -> Controller
      -> Service
      -> MyBatis Mapper
      -> MySQL
      -> Spring AI Integration
      -> OpenAI API
```

Spring Boot 애플리케이션은 회원, JWT 인증, 토론 상태, 게시판, 댓글, 투표 데이터를 관리합니다. Spring AI는 OpenAI API를 호출해 AI 발화 생성과 요약 생성을 담당합니다.

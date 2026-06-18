# ARENA Client

ARENA Client는 사용자의 선택 고민과 상상 논쟁을 두 AI 페르소나의 토론으로 풀어주는 AI 논쟁 커뮤니티 서비스의 Vue 기반 화면 프로젝트입니다.

사용자는 토론 주제를 입력하고 `실용 판정` 또는 `예능 배틀` 모드를 선택합니다. 이후 냉정파와 열정파가 번갈아 제시하는 주장을 확인하고, 충분히 토론이 진행되면 `종료하기`로 요약 결과를 게시판에 공유할 수 있습니다.

이 프론트엔드는 단순한 챗봇 입력 화면이 아니라, 토론 생성, 실시간형 토론 경험, 요약 공유, 게시판 탐색, 투표, 댓글까지 하나의 흐름으로 이어지는 웹 애플리케이션을 목표로 합니다.

## Client Quickstart

현재 저장소는 Vue 3 + Vite 기반 목업 앱으로 구성되어 있습니다. 아래 명령어를 기준으로 로컬 개발 서버를 실행합니다.

### 1. 개발 환경

권장 환경은 다음과 같습니다.

| 항목 | 버전 |
| --- | --- |
| Node.js | 20 LTS 이상 |
| Package Manager | npm |
| Framework | Vue 3 |
| Bundler | Vite |

### 2. 의존성 설치

프로젝트 루트에서 의존성을 설치합니다.

```bash
npm install
```

### 3. 환경변수 파일 생성

프로젝트 루트에 `.env.local` 파일을 생성합니다.

```env
VITE_API_BASE_URL=http://localhost:8080
```

환경변수:

| 변수 | 설명 |
| --- | --- |
| `VITE_API_BASE_URL` | Spring Boot API 서버 주소입니다. |

프론트엔드는 Python AI 서버를 직접 호출하지 않습니다. AI 발화 생성과 요약 생성은 Spring Boot API를 통해 요청합니다.

### 4. 개발 서버 실행

```bash
npm run dev
```

기본 접속 주소:

| 항목 | 주소 |
| --- | --- |
| Vue 개발 서버 | `http://localhost:5173` |
| Spring Boot API | `http://localhost:8080` |

### 5. 빌드

```bash
npm run build
```

빌드 결과는 `dist/`에 생성됩니다.

### 6. 빌드 결과 미리보기

```bash
npm run preview
```

## 서비스 배경

일상에는 진지하게 조사하기에는 작지만 계속 결정을 미루게 되는 선택이 많습니다. 점심 메뉴, 선물, 구매 후보, 여행지, 일정처럼 가볍지만 쉽게 결론이 나지 않는 문제가 대표적입니다.

반대로 커뮤니티에는 실제 결론보다 토론 과정의 재미와 공유 가능성이 더 중요한 상상 논쟁도 많습니다. 예를 들어 “오타니 10명 vs 북극곰” 같은 주제는 객관적인 정답보다 말맛, 반박, 캐릭터성이 중요합니다.

ARENA는 이 두 흐름을 하나의 서비스 안에서 다룹니다. 사용자는 실제 결정을 돕는 근거 중심 토론을 만들 수도 있고, 재미와 몰입을 위한 캐릭터 중심 논쟁을 만들 수도 있습니다.

## 핵심 가치

- 선택지를 다양한 관점에서 빠르게 비교할 수 있습니다.
- AI 토론 과정을 채팅형 콘텐츠처럼 읽고 공유할 수 있습니다.
- 사용자가 직접 토론의 진행과 종료 시점을 제어할 수 있습니다.
- 게시판에 공유된 토론에서 다른 사용자들의 선택 투표 비율을 확인할 수 있습니다.
- 검색, 필터, 정렬을 통해 관심 있는 논쟁을 쉽게 찾을 수 있습니다.
- 댓글을 통해 토론 결과에 대한 추가 의견을 나눌 수 있습니다.

## 주요 기능

### 토론 생성

사용자는 카카오톡 mock 로그인 후 새 토론 화면에서 토론 주제를 입력하고 모드를 선택합니다.

| 모드 | 설명 |
| --- | --- |
| 실용 판정 | 실제 선택을 돕기 위해 장단점, 조건, 리스크, 판단 기준을 중심으로 토론합니다. |
| 예능 배틀 | 재미와 공유 가능성을 높이기 위해 캐릭터성, 과장, 반박의 흐름을 강조합니다. |

### AI 토론방

토론방은 냉정파와 열정파의 발화를 채팅 UI로 보여줍니다.

- 냉정파: 비용, 리스크, 효율, 실행 가능성, 장기 결과를 중심으로 주장합니다.
- 열정파: 재미, 감정적 보상, 취향, 즉시성, 몰입감을 중심으로 주장합니다.

냉정파와 열정파는 토론 발화 페르소나입니다. 퍼센트는 AI 페르소나의 우세 점수나 토론 중 판정값이 아니라, 토론이 게시판에 공유된 뒤 사용자들이 선택지 A/B에 투표한 결과입니다.

토론은 자동으로 무한 진행되지 않습니다. 사용자는 `한 라운드 더` 버튼으로 다음 발화를 요청하고, 충분히 유용하거나 재미있어진 시점에 `종료하기`를 선택합니다. AI가 논쟁의 정점을 감지하면 토론은 자동으로 종료 상태가 되고 추가 라운드 생성이 중단됩니다.

### 토론 요약

사용자가 종료하기를 누르거나 정점이 감지되면 서버가 AI 요약을 생성합니다.

요약에는 다음 정보가 포함됩니다.

| 항목 | 설명 |
| --- | --- |
| 핵심 주장 | 양측 페르소나가 주장한 주요 근거 |
| 하이라이트 | 토론에서 가장 인상적인 장면 |
| 선택 기준 | 사용자가 결정을 내릴 때 참고할 기준 |
| 남은 쟁점 | 아직 완전히 해소되지 않은 조건 |
| 요약 본문 | 게시글 상세와 요약 카드에 사용할 텍스트 |

### 게시판 공유

종료 이후 요약이 생성된 토론은 게시판에 공유할 수 있습니다. 공유 시 게시글 제목, 사용자 투표에 사용할 선택지 A/B, 공개 여부를 입력합니다.

하나의 토론은 하나의 게시글로만 공유됩니다.

### 게시판 탐색

게시판 목록에서는 공유된 토론을 카드 형태로 확인합니다.

제공 기능:

- 키워드 검색
- 실용 판정/예능 배틀 모드 필터
- 최신순, 댓글순, 투표순 정렬
- 게시판 사용자 투표 비율 표시
- 댓글 수 표시

### 게시글 상세

게시글 상세에서는 토론 요약, 전체 토론 로그, 투표, 댓글을 확인합니다.

로그인 사용자는 투표와 댓글 작성이 가능하며, 작성자는 본인 댓글이나 게시글을 삭제할 수 있습니다.

## 사용자 흐름

1. 사용자가 카카오톡 mock 로그인으로 시작합니다.
2. 내 토론 화면에서 기존 토론 카드를 확인하거나 `새 토론`으로 이동합니다.
3. 새 토론 화면에서 토론 주제와 모드를 선택합니다.
4. 토론방에서 냉정파와 열정파의 AI 토론을 확인합니다.
5. 사용자가 `한 라운드 더`로 토론을 진행합니다.
6. 충분히 진행되면 사용자가 `종료하기`를 선택하거나 AI가 정점을 감지해 토론을 자동 종료합니다.
7. 서버가 토론 요약을 생성합니다.
8. 사용자가 요약된 토론을 게시판에 공유합니다.
9. 다른 사용자가 게시글을 검색하거나 필터링해 탐색합니다.
10. 다른 사용자가 게시글에서 두 선택지 중 하나에 투표하고 비율을 확인합니다.
11. 댓글을 통해 토론 결과에 대한 의견을 나눕니다.

## 화면 구성

| 화면 | 경로 | 주요 기능 | 로그인 필요 |
| --- | --- | --- | --- |
| 첫 진입 | `/` | 로그인 화면으로 이동 | 아니오 |
| 카카오 로그인 | `/auth` | 카카오톡 mock 로그인 | 아니오 |
| 내 토론 | `/debates` | 내 토론 카드 목록, 새 토론 이동 | 예 |
| 새 토론 | `/new` | 주제 입력, 모드 선택, 토론 시작 | 예 |
| 토론방 | `/debates/:debateId` | AI 발화 목록, 한 라운드 더, 종료하기 | 예 |
| 토론 종료/요약 | `/debates/:debateId/summary` | 요약 확인, 게시판 공유 | 예 |
| 게시판 목록 | `/posts` | 검색, 필터, 정렬, 사용자 투표 비율 | 아니오 |
| 게시글 상세 | `/posts/:postId` | 요약, 전체 로그, 사용자 투표, 댓글 | 읽기 아니오 / 쓰기 예 |

## 기술 구성

| 구분 | 기술 |
| --- | --- |
| Framework | Vue 3 |
| Build Tool | Vite |
| Language | JavaScript |
| State Management | Pinia |
| Routing | Vue Router |
| HTTP Client | Axios |
| Styling | CSS Modules 또는 scoped CSS |
| API Server | Spring Boot |

## 구조 개요

```text
Browser
  -> Vue 3 Application
      -> Vue Router
      -> Pinia Stores
      -> Axios API Client
      -> Spring Boot API
          -> MySQL
          -> Python FastAPI AI Server
              -> OpenAI API
```

프론트엔드는 화면 상태와 사용자 상호작용을 담당합니다. 인증, 토론 생성, 게시글, 댓글, 투표 데이터는 Spring Boot API를 통해 처리합니다. Python AI 서버는 프론트엔드에서 직접 호출하지 않습니다.

## 권장 프로젝트 구조

```text
src/
  api/
    axios.js
    authApi.js
    debateApi.js
    postApi.js
    commentApi.js
  assets/
  components/
    common/
    debate/
    post/
  router/
    index.js
  stores/
    authStore.js
    debateStore.js
    postStore.js
  views/
    AuthView.vue
    MyDebatesView.vue
    HomeView.vue
    DebateRoomView.vue
    DebateSummaryView.vue
    PostListView.vue
    PostDetailView.vue
  App.vue
  main.js
```

## Pinia Store 설계

### authStore

사용자 인증 상태를 관리합니다.

상태:

- `user`
- `isAuthenticated`
- `loading`

주요 액션:

- `kakaoLogin()`
- `logout()`
- `fetchMe()`

### debateStore

토론 생성, 발화 생성, 종료, 요약 상태를 관리합니다.

상태:

- `currentDebate`
- `messages`
- `summary`
- `turnLoading`
- `stopLoading`

주요 액션:

- `createDebate(payload)`
- `fetchDebate(debateId)`
- `generateNextTurn(debateId)`
- `stopDebate(debateId)`
- `shareDebate(debateId, payload)`

### postStore

게시글 목록, 상세, 투표 상태를 관리합니다.

상태:

- `posts`
- `postDetail`
- `searchCondition`
- `pagination`
- `loading`

주요 액션:

- `fetchPosts(params)`
- `fetchPost(postId)`
- `vote(postId, payload)`
- `deletePost(postId)`

### commentStore

댓글 작성과 삭제를 관리합니다.

상태:

- `comments`
- `loading`

주요 액션:

- `createComment(postId, payload)`
- `deleteComment(commentId)`

## API 연동 기준

### 인증

| 기능 | Method | Endpoint |
| --- | --- | --- |
| 카카오 로그인 | POST | `/api/auth/kakao` |
| 로그아웃 | POST | `/logout` |

### 토론

| 기능 | Method | Endpoint |
| --- | --- | --- |
| 토론 생성 | POST | `/api/debates` |
| 다음 턴 생성 | POST | `/api/debates/{debateId}/turns` |
| 종료/요약 생성 | POST | `/api/debates/{debateId}/stop` |
| 게시판 공유 | POST | `/api/debates/{debateId}/share` |

### 게시글

| 기능 | Method | Endpoint |
| --- | --- | --- |
| 게시글 목록 | GET | `/api/posts` |
| 게시글 상세 | GET | `/api/posts/{postId}` |
| 게시글 삭제 | DELETE | `/api/posts/{postId}` |
| 투표 | POST | `/api/posts/{postId}/votes` |

### 댓글

| 기능 | Method | Endpoint |
| --- | --- | --- |
| 댓글 작성 | POST | `/api/posts/{postId}/comments` |
| 댓글 삭제 | DELETE | `/api/comments/{commentId}` |

## Axios 정책

Axios 인스턴스는 `src/api/axios.js`에서 관리합니다.

기본 정책:

- `baseURL`은 `VITE_API_BASE_URL`을 사용합니다.
- 세션 쿠키 기반 인증을 위해 `withCredentials: true`를 사용합니다.
- 401 응답은 로그인 화면으로 유도합니다.
- 403 응답은 권한 없음 메시지를 표시합니다.
- 502 응답은 AI 서버 실패로 안내하고 재시도 버튼을 제공합니다.

예시:

```javascript
import axios from 'axios'

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  withCredentials: true,
})
```

## UI 원칙

- 운영 도구처럼 과하게 장식하지 않고, 토론 콘텐츠를 읽기 쉽게 배치합니다.
- 첫 진입 화면은 카카오톡 로그인을 가장 먼저 보여줍니다.
- 내 토론 화면은 사용자가 만든 토론 주제를 카드형 목록으로 보여줍니다.
- 새 토론 화면은 주제 입력과 모드 선택을 가장 먼저 보여줍니다.
- 토론방은 채팅 UI를 사용하되, 사용자가 라운드를 명시적으로 제어하게 합니다.
- 게시판 카드는 요약, 모드, 사용자 투표 비율, 댓글 수를 한눈에 보여줍니다.
- 상세 화면은 요약과 사용자 투표를 먼저 보여주고, 전체 로그와 댓글은 아래 흐름으로 배치합니다.
- 로그인 필요 액션은 버튼 클릭 시 인증 화면으로 자연스럽게 유도합니다.

## 상태와 권한

### 토론 상태

| 상태 | 설명 | 가능한 액션 |
| --- | --- | --- |
| ACTIVE | 진행 중인 토론 | 다음 턴 생성, 종료하기 |
| STOPPED | 종료 후 요약이 생성된 토론 | 게시판 공유 |
| SHARED | 게시판에 공유된 토론 | 게시글 상세 조회 |

### 사용자 권한

| 사용자 | 가능 기능 |
| --- | --- |
| 비회원 | 게시판 목록/상세 조회, 카카오 로그인 |
| 회원 | 토론 생성, 공유, 투표, 댓글 작성 |
| 작성자 | 본인 게시글 삭제, 본인 댓글 삭제 |

## Mock 데이터 전략

백엔드 API 연결 전에는 화면 개발을 위해 mock 데이터를 사용합니다.

권장 방식:

- `src/mocks/`에 샘플 토론, 게시글, 댓글 데이터를 둡니다.
- Pinia action 내부에서 mock 응답과 실제 Axios 호출을 쉽게 교체할 수 있게 분리합니다.
- 화면 컴포넌트는 API 응답 형태와 같은 데이터 구조만 의존합니다.

## 개발 규칙

- 화면 단위 컴포넌트는 `views/`에 둡니다.
- 재사용 UI는 `components/common/`에 둡니다.
- 토론 전용 컴포넌트는 `components/debate/`에 둡니다.
- 게시판 전용 컴포넌트는 `components/post/`에 둡니다.
- API 호출은 컴포넌트에서 직접 하지 않고 store 또는 api 모듈을 통해 호출합니다.
- 라우팅 경로와 API 경로는 상수화해 중복을 줄입니다.
- 버튼, 카드, 입력 필드의 시각 규칙은 공통 CSS 변수로 관리합니다.

## 구현 현황

- Vue 3 + Vite 프로젝트 scaffold
- Vue Router 기반 8개 화면 라우팅
- Pinia store 기반 인증, 토론, 게시글, 댓글 mock 상태 관리
- Axios API 모듈 분리
- 고충실도 HTML 목업의 핵심 UI를 Vue 컴포넌트로 반영
- 게시판 검색, 모드 필터, 정렬 mock 동작
- 토론방 라운드 생성, 정점 감지 자동 종료, 공유 흐름 mock 동작
- Vitest 기반 라우터/store 테스트

## 향후 작업

- Spring Boot API와 실제 연동
- 실제 카카오 로그인과 세션 인증 상태 조회 API 연결
- 서버 검증 오류와 AI 서버 장애 UI 고도화
- 게시글 페이지네이션 구현
- 댓글 삭제와 게시글 삭제 UI 연결
- 화면별 접근성 점검과 반응형 QA

## 관련 문서

백엔드 및 기획 문서는 별도 저장소의 문서를 기준으로 합니다.

- 요구사항 정의서
- API 명세서
- DB 설계서
- 화면정의서
- 유스케이스 문서

프론트엔드 구현 시 위 문서의 API 경계와 상태 전이를 우선 기준으로 삼습니다.

# Codex Handoff

이 문서는 다른 컴퓨터에서 Codex로 이어서 작업할 때 보는 현재 상태 메모입니다.

## 현재 기준

- GitHub 저장소: `https://github.com/SSAFY1516Final/ARENA`
- 작업 브랜치: `WIP/refactor/ai-pipeline-separation`
- 프론트 폴더: `client/`
- 백엔드 폴더: `server/`
- 로컬 프론트 포트: `15173`
- 로컬 백엔드 포트: `18080` host -> `8080` container

## Codex 작업 규칙

- 코드, 프론트 기능, 백엔드 기능, 설정, Docker Compose, DB 스키마를 수정한 뒤에는 반드시 관련 Docker 컨테이너를 리프레시한다.
- 프론트 소스만 수정해도 Vite HMR이 변경을 놓칠 수 있으므로 `docker compose restart client` 후 브라우저가 실제 새 코드를 서빙하는지 확인한다.
- 백엔드 코드나 서버 설정을 수정하면 `docker compose up -d --build app`으로 이미지를 갱신한다.
- DB 스키마를 수정하면 기존 Docker volume에는 자동 반영되지 않으므로 필요한 `ALTER TABLE` 적용 여부를 확인한다.
- 완료 보고 전에는 `docker compose ps`와 필요한 포트/응답 확인까지 수행한다.

## 다른 컴퓨터에서 시작하기

```bash
git clone https://github.com/SSAFY1516Final/ARENA.git
cd ARENA
git fetch origin WIP/refactor/ai-pipeline-separation
git switch WIP/refactor/ai-pipeline-separation
```

GitHub remote 이름이 `origin`이 아닌 경우:

```bash
git remote -v
git fetch <github-remote-name> WIP/refactor/ai-pipeline-separation
git switch WIP/refactor/ai-pipeline-separation
```

## 환경 변수

루트 `.env.example`을 `.env`로 복사합니다.

```bash
cp .env.example .env
```

프론트는 `client/.env.example`을 `client/.env`로 복사합니다.

```bash
cp client/.env.example client/.env
```

필수 값:

- `GMS_KEY`
- `JWT_SECRET`
- `KAKAO_REST_API_KEY`
- `VITE_KAKAO_REST_API_KEY`

로컬 카카오 로그인 기준:

- `KAKAO_REDIRECT_URI=http://localhost:15173/auth/kakao/callback`
- `VITE_KAKAO_REDIRECT_URI=http://localhost:15173/auth/kakao/callback`
- Kakao Developers Web domain에 `http://localhost:15173` 등록
- Kakao Developers Redirect URI에 `http://localhost:15173/auth/kakao/callback` 등록

## 실행

Docker로 MySQL과 백엔드를 같이 실행:

```bash
docker compose up --build
```

프론트 실행:

```bash
cd client
npm install
npm run dev -- --host 0.0.0.0 --port 15173
```

브라우저:

```text
http://localhost:15173
```

## 검증 명령

프론트:

```bash
cd client
npm test -- --run
npm run build
```

백엔드:

```bash
cd server
./gradlew test
```

## 현재 반영된 큰 변경

- Kakao OAuth + JWT 기반 인증 흐름 유지
- 내 프로필 조회 및 닉네임 수정 API/UI 추가
- `/auth` 로그인 전 화면을 서비스 미리보기형으로 개선
- `/new` 선택형 세부 주제 후보 기반 토론 생성 UI로 정리
- `/debates/:id` 채팅형 토론 진행, 판단, 마무리, 게시판 공유 흐름 정리
- `/debates/:id/result` 결과 페이지 추가: 선택 후 결과 페이지로 이동하고, 세부주제 기준 라운드의 10개 발화를 다시 보며 선택한 라운드 기준으로 게시글을 여러 번 등록 가능
- 내 토론 카드 클릭은 기본적으로 `/debates/:id/result?from=recent`로 이동
- MVP 한계: 사용자가 선택한 진영은 현재 프론트 로컬 저장소(`arena.debate.choice.{debateId}`)로 복원하며, 추후 백엔드/DB 필드로 영속화 필요
- `/debates` 내 토론 카드 목업 티 나는 문구 제거
- Spring AI는 베이스 환경만 유지하고 고도화 파이프라인은 추후 분리 개발 전제로 정리
- 산출물 문서와 README를 현재 MVP 방향에 맞게 갱신

## 다음 작업 추천

1. Kakao 로그인 실계정으로 `/auth -> /new -> /debates/:id` 전체 흐름 재확인
2. `/new` 후보 생성은 `POST /api/ai/round-candidates` Spring AI API로 처리
   - 고정 5초 로딩은 제거하고, API 응답 도착 즉시 후보를 표시한다.
3. 토론 결과 페이지에서 라운드 선택, 새로운 토론 이동, 게시글 등록 후 `/posts/:id` 이동이 자연스럽게 이어지는지 QA
4. 모바일 폭에서 `/auth`, `/new`, `/debates/:id` 텍스트 줄바꿈과 버튼 배치 점검
5. 제출 전 `docs/deliverables/test-report.md`에 최종 실행 캡처 또는 검증 일시 추가

## 2026-06-24 Spring AI 후보 생성 진행 메모

- `/new` 후보 생성 목업은 실제 Spring AI 후보 생성 API로 교체됐다.
- 새 API는 `POST /api/ai/round-candidates`이며 인증이 필요하다.
- 후보 생성은 `topic-frame-generator.txt`, `topic-round-candidates-generator.txt`, `topic-round-candidates-validator.txt` 3단계 prompt를 사용한다.
- 후보 생성 요청, 렌더링된 프롬프트, raw 응답, 파싱 결과, 성공/실패 상태는 DB에 저장한다.
- 저장 테이블은 `ai_round_candidate_runs`, `ai_prompt_call_logs`이다.
- `/api/debates/{debateId}/turns`는 호환용 단일 턴 API로 유지한다.
- 새 토론방 자동 진행은 `/api/debates/{debateId}/turns/batch`를 사용하며, 이 API는 서버 백그라운드 작업을 시작하고 `GENERATING`/`COMPLETE` 상태를 반환한다. 실제 생성은 `debate-single-round-fast-generator.txt` 기반 GMS `gpt-5.4-mini` 호출 1회로 10턴을 만든다.
- 사용자가 토론방을 떠나도 서버 작업은 계속 저장을 진행한다. 다시 내 토론의 ACTIVE 토론으로 들어오면 `/debates/:id`가 열리고, 프론트는 detail polling으로 저장 완료를 확인한다.
- 대화 생성의 렌더링된 프롬프트, raw 응답, 파싱 결과, latency는 `ai_debate_turn_logs`에 저장한다.
- 서버 파서는 10턴, A 5개/B 5개, 홀수 A/짝수 B 순서를 검증하고, 10개 미만만 저장된 ACTIVE 토론은 부족한 발화를 보강한다.
- 라운드 기준은 세부주제다. 처음 선택한 세부주제에서 생성된 10개 발화는 모두 `round_no = 1`로 저장하고, 발화 순서는 메시지 `id`로 유지한다.
- 진영 선택 후 `selectedRoundNo`는 현재 세부주제 라운드인 `1`로 저장한다. 요약 API가 느려도 결과 페이지 이동은 선택 전환 시간 후 진행한다.

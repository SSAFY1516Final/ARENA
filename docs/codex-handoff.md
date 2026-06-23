# Codex Handoff

이 문서는 다른 컴퓨터에서 Codex로 이어서 작업할 때 보는 현재 상태 메모입니다.

## 현재 기준

- GitHub 저장소: `https://github.com/SSAFY1516Final/ARENA`
- 작업 브랜치: `WIP/refactor/ai-pipeline-separation`
- 프론트 폴더: `client/`
- 백엔드 폴더: `server/`
- 로컬 프론트 포트: `15173`
- 로컬 백엔드 포트: `8080`

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

- `OPENAI_API_KEY`
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
- `/debates` 내 토론 카드 목업 티 나는 문구 제거
- Spring AI는 베이스 환경만 유지하고 고도화 파이프라인은 추후 분리 개발 전제로 정리
- 산출물 문서와 README를 현재 MVP 방향에 맞게 갱신

## 다음 작업 추천

1. Kakao 로그인 실계정으로 `/auth -> /new -> /debates/:id` 전체 흐름 재확인
2. `/new` 후보 생성 목업을 실제 Spring AI 후보 생성 API로 교체
3. 토론 마무리 후 게시판 공유 결과가 `/posts/:id`에서 자연스럽게 이어지는지 QA
4. 모바일 폭에서 `/auth`, `/new`, `/debates/:id` 텍스트 줄바꿈과 버튼 배치 점검
5. 제출 전 `docs/deliverables/test-report.md`에 최종 실행 캡처 또는 검증 일시 추가

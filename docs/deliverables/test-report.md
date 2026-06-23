# ARENA 최종 검증 기록

## 검증 기준

본 문서는 제출 전 구현 상태를 확인하기 위한 테스트 로그 요약이다. 실제 API 키와 개인 토큰 값은 문서에 기록하지 않는다.

## 검증 환경

| 항목 | 값 |
| --- | --- |
| 날짜 | 2026-06-22 |
| Backend | Spring Boot, Java 17, MyBatis, Spring Security, Spring AI |
| Frontend | Vue 3, Vite, Pinia, Axios |
| DB | MySQL 8, Docker Compose |
| 로컬 프론트 | `http://localhost:15173` |
| 로컬 백엔드 | `http://localhost:18080` |

## 자동 테스트

### Backend

Command:

```bash
cd server
./gradlew test
```

Result:

```text
BUILD SUCCESSFUL
```

### Frontend

Command:

```bash
cd client
npm test -- --run
```

Result:

```text
Test Files  13 passed (13)
Tests       26 passed (26)
```

### Frontend Production Build

Command:

```bash
cd client
npm run build
```

Result:

```text
vite build completed successfully
```

### Diff Check

Command:

```bash
git diff --check
```

Result:

```text
No whitespace errors
```

## 브라우저 확인

| 화면 | 확인 내용 | 결과 |
| --- | --- | --- |
| `/auth` | 카카오 로그인 버튼, 로그인 전 소개 화면, 콘솔 에러 없음 | 통과 |
| `/auth/kakao/callback` | Kakao authorization code 처리 후 서비스 진입 | 통과 |
| `/debates` | 로그인 사용자 닉네임 표시 | 통과 |
| 상단 프로필 | 닉네임 수정 패널 열기, 저장, 원복 | 통과 |
| `/new` | 선택형 후보 파이프라인 목업, 후보 3개 표시, 실용/예능 문구 제거 | 통과 |

## 수동 확인 포인트

데모 전 다음 값을 로컬 환경에 설정한다.

```bash
JWT_SECRET=...
OPENAI_API_KEY=...
OPENAI_MODEL=gpt-4o-mini
KAKAO_REST_API_KEY=...
KAKAO_CLIENT_SECRET=...
KAKAO_REDIRECT_URI=http://localhost:15173/auth/kakao/callback
CORS_ALLOWED_ORIGINS=http://localhost:5173,http://127.0.0.1:5173,http://localhost:15173
```

Kakao Developers 콘솔에는 로컬 개발 포트에 맞춰 다음 값을 등록한다.

- Redirect URI: `http://localhost:15173/auth/kakao/callback`
- Web domain: `http://localhost:15173`

## 남은 리스크

| 항목 | 설명 | 대응 |
| --- | --- | --- |
| 선택형 후보 생성 | 현재 `/new` 후보 생성/검증/정렬은 프론트 목업 | 실제 AI 파이프라인은 실험 브랜치 비교 후 서버 API로 확정 |
| OpenAI 호출 | API 키와 과금 상태에 따라 실패 가능 | 데모 전 `.env`와 모델명 확인 |
| Kakao OAuth | Redirect URI와 실행 포트가 일치해야 함 | 데모 포트를 고정하고 Kakao 콘솔 값 재확인 |

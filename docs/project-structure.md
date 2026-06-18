# 프로젝트 구조 및 제출 체크리스트

## 권장 구조

```text
.
├── README.md
├── client
│   ├── package.json
│   └── src
├── server
│   ├── build.gradle.kts
│   ├── gradlew
│   ├── gradle
│   └── src
│       └── main
│           ├── java
│           │   └── com/ssafy/arena
│           │       ├── ai
│           │       ├── config
│           │       ├── controller
│           │       ├── domain
│           │       ├── dto
│           │       ├── mapper
│           │       ├── security
│           │       └── service
│           └── resources
│               ├── application.yml
│               ├── db/schema.sql
│               └── mapper
├── docs
│   ├── requirements.md
│   ├── auth-design.md
│   ├── ai-design.md
│   ├── api.md
│   └── project-structure.md
└── docker-compose.yml
```

## 계층 구조

| 계층 | 역할 |
| --- | --- |
| `server` | Spring Boot 백엔드 애플리케이션 |
| `client` | Vue 3 프론트엔드 애플리케이션 |
| Controller | REST API 엔드포인트, 요청 검증, 인증 사용자 주입 |
| Service | 비즈니스 로직, 트랜잭션, 권한 검증. 인증 흐름은 `AuthService`, 사용자 관리는 `UserService`로 분리 |
| Repository/Mapper | MyBatis 기반 DB 접근 |
| Security | JWT 발급/검증, SecurityFilterChain, Role 기반 인가 |
| AI | Spring AI ChatClient 호출, 프롬프트 관리, 응답 파싱 |

## 제출 체크리스트

### 필수 문서

- [x] README.md
- [x] 요구사항 정의서
- [x] 인증 설계
- [x] 인가 설계
- [x] Role 설계
- [x] AI 기능 설계
- [x] API 설명

### 구현 확인

- [ ] Spring Boot 서버 실행
- [ ] MySQL 연결
- [ ] 회원가입/로그인 또는 Kakao OAuth 로그인
- [ ] JWT 발급 및 인증 API 호출
- [ ] USER 권한 API 접근
- [ ] ADMIN 권한 API 접근 제한 확인
- [ ] Spring AI 발화 생성
- [ ] 토론 요약 생성

### 데모 확인

- [ ] Kakao Developers redirect URI 등록
- [ ] `.env` 설정
- [ ] Docker Compose 실행
- [ ] 브라우저 CORS 확인
- [ ] API 테스트 로그 또는 화면 캡처 준비

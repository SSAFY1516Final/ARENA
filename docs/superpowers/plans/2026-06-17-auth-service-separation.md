# Auth Service Separation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Separate authentication orchestration from user domain management without changing public auth APIs.

**Architecture:** Add `AuthService` as the application service for signup, local login, Kakao login, and JWT issuance. Keep `UserService` focused on user persistence, lookup, provider user creation, role management, and `UserDetailsService`.

**Tech Stack:** Java 17, Spring Boot 3, Spring Security, JWT, MyBatis, JUnit 5, Mockito.

---

### Task 1: Add AuthService Tests

**Files:**
- Create: `server/src/test/java/com/ssafy/arena/service/AuthServiceTest.java`

- [ ] Write tests proving local login returns a bearer JWT through `JwtTokenProvider`.
- [ ] Write tests proving Kakao login fetches a Kakao profile, finds or creates the user, and returns a bearer JWT.
- [ ] Run `cd server && ./gradlew test --tests com.ssafy.arena.service.AuthServiceTest` and confirm it fails because `AuthService` does not exist yet.

### Task 2: Implement AuthService

**Files:**
- Create: `server/src/main/java/com/ssafy/arena/service/AuthService.java`
- Modify: `server/src/main/java/com/ssafy/arena/service/UserService.java`

- [ ] Move auth orchestration into `AuthService`.
- [ ] Keep password verification delegated to `UserService.authenticateLocal`.
- [ ] Keep Kakao provider user creation delegated to `UserService.findOrCreateKakaoUser`.
- [ ] Run the new test and confirm it passes.

### Task 3: Wire Controller To AuthService

**Files:**
- Modify: `server/src/main/java/com/ssafy/arena/controller/AuthController.java`
- Modify: `server/src/test/java/com/ssafy/arena/service/UserServiceTest.java`

- [ ] Replace controller dependencies on `UserService`, `KakaoOAuthClient`, and `JwtTokenProvider` with `AuthService` for auth endpoints.
- [ ] Keep `/api/auth/signup`, `/api/auth/login`, `/api/auth/kakao`, and `/api/auth/logout` behavior unchanged.
- [ ] Update `UserServiceTest` names for local authentication responsibility.
- [ ] Run `cd server && ./gradlew test`.

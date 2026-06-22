# ARENA 산출물 모음

이 폴더는 GitLab `pjt_spring/java_seoul_16_jaeyoung_minyong` 레포에서 가져온 기존 ARENA 산출물을 현재 최종 프로젝트 기준으로 보정한 문서 모음이다.

## 포함 산출물

| 산출물 | 파일 | 현재 상태 |
| --- | --- | --- |
| 요구사항 정의서 | `requirements/arena-requirements.md` | 카카오 OAuth, JWT, 관리자 기능, Spring AI 베이스 정책 반영 |
| 유즈케이스 문서 및 다이어그램 | `use-cases/arena-use-cases.md` | 최신 Mermaid 원본 포함 |
| ERD | `erd/arena-erd.md`, `erd/arena-erd.png` | 텍스트 정의는 현재 DB 스키마 기준으로 보정 |
| WBS | `wbs/arena-wbs.xlsx`, `wbs/images/arena-wbs-clean-ko.png` | Excel 텍스트를 카카오 로그인/Spring AI 베이스 기준으로 보정 |
| 간트차트 | `gantt/arena-gantt.xlsx` | Excel 텍스트를 카카오 로그인/Spring AI 베이스 기준으로 보정 |
| 화면설계서 | `screen-definition/figma-screen-definition.md` | Vue 3, Kakao OAuth, 관리자 화면 기준으로 보정 |
| API 설계서 | `api/arena-rest-api.md` | 현재 컨트롤러 기준으로 보정 |

## 참고 사항

- 일부 이미지 산출물은 이전 버전에서 생성된 정적 이미지라, 최신 Mermaid/문서 내용과 100% 동일하지 않을 수 있다.
- 제출 시 최신 기준은 각 Markdown 문서를 우선으로 본다.
- 선택형 주제 후보 생성 등 고도화 AI 파이프라인은 `ai-experiment-choice-pipeline` 브랜치에 보존되어 있으며, 현재 `dev` 기준 산출물은 Spring AI 베이스 환경을 전제로 한다.

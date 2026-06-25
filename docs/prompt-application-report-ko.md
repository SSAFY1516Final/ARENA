# ARENA 프롬프트 적용 현황 정리

작성일: 2026-06-24

## 요약

동료가 테스트한 최신 `debate-single-round-fast-generator.txt`는 현재 프로젝트의 `server/src/main/resources/prompts/debate-single-round-fast-generator.txt`에 반영되어 있다. 이번 수정 전에는 이 프롬프트가 10개 발화를 한 번에 반환하도록 설계되어 있는데도, 서비스가 `/api/debates/{debateId}/turns`를 반복 호출하면서 매번 전체 10턴을 다시 생성하고 그중 한 턴만 저장했다. 그래서 오래 걸리고, 호출마다 결과가 달라져 동료 테스트 결과와 서비스 결과가 크게 달라질 수 있었다.

현재 새 토론 시작 흐름은 `POST /api/debates/{debateId}/turns/batch`를 한 번 호출해 서버 백그라운드 생성 작업을 시작한다. 서버는 브라우저 연결과 무관하게 10개 발화를 모두 생성해 DB에 저장한다. 프론트는 `GENERATING` 상태 동안 준비중 화면을 보여주고 `GET /api/debates/{debateId}`를 polling하다가, 저장된 10개 메시지가 확인되면 2초 입력중 표시와 함께 순차적으로 보여준다.

라운드 기준은 “세부주제”다. 사용자가 처음 선택한 세부주제에서 나온 10개 발화가 1라운드이며, 이 10개 메시지는 모두 `roundNo: 1`로 저장된다. 발화 순서는 `messageId` 오름차순으로 유지한다.

## 사용자가 제공한 자료와 현재 반영 위치

| 제공 자료 | 현재 프로젝트 반영 |
| --- | --- |
| `C:/Users/SSAFY/Downloads/debate-reproduction-package.md` | 재현 패키지의 전체 방향을 Spring AI 주제 후보 생성 및 토론 생성 흐름에 반영 |
| `C:/Users/SSAFY/Downloads/gd.zip` | 프로젝트 코드 생성 및 문서 갱신 참고 자료로 사용 |
| `C:/Users/SSAFY/Downloads/debate-single-round-fast-generator.txt` | `server/src/main/resources/prompts/debate-single-round-fast-generator.txt`로 적용 |
| `C:/Users/SSAFY/Downloads/topic-round-candidates-generator.txt` | `server/src/main/resources/prompts/topic-round-candidates-generator.txt`로 교체 적용. 단, 백엔드 DTO 계약에 맞게 각 후보의 `roundId` 필드 요구를 보강 |
| `C:/Users/SSAFY/Downloads/topic-round-candidates-validator.txt` | `server/src/main/resources/prompts/topic-round-candidates-validator.txt`로 교체 적용 |

## 현재 AI 파이프라인

1. 사용자가 `/new`에서 원본 주제를 입력한다.
2. `POST /api/ai/round-candidates`가 호출된다.
3. 서버는 다음 3개 프롬프트를 순서대로 사용한다.
   - `topic-frame-generator.txt`
   - `topic-round-candidates-generator.txt`
   - `topic-round-candidates-validator.txt`
4. 후보 생성 결과의 `sideA`, `sideB`, `debateAxis`, `sideAFrame`, `sideBFrame`, `roundTitle`, `basicConditions`가 토론 세션에 저장된다.
5. 사용자가 세부 주제를 선택하고 토론을 시작하면 `POST /api/debates/{debateId}/turns/batch`가 호출되어 서버 백그라운드 생성 작업이 시작된다.
6. 서버는 `debate-single-round-fast-generator.txt`를 렌더링해서 Spring AI를 한 번 호출한다.
7. AI가 반환한 `turns[10]`을 모두 `debate_messages`에 저장한다. 처음 선택한 세부주제의 10개 발화는 모두 `round_no = 1`인 1라운드다.
8. 같은 호출의 렌더링 프롬프트, raw 응답, 파싱 결과, latency를 `ai_debate_turn_logs`에 메시지별로 저장한다.

## 세부 후보 생성 프롬프트 교체 내용

2026-06-25 기준 `topic-round-candidates-generator.txt`와 `topic-round-candidates-validator.txt`는 사용자가 다시 제공한 예전 버전으로 교체했다.

이번 버전의 가장 큰 차이는 후보 카드의 `title` 정책이다. 기존 프로젝트 프롬프트는 `혼자 운동 vs 같이 운동`처럼 A/B 대립 구도가 드러나는 제목을 좋은 예시로 허용했다. 교체된 프롬프트는 `title`을 하나의 중립적인 세부주제 이름으로 제한하고, `vs`나 A/B 주장을 제목에 드러내지 못하게 한다. A/B의 실제 주장 방향은 `sideAFrame`, `sideBFrame`에만 들어가야 한다.

generator 원본에는 현재 백엔드가 사용하는 `roundId` 필드가 출력 예시에 없었다. 최종 validator가 `roundId`를 붙일 수는 있지만, 중간 파싱과 로그 분석의 안정성을 위해 generator 프롬프트에도 `roundId` 필드 요구를 추가했다.

## 토론 생성 프롬프트에 들어가는 실제 변수

현재 `AiPipelineService`는 최신 단일 라운드 프롬프트에 아래 값을 채운다.

| 변수 | 현재 값 |
| --- | --- |
| `mode` | 토론 세션의 `mode` |
| `bigTopic` | 선택된 세부 토론 주제 |
| `roundTitle` | 사용자가 선택한 세부 후보의 `title` |
| `coreQuestion` | 선택된 세부 토론 주제 |
| `debateAxis` | 후보 생성 단계에서 저장한 `debateAxis` |
| `sideA` | 후보 생성 단계의 `sideA`, 서비스 내부 매핑은 `COOL_HEADED` |
| `sideB` | 후보 생성 단계의 `sideB`, 서비스 내부 매핑은 `PASSIONATE` |
| `sideAFrame` | 선택 후보의 `sideAFrame` |
| `sideBFrame` | 선택 후보의 `sideBFrame` |
| `basicConditions` | `topic-frame-generator`가 만든 기본 조건 |
| `totalTurnCount` | `10` |
| `tone` | `COMMUNITY` |
| `creativityLevel` | `HIGH` |
| `evidencePolicy` | `OPTIONAL` |
| `evidenceSourcesJson` | 현재는 `[]` |

2026-06-25 추가 점검에서 실제 발화에 `이 라운드의 기준이야`처럼 내부 구성어가 노출되는 케이스를 확인했다. 원인은 대화 생성 프롬프트가 `selectedRound standard` 같은 내부 지시를 쓰면서도 사용자에게 보이는 `message` 값에서 `라운드`, `기준`, `프레임`, `축`, `프롬프트`, `스키마`, `JSON` 같은 내부 용어를 충분히 금지하지 않았기 때문이다. 현재는 `debate-single-round-fast-generator.txt`의 금지 문자열과 자체 점검 항목을 보강해, 발화 메시지에는 내부 라운드/프롬프트 구성어 대신 `보안과 데이터 통제`, `외부 의존`, `운영 부담`처럼 주제 고유의 쟁점이 직접 나오도록 했다.

## 결과 요약 프롬프트

결과 페이지의 요약은 `debate-single-round-fast-generator.txt`가 아니라 `server/src/main/resources/prompts/debate-summary-highlight-generator.txt`를 사용한다. 이 프롬프트는 토론 종료 API인 `POST /api/debates/{debateId}/stop`에서 Spring AI 호출에 사용되고, 결과는 `debate_summaries`에 저장된다.

2026-06-25 기준 요약 프롬프트는 Java 하드코딩에서 리소스 파일로 분리했다. 이후 피드백을 반영해 단순히 추상적인 판단 기준 하나만 압축하지 않고, `A가 지키려는 가치 vs B가 우려하는 비용`이 함께 보이도록 수정했다. 결과 페이지의 `summaryText`는 "A는 무엇을 위해 어떤 선택을 해야 한다고 주장했는지"와 "B는 어떤 조건이 해결되지 않으면 그 선택이 더 큰 문제를 만든다고 우려했는지"를 1문장으로 압축한다. 제도, 규제, 도입, 보류형 주제에서는 `언제 제도권에 편입할 것인가`, `어떤 조건 전에는 미뤄야 하는가`가 드러나도록 했다.

사용자에게 보이는 요약 필드 값에서는 내부 프롬프트/스키마/파이프라인 용어를 쓰지 말라는 규칙도 유지한다. 금지 대상은 `coreQuestion`, `selectedRound`, `debateAxis`, `sideAFrame`, `sideBFrame`, `topicFrame`, `candidate`, `prompt`, `JSON`, `schema`, `프롬프트`, `스키마`, `필드`, `라운드`, `프레임`, `축`, `조건상`, `기준상` 등이다. JSON key 이름은 응답 계약상 유지하지만, 실제 `coreArguments`, `highlight`, `decisionCriteria`, `remainingIssue`, `summaryText` 값에는 자연스러운 한국어만 들어가도록 지시한다.

## 결과가 기존 테스트와 달라질 수 있었던 이유

가장 큰 원인은 호출 방식이었다. 프롬프트는 10턴 전체를 한 번에 만들도록 설계되었지만, 기존 서비스는 턴마다 같은 프롬프트를 다시 호출했다. LLM은 호출마다 다른 10턴 세트를 생성할 수 있고, 서버는 그중 특정 turnIndex만 골라 저장했기 때문에 최종 대화가 하나의 일관된 라운드가 아니라 여러 번 생성된 결과의 조각이 될 수 있었다.

두 번째 이유는 입력 컨텍스트 차이다. 최신 프롬프트는 `subjectProfile`, `userProvidedFacts`, `evidenceSourcesJson` 같은 구체 정보가 있으면 주제 고유의 사실과 조건을 적극 활용하도록 설계되어 있다. 현재 프로젝트는 아직 별도 증거 수집 또는 사용자 제공 사실 주입 파이프라인이 없어서 `evidenceSourcesJson`이 빈 배열이다. 따라서 AI는 실제 기록, 최근 사례, 수치, 이름 있는 대안 등을 만들지 못하고, 선택된 주제와 후보 프레임 안에서만 논리적으로 말해야 한다.

세 번째 이유는 후보 생성 단계가 원본 주제를 한 번 재구성한다는 점이다. 사용자가 입력한 원문이 바로 토론 생성 프롬프트로 들어가는 것이 아니라, `topic-frame-generator`와 후보 생성/검증 단계를 거쳐 `coreQuestion`, `debateAxis`, `sideAFrame`, `sideBFrame`로 바뀐다. 이 과정에서 동료가 테스트한 원본 입력과 서비스의 실제 렌더링 입력이 달라질 수 있다.

네 번째 이유는 라운드 번호 해석 문제였다. 프롬프트의 `turnIndex`는 1~10 발화 순서인데, 서비스 일부가 이를 DB의 `roundNo`로 저장했다. 그 결과 결과 페이지는 10개 발화를 1라운드로 보지 못하고 여러 라운드로 쪼개서 표시할 수 있었다. 현재는 세부주제 기준 1라운드로 정리해 10개 발화 모두 `roundNo: 1`로 저장한다.

## 이번 수정으로 바뀐 점

- 새 백엔드 엔드포인트 `POST /api/debates/{debateId}/turns/batch` 추가
- `AiClient.generateRound()` 추가
- `AiResponseParser.parseRound()` 추가
- AI 응답의 `turns[]` 전체를 `AiGeneratedTurnResponse` 목록으로 파싱
- `side: "A"`는 `COOL_HEADED`, `side: "B"`는 `PASSIONATE`로 매핑
- `DebateService.generateInitialTurns()`에서 AI 1회 호출 후 모든 메시지 저장
- 이미 10개 메시지가 있는 토론은 AI를 다시 호출하지 않고 기존 DB 메시지 반환
- 10개 미만만 저장된 기존 ACTIVE 토론은 기존 메시지를 보존하고 부족한 발화만 배치 응답에서 보강
- 프론트는 `GENERATING` 동안 준비중 표시를 유지하고 detail polling으로 DB 저장 완료를 확인
- 저장된 10개 메시지가 확인된 뒤 2초 입력중 표시와 함께 순차 공개
- 세부주제 하나에서 생성된 10개 발화를 모두 같은 1라운드(`roundNo: 1`)로 저장
- 사용자가 진영을 선택하면 현재 세부주제 라운드인 `selectedRoundNo: 1`을 저장
- 요약 생성이 늦어도 결과 페이지 이동은 선택 전환 시간 후 진행
- `AiResponseParser.parseRound()`가 10턴, A 5개/B 5개, A/B 교대 순서를 강제 검증

## DB 보존 항목

| 테이블 | 저장 내용 |
| --- | --- |
| `debate_sessions` | 원본 주제, 선택 주제, A/B 라벨, debateAxis, sideAFrame, sideBFrame, selectedRoundId, roundTitle, basicConditions, 선택한 진영과 라운드 |
| `debate_messages` | 실제 화면에 표시되는 10개 AI 발화 |
| `ai_debate_turn_logs` | 렌더링된 토론 생성 프롬프트, raw AI 응답, 파싱 결과 JSON, latency |
| `ai_round_candidate_runs` | 주제 후보 생성 run 상태와 최종 응답 |
| `ai_prompt_call_logs` | 후보 생성 단계별 프롬프트, raw 응답, 파싱 결과, latency |

## 아직 남은 한계

- `evidenceSourcesJson`이 아직 빈 배열이라 실제 기록이나 출처 기반 발화는 나오지 않는다.
- 원본 테스트처럼 특정 인물/팀/제품의 상세 사실을 넣으려면 subject profile 또는 evidence context 주입 단계가 추가로 필요하다.
- 후보 생성 단계가 원본 주제를 재작성하므로, 동료 테스트 프롬프트에 직접 넣은 입력과 서비스 입력은 동일하지 않을 수 있다.

# Spring AI 기능 설계

## 목표

ARENA의 AI 기능은 사용자의 선택 고민을 토론 가능한 구조로 바꾸고, 두 페르소나가 서로 다른 관점에서 논리를 전개하도록 돕는다.

## Provider

- 기본 Provider: OpenAI API
- Spring 연동: Spring AI `ChatClient`
- 모델: 환경 변수 `OPENAI_MODEL`로 지정, 기본값 `gpt-4o-mini`

## AI 발화 생성

### 입력

| 필드 | 설명 |
| --- | --- |
| `topic` | 토론 주제 |
| `mode` | PRACTICAL 또는 ENTERTAINMENT |
| `roundNo` | 현재 라운드 번호 |
| `previousMessages` | 이전 발화 목록 |

### 출력

```json
{
  "content": "다음 발화 내용",
  "peakReached": false
}
```

### 페르소나 규칙

| Speaker | 역할 |
| --- | --- |
| COOL_HEADED | 근거, 비용, 리스크, 실용성을 중심으로 발화 |
| PASSIONATE | 만족감, 몰입감, 재미, 경험 가치를 중심으로 발화 |

라운드 번호에 따라 두 발화자가 번갈아 등장한다.

## AI 요약 생성

토론 종료 시 게시글 공유에 필요한 요약 데이터를 생성한다.

```json
{
  "coreArguments": "핵심 주장",
  "highlight": "가장 인상적인 대립점",
  "decisionCriteria": "판단 기준",
  "remainingIssue": "남은 쟁점",
  "summaryText": "게시글 공유용 요약"
}
```

## 예외 처리

- AI 응답이 JSON으로 파싱되지 않으면 `502 Bad Gateway`로 처리한다.
- API 키가 없거나 Provider 호출에 실패하면 사용자에게 재시도 안내 메시지를 반환한다.
- 프롬프트는 서버에서 관리해 클라이언트가 AI 시스템 지시문을 직접 조작하지 못하게 한다.

## 향후 개선

- AI 응답 스트리밍
- 사용자 선호도 기반 페르소나 조정
- 토론 품질 점수화
- 요약 결과의 금칙어/정책 필터링

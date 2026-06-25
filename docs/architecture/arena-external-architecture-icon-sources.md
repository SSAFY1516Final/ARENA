# ARENA External Architecture Icon Sources

이 다이어그램은 발표자료용 외부 아키텍처 설명에 맞춰 실제 기술 스택 아이콘을 SVG로 임베드한다.
대부분의 아이콘은 Simple Icons CDN에서 받은 SVG를 사용했고, OpenAI 자체 서비스처럼 보이지 않도록 GMS 영역은 별도 `AI` 텍스트 배지로 처리했다.

## 사용 아이콘

| 영역 | 사용 방식 | 출처 |
| --- | --- | --- |
| User Browser | Google Chrome SVG 아이콘 | https://cdn.simpleicons.org/googlechrome |
| Vue Client | Vue.js SVG 아이콘 | https://cdn.simpleicons.org/vuedotjs |
| Vue Client | Vite SVG 아이콘 | https://cdn.simpleicons.org/vite |
| Spring API | Spring Boot SVG 아이콘 | https://cdn.simpleicons.org/springboot |
| Spring API | Spring Security SVG 아이콘 | https://cdn.simpleicons.org/springsecurity |
| Spring API | JWT SVG 아이콘 | https://cdn.simpleicons.org/jsonwebtokens |
| Database | MySQL SVG 아이콘 | https://cdn.simpleicons.org/mysql |
| External Services | Kakao SVG 아이콘 | https://cdn.simpleicons.org/kakao |
| Docker Compose | Docker SVG 아이콘 | https://cdn.simpleicons.org/docker |
| Development Flow | GitHub SVG 아이콘 | https://cdn.simpleicons.org/github |
| GMS OpenAI-compatible API | OpenAI 로고 대신 커스텀 `AI` 텍스트 배지 | 자체 제작 |

## 참고 링크

- Simple Icons: https://simpleicons.org/
- Docker media resources: https://www.docker.com/company/newsroom/media-resources/
- GitHub brand: https://brand.github.com/foundations/logo
- Kakao Login design guide: https://developers.kakao.com/docs/en/kakaologin/design-guide
- MySQL logos: https://www.mysql.com/about/legal/logos.html
- OpenAI brand guide: https://openai.com/brand/

## 메모

- 다이어그램의 라벨은 `GMS OpenAI-compatible API`로 고정해 OpenAI 본 서비스와 혼동되지 않게 했다.
- 내부 클래스명, 테이블명, 프롬프트 파일명은 발표용 외부 아키텍처 범위에서 제외했다.
- SVG 산출물은 아이콘 파일을 base64 data URI로 포함하므로 발표자료에 단독 삽입해도 아이콘이 깨지지 않는다.

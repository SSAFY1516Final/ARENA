package com.ssafy.arena.ai;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SummaryPromptContractTest {
    private final PromptTemplateLoader loader = new PromptTemplateLoader();

    @Test
    void summaryPromptIsFileBasedAndSummarizesActualDebateSituationWithoutRepeatingTitle() {
        String prompt = loader.load("debate-summary-highlight-generator.txt");

        assertThat(prompt)
                .contains("결과 페이지에 보여줄 핵심 하이라이트")
                .contains("끝까지 싸운 축")
                .contains("A가 지키려는 가치 vs B가 우려하는 비용")
                .contains("서론을 쓰지 않는다")
                .contains("A는 무엇을 주장했고 B는 무엇을 우려했는지")
                .contains("A는 ○○를 위해 △△해야 한다고 주장했고, B는 □□가 해결되지 않은 상태에서는 △△가 더 큰 문제를 만든다고 맞섰다")
                .contains("언제 제도권에 편입할 것인가")
                .contains("도입파는 제도권 편입을 통해 공시와 감시 체계를 갖춰야 한다고 주장했고, 반대파는 규제 검증 전 편입은 혼선과 책임 공백을 키운다고 맞섰다")
                .contains("1문장")
                .contains("55자 이상 120자 이하")
                .contains("summaryText");
    }

    @Test
    void summaryPromptForbidsInternalPromptAndSchemaTermsInUserFacingFields() {
        String prompt = loader.load("debate-summary-highlight-generator.txt");

        assertThat(prompt)
                .contains("사용자에게 보이는 값")
                .contains("coreArguments")
                .contains("summaryText")
                .contains("debateAxis")
                .contains("sideAFrame")
                .contains("sideBFrame")
                .contains("topicFrame")
                .contains("candidate")
                .contains("prompt")
                .contains("JSON")
                .contains("schema")
                .contains("프롬프트")
                .contains("스키마")
                .contains("필드")
                .contains("라운드")
                .contains("프레임")
                .contains("축")
                .contains("조건상")
                .contains("기준상");
    }
}

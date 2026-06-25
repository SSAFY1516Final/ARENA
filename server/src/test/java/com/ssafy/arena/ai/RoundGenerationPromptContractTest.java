package com.ssafy.arena.ai;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RoundGenerationPromptContractTest {
    private final PromptTemplateLoader loader = new PromptTemplateLoader();

    @Test
    void debateMessagesForbidInternalRoundAndPromptTerms() {
        String prompt = loader.load("debate-single-round-fast-generator.txt");

        assertThat(prompt)
                .contains("User-facing message values must not expose internal prompt, schema, or debate-construction terms.")
                .contains("\"이 라운드\"")
                .contains("\"라운드의 기준\"")
                .contains("\"기준이야\"")
                .contains("\"selectedRound\"")
                .contains("\"sideAFrame\"")
                .contains("\"sideBFrame\"")
                .contains("\"프롬프트\"")
                .contains("\"스키마\"")
                .contains("\"프레임\"");
    }
}

package com.ssafy.arena.ai;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RoundCandidatePromptContractTest {
    private final PromptTemplateLoader loader = new PromptTemplateLoader();

    @Test
    void candidateGeneratorRequiresNeutralMicroTopicTitlesAndRoundIds() {
        String prompt = loader.load("topic-round-candidates-generator.txt");

        assertThat(prompt)
                .contains("The user-facing `title` must be one neutral micro-topic title.")
                .contains("Do not use `vs` in the `title`.")
                .contains("\"roundId\": \"R1\"");
    }

    @Test
    void candidateValidatorKeepsNeutralMicroTopicTitlesAndProjectOutputShape() {
        String prompt = loader.load("topic-round-candidates-validator.txt");

        assertThat(prompt)
                .contains("The `title` must be one neutral micro-topic title.")
                .contains("Do not use `vs` in titles.")
                .contains("\"topCandidates\"")
                .contains("\"roundId\": \"R1\"")
                .contains("\"rejectedCandidates\"");
    }
}

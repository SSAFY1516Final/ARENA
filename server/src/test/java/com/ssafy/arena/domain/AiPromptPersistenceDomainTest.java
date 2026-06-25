package com.ssafy.arena.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AiPromptPersistenceDomainTest {
    @Test
    void roundCandidateRunStoresRequestAndResultMetadata() {
        AiRoundCandidateRun run = AiRoundCandidateRun.builder()
                .id(10L)
                .userId(7L)
                .originalTopic("lunch menu")
                .mode(DebateMode.PRACTICAL)
                .candidateCount(5)
                .status("STARTED")
                .topicFrameJson("{\"isDebatable\":true}")
                .finalResponseJson("{\"roundCandidates\":[]}")
                .errorMessage(null)
                .build();

        assertThat(run.getUserId()).isEqualTo(7L);
        assertThat(run.getOriginalTopic()).isEqualTo("lunch menu");
        assertThat(run.getMode()).isEqualTo(DebateMode.PRACTICAL);
        assertThat(run.getCandidateCount()).isEqualTo(5);
        assertThat(run.getStatus()).isEqualTo("STARTED");
        assertThat(run.getTopicFrameJson()).contains("isDebatable");
        assertThat(run.getFinalResponseJson()).contains("roundCandidates");
    }

    @Test
    void promptCallLogStoresRenderedPromptAndProviderResponse() {
        AiPromptCallLog log = AiPromptCallLog.builder()
                .id(11L)
                .runId(10L)
                .stage("TOPIC_FRAME")
                .promptName("topic-frame-generator.txt")
                .model("gpt-5.4-mini")
                .renderedPrompt("rendered prompt")
                .rawResponse("{\"isDebatable\":true}")
                .parsedResponseJson("{\"isDebatable\":true}")
                .status("SUCCEEDED")
                .latencyMs(123L)
                .build();

        assertThat(log.getRunId()).isEqualTo(10L);
        assertThat(log.getStage()).isEqualTo("TOPIC_FRAME");
        assertThat(log.getPromptName()).isEqualTo("topic-frame-generator.txt");
        assertThat(log.getRenderedPrompt()).isEqualTo("rendered prompt");
        assertThat(log.getRawResponse()).contains("isDebatable");
        assertThat(log.getStatus()).isEqualTo("SUCCEEDED");
        assertThat(log.getLatencyMs()).isEqualTo(123L);
    }
}

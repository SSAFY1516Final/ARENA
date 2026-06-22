package com.ssafy.arena.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.arena.common.ApiException;
import com.ssafy.arena.domain.Speaker;
import com.ssafy.arena.dto.ai.AiNextTurnResponse;
import com.ssafy.arena.dto.ai.AiSummaryResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class AiResponseParserTest {
    private final AiResponseParser parser = new AiResponseParser(new ObjectMapper());

    @Test
    void parsesNextTurnJsonWithDefaultPeakValue() {
        AiNextTurnResponse response = parser.parseNextTurn(
                "{\"content\":\"돈까스는 실패 확률이 낮습니다.\"}",
                Speaker.COOL_HEADED,
                false
        );

        assertThat(response.speaker()).isEqualTo(Speaker.COOL_HEADED);
        assertThat(response.content()).isEqualTo("돈까스는 실패 확률이 낮습니다.");
        assertThat(response.peakReached()).isFalse();
    }

    @Test
    void parsesSummaryJson() {
        AiSummaryResponse response = parser.parseSummary("""
                {
                  "coreArguments": "안정성과 만족감의 대립",
                  "highlight": "오후 일정과 보상 심리",
                  "decisionCriteria": "업무 집중이 중요하면 안정성",
                  "remainingIssue": "개인 취향",
                  "summaryText": "돈까스는 안정성, 제육은 만족감입니다."
                }
                """);

        assertThat(response.coreArguments()).isEqualTo("안정성과 만족감의 대립");
        assertThat(response.highlight()).isEqualTo("오후 일정과 보상 심리");
        assertThat(response.decisionCriteria()).isEqualTo("업무 집중이 중요하면 안정성");
        assertThat(response.remainingIssue()).isEqualTo("개인 취향");
        assertThat(response.summaryText()).isEqualTo("돈까스는 안정성, 제육은 만족감입니다.");
    }

    @Test
    void malformedNextTurnJsonBecomesBadGateway() {
        assertThatThrownBy(() -> parser.parseNextTurn("{bad-json", Speaker.PASSIONATE, true))
                .isInstanceOf(ApiException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.BAD_GATEWAY);
    }
}

package com.ssafy.arena.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.arena.common.ApiException;
import com.ssafy.arena.domain.Speaker;
import com.ssafy.arena.dto.ai.AiGeneratedTurnResponse;
import com.ssafy.arena.dto.ai.AiNextTurnResponse;
import com.ssafy.arena.dto.ai.RoundCandidateResponse;
import com.ssafy.arena.dto.ai.TopicFrameResponse;
import com.ssafy.arena.dto.ai.AiSummaryResponse;
import java.util.List;
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

    @Test
    void parsesNextTurnFromFastRoundTurnsArray() {
        AiNextTurnResponse response = parser.parseNextTurn("""
                {
                  "turns": [
                    {
                      "turnIndex": 1,
                      "side": "A",
                      "message": "A side pressures the premise."
                    }
                  ]
                }
                """, Speaker.COOL_HEADED, false);

        assertThat(response.speaker()).isEqualTo(Speaker.COOL_HEADED);
        assertThat(response.content()).isEqualTo("A side pressures the premise.");
        assertThat(response.peakReached()).isFalse();
    }

    @Test
    void parsesRequestedTurnIndexFromFastRoundTurnsArray() {
        AiNextTurnResponse response = parser.parseNextTurn("""
                {
                  "turns": [
                    {
                      "turnIndex": 1,
                      "side": "A",
                      "message": "First message."
                    },
                    {
                      "turnIndex": 2,
                      "side": "B",
                      "message": "Second message."
                    }
                  ]
                }
                """, Speaker.PASSIONATE, false, 2);

        assertThat(response.speaker()).isEqualTo(Speaker.PASSIONATE);
        assertThat(response.content()).isEqualTo("Second message.");
    }

    @Test
    void parsesExactlyTenTurnsFromFastRoundTurnsArray() {
        List<AiGeneratedTurnResponse> turns = parser.parseRound("""
                {
                  "turns": [
                    {"turnIndex": 1, "side": "A", "message": "A opens the claim."},
                    {"turnIndex": 2, "side": "B", "message": "B challenges the claim."},
                    {"turnIndex": 3, "side": "A", "message": "A reframes the axis."},
                    {"turnIndex": 4, "side": "B", "message": "B gives a counter scene."},
                    {"turnIndex": 5, "side": "A", "message": "A exposes the hidden cost."},
                    {"turnIndex": 6, "side": "B", "message": "B pressures the constraint."},
                    {"turnIndex": 7, "side": "A", "message": "A turns the tradeoff back."},
                    {"turnIndex": 8, "side": "B", "message": "B pushes a user scene."},
                    {"turnIndex": 9, "side": "A", "message": "A summarizes the clash."},
                    {"turnIndex": 10, "side": "B", "message": "B leaves a challenge."}
                  ]
                }
                """);

        assertThat(turns).hasSize(10);
        assertThat(turns.get(0).speaker()).isEqualTo(Speaker.COOL_HEADED);
        assertThat(turns.get(0).turnIndex()).isEqualTo(1);
        assertThat(turns.get(0).content()).isEqualTo("A opens the claim.");
        assertThat(turns.get(0).peakReached()).isFalse();
        assertThat(turns.get(1).speaker()).isEqualTo(Speaker.PASSIONATE);
        assertThat(turns.get(1).turnIndex()).isEqualTo(2);
        assertThat(turns.get(1).content()).isEqualTo("B challenges the claim.");
        assertThat(turns.get(9).speaker()).isEqualTo(Speaker.PASSIONATE);
        assertThat(turns.get(9).turnIndex()).isEqualTo(10);
    }

    @Test
    void rejectsFastRoundWhenTurnCountIsNotTen() {
        assertThatThrownBy(() -> parser.parseRound("""
                {
                  "turns": [
                    {"turnIndex": 1, "side": "A", "message": "A opens."},
                    {"turnIndex": 2, "side": "B", "message": "B answers."}
                  ]
                }
                """))
                .isInstanceOf(ApiException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.BAD_GATEWAY);
    }

    @Test
    void rejectsFastRoundWhenSideBalanceIsBroken() {
        assertThatThrownBy(() -> parser.parseRound("""
                {
                  "turns": [
                    {"turnIndex": 1, "side": "A", "message": "A opens."},
                    {"turnIndex": 2, "side": "A", "message": "A repeats."},
                    {"turnIndex": 3, "side": "A", "message": "A keeps going."},
                    {"turnIndex": 4, "side": "A", "message": "A again."},
                    {"turnIndex": 5, "side": "A", "message": "A again."},
                    {"turnIndex": 6, "side": "A", "message": "A again."},
                    {"turnIndex": 7, "side": "A", "message": "A again."},
                    {"turnIndex": 8, "side": "A", "message": "A again."},
                    {"turnIndex": 9, "side": "A", "message": "A again."},
                    {"turnIndex": 10, "side": "A", "message": "A ends."}
                  ]
                }
                """))
                .isInstanceOf(ApiException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.BAD_GATEWAY);
    }

    @Test
    void parsesFastRoundWhenSideUsesDisplayLabelsAndTurnIndexIsMissing() {
        List<AiGeneratedTurnResponse> turns = parser.parseRound("""
                {
                  "turns": [
                    {"side": "찬성", "message": "A opens the claim."},
                    {"side": "반대", "message": "B challenges the claim."},
                    {"side": "찬성", "message": "A reframes the axis."},
                    {"side": "반대", "message": "B gives a counter scene."},
                    {"side": "찬성", "message": "A exposes the hidden cost."},
                    {"side": "반대", "message": "B pressures the constraint."},
                    {"side": "찬성", "message": "A turns the tradeoff back."},
                    {"side": "반대", "message": "B pushes a user scene."},
                    {"side": "찬성", "message": "A summarizes the clash."},
                    {"side": "반대", "message": "B leaves a challenge."}
                  ]
                }
                """);

        assertThat(turns).hasSize(10);
        assertThat(turns).extracting(AiGeneratedTurnResponse::turnIndex)
                .containsExactly(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        assertThat(turns).extracting(AiGeneratedTurnResponse::speaker)
                .containsExactly(
                        Speaker.COOL_HEADED,
                        Speaker.PASSIONATE,
                        Speaker.COOL_HEADED,
                        Speaker.PASSIONATE,
                        Speaker.COOL_HEADED,
                        Speaker.PASSIONATE,
                        Speaker.COOL_HEADED,
                        Speaker.PASSIONATE,
                        Speaker.COOL_HEADED,
                        Speaker.PASSIONATE
                );
    }

    @Test
    void parsesTopicFrameFromFencedJson() {
        TopicFrameResponse response = parser.parseTopicFrame("""
                ```json
                {
                  "normalizedBigTopic": "Lunch choice",
                  "sideA": "Choose A",
                  "sideB": "Choose B",
                  "basicConditions": "Same budget",
                  "isDebatable": true,
                  "assumptions": ["weekday"],
                  "missingFields": [],
                  "problemReason": null
                }
                ```
                """);

        assertThat(response.normalizedBigTopic()).isEqualTo("Lunch choice");
        assertThat(response.sideA()).isEqualTo("Choose A");
        assertThat(response.debatable()).isTrue();
        assertThat(response.assumptions()).containsExactly("weekday");
    }

    @Test
    void parsesGeneratedCandidateJson() {
        List<RoundCandidateResponse> candidates = parser.parseGeneratedCandidates("""
                {
                  "candidates": [
                    {
                      "roundId": "R1",
                      "title": "Stability vs thrill",
                      "coreQuestion": "Which lunch standard is stronger?",
                      "debateAxis": "Failure risk versus immediate satisfaction",
                      "sideAFrame": "A reduces failure risk.",
                      "sideBFrame": "B gives stronger immediate satisfaction."
                    }
                  ]
                }
                """);

        assertThat(candidates).hasSize(1);
        assertThat(candidates.get(0).roundId()).isEqualTo("R1");
        assertThat(candidates.get(0).coreQuestion()).contains("lunch");
    }

    @Test
    void parsesValidatedCandidateJson() {
        List<RoundCandidateResponse> candidates = parser.parseValidatedCandidates("""
                {
                  "topCandidates": [
                    {
                      "roundId": "R1",
                      "title": "Stability vs thrill",
                      "coreQuestion": "Which lunch standard is stronger?",
                      "debateAxis": "Failure risk versus immediate satisfaction",
                      "sideAFrame": "A reduces failure risk.",
                      "sideBFrame": "B gives stronger immediate satisfaction."
                    }
                  ],
                  "rejectedCandidates": []
                }
                """);

        assertThat(candidates).extracting(RoundCandidateResponse::title)
                .containsExactly("Stability vs thrill");
    }
}

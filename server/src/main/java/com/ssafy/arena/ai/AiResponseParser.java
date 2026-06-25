package com.ssafy.arena.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.arena.common.ApiException;
import com.ssafy.arena.domain.Speaker;
import com.ssafy.arena.dto.ai.AiGeneratedTurnResponse;
import com.ssafy.arena.dto.ai.AiNextTurnResponse;
import com.ssafy.arena.dto.ai.AiSummaryResponse;
import com.ssafy.arena.dto.ai.RoundCandidateResponse;
import com.ssafy.arena.dto.ai.TopicFrameResponse;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AiResponseParser {
    private static final int FAST_ROUND_TURN_COUNT = 10;

    private final ObjectMapper objectMapper;

    public AiNextTurnResponse parseNextTurn(String content, Speaker speaker, boolean defaultPeakReached) {
        return parseNextTurn(content, speaker, defaultPeakReached, 1);
    }

    public AiNextTurnResponse parseNextTurn(String content, Speaker speaker, boolean defaultPeakReached, int turnIndex) {
        try {
            var node = objectMapper.readTree(extractJson(content));
            String message = node.path("content").asText(null);
            if (message == null && node.path("turns").isArray() && !node.path("turns").isEmpty()) {
                for (var turn : node.path("turns")) {
                    if (turn.path("turnIndex").asInt(-1) == turnIndex) {
                        message = turn.path("message").asText(null);
                        break;
                    }
                }
                if (message == null && turnIndex > 0 && node.path("turns").size() >= turnIndex) {
                    message = node.path("turns").get(turnIndex - 1).path("message").asText(null);
                }
            }
            if (message == null || message.isBlank()) {
                throw new IllegalArgumentException("Missing next-turn message");
            }
            return new AiNextTurnResponse(
                    speaker,
                    message,
                    node.path("peakReached").asBoolean(defaultPeakReached)
            );
        } catch (JsonProcessingException | RuntimeException ex) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Spring AI next-turn generation failed");
        }
    }

    public List<AiGeneratedTurnResponse> parseRound(String content) {
        try {
            var node = objectMapper.readTree(extractJson(content));
            var turnsNode = node.path("turns");
            if (!turnsNode.isArray() || turnsNode.isEmpty()) {
                throw new IllegalArgumentException("Missing fast-round turns");
            }
            if (turnsNode.size() != FAST_ROUND_TURN_COUNT) {
                throw new IllegalArgumentException("Fast-round turns must be exactly " + FAST_ROUND_TURN_COUNT);
            }

            List<AiGeneratedTurnResponse> turns = new ArrayList<>();
            int fallbackIndex = 1;
            int sideACount = 0;
            int sideBCount = 0;
            for (var turnNode : turnsNode) {
                int turnIndex = turnNode.path("turnIndex").canConvertToInt()
                        ? turnNode.path("turnIndex").asInt()
                        : fallbackIndex;
                if (turnIndex != fallbackIndex) {
                    throw new IllegalArgumentException("Invalid fast-round turnIndex order");
                }
                String side = turnNode.path("side").asText(null);
                String expectedSide = turnIndex % 2 == 1 ? "A" : "B";
                if (isContractSide(side) && !expectedSide.equalsIgnoreCase(side)) {
                    throw new IllegalArgumentException("Invalid fast-round side order");
                }
                if ("A".equals(expectedSide)) {
                    sideACount += 1;
                } else {
                    sideBCount += 1;
                }
                String message = turnNode.path("message").asText(null);
                if (message == null || message.isBlank()) {
                    message = turnNode.path("content").asText(null);
                }
                if (message == null || message.isBlank()) {
                    throw new IllegalArgumentException("Missing fast-round message");
                }
                turns.add(new AiGeneratedTurnResponse(
                        parseSpeaker(expectedSide, turnIndex),
                        turnIndex,
                        message,
                        turnNode.path("peakReached").asBoolean(turnIndex >= 6)
                ));
                fallbackIndex += 1;
            }
            if (sideACount != FAST_ROUND_TURN_COUNT / 2 || sideBCount != FAST_ROUND_TURN_COUNT / 2) {
                throw new IllegalArgumentException("Fast-round side balance must be 5 A turns and 5 B turns");
            }
            return turns;
        } catch (JsonProcessingException | RuntimeException ex) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Spring AI round generation failed");
        }
    }

    public AiSummaryResponse parseSummary(String content) {
        try {
            var node = objectMapper.readTree(extractJson(content));
            return new AiSummaryResponse(
                    node.path("coreArguments").asText(),
                    node.path("highlight").asText(),
                    node.path("decisionCriteria").asText(),
                    node.path("remainingIssue").asText(),
                    node.path("summaryText").asText()
            );
        } catch (JsonProcessingException | RuntimeException ex) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Spring AI summary generation failed");
        }
    }

    public TopicFrameResponse parseTopicFrame(String content) {
        try {
            var node = objectMapper.readTree(extractJson(content));
            return new TopicFrameResponse(
                    node.path("normalizedBigTopic").asText(null),
                    node.path("sideA").asText(null),
                    node.path("sideB").asText(null),
                    node.path("basicConditions").asText(null),
                    node.path("isDebatable").asBoolean(false),
                    readStringList(node.path("assumptions")),
                    readStringList(node.path("missingFields")),
                    node.path("problemReason").asText(null)
            );
        } catch (JsonProcessingException | RuntimeException ex) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Spring AI topic frame generation failed");
        }
    }

    public List<RoundCandidateResponse> parseGeneratedCandidates(String content) {
        return parseCandidateArray(content, "candidates");
    }

    public List<RoundCandidateResponse> parseValidatedCandidates(String content) {
        return parseCandidateArray(content, "topCandidates");
    }

    public String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Spring AI response serialization failed");
        }
    }

    private List<RoundCandidateResponse> parseCandidateArray(String content, String fieldName) {
        try {
            var node = objectMapper.readTree(extractJson(content));
            var array = node.path(fieldName);
            if (!array.isArray()) {
                throw new IllegalArgumentException("Missing candidate array: " + fieldName);
            }

            List<RoundCandidateResponse> candidates = new ArrayList<>();
            for (var candidate : array) {
                candidates.add(new RoundCandidateResponse(
                        null,
                        candidate.path("roundId").asText(null),
                        candidate.path("title").asText(null),
                        candidate.path("coreQuestion").asText(null),
                        candidate.path("debateAxis").asText(null),
                        candidate.path("sideAFrame").asText(null),
                        candidate.path("sideBFrame").asText(null)
                ));
            }
            return candidates;
        } catch (JsonProcessingException | RuntimeException ex) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Spring AI round candidate generation failed");
        }
    }

    private List<String> readStringList(com.fasterxml.jackson.databind.JsonNode node) {
        if (!node.isArray()) {
            return List.of();
        }
        List<String> values = new ArrayList<>();
        for (var item : node) {
            values.add(item.asText());
        }
        return values;
    }

    private Speaker parseSpeaker(String side, int turnIndex) {
        if ("A".equalsIgnoreCase(side)) {
            return Speaker.COOL_HEADED;
        }
        if ("B".equalsIgnoreCase(side)) {
            return Speaker.PASSIONATE;
        }
        return turnIndex % 2 == 1 ? Speaker.COOL_HEADED : Speaker.PASSIONATE;
    }

    private boolean isContractSide(String side) {
        return "A".equalsIgnoreCase(side) || "B".equalsIgnoreCase(side);
    }

    private String extractJson(String content) {
        if (content == null) {
            throw new IllegalArgumentException("content is null");
        }
        String trimmed = content.trim();
        if (trimmed.startsWith("```")) {
            int firstNewLine = trimmed.indexOf('\n');
            int lastFence = trimmed.lastIndexOf("```");
            if (firstNewLine >= 0 && lastFence > firstNewLine) {
                return trimmed.substring(firstNewLine + 1, lastFence).trim();
            }
        }

        int objectStart = trimmed.indexOf('{');
        int objectEnd = trimmed.lastIndexOf('}');
        if (objectStart >= 0 && objectEnd > objectStart) {
            return trimmed.substring(objectStart, objectEnd + 1);
        }
        return trimmed;
    }
}

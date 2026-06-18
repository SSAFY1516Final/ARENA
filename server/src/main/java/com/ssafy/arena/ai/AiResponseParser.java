package com.ssafy.arena.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.arena.common.ApiException;
import com.ssafy.arena.domain.Speaker;
import com.ssafy.arena.dto.ai.AiNextTurnResponse;
import com.ssafy.arena.dto.ai.AiSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AiResponseParser {
    private final ObjectMapper objectMapper;

    public AiNextTurnResponse parseNextTurn(String content, Speaker speaker, boolean defaultPeakReached) {
        try {
            var node = objectMapper.readTree(content);
            return new AiNextTurnResponse(
                    speaker,
                    node.path("content").asText(),
                    node.path("peakReached").asBoolean(defaultPeakReached)
            );
        } catch (JsonProcessingException | RuntimeException ex) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Spring AI next-turn generation failed");
        }
    }

    public AiSummaryResponse parseSummary(String content) {
        try {
            var node = objectMapper.readTree(content);
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
}

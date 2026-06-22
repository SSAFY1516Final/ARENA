package com.ssafy.arena.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.arena.common.ApiException;
import com.ssafy.arena.domain.Speaker;
import com.ssafy.arena.dto.ai.AiNextTurnResponse;
import com.ssafy.arena.dto.ai.AiSummaryResponse;
import com.ssafy.arena.dto.ai.TopicCandidateItem;
import com.ssafy.arena.dto.ai.TopicCandidateResponse;
import java.util.ArrayList;
import java.util.List;
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

    public TopicCandidateResponse parseTopicCandidates(String content) {
        try {
            var root = objectMapper.readTree(content);
            var itemsNode = root.path("items");
            List<TopicCandidateItem> items = new ArrayList<>();
            if (itemsNode.isArray()) {
                for (var itemNode : itemsNode) {
                    items.add(new TopicCandidateItem(
                            itemNode.path("title").asText(),
                            itemNode.path("reason").asText(),
                            itemNode.path("noveltyScore").asInt(0),
                            itemNode.path("fitScore").asInt(0),
                            itemNode.path("funScore").asInt(0)
                    ));
                }
            }
            return new TopicCandidateResponse(items);
        } catch (RuntimeException | com.fasterxml.jackson.core.JsonProcessingException ex) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Spring AI topic candidate generation failed");
        }
    }
}

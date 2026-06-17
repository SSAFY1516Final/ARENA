package com.ssafy.arena.ai;

import com.ssafy.arena.common.ApiException;
import com.ssafy.arena.domain.Speaker;
import com.ssafy.arena.dto.ai.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiClient {
    private final ChatClient.Builder chatClientBuilder;
    private final ObjectMapper objectMapper;

    public AiNextTurnResponse nextTurn(AiNextTurnRequest request) {
        return springAiNextTurn(request);
    }

    public AiSummaryResponse summarize(AiSummaryRequest request) {
        return springAiSummary(request);
    }

    private AiNextTurnResponse springAiNextTurn(AiNextTurnRequest request) {
        try {
            Speaker speaker = request.roundNo() % 2 == 1 ? Speaker.COOL_HEADED : Speaker.PASSIONATE;
            String content = chatClientBuilder.build()
                    .prompt()
                    .system("""
                            ARENA 토론 서비스의 다음 발화를 생성한다.
                            반드시 JSON만 반환한다. keys: content(string), peakReached(boolean).
                            냉정파는 근거와 리스크, 열정파는 만족감과 몰입감을 중심으로 말한다.
                            """)
                    .user("""
                            주제: %s
                            모드: %s
                            발화자: %s
                            라운드: %d
                            이전 발화: %s
                            """.formatted(request.topic(), request.mode(), speaker, request.roundNo(), request.previousMessages()))
                    .call()
                    .content();
            var node = objectMapper.readTree(content);
            return new AiNextTurnResponse(
                    speaker,
                    node.path("content").asText(),
                    node.path("peakReached").asBoolean(request.roundNo() >= 6)
            );
        } catch (JsonProcessingException | RuntimeException ex) {
            log.error("Spring AI next-turn generation failed. topic={}, mode={}, roundNo={}",
                    request.topic(), request.mode(), request.roundNo(), ex);
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Spring AI next-turn generation failed");
        }
    }

    private AiSummaryResponse springAiSummary(AiSummaryRequest request) {
        try {
            String content = chatClientBuilder.build()
                    .prompt()
                    .system("""
                            ARENA 토론을 게시글 공유용으로 요약한다.
                            반드시 JSON만 반환한다.
                            keys: coreArguments, highlight, decisionCriteria, remainingIssue, summaryText.
                            """)
                    .user("""
                            주제: %s
                            모드: %s
                            토론 로그: %s
                            """.formatted(request.topic(), request.mode(), request.messages()))
                    .call()
                    .content();
            var node = objectMapper.readTree(content);
            return new AiSummaryResponse(
                    node.path("coreArguments").asText(),
                    node.path("highlight").asText(),
                    node.path("decisionCriteria").asText(),
                    node.path("remainingIssue").asText(),
                    node.path("summaryText").asText()
            );
        } catch (JsonProcessingException | RuntimeException ex) {
            log.error("Spring AI summary generation failed. topic={}, mode={}, messageCount={}",
                    request.topic(), request.mode(), request.messages().size(), ex);
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Spring AI summary generation failed");
        }
    }
}

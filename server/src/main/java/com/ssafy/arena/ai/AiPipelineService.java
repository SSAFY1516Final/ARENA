package com.ssafy.arena.ai;

import com.ssafy.arena.domain.Speaker;
import com.ssafy.arena.dto.ai.AiNextTurnRequest;
import com.ssafy.arena.dto.ai.AiNextTurnResponse;
import com.ssafy.arena.dto.ai.AiRoundResponse;
import com.ssafy.arena.dto.ai.AiSummaryRequest;
import com.ssafy.arena.dto.ai.AiSummaryResponse;
import java.util.Map;
import static java.util.Map.entry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiPipelineService implements AiClient {
    private static final String FAST_ROUND_PROMPT = "debate-single-round-fast-generator.txt";
    private static final String SUMMARY_PROMPT = "debate-summary-highlight-generator.txt";
    private static final String JSON_ONLY_SYSTEM_PROMPT = "Return valid JSON only. Do not use markdown.";
    private static final int FAST_ROUND_TURN_COUNT = 10;
    private static final int FAST_ROUND_MAX_ATTEMPTS = 3;

    private final SpringAiClient springAiClient;
    private final AiPromptFactory promptFactory;
    private final PromptTemplateLoader promptTemplateLoader;
    private final AiResponseParser responseParser;

    @Override
    public AiNextTurnResponse nextTurn(AiNextTurnRequest request) {
        int roundNo = normalizeRoundNo(request.roundNo());
        Speaker speaker = selectSpeaker(roundNo);
        String renderedPrompt = renderFastRoundPrompt(request, speaker);
        long startedAt = System.nanoTime();
        String rawResponse = springAiClient.call(
                promptFactory.nextTurnSystemPrompt(),
                renderedPrompt
        );
        long latencyMs = (System.nanoTime() - startedAt) / 1_000_000;
        AiNextTurnResponse parsed = responseParser.parseNextTurn(rawResponse, speaker, roundNo >= 6, roundNo);
        return parsed.withArtifacts(
                FAST_ROUND_PROMPT,
                renderedPrompt,
                rawResponse,
                responseParser.toJson(parsed),
                latencyMs
        );
    }

    @Override
    public AiRoundResponse generateRound(AiNextTurnRequest request) {
        int roundNo = normalizeRoundNo(request.roundNo());
        Speaker speaker = selectSpeaker(roundNo);
        String renderedPrompt = renderFastRoundPrompt(request, speaker);
        String systemPrompt = promptFactory.nextTurnSystemPrompt();
        RuntimeException lastFailure = null;
        for (int attempt = 1; attempt <= FAST_ROUND_MAX_ATTEMPTS; attempt += 1) {
            long startedAt = System.nanoTime();
            try {
                String rawResponse = springAiClient.call(systemPrompt, renderedPrompt);
                long latencyMs = (System.nanoTime() - startedAt) / 1_000_000;
                var parsedTurns = responseParser.parseRound(rawResponse);
                AiRoundResponse parsed = new AiRoundResponse(parsedTurns);
                return parsed.withArtifacts(
                        FAST_ROUND_PROMPT,
                        renderedPrompt,
                        rawResponse,
                        responseParser.toJson(parsedTurns),
                        latencyMs
                );
            } catch (RuntimeException ex) {
                lastFailure = ex;
                if (attempt == FAST_ROUND_MAX_ATTEMPTS) {
                    break;
                }
                log.warn("Spring AI fast-round generation attempt failed. attempt={}/{}",
                        attempt, FAST_ROUND_MAX_ATTEMPTS);
            }
        }
        throw lastFailure;
    }

    @Override
    public AiSummaryResponse summarize(AiSummaryRequest request) {
        String renderedPrompt = promptTemplateLoader.loadAndRender(
                SUMMARY_PROMPT,
                Map.ofEntries(
                        entry("topic", request.topic()),
                        entry("mode", request.mode()),
                        entry("messagesJson", responseParser.toJson(request.messages()))
                )
        );
        String content = springAiClient.call(
                JSON_ONLY_SYSTEM_PROMPT,
                renderedPrompt
        );
        return responseParser.parseSummary(content);
    }

    private int normalizeRoundNo(Integer roundNo) {
        return roundNo == null ? 1 : roundNo;
    }

    private Speaker selectSpeaker(int roundNo) {
        return roundNo % 2 == 1 ? Speaker.COOL_HEADED : Speaker.PASSIONATE;
    }

    private String renderFastRoundPrompt(AiNextTurnRequest request, Speaker speaker) {
        return promptTemplateLoader.loadAndRender(
                FAST_ROUND_PROMPT,
                promptVariables(request, speaker)
        );
    }

    private Map<String, ?> promptVariables(AiNextTurnRequest request, Speaker speaker) {
        String topic = request.topic();
        String sideA = hasText(request.sideALabel()) ? request.sideALabel() : Speaker.COOL_HEADED.name();
        String sideB = hasText(request.sideBLabel()) ? request.sideBLabel() : Speaker.PASSIONATE.name();
        String debateAxis = hasText(request.debateAxis()) ? request.debateAxis() : topic;
        String sideAFrame = hasText(request.sideAFrame())
                ? request.sideAFrame()
                : "Defend " + sideA + " as the stronger answer to the selected debate question.";
        String sideBFrame = hasText(request.sideBFrame())
                ? request.sideBFrame()
                : "Defend " + sideB + " as the stronger answer to the selected debate question.";
        String roundTitle = hasText(request.roundTitle()) ? request.roundTitle() : topic;
        String basicConditions = hasText(request.basicConditions())
                ? request.basicConditions()
                : "선택된 세부 주제와 A/B 프레임을 기준으로 한다.";
        return Map.ofEntries(
                entry("mode", request.mode()),
                entry("bigTopic", topic),
                entry("roundTitle", roundTitle),
                entry("coreQuestion", topic),
                entry("debateAxis", debateAxis),
                entry("sideA", sideA),
                entry("sideB", sideB),
                entry("sideAFrame", sideAFrame),
                entry("sideBFrame", sideBFrame),
                entry("basicConditions", basicConditions),
                entry("totalTurnCount", FAST_ROUND_TURN_COUNT),
                entry("tone", "COMMUNITY"),
                entry("creativityLevel", "HIGH"),
                entry("evidencePolicy", "OPTIONAL"),
                entry("evidenceSourcesJson", "[]")
        );
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}

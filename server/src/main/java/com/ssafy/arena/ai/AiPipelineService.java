package com.ssafy.arena.ai;

import com.ssafy.arena.domain.Speaker;
import com.ssafy.arena.dto.ai.AiNextTurnRequest;
import com.ssafy.arena.dto.ai.AiNextTurnResponse;
import com.ssafy.arena.dto.ai.AiSummaryRequest;
import com.ssafy.arena.dto.ai.AiSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiPipelineService implements AiClient {
    private final SpringAiClient springAiClient;
    private final AiPromptFactory promptFactory;
    private final AiResponseParser responseParser;

    @Override
    public AiNextTurnResponse nextTurn(AiNextTurnRequest request) {
        int roundNo = normalizeRoundNo(request.roundNo());
        Speaker speaker = selectSpeaker(roundNo);
        String content = springAiClient.call(
                promptFactory.nextTurnSystemPrompt(),
                promptFactory.nextTurnUserPrompt(request, speaker)
        );
        return responseParser.parseNextTurn(content, speaker, roundNo >= 6);
    }

    @Override
    public AiSummaryResponse summarize(AiSummaryRequest request) {
        String content = springAiClient.call(
                promptFactory.summarySystemPrompt(),
                promptFactory.summaryUserPrompt(request)
        );
        return responseParser.parseSummary(content);
    }

    private int normalizeRoundNo(Integer roundNo) {
        return roundNo == null ? 1 : roundNo;
    }

    private Speaker selectSpeaker(int roundNo) {
        return roundNo % 2 == 1 ? Speaker.COOL_HEADED : Speaker.PASSIONATE;
    }
}

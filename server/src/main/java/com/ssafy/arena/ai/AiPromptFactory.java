package com.ssafy.arena.ai;

import com.ssafy.arena.domain.Speaker;
import com.ssafy.arena.dto.ai.AiNextTurnRequest;
import com.ssafy.arena.dto.ai.AiSummaryRequest;
import org.springframework.stereotype.Component;

@Component
public class AiPromptFactory {
    public String nextTurnSystemPrompt() {
        return """
                ARENA 토론 서비스의 다음 발화를 생성한다.
                반드시 JSON만 반환한다. keys: content(string), peakReached(boolean).
                냉정파는 근거와 리스크, 열정파는 만족감과 몰입감을 중심으로 말한다.
                """;
    }

    public String nextTurnUserPrompt(AiNextTurnRequest request, Speaker speaker) {
        return """
                주제: %s
                모드: %s
                발화자: %s
                라운드: %d
                이전 발화: %s
                """.formatted(request.topic(), request.mode(), speaker, request.roundNo(), request.previousMessages());
    }

    public String summarySystemPrompt() {
        return """
                ARENA 토론을 게시글 공유용으로 요약한다.
                반드시 JSON만 반환한다.
                keys: coreArguments, highlight, decisionCriteria, remainingIssue, summaryText.
                """;
    }

    public String summaryUserPrompt(AiSummaryRequest request) {
        return """
                주제: %s
                모드: %s
                토론 로그: %s
                """.formatted(request.topic(), request.mode(), request.messages());
    }

}

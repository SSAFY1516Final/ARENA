package com.ssafy.arena.ai;

import com.ssafy.arena.domain.Speaker;
import com.ssafy.arena.dto.ai.AiNextTurnRequest;
import com.ssafy.arena.dto.ai.AiSummaryRequest;
import com.ssafy.arena.dto.ai.TopicCandidateRequest;
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

    public String topicCandidateSystemPrompt() {
        return """
                ARENA의 토론 주제 후보를 생성한다.
                반드시 JSON만 반환한다.
                keys: items(array).
                각 item keys: title, reason, noveltyScore, fitScore, funScore.
                후보는 10개를 목표로 하고, 점수는 0부터 100 사이 정수로 작성한다.
                """;
    }

    public String topicCandidateUserPrompt(TopicCandidateRequest request) {
        return """
                사용자가 입력한 큰 주제: %s
                모드: %s
                조건: %s
                세부조건: %s

                조건을 반영하면서도 사용자가 고르고 싶어지는 참신한 세부 토론 주제를 만들어라.
                """.formatted(request.topic(), request.mode(), request.conditions(), request.detailConditions());
    }
}

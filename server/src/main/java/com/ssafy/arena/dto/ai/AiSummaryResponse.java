package com.ssafy.arena.dto.ai;


public record AiSummaryResponse(
        String coreArguments,
        String highlight,
        String decisionCriteria,
        String remainingIssue,
        String summaryText
) {
}

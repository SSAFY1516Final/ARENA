package com.ssafy.arena.dto.debate;

import com.ssafy.arena.domain.DebateSummary;

public record DebateSummaryResponse(
        String coreArguments,
        String highlight,
        String decisionCriteria,
        String remainingIssue,
        String summaryText
) {
    public static DebateSummaryResponse from(DebateSummary summary) {
        return new DebateSummaryResponse(
                summary.getCoreArguments(),
                summary.getHighlight(),
                summary.getDecisionCriteria(),
                summary.getRemainingIssue(),
                summary.getSummaryText()
        );
    }
}

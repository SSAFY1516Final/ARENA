package com.ssafy.arena.dto.ai;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record TopicCandidateItem(
        @NotBlank String title,
        String reason,
        @Min(0) @Max(100) Integer noveltyScore,
        @Min(0) @Max(100) Integer fitScore,
        @Min(0) @Max(100) Integer funScore
) {
    public int totalScore() {
        return safeScore(noveltyScore) + safeScore(fitScore) + safeScore(funScore);
    }

    private int safeScore(Integer score) {
        return score == null ? 0 : score;
    }
}

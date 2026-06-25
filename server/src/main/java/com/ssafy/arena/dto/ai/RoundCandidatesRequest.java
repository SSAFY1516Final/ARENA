package com.ssafy.arena.dto.ai;

import com.ssafy.arena.domain.DebateMode;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record RoundCandidatesRequest(
        @NotBlank @jakarta.validation.constraints.Size(max = 255) String topic,
        DebateMode mode,
        @Min(5) @Max(5) Integer candidateCount
) {
    public DebateMode modeOrDefault() {
        return mode == null ? DebateMode.PRACTICAL : mode;
    }

    public int candidateCountOrDefault() {
        return candidateCount == null ? 5 : candidateCount;
    }
}

package com.ssafy.arena.dto.ai;

public record RoundCandidateResponse(
        Long candidateId,
        String roundId,
        String title,
        String coreQuestion,
        String debateAxis,
        String sideAFrame,
        String sideBFrame
) {
}

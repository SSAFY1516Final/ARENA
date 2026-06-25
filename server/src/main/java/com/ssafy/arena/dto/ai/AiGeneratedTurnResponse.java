package com.ssafy.arena.dto.ai;

import com.ssafy.arena.domain.Speaker;

public record AiGeneratedTurnResponse(
        Speaker speaker,
        Integer turnIndex,
        String content,
        Boolean peakReached
) {
}

package com.ssafy.arena.dto.ai;


import com.ssafy.arena.domain.Speaker;

public record AiNextTurnResponse(
        Speaker speaker,
        String content,
        Boolean peakReached
) {
}

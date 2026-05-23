package com.ssafy.arena.dto.ai;


import com.ssafy.arena.domain.Speaker;

public record AiMessagePayload(
        Speaker speaker,
        Integer roundNo,
        String content
) {
}

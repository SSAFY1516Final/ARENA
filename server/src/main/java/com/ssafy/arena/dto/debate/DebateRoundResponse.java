package com.ssafy.arena.dto.debate;

import com.ssafy.arena.domain.Speaker;

public record DebateRoundResponse(
        Integer roundNo,
        Long debateId,
        Speaker selectedSide,
        String title,
        String topic,
        String description,
        DebateSummaryResponse summary
) {
}

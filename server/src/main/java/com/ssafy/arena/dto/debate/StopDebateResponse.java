package com.ssafy.arena.dto.debate;

import com.ssafy.arena.domain.DebateStatus;

public record StopDebateResponse(
        Long debateId,
        DebateStatus status,
        DebateSummaryResponse summary
) {
}

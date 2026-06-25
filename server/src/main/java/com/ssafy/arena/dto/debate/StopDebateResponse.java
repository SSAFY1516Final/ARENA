package com.ssafy.arena.dto.debate;

import com.ssafy.arena.domain.DebateStatus;
import com.ssafy.arena.domain.Speaker;

public record StopDebateResponse(
        Long debateId,
        DebateStatus status,
        Speaker selectedSide,
        Integer selectedRoundNo,
        DebateSummaryResponse summary
) {
}

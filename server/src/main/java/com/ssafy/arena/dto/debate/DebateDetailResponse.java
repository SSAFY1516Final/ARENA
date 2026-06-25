package com.ssafy.arena.dto.debate;

import com.ssafy.arena.domain.DebateMessage;
import java.util.List;

public record DebateDetailResponse(
        CreateDebateResponse debate,
        List<DebateMessage> messages,
        List<DebateRoundResponse> rounds,
        DebateSummaryResponse summary
) {
}

package com.ssafy.arena.dto.ai;


import com.ssafy.arena.domain.DebateMode;
import java.util.List;

public record AiSummaryRequest(
        String topic,
        DebateMode mode,
        List<AiMessagePayload> messages
) {
}

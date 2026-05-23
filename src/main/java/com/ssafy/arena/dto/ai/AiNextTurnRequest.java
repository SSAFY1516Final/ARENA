package com.ssafy.arena.dto.ai;


import com.ssafy.arena.domain.DebateMode;
import java.util.List;

public record AiNextTurnRequest(
        String topic,
        DebateMode mode,
        Integer roundNo,
        List<AiMessagePayload> previousMessages
) {
}

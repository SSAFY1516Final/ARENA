package com.ssafy.arena.dto.debate;

import com.ssafy.arena.domain.DebateMode;
import com.ssafy.arena.domain.DebateSession;
import com.ssafy.arena.domain.DebateStatus;

public record CreateDebateResponse(
        Long debateId,
        String topic,
        DebateMode mode,
        DebateStatus status
) {
    public static CreateDebateResponse from(DebateSession session) {
        return new CreateDebateResponse(session.getId(), session.getTopic(), session.getMode(), session.getStatus());
    }
}

package com.ssafy.arena.dto.debate;

import com.ssafy.arena.domain.DebateMode;
import com.ssafy.arena.domain.DebateSession;
import com.ssafy.arena.domain.DebateStatus;
import java.time.LocalDateTime;

public record DebateListItem(
        Long debateId,
        String topic,
        DebateMode mode,
        DebateStatus status,
        String summaryCard,
        String shareBody,
        LocalDateTime updatedAt
) {
    public static DebateListItem from(DebateSession session) {
        return new DebateListItem(
                session.getId(),
                session.getTopic(),
                session.getMode(),
                session.getStatus(),
                session.getStatus().name(),
                "",
                session.getStoppedAt() == null ? session.getCreatedAt() : session.getStoppedAt()
        );
    }
}

package com.ssafy.arena.dto.debate;

import com.ssafy.arena.domain.DebateMode;
import com.ssafy.arena.domain.DebateSession;
import com.ssafy.arena.domain.DebateStatus;
import com.ssafy.arena.domain.Speaker;
import java.time.LocalDateTime;
import java.util.List;

public record DebateListItem(
        Long debateId,
        String originalTopic,
        String topic,
        Long candidateRunId,
        Long selectedCandidateId,
        String sideALabel,
        String sideBLabel,
        DebateMode mode,
        DebateStatus status,
        String summaryCard,
        String shareBody,
        Integer roundCount,
        Integer coolCount,
        Integer hotCount,
        LocalDateTime updatedAt
) {
    public static DebateListItem from(DebateSession session) {
        return from(session, List.of(session));
    }

    public static DebateListItem from(DebateSession session, List<DebateSession> roundSessions) {
        int coolCount = countSelections(roundSessions, Speaker.COOL_HEADED);
        int hotCount = countSelections(roundSessions, Speaker.PASSIONATE);
        return new DebateListItem(
                session.getId(),
                session.getOriginalTopic(),
                session.getTopic(),
                session.getCandidateRunId(),
                session.getSelectedCandidateId(),
                firstPresent(session.getSideALabel(), firstRoundLabel(roundSessions, true)),
                firstPresent(session.getSideBLabel(), firstRoundLabel(roundSessions, false)),
                session.getMode(),
                session.getStatus(),
                session.getStatus().name(),
                "",
                roundSessions.size(),
                coolCount,
                hotCount,
                session.getStoppedAt() == null ? session.getCreatedAt() : session.getStoppedAt()
        );
    }

    private static int countSelections(List<DebateSession> sessions, Speaker side) {
        return (int) sessions.stream()
                .filter(session -> session.getSelectedSide() == side)
                .count();
    }

    private static String firstRoundLabel(List<DebateSession> sessions, boolean sideA) {
        return sessions.stream()
                .map(session -> sideA ? session.getSideALabel() : session.getSideBLabel())
                .filter(label -> label != null && !label.isBlank())
                .findFirst()
                .orElse("");
    }

    private static String firstPresent(String primary, String fallback) {
        if (primary != null && !primary.isBlank()) {
            return primary;
        }
        return fallback == null ? "" : fallback;
    }
}

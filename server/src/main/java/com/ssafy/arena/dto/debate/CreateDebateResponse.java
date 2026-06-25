package com.ssafy.arena.dto.debate;

import com.ssafy.arena.domain.DebateMode;
import com.ssafy.arena.domain.DebateSession;
import com.ssafy.arena.domain.DebateStatus;
import com.ssafy.arena.domain.Speaker;

public record CreateDebateResponse(
        Long debateId,
        String originalTopic,
        String topic,
        String sideALabel,
        String sideBLabel,
        String debateAxis,
        String sideAFrame,
        String sideBFrame,
        String selectedRoundId,
        String roundTitle,
        String basicConditions,
        Long candidateRunId,
        Long selectedCandidateId,
        DebateMode mode,
        DebateStatus status,
        Speaker selectedSide,
        Integer selectedRoundNo
) {
    public static CreateDebateResponse from(DebateSession session) {
        return new CreateDebateResponse(
                session.getId(),
                session.getOriginalTopic(),
                session.getTopic(),
                session.getSideALabel(),
                session.getSideBLabel(),
                session.getDebateAxis(),
                session.getSideAFrame(),
                session.getSideBFrame(),
                session.getSelectedRoundId(),
                session.getRoundTitle(),
                session.getBasicConditions(),
                session.getCandidateRunId(),
                session.getSelectedCandidateId(),
                session.getMode(),
                session.getStatus(),
                session.getSelectedSide(),
                session.getSelectedRoundNo()
        );
    }
}

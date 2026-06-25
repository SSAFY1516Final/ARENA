package com.ssafy.arena.domain;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DebateSession {
    private Long id;
    private Long userId;
    private String originalTopic;
    private String topic;
    private String sideALabel;
    private String sideBLabel;
    private String debateAxis;
    private String sideAFrame;
    private String sideBFrame;
    private String selectedRoundId;
    private String roundTitle;
    private String basicConditions;
    private Long candidateRunId;
    private Long selectedCandidateId;
    private DebateMode mode;
    private DebateStatus status;
    private Boolean peakReached;
    private Speaker selectedSide;
    private Integer selectedRoundNo;
    private LocalDateTime createdAt;
    private LocalDateTime stoppedAt;
}

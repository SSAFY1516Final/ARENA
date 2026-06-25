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
public class AiRoundCandidateRun {
    private Long id;
    private Long userId;
    private String originalTopic;
    private DebateMode mode;
    private Integer candidateCount;
    private String status;
    private String topicFrameJson;
    private String finalResponseJson;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}

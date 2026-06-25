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
public class AiRoundCandidate {
    private Long id;
    private Long runId;
    private String roundId;
    private String title;
    private String coreQuestion;
    private String debateAxis;
    private String sideAFrame;
    private String sideBFrame;
    private Integer sortOrder;
    private String candidateJson;
    private LocalDateTime createdAt;
}

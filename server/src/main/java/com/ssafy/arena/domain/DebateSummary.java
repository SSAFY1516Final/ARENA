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
public class DebateSummary {
    private Long id;
    private Long debateSessionId;
    private String coreArguments;
    private String highlight;
    private String decisionCriteria;
    private String remainingIssue;
    private String summaryText;
    private LocalDateTime createdAt;
}

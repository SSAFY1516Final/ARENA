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
    private String topic;
    private DebateMode mode;
    private DebateStatus status;
    private Boolean peakReached;
    private LocalDateTime createdAt;
    private LocalDateTime stoppedAt;
}

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
public class DebateMessage {
    private Long id;
    private Long debateSessionId;
    private Speaker speaker;
    private Integer roundNo;
    private String content;
    private LocalDateTime createdAt;
}

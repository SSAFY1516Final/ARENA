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
public class AiDebateTurnLog {
    private Long id;
    private Long debateSessionId;
    private Long debateMessageId;
    private Integer roundNo;
    private Speaker speaker;
    private String promptName;
    private String model;
    private String renderedPrompt;
    private String rawResponse;
    private String parsedResponseJson;
    private String status;
    private String errorMessage;
    private Long latencyMs;
    private LocalDateTime createdAt;
}

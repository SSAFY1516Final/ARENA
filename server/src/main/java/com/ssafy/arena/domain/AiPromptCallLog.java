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
public class AiPromptCallLog {
    private Long id;
    private Long runId;
    private String stage;
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

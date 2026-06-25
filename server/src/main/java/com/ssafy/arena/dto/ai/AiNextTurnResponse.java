package com.ssafy.arena.dto.ai;


import com.ssafy.arena.domain.Speaker;
import com.fasterxml.jackson.annotation.JsonIgnore;

public record AiNextTurnResponse(
        Speaker speaker,
        String content,
        Boolean peakReached,
        @JsonIgnore String promptName,
        @JsonIgnore String renderedPrompt,
        @JsonIgnore String rawResponse,
        @JsonIgnore String parsedResponseJson,
        @JsonIgnore Long latencyMs
) {
    public AiNextTurnResponse(Speaker speaker, String content, Boolean peakReached) {
        this(speaker, content, peakReached, null, null, null, null, null);
    }

    public AiNextTurnResponse withArtifacts(
            String promptName,
            String renderedPrompt,
            String rawResponse,
            String parsedResponseJson,
            Long latencyMs
    ) {
        return new AiNextTurnResponse(
                speaker,
                content,
                peakReached,
                promptName,
                renderedPrompt,
                rawResponse,
                parsedResponseJson,
                latencyMs
        );
    }
}

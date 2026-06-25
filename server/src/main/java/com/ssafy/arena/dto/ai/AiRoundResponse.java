package com.ssafy.arena.dto.ai;

import java.util.List;

public record AiRoundResponse(
        List<AiGeneratedTurnResponse> turns,
        String promptName,
        String renderedPrompt,
        String rawResponse,
        String parsedResponseJson,
        Long latencyMs
) {
    public AiRoundResponse(List<AiGeneratedTurnResponse> turns) {
        this(turns, null, null, null, null, null);
    }

    public AiRoundResponse withArtifacts(
            String promptName,
            String renderedPrompt,
            String rawResponse,
            String parsedResponseJson,
            Long latencyMs
    ) {
        return new AiRoundResponse(
                turns,
                promptName,
                renderedPrompt,
                rawResponse,
                parsedResponseJson,
                latencyMs
        );
    }
}

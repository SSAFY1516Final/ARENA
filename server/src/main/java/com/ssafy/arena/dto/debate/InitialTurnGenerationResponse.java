package com.ssafy.arena.dto.debate;

import java.util.List;

public record InitialTurnGenerationResponse(
        InitialTurnGenerationStatus status,
        List<DebateMessageResponse> messages
) {
}

package com.ssafy.arena.ai;

import com.ssafy.arena.dto.ai.AiNextTurnRequest;
import com.ssafy.arena.dto.ai.AiNextTurnResponse;
import com.ssafy.arena.dto.ai.AiSummaryRequest;
import com.ssafy.arena.dto.ai.AiSummaryResponse;

public interface AiClient {
    AiNextTurnResponse nextTurn(AiNextTurnRequest request);

    AiSummaryResponse summarize(AiSummaryRequest request);
}

package com.ssafy.arena.dto.ai;

import java.util.List;

public record RoundCandidatesResponse(
        Long candidateRunId,
        TopicFrameResponse topicFrame,
        List<RoundCandidateResponse> roundCandidates,
        List<Long> usedCandidateIds,
        List<String> warnings
) {
}

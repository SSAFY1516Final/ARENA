package com.ssafy.arena.dto.ai;

import java.util.List;

public record TopicCandidateResponse(
        List<TopicCandidateItem> items
) {
    public TopicCandidateResponse {
        items = items == null ? List.of() : List.copyOf(items);
    }
}

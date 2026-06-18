package com.ssafy.arena.dto.ai;

import com.ssafy.arena.domain.DebateMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record TopicCandidateRequest(
        @NotBlank @Size(max = 120) String topic,
        @NotNull DebateMode mode,
        @Size(max = 10) List<@Size(max = 80) String> conditions,
        @Size(max = 500) String detailConditions
) {
    public TopicCandidateRequest {
        conditions = conditions == null ? List.of() : List.copyOf(conditions);
    }
}

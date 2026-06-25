package com.ssafy.arena.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record TopicFrameResponse(
        String normalizedBigTopic,
        String sideA,
        String sideB,
        String basicConditions,
        @JsonProperty("isDebatable") boolean debatable,
        List<String> assumptions,
        List<String> missingFields,
        String problemReason
) {
}

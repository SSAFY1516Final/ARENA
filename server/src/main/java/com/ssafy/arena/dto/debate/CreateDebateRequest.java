package com.ssafy.arena.dto.debate;

import com.ssafy.arena.domain.DebateMode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateDebateRequest(
        @NotBlank @Size(max = 255) String originalTopic,
        @NotBlank @Size(max = 255) String topic,
        @NotNull DebateMode mode,
        @Size(max = 120) String sideALabel,
        @Size(max = 120) String sideBLabel,
        @Size(max = 255) String debateAxis,
        @Size(max = 1000) String sideAFrame,
        @Size(max = 1000) String sideBFrame,
        @Size(max = 60) String selectedRoundId,
        @Size(max = 255) String roundTitle,
        @Size(max = 1000) String basicConditions,
        Long candidateRunId,
        Long selectedCandidateId
) {
    public CreateDebateRequest(String originalTopic, String topic, DebateMode mode) {
        this(originalTopic, topic, mode, null, null, null, null, null, null, null, null, null, null);
    }

    public CreateDebateRequest(
            String originalTopic,
            String topic,
            DebateMode mode,
            String sideALabel,
            String sideBLabel
    ) {
        this(originalTopic, topic, mode, sideALabel, sideBLabel, null, null, null, null, null, null, null, null);
    }

    public CreateDebateRequest(
            String originalTopic,
            String topic,
            DebateMode mode,
            String sideALabel,
            String sideBLabel,
            String debateAxis,
            String sideAFrame,
            String sideBFrame
    ) {
        this(originalTopic, topic, mode, sideALabel, sideBLabel, debateAxis, sideAFrame, sideBFrame, null, null, null, null, null);
    }
}

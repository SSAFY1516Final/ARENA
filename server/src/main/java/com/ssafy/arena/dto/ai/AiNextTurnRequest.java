package com.ssafy.arena.dto.ai;


import com.ssafy.arena.domain.DebateMode;
import java.util.List;

public record AiNextTurnRequest(
        String topic,
        DebateMode mode,
        Integer roundNo,
        List<AiMessagePayload> previousMessages,
        String sideALabel,
        String sideBLabel,
        String debateAxis,
        String sideAFrame,
        String sideBFrame,
        String roundTitle,
        String basicConditions
) {
    public AiNextTurnRequest(String topic, DebateMode mode, Integer roundNo, List<AiMessagePayload> previousMessages) {
        this(topic, mode, roundNo, previousMessages, null, null, null, null, null, null, null);
    }

    public AiNextTurnRequest(
            String topic,
            DebateMode mode,
            Integer roundNo,
            List<AiMessagePayload> previousMessages,
            String sideALabel,
            String sideBLabel
    ) {
        this(topic, mode, roundNo, previousMessages, sideALabel, sideBLabel, null, null, null, null, null);
    }

    public AiNextTurnRequest(
            String topic,
            DebateMode mode,
            Integer roundNo,
            List<AiMessagePayload> previousMessages,
            String sideALabel,
            String sideBLabel,
            String debateAxis,
            String sideAFrame,
            String sideBFrame
    ) {
        this(topic, mode, roundNo, previousMessages, sideALabel, sideBLabel, debateAxis, sideAFrame, sideBFrame, null, null);
    }
}

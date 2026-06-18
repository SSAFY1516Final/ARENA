package com.ssafy.arena.dto.debate;

import com.ssafy.arena.domain.DebateMessage;
import com.ssafy.arena.domain.Speaker;

public record DebateMessageResponse(
        Long messageId,
        Speaker speaker,
        Integer roundNo,
        String content,
        Boolean peakReached
) {
    public static DebateMessageResponse of(DebateMessage message, Boolean peakReached) {
        return new DebateMessageResponse(
                message.getId(),
                message.getSpeaker(),
                message.getRoundNo(),
                message.getContent(),
                peakReached
        );
    }
}

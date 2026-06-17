package com.ssafy.arena.dto.post;


import com.ssafy.arena.domain.DebateMode;

public record PostSearchCondition(
        int offset,
        int size,
        String keyword,
        DebateMode mode,
        String sort
) {
}

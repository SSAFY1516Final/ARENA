package com.ssafy.arena.dto.debate;


public record ShareDebateResponse(
        Long postId,
        Long debateId,
        String title,
        String voteOptionA,
        String voteOptionB
) {
}

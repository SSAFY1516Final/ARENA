package com.ssafy.arena.dto.post;


public record VoteResponse(
        Long postId,
        String voteOptionA,
        String voteOptionB,
        Integer voteCountA,
        Integer voteCountB,
        Double voteRatioA,
        Double voteRatioB
) {
}

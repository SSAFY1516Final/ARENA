package com.ssafy.arena.dto.post;


import com.ssafy.arena.dto.comment.CommentResponse;
import com.ssafy.arena.domain.DebateMessage;
import com.ssafy.arena.dto.debate.DebateSummaryResponse;
import java.util.List;

public record PostDetailResponse(
        PostListItem post,
        DebateSummaryResponse summary,
        List<DebateMessage> messages,
        List<CommentResponse> comments
) {
}

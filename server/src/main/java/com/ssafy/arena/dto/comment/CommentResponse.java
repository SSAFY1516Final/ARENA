package com.ssafy.arena.dto.comment;

import com.ssafy.arena.domain.*;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CommentResponse {
    private Long commentId;
    private Long postId;
    private String authorNickname;
    private String content;
    private LocalDateTime createdAt;
}

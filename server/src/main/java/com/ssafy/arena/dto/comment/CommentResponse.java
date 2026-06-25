package com.ssafy.arena.dto.comment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CommentResponse {
    private Long commentId;
    private Long postId;
    @JsonIgnore
    private Long authorUserId;
    private String authorNickname;
    private String content;
    private Boolean isOwner = false;
    private LocalDateTime createdAt;
}

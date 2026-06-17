package com.ssafy.arena.domain;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Post {
    private Long id;
    private Long debateSessionId;
    private Long userId;
    private String title;
    private String summaryCard;
    private String voteOptionA;
    private String voteOptionB;
    private Boolean isPublic;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

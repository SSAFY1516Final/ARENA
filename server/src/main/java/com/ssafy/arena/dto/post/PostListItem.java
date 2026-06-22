package com.ssafy.arena.dto.post;


import com.ssafy.arena.domain.DebateMode;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class PostListItem {
    private Long postId;
    private String title;
    private DebateMode mode;
    private String summaryCard;
    private String authorNickname;
    private Integer commentCount;
    private String voteOptionA;
    private String voteOptionB;
    private Integer voteCountA;
    private Integer voteCountB;
    private Double voteRatioA;
    private Double voteRatioB;
    private LocalDateTime createdAt;
}

package com.ssafy.arena.dto.debate;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ShareDebateRequest(
        @NotBlank @Size(max = 120) String title,
        @NotBlank @Size(max = 80) String voteOptionA,
        @NotBlank @Size(max = 80) String voteOptionB,
        @NotNull Boolean isPublic,
        @Min(1) Integer roundNo,
        @Size(max = 5000) String body
) {
    public ShareDebateRequest(String title, String voteOptionA, String voteOptionB, Boolean isPublic) {
        this(title, voteOptionA, voteOptionB, isPublic, null, null);
    }

    public ShareDebateRequest(String title, String voteOptionA, String voteOptionB, Boolean isPublic, String body) {
        this(title, voteOptionA, voteOptionB, isPublic, null, body);
    }
}

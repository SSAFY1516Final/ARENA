package com.ssafy.arena.dto.debate;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ShareDebateRequest(
        @NotBlank @Size(max = 120) String title,
        @NotBlank @Size(max = 80) String voteOptionA,
        @NotBlank @Size(max = 80) String voteOptionB,
        @NotNull Boolean isPublic
) {
}

package com.ssafy.arena.dto.comment;

import com.ssafy.arena.domain.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCommentRequest(
        @NotBlank @Size(max = 1000) String content
) {
}

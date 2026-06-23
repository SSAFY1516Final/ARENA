package com.ssafy.arena.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateNicknameRequest(
        @NotBlank
        @Size(min = 2, max = 20)
        @Pattern(regexp = "^[가-힣a-zA-Z0-9_]+$")
        String nickname
) {
}

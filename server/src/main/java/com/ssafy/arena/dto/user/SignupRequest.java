package com.ssafy.arena.dto.user;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @NotBlank @Size(max = 50) String loginId,
        @NotBlank @Size(max = 50) String nickname,
        @NotBlank @Size(min = 8, max = 100) String password
) {
}

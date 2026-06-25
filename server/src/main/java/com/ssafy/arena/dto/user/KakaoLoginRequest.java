package com.ssafy.arena.dto.user;

import jakarta.validation.constraints.NotBlank;

public record KakaoLoginRequest(
        @NotBlank String code,
        String redirectUri
) {
}

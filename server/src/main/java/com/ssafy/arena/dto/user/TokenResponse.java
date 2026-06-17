package com.ssafy.arena.dto.user;

public record TokenResponse(
        String tokenType,
        String accessToken
) {
    public static TokenResponse bearer(String accessToken) {
        return new TokenResponse("Bearer", accessToken);
    }
}

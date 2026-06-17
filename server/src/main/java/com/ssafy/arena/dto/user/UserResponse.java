package com.ssafy.arena.dto.user;

import com.ssafy.arena.domain.User;

public record UserResponse(
        Long userId,
        String loginId,
        String nickname
) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getLoginId(), user.getNickname());
    }
}

package com.ssafy.arena.dto.admin;

import com.ssafy.arena.domain.User;
import com.ssafy.arena.domain.UserRole;
import java.time.LocalDateTime;

public record AdminUserResponse(
        Long userId,
        String loginId,
        String nickname,
        UserRole role,
        LocalDateTime createdAt
) {
    public static AdminUserResponse from(User user) {
        return new AdminUserResponse(
                user.getId(),
                user.getLoginId(),
                user.getNickname(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}

package com.ssafy.arena.dto.admin;

import com.ssafy.arena.domain.UserRole;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRoleRequest(
        @NotNull UserRole role
) {
}

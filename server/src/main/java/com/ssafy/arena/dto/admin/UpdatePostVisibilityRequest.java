package com.ssafy.arena.dto.admin;

import jakarta.validation.constraints.NotNull;

public record UpdatePostVisibilityRequest(
        @NotNull Boolean isPublic
) {
}

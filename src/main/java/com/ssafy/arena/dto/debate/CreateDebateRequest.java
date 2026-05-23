package com.ssafy.arena.dto.debate;

import com.ssafy.arena.domain.DebateMode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateDebateRequest(
        @NotBlank @Size(max = 255) String topic,
        @NotNull DebateMode mode
) {
}

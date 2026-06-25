package com.ssafy.arena.dto.post;

import com.ssafy.arena.domain.VoteChoice;

import jakarta.validation.constraints.NotNull;

public record VoteRequest(@NotNull VoteChoice choice) {
}

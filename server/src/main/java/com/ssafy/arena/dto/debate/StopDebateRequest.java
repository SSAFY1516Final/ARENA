package com.ssafy.arena.dto.debate;

import com.ssafy.arena.domain.Speaker;
import jakarta.validation.constraints.Min;

public record StopDebateRequest(
        Speaker selectedSide,
        @Min(1) Integer selectedRoundNo
) {
}

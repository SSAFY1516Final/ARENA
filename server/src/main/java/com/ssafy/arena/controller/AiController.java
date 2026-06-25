package com.ssafy.arena.controller;

import com.ssafy.arena.ai.AiRoundCandidateService;
import com.ssafy.arena.domain.UserPrincipal;
import com.ssafy.arena.dto.ai.RoundCandidatesRequest;
import com.ssafy.arena.dto.ai.RoundCandidatesResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {
    private final AiRoundCandidateService aiRoundCandidateService;

    @PostMapping("/round-candidates")
    public RoundCandidatesResponse roundCandidates(
            @AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestBody RoundCandidatesRequest request
    ) {
        return aiRoundCandidateService.generate(user.getId(), request);
    }

    @GetMapping("/round-candidates/runs/{runId}")
    public RoundCandidatesResponse roundCandidateRun(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable Long runId
    ) {
        return aiRoundCandidateService.getExistingRun(user.getId(), runId);
    }
}

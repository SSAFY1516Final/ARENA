package com.ssafy.arena.controller;

import com.ssafy.arena.domain.UserPrincipal;
import com.ssafy.arena.dto.debate.*;
import com.ssafy.arena.service.DebateService;
import com.ssafy.arena.service.InitialTurnGenerationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/debates")
@RequiredArgsConstructor
public class DebateController {
    private final DebateService debateService;
    private final InitialTurnGenerationService initialTurnGenerationService;

    @GetMapping
    public List<DebateListItem> list(@AuthenticationPrincipal UserPrincipal user) {
        return debateService.listMyDebates(user.getId());
    }

    @GetMapping("/{debateId}")
    public DebateDetailResponse detail(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable Long debateId
    ) {
        return debateService.detail(user.getId(), debateId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateDebateResponse create(
            @AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestBody CreateDebateRequest request
    ) {
        return CreateDebateResponse.from(debateService.create(user.getId(), request));
    }

    @PostMapping("/{debateId}/turns")
    public DebateMessageResponse nextTurn(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable Long debateId
    ) {
        return debateService.nextTurn(user.getId(), debateId);
    }

    @PostMapping("/{debateId}/turns/batch")
    public InitialTurnGenerationResponse generateInitialTurns(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable Long debateId
    ) {
        return initialTurnGenerationService.requestGeneration(user.getId(), debateId);
    }

    @PostMapping("/{debateId}/stop")
    public StopDebateResponse stop(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable Long debateId,
            @Valid @RequestBody(required = false) StopDebateRequest request
    ) {
        return debateService.stop(user.getId(), debateId, request);
    }

    @PostMapping("/{debateId}/share")
    @ResponseStatus(HttpStatus.CREATED)
    public ShareDebateResponse share(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable Long debateId,
            @Valid @RequestBody ShareDebateRequest request
    ) {
        return debateService.share(user.getId(), debateId, request);
    }

    @DeleteMapping("/{debateId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable Long debateId
    ) {
        debateService.delete(user.getId(), debateId);
    }
}

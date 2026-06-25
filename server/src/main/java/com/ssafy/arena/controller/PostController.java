package com.ssafy.arena.controller;

import com.ssafy.arena.domain.DebateMode;
import com.ssafy.arena.domain.UserPrincipal;
import com.ssafy.arena.dto.post.*;
import com.ssafy.arena.service.PostService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @GetMapping
    public PostListResponse list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) DebateMode mode,
            @RequestParam(defaultValue = "latest") String sort
    ) {
        return postService.list(page, size, keyword, mode, sort);
    }

    @GetMapping("/{postId}")
    public PostDetailResponse detail(@AuthenticationPrincipal UserPrincipal user, @PathVariable Long postId) {
        return postService.detail(postId, user == null ? null : user.getId());
    }

    @DeleteMapping("/{postId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal UserPrincipal user, @PathVariable Long postId) {
        postService.delete(user.getId(), postId);
    }

    @PostMapping("/{postId}/votes")
    public VoteResponse vote(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable Long postId,
            @Valid @RequestBody VoteRequest request
    ) {
        return postService.vote(user.getId(), postId, request);
    }
}

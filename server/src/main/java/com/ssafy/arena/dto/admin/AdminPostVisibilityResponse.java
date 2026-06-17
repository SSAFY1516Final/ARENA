package com.ssafy.arena.dto.admin;

import com.ssafy.arena.domain.Post;

public record AdminPostVisibilityResponse(
        Long postId,
        boolean isPublic
) {
    public static AdminPostVisibilityResponse from(Post post) {
        return new AdminPostVisibilityResponse(post.getId(), Boolean.TRUE.equals(post.getIsPublic()));
    }
}

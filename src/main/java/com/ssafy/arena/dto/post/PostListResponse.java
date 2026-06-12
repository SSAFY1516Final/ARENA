package com.ssafy.arena.dto.post;


import java.util.List;

public record PostListResponse(
        List<PostListItem> items,
        int page,
        int size,
        int totalCount
) {
}

package com.ssafy.arena.service;

import com.ssafy.arena.domain.Comment;
import com.ssafy.arena.dto.comment.*;
import com.ssafy.arena.common.ApiException;
import com.ssafy.arena.mapper.CommentMapper;
import com.ssafy.arena.mapper.PostMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {
    private final CommentMapper commentMapper;
    private final PostMapper postMapper;

    @Transactional
    public CommentResponse create(Long userId, Long postId, CreateCommentRequest request) {
        if (postMapper.findById(postId) == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "post not found");
        }
        Comment comment = Comment.builder()
                .postId(postId)
                .userId(userId)
                .content(request.content())
                .build();
        commentMapper.insert(comment);
        return commentMapper.findByPostId(postId).stream()
                .filter(item -> item.getCommentId().equals(comment.getId()))
                .findFirst()
                .orElseThrow(() -> {
                    log.error("Comment was inserted but response lookup failed. commentId={}, postId={}, userId={}",
                            comment.getId(), postId, userId);
                    return new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "comment creation failed");
                });
    }

    @Transactional
    public void delete(Long userId, Long commentId) {
        Comment comment = commentMapper.findById(commentId);
        if (comment == null || comment.getDeletedAt() != null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "comment not found");
        }
        if (!comment.getUserId().equals(userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "not comment owner");
        }
        commentMapper.softDelete(commentId);
    }
}

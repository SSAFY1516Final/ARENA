package com.ssafy.arena.mapper;

import com.ssafy.arena.domain.Comment;
import com.ssafy.arena.dto.comment.*;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CommentMapper {
    void insert(Comment comment);

    Comment findById(@Param("id") Long id);

    List<CommentResponse> findByPostId(@Param("postId") Long postId);

    void softDelete(@Param("id") Long id);
}

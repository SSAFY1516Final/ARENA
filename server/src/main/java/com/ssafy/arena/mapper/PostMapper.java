package com.ssafy.arena.mapper;

import com.ssafy.arena.domain.Post;
import com.ssafy.arena.domain.VoteChoice;
import com.ssafy.arena.dto.post.*;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PostMapper {
    void insert(Post post);

    Post findById(@Param("id") Long id);

    PostListItem findListItemById(@Param("id") Long id);

    List<PostListItem> findPosts(PostSearchCondition condition);

    int countPosts(PostSearchCondition condition);

    void deleteById(@Param("id") Long id);

    void updateVisibility(@Param("id") Long id, @Param("isPublic") boolean isPublic);

    Integer countVotes(@Param("postId") Long postId, @Param("choice") VoteChoice choice);

    void upsertVote(@Param("postId") Long postId, @Param("userId") Long userId, @Param("choice") VoteChoice choice);
}

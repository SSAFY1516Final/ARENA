package com.ssafy.arena.service;

import com.ssafy.arena.domain.*;
import com.ssafy.arena.dto.post.*;
import com.ssafy.arena.mapper.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ssafy.arena.mapper.CommentMapper;
import com.ssafy.arena.mapper.DebateMapper;
import com.ssafy.arena.common.ApiException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {
    @Mock
    private PostMapper postMapper;

    @Mock
    private DebateMapper debateMapper;

    @Mock
    private CommentMapper commentMapper;

    @InjectMocks
    private PostService postService;

    @Test
    void listClampsPagingAndFallsBackToLatestSort() {
        when(postMapper.findPosts(any())).thenReturn(List.of());
        when(postMapper.countPosts(any())).thenReturn(0);

        PostListResponse response = postService.list(0, 100, "  ", null, "unknown");

        ArgumentCaptor<PostSearchCondition> captor = ArgumentCaptor.forClass(PostSearchCondition.class);
        verify(postMapper).findPosts(captor.capture());
        PostSearchCondition condition = captor.getValue();
        assertThat(response.page()).isEqualTo(1);
        assertThat(response.size()).isEqualTo(50);
        assertThat(condition.offset()).isZero();
        assertThat(condition.size()).isEqualTo(50);
        assertThat(condition.keyword()).isNull();
        assertThat(condition.sort()).isEqualTo("latest");
    }

    @Test
    void voteUpsertsChoiceAndReturnsVoteRatios() {
        Post post = Post.builder()
                .id(5L)
                .voteOptionA("제육")
                .voteOptionB("돈까스")
                .build();
        when(postMapper.findById(5L)).thenReturn(post);
        when(postMapper.countVotes(5L, VoteChoice.A)).thenReturn(3);
        when(postMapper.countVotes(5L, VoteChoice.B)).thenReturn(1);

        VoteResponse response = postService.vote(7L, 5L, new VoteRequest(VoteChoice.A));

        verify(postMapper).upsertVote(5L, 7L, VoteChoice.A);
        assertThat(response.voteCountA()).isEqualTo(3);
        assertThat(response.voteCountB()).isEqualTo(1);
        assertThat(response.voteRatioA()).isEqualTo(75.0);
        assertThat(response.voteRatioB()).isEqualTo(25.0);
    }

    @Test
    void updateVisibilityChangesPostPublicFlagForAdmin() {
        Post post = Post.builder()
                .id(5L)
                .isPublic(true)
                .build();
        when(postMapper.findById(5L)).thenReturn(post);

        Post updated = postService.updateVisibility(5L, false);

        verify(postMapper).updateVisibility(5L, false);
        assertThat(updated.getIsPublic()).isFalse();
    }

    @Test
    void detailRejectsPrivatePost() {
        when(postMapper.findById(5L)).thenReturn(Post.builder()
                .id(5L)
                .isPublic(false)
                .build());

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> postService.detail(5L))
                .isInstanceOf(ApiException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.NOT_FOUND);
    }
}

package com.ssafy.arena.service;

import com.ssafy.arena.domain.*;
import com.ssafy.arena.dto.comment.CommentResponse;
import com.ssafy.arena.dto.post.*;
import com.ssafy.arena.mapper.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
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
import org.mockito.InOrder;
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
    void deleteOwnedPostRemovesVotesAndCommentsBeforePost() {
        when(postMapper.findById(5L)).thenReturn(Post.builder()
                .id(5L)
                .userId(7L)
                .build());

        postService.delete(7L, 5L);

        InOrder inOrder = inOrder(postMapper);
        inOrder.verify(postMapper).deleteVotesByPostId(5L);
        inOrder.verify(postMapper).deleteCommentsByPostId(5L);
        inOrder.verify(postMapper).deleteById(5L);
    }

    @Test
    void detailRejectsPrivatePost() {
        when(postMapper.findById(5L)).thenReturn(Post.builder()
                .id(5L)
                .isPublic(false)
                .build());

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> postService.detail(5L, 7L))
                .isInstanceOf(ApiException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void detailIncludesViewerVoteChoiceAndOwnershipFlags() {
        Post post = Post.builder()
                .id(5L)
                .userId(7L)
                .debateSessionId(11L)
                .shareRoundNo(2)
                .isPublic(true)
                .build();
        PostListItem item = new PostListItem();
        item.setPostId(5L);
        item.setVoteOptionA("제육");
        item.setVoteOptionB("돈까스");
        CommentResponse ownComment = new CommentResponse();
        ownComment.setCommentId(30L);
        ownComment.setAuthorUserId(7L);
        CommentResponse otherComment = new CommentResponse();
        otherComment.setCommentId(31L);
        otherComment.setAuthorUserId(8L);

        when(postMapper.findById(5L)).thenReturn(post);
        when(postMapper.findListItemById(5L)).thenReturn(item);
        when(postMapper.findVoteChoice(5L, 7L)).thenReturn(VoteChoice.B);
        when(debateMapper.findSummary(11L)).thenReturn(DebateSummary.builder()
                .summaryText("돈까스가 안정적입니다.")
                .build());
        when(debateMapper.findSessionById(11L)).thenReturn(DebateSession.builder()
                .id(11L)
                .selectedSide(Speaker.PASSIONATE)
                .roundTitle("점심 안정성")
                .topic("오늘 점심 제육 vs 돈까스, 지금 바로 선택해야 한다면 무엇이 더 나은가")
                .debateAxis("안정성 vs 만족감")
                .build());
        when(debateMapper.findMessages(11L)).thenReturn(List.of());
        when(commentMapper.findByPostId(5L)).thenReturn(List.of(ownComment, otherComment));

        PostDetailResponse response = postService.detail(5L, 7L);

        assertThat(response.post().getUserVoteChoice()).isEqualTo(VoteChoice.B);
        assertThat(response.post().getIsOwner()).isTrue();
        assertThat(response.round().roundNo()).isEqualTo(2);
        assertThat(response.round().title()).isEqualTo("점심 안정성");
        assertThat(response.round().topic()).contains("제육 vs 돈까스");
        assertThat(response.round().description()).isEqualTo("안정성 vs 만족감");
        assertThat(response.round().summary().summaryText()).isEqualTo("돈까스가 안정적입니다.");
        assertThat(response.comments()).extracting(CommentResponse::getIsOwner)
                .containsExactly(true, false);
    }
}

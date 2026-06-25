package com.ssafy.arena.service;

import com.ssafy.arena.domain.*;
import com.ssafy.arena.dto.comment.*;
import com.ssafy.arena.dto.debate.*;
import com.ssafy.arena.dto.post.*;
import com.ssafy.arena.common.ApiException;
import com.ssafy.arena.mapper.CommentMapper;
import com.ssafy.arena.mapper.DebateMapper;
import com.ssafy.arena.mapper.PostMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {
    private final PostMapper postMapper;
    private final DebateMapper debateMapper;
    private final CommentMapper commentMapper;

    public PostListResponse list(int page, int size, String keyword, DebateMode mode, String sort) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 50);
        String safeSort = switch (sort == null ? "latest" : sort) {
            case "comments", "votes" -> sort;
            default -> "latest";
        };
        PostSearchCondition condition = new PostSearchCondition(
                (safePage - 1) * safeSize,
                safeSize,
                blankToNull(keyword),
                mode,
                safeSort
        );
        return new PostListResponse(
                postMapper.findPosts(condition),
                safePage,
                safeSize,
                postMapper.countPosts(condition)
        );
    }

    public PostDetailResponse detail(Long postId, Long viewerUserId) {
        Post post = requirePost(postId);
        if (!Boolean.TRUE.equals(post.getIsPublic())) {
            throw new ApiException(HttpStatus.NOT_FOUND, "post not found");
        }
        PostListItem item = postMapper.findListItemById(postId);
        if (viewerUserId != null) {
            item.setUserVoteChoice(postMapper.findVoteChoice(postId, viewerUserId));
            item.setIsOwner(post.getUserId().equals(viewerUserId));
        } else {
            item.setIsOwner(false);
        }
        DebateSummary summary = debateMapper.findSummary(post.getDebateSessionId());
        DebateSummaryResponse summaryResponse = summary == null ? null : DebateSummaryResponse.from(summary);
        DebateRoundResponse roundResponse = sharedRoundResponse(post, summaryResponse);
        List<DebateMessage> messages = debateMapper.findMessages(post.getDebateSessionId());
        List<CommentResponse> comments = commentMapper.findByPostId(postId);
        comments.forEach((comment) -> comment.setIsOwner(
                viewerUserId != null && viewerUserId.equals(comment.getAuthorUserId())
        ));
        return new PostDetailResponse(item, summaryResponse, roundResponse, messages, comments);
    }

    @Transactional
    public void delete(Long userId, Long postId) {
        Post post = requirePost(postId);
        if (!post.getUserId().equals(userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "not post owner");
        }
        postMapper.deleteVotesByPostId(postId);
        postMapper.deleteCommentsByPostId(postId);
        postMapper.deleteById(postId);
    }

    @Transactional
    public VoteResponse vote(Long userId, Long postId, VoteRequest request) {
        Post post = requirePost(postId);
        postMapper.upsertVote(postId, userId, request.choice());
        return voteSummary(post);
    }

    @Transactional
    public Post updateVisibility(Long postId, boolean isPublic) {
        Post post = requirePost(postId);
        postMapper.updateVisibility(postId, isPublic);
        post.setIsPublic(isPublic);
        return post;
    }

    private VoteResponse voteSummary(Post post) {
        int countA = nullToZero(postMapper.countVotes(post.getId(), VoteChoice.A));
        int countB = nullToZero(postMapper.countVotes(post.getId(), VoteChoice.B));
        int total = countA + countB;
        double ratioA = total == 0 ? 0.0 : Math.round((countA * 1000.0 / total)) / 10.0;
        double ratioB = total == 0 ? 0.0 : Math.round((countB * 1000.0 / total)) / 10.0;
        return new VoteResponse(post.getId(), post.getVoteOptionA(), post.getVoteOptionB(), countA, countB, ratioA, ratioB);
    }

    private Post requirePost(Long postId) {
        Post post = postMapper.findById(postId);
        if (post == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "post not found");
        }
        return post;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private DebateRoundResponse sharedRoundResponse(Post post, DebateSummaryResponse summaryResponse) {
        DebateSession session = debateMapper.findSessionById(post.getDebateSessionId());
        if (session == null) {
            return null;
        }
        return new DebateRoundResponse(
                post.getShareRoundNo() == null || post.getShareRoundNo() < 1 ? 1 : post.getShareRoundNo(),
                session.getId(),
                session.getSelectedSide(),
                firstPresent(session.getRoundTitle(), session.getTopic(), session.getOriginalTopic()),
                session.getTopic(),
                firstPresent(session.getDebateAxis(), session.getBasicConditions(), ""),
                summaryResponse
        );
    }

    private String firstPresent(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    private int nullToZero(Integer value) {
        return value == null ? 0 : value;
    }
}

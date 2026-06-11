package com.ssafy.arena.service;

import com.ssafy.arena.domain.*;
import com.ssafy.arena.dto.ai.*;
import com.ssafy.arena.dto.debate.*;
import com.ssafy.arena.ai.AiClient;
import com.ssafy.arena.common.ApiException;
import com.ssafy.arena.mapper.DebateMapper;
import com.ssafy.arena.mapper.PostMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DebateService {
    private final DebateMapper debateMapper;
    private final PostMapper postMapper;
    private final AiClient aiClient;

    @Transactional
    public DebateSession create(Long userId, CreateDebateRequest request) {
        DebateSession session = DebateSession.builder()
                .userId(userId)
                .topic(request.topic())
                .mode(request.mode())
                .status(DebateStatus.ACTIVE)
                .peakReached(false)
                .build();
        debateMapper.insertSession(session);
        return session;
    }

    public List<DebateListItem> listMyDebates(Long userId) {
        return debateMapper.findSessionsByUserId(userId).stream()
                .map(DebateListItem::from)
                .toList();
    }

    public DebateDetailResponse detail(Long userId, Long debateId) {
        DebateSession session = getOwnedSession(userId, debateId);
        DebateSummary summary = debateMapper.findSummary(debateId);
        DebateSummaryResponse summaryResponse = summary == null ? null : DebateSummaryResponse.from(summary);
        return new DebateDetailResponse(
                CreateDebateResponse.from(session),
                debateMapper.findMessages(debateId),
                summaryResponse
        );
    }

    @Transactional
    public DebateMessageResponse nextTurn(Long userId, Long debateId) {
        DebateSession session = getOwnedSession(userId, debateId);
        requireStatus(session, DebateStatus.ACTIVE);

        List<DebateMessage> messages = debateMapper.findMessages(debateId);
        Integer roundNo = debateMapper.findNextRoundNo(debateId);
        AiNextTurnResponse aiResponse = aiClient.nextTurn(new AiNextTurnRequest(
                session.getTopic(),
                session.getMode(),
                roundNo,
                toPayload(messages)
        ));

        DebateMessage message = DebateMessage.builder()
                .debateSessionId(debateId)
                .speaker(aiResponse.speaker())
                .roundNo(roundNo)
                .content(aiResponse.content())
                .build();
        debateMapper.insertMessage(message);

        boolean peakReached = Boolean.TRUE.equals(aiResponse.peakReached());
        if (peakReached) {
            debateMapper.updatePeakReached(debateId, true);
        }
        return DebateMessageResponse.of(message, peakReached);
    }

    @Transactional
    public StopDebateResponse stop(Long userId, Long debateId) {
        DebateSession session = getOwnedSession(userId, debateId);
        requireStatus(session, DebateStatus.ACTIVE);

        List<DebateMessage> messages = debateMapper.findMessages(debateId);
        AiSummaryResponse aiSummary = aiClient.summarize(new AiSummaryRequest(
                session.getTopic(),
                session.getMode(),
                toPayload(messages)
        ));

        debateMapper.updateSessionStatus(debateId, DebateStatus.STOPPED);
        DebateSummary summary = DebateSummary.builder()
                .debateSessionId(debateId)
                .coreArguments(aiSummary.coreArguments())
                .highlight(aiSummary.highlight())
                .decisionCriteria(aiSummary.decisionCriteria())
                .remainingIssue(aiSummary.remainingIssue())
                .summaryText(aiSummary.summaryText())
                .build();
        debateMapper.insertSummary(summary);
        return new StopDebateResponse(debateId, DebateStatus.STOPPED, DebateSummaryResponse.from(summary));
    }

    @Transactional
    public ShareDebateResponse share(Long userId, Long debateId, ShareDebateRequest request) {
        DebateSession session = getOwnedSession(userId, debateId);
        requireStatus(session, DebateStatus.STOPPED);
        DebateSummary summary = debateMapper.findSummary(debateId);
        if (summary == null) {
            log.warn("Debate share requested without summary. debateId={}, userId={}", debateId, userId);
            throw new ApiException(HttpStatus.CONFLICT, "debate summary is required before sharing");
        }

        Post post = Post.builder()
                .debateSessionId(debateId)
                .userId(userId)
                .title(request.title())
                .summaryCard(summary.getSummaryText())
                .voteOptionA(request.voteOptionA())
                .voteOptionB(request.voteOptionB())
                .isPublic(request.isPublic())
                .build();
        postMapper.insert(post);
        debateMapper.updateSessionStatus(debateId, DebateStatus.SHARED);
        return new ShareDebateResponse(post.getId(), debateId, post.getTitle(), post.getVoteOptionA(), post.getVoteOptionB());
    }

    private DebateSession getOwnedSession(Long userId, Long debateId) {
        DebateSession session = debateMapper.findSessionById(debateId);
        if (session == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "debate not found");
        }
        if (!session.getUserId().equals(userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "not debate owner");
        }
        return session;
    }

    private void requireStatus(DebateSession session, DebateStatus status) {
        if (session.getStatus() != status) {
            throw new ApiException(HttpStatus.CONFLICT, "invalid debate status");
        }
    }

    private List<AiMessagePayload> toPayload(List<DebateMessage> messages) {
        return messages.stream()
                .map(message -> new AiMessagePayload(message.getSpeaker(), message.getRoundNo(), message.getContent()))
                .toList();
    }
}

package com.ssafy.arena.service;

import com.ssafy.arena.domain.*;
import com.ssafy.arena.dto.ai.*;
import com.ssafy.arena.dto.debate.*;
import com.ssafy.arena.ai.AiClient;
import com.ssafy.arena.ai.TopicSideLabelNormalizer;
import com.ssafy.arena.common.ApiException;
import com.ssafy.arena.mapper.AiDebateTurnLogMapper;
import com.ssafy.arena.mapper.DebateMapper;
import com.ssafy.arena.mapper.PostMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DebateService {
    private static final int INITIAL_TURN_COUNT = 10;

    private final DebateMapper debateMapper;
    private final PostMapper postMapper;
    private final AiClient aiClient;
    private final AiDebateTurnLogMapper aiDebateTurnLogMapper;

    @Value("${spring.ai.openai.chat.options.model:gpt-5.4-mini}")
    private String aiModel = "gpt-5.4-mini";

    @Transactional
    public DebateSession create(Long userId, CreateDebateRequest request) {
        DebateSession session = DebateSession.builder()
                .userId(userId)
                .originalTopic(request.originalTopic())
                .topic(request.topic())
                .sideALabel(TopicSideLabelNormalizer.normalize(request.sideALabel()))
                .sideBLabel(TopicSideLabelNormalizer.normalize(request.sideBLabel()))
                .debateAxis(request.debateAxis())
                .sideAFrame(request.sideAFrame())
                .sideBFrame(request.sideBFrame())
                .selectedRoundId(request.selectedRoundId())
                .roundTitle(request.roundTitle())
                .basicConditions(request.basicConditions())
                .candidateRunId(request.candidateRunId())
                .selectedCandidateId(request.selectedCandidateId())
                .mode(request.mode())
                .status(DebateStatus.ACTIVE)
                .peakReached(false)
                .build();
        debateMapper.insertSession(session);
        return session;
    }

    public List<DebateListItem> listMyDebates(Long userId) {
        List<DebateSession> sessions = new ArrayList<>(findUserSessions(userId));
        sessions.sort(Comparator
                .comparing(DebateService::updatedAtOf)
                .reversed()
                .thenComparing(Comparator.comparing(DebateService::sessionIdOf).reversed()));

        LinkedHashMap<String, DebateSession> latestSessionsByOriginalQuestion = new LinkedHashMap<>();
        sessions.forEach(session ->
                latestSessionsByOriginalQuestion.putIfAbsent(originalQuestionKey(session), session)
        );

        return latestSessionsByOriginalQuestion.values().stream()
                .map(session -> DebateListItem.from(session, roundSessionsFor(sessions, session)))
                .toList();
    }

    private List<DebateSession> roundSessionsFor(List<DebateSession> sessions, DebateSession anchorSession) {
        return sessions.stream()
                .filter(session -> originalQuestionKey(session).equals(originalQuestionKey(anchorSession)))
                .sorted(Comparator
                        .comparing(DebateService::createdAtOf)
                        .thenComparing(DebateService::sessionIdOf))
                .toList();
    }

    private static LocalDateTime updatedAtOf(DebateSession session) {
        if (session.getStoppedAt() != null) {
            return session.getStoppedAt();
        }
        if (session.getCreatedAt() != null) {
            return session.getCreatedAt();
        }
        return LocalDateTime.MIN;
    }

    private static LocalDateTime createdAtOf(DebateSession session) {
        return session.getCreatedAt() == null ? LocalDateTime.MIN : session.getCreatedAt();
    }

    private static Long sessionIdOf(DebateSession session) {
        return session.getId() == null ? Long.MIN_VALUE : session.getId();
    }

    private static String originalQuestionKey(DebateSession session) {
        String originalTopic = normalizeListKey(session.getOriginalTopic());
        return originalTopic.isBlank() ? normalizeListKey(session.getTopic()) : originalTopic;
    }

    private static String normalizeListKey(String value) {
        return value == null ? "" : value.trim();
    }

    private List<DebateSession> findUserSessions(Long userId) {
        List<DebateSession> sessions = debateMapper.findSessionsByUserId(userId);
        return sessions == null ? List.of() : sessions;
    }

    private List<DebateSession> roundSessionsFor(Long userId, DebateSession anchorSession) {
        String anchorKey = originalQuestionKey(anchorSession);
        List<DebateSession> sessions = new ArrayList<>(findUserSessions(userId).stream()
                .filter(session -> originalQuestionKey(session).equals(anchorKey))
                .filter(session -> shouldIncludeRoundSession(session, anchorSession))
                .toList());
        boolean includesAnchor = sessions.stream()
                .anyMatch(session -> sessionIdOf(session).equals(sessionIdOf(anchorSession)));
        if (!includesAnchor) {
            sessions.add(anchorSession);
        }
        sessions.sort(Comparator
                .comparing(DebateService::createdAtOf)
                .thenComparing(DebateService::sessionIdOf));
        return sessions;
    }

    private static boolean shouldIncludeRoundSession(DebateSession session, DebateSession anchorSession) {
        if (sessionIdOf(session).equals(sessionIdOf(anchorSession))) {
            return true;
        }
        if (!isCompletedRoundSession(session)) {
            return false;
        }
        return !createdAtOf(session).isAfter(createdAtOf(anchorSession));
    }

    private static boolean isCompletedRoundSession(DebateSession session) {
        return session.getStatus() == DebateStatus.STOPPED || session.getStatus() == DebateStatus.SHARED;
    }

    private int sessionRoundNo(Long userId, DebateSession session) {
        List<DebateSession> sessions = roundSessionsFor(userId, session);
        for (int index = 0; index < sessions.size(); index += 1) {
            if (sessionIdOf(sessions.get(index)).equals(sessionIdOf(session))) {
                return index + 1;
            }
        }
        return 1;
    }

    private DebateRoundResponse roundResponse(DebateSession session, int roundNo) {
        DebateSummary summary = debateMapper.findSummary(session.getId());
        DebateSummaryResponse summaryResponse = summary == null ? null : DebateSummaryResponse.from(summary);
        return new DebateRoundResponse(
                roundNo,
                session.getId(),
                session.getSelectedSide(),
                firstPresent(session.getRoundTitle(), session.getTopic(), session.getOriginalTopic()),
                session.getTopic(),
                firstPresent(session.getDebateAxis(), session.getBasicConditions(), ""),
                summaryResponse
        );
    }

    private static String firstPresent(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    private static DebateMessage copyMessageWithRoundNo(DebateMessage message, int roundNo) {
        return DebateMessage.builder()
                .id(message.getId())
                .debateSessionId(message.getDebateSessionId())
                .speaker(message.getSpeaker())
                .roundNo(roundNo)
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                .build();
    }

    public DebateDetailResponse detail(Long userId, Long debateId) {
        DebateSession session = getOwnedSession(userId, debateId);
        List<DebateSession> roundSessions = roundSessionsFor(userId, session);
        List<DebateRoundResponse> rounds = new ArrayList<>();
        List<DebateMessage> roundMessages = new ArrayList<>();
        for (int index = 0; index < roundSessions.size(); index += 1) {
            DebateSession roundSession = roundSessions.get(index);
            int roundNo = index + 1;
            rounds.add(roundResponse(roundSession, roundNo));
            debateMapper.findMessages(roundSession.getId()).stream()
                    .map(message -> copyMessageWithRoundNo(message, roundNo))
                    .forEach(roundMessages::add);
        }
        DebateSummary summary = debateMapper.findSummary(debateId);
        DebateSummaryResponse summaryResponse = summary == null ? null : DebateSummaryResponse.from(summary);
        return new DebateDetailResponse(
                CreateDebateResponse.from(session),
                roundMessages,
                rounds,
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
                toPayload(messages),
                session.getSideALabel(),
                session.getSideBLabel(),
                session.getDebateAxis(),
                session.getSideAFrame(),
                session.getSideBFrame(),
                session.getRoundTitle(),
                session.getBasicConditions()
        ));

        DebateMessage message = DebateMessage.builder()
                .debateSessionId(debateId)
                .speaker(aiResponse.speaker())
                .roundNo(roundNo)
                .content(aiResponse.content())
                .build();
        debateMapper.insertMessage(message);
        saveAiTurnLog(debateId, message, aiResponse);

        boolean peakReached = Boolean.TRUE.equals(aiResponse.peakReached());
        if (peakReached) {
            debateMapper.updatePeakReached(debateId, true);
        }
        return DebateMessageResponse.of(message, peakReached);
    }

    @Transactional
    public List<DebateMessageResponse> generateInitialTurns(Long userId, Long debateId) {
        DebateSession session = getOwnedSession(userId, debateId);
        requireStatus(session, DebateStatus.ACTIVE);
        int sessionRoundNo = sessionRoundNo(userId, session);

        List<DebateMessage> existingMessages = new ArrayList<>(debateMapper.findMessages(debateId));
        if (existingMessages.size() >= INITIAL_TURN_COUNT) {
            boolean peakReached = Boolean.TRUE.equals(session.getPeakReached())
                    || existingMessages.size() >= INITIAL_TURN_COUNT;
            return existingMessages.stream()
                    .limit(INITIAL_TURN_COUNT)
                    .map(message -> DebateMessageResponse.of(copyMessageWithRoundNo(message, sessionRoundNo), peakReached))
                    .toList();
        }

        AiRoundResponse aiResponse = aiClient.generateRound(new AiNextTurnRequest(
                session.getTopic(),
                session.getMode(),
                1,
                List.of(),
                session.getSideALabel(),
                session.getSideBLabel(),
                session.getDebateAxis(),
                session.getSideAFrame(),
                session.getSideBFrame(),
                session.getRoundTitle(),
                session.getBasicConditions()
        ));

        int existingTurnCount = Math.min(existingMessages.size(), INITIAL_TURN_COUNT);
        List<DebateMessage> completedMessages = new ArrayList<>(existingMessages);
        for (AiGeneratedTurnResponse turn : aiResponse.turns()) {
            Integer turnIndex = turn.turnIndex();
            if (turnIndex == null || turnIndex < 1 || turnIndex > INITIAL_TURN_COUNT || turnIndex <= existingTurnCount) {
                continue;
            }
            DebateMessage message = DebateMessage.builder()
                    .debateSessionId(debateId)
                    .speaker(turn.speaker())
                    .roundNo(sessionRoundNo)
                    .content(turn.content())
                    .build();
            debateMapper.insertMessage(message);
            saveAiTurnLog(debateId, message, aiResponse);
            completedMessages.add(message);
        }

        completedMessages.sort(Comparator
                .comparing(DebateMessage::getId, Comparator.nullsLast(Long::compareTo)));
        if (completedMessages.size() < INITIAL_TURN_COUNT) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Spring AI round generation returned incomplete turns");
        }

        boolean peakReached = completedMessages.size() >= INITIAL_TURN_COUNT
                || aiResponse.turns().stream().anyMatch(turn -> Boolean.TRUE.equals(turn.peakReached()));
        if (peakReached) {
            debateMapper.updatePeakReached(debateId, true);
        }
        return completedMessages.stream()
                .limit(INITIAL_TURN_COUNT)
                .map(message -> DebateMessageResponse.of(copyMessageWithRoundNo(message, sessionRoundNo), peakReached))
                .toList();
    }

    public InitialTurnGenerationResponse initialTurnGenerationSnapshot(Long userId, Long debateId) {
        DebateSession session = getOwnedSession(userId, debateId);
        requireStatus(session, DebateStatus.ACTIVE);
        int sessionRoundNo = sessionRoundNo(userId, session);
        List<DebateMessage> existingMessages = new ArrayList<>(debateMapper.findMessages(debateId));
        boolean complete = existingMessages.size() >= INITIAL_TURN_COUNT;
        boolean peakReached = Boolean.TRUE.equals(session.getPeakReached()) || complete;
        return new InitialTurnGenerationResponse(
                complete ? InitialTurnGenerationStatus.COMPLETE : InitialTurnGenerationStatus.GENERATING,
                existingMessages.stream()
                        .limit(INITIAL_TURN_COUNT)
                        .map(message -> DebateMessageResponse.of(copyMessageWithRoundNo(message, sessionRoundNo), peakReached))
                        .toList()
        );
    }

    @Transactional
    public StopDebateResponse stop(Long userId, Long debateId) {
        return stop(userId, debateId, null);
    }

    @Transactional
    public StopDebateResponse stop(Long userId, Long debateId, StopDebateRequest request) {
        DebateSession session = getOwnedSession(userId, debateId);
        requireStatus(session, DebateStatus.ACTIVE);

        List<DebateMessage> messages = debateMapper.findMessages(debateId);
        if (messages.size() < INITIAL_TURN_COUNT) {
            throw new ApiException(HttpStatus.CONFLICT, "complete debate messages are required before stopping");
        }
        AiSummaryResponse aiSummary = aiClient.summarize(new AiSummaryRequest(
                session.getTopic(),
                session.getMode(),
                toPayload(messages)
        ));

        debateMapper.updateSessionStatus(debateId, DebateStatus.STOPPED);
        Speaker selectedSide = request == null ? null : request.selectedSide();
        Integer selectedRoundNo = request == null ? null : request.selectedRoundNo();
        if (selectedSide != null || selectedRoundNo != null) {
            debateMapper.updateSelection(debateId, selectedSide, selectedRoundNo);
        }
        DebateSummary summary = DebateSummary.builder()
                .debateSessionId(debateId)
                .coreArguments(aiSummary.coreArguments())
                .highlight(aiSummary.highlight())
                .decisionCriteria(aiSummary.decisionCriteria())
                .remainingIssue(aiSummary.remainingIssue())
                .summaryText(aiSummary.summaryText())
                .build();
        debateMapper.insertSummary(summary);
        return new StopDebateResponse(
                debateId,
                DebateStatus.STOPPED,
                selectedSide,
                selectedRoundNo,
                DebateSummaryResponse.from(summary)
        );
    }

    @Transactional
    public ShareDebateResponse share(Long userId, Long debateId, ShareDebateRequest request) {
        DebateSession anchorSession = getOwnedSession(userId, debateId);
        requireShareableStatus(anchorSession);
        int shareRoundNo = normalizeShareRoundNo(request.roundNo());
        DebateSession targetSession = shareTargetSession(userId, anchorSession, shareRoundNo);
        requireShareableStatus(targetSession);
        DebateSummary summary = debateMapper.findSummary(targetSession.getId());
        if (summary == null) {
            log.warn("Debate share requested without summary. debateId={}, userId={}", targetSession.getId(), userId);
            throw new ApiException(HttpStatus.CONFLICT, "debate summary is required before sharing");
        }

        Post post = Post.builder()
                .debateSessionId(targetSession.getId())
                .userId(userId)
                .shareRoundNo(shareRoundNo)
                .title(sharePostTitle(targetSession, request.title()))
                .summaryCard(summary.getSummaryText())
                .shareBody(sharePostBody(request.body()))
                .voteOptionA(request.voteOptionA())
                .voteOptionB(request.voteOptionB())
                .isPublic(request.isPublic())
                .build();
        postMapper.insert(post);
        debateMapper.updateSessionStatus(targetSession.getId(), DebateStatus.SHARED);
        return new ShareDebateResponse(post.getId(), targetSession.getId(), post.getTitle(), post.getVoteOptionA(), post.getVoteOptionB());
    }

    @Transactional
    public void delete(Long userId, Long debateId) {
        getOwnedSession(userId, debateId);
        debateMapper.deletePostVotesByDebateId(debateId);
        debateMapper.deleteCommentsByDebateId(debateId);
        debateMapper.deletePostByDebateId(debateId);
        debateMapper.deleteAiDebateTurnLogsByDebateId(debateId);
        debateMapper.deleteSummaryByDebateId(debateId);
        debateMapper.deleteMessagesByDebateId(debateId);
        debateMapper.deleteSessionById(debateId);
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

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private void requireStatus(DebateSession session, DebateStatus status) {
        if (session.getStatus() != status) {
            throw new ApiException(HttpStatus.CONFLICT, "invalid debate status");
        }
    }

    private void requireShareableStatus(DebateSession session) {
        if (session.getStatus() != DebateStatus.STOPPED && session.getStatus() != DebateStatus.SHARED) {
            throw new ApiException(HttpStatus.CONFLICT, "invalid debate status");
        }
    }

    private int normalizeShareRoundNo(Integer roundNo) {
        return roundNo == null || roundNo < 1 ? 1 : roundNo;
    }

    private DebateSession shareTargetSession(Long userId, DebateSession anchorSession, int shareRoundNo) {
        List<DebateSession> sessions = roundSessionsFor(userId, anchorSession);
        if (shareRoundNo > sessions.size()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "shared round not found");
        }
        return sessions.get(shareRoundNo - 1);
    }

    private String sharePostTitle(DebateSession session, String fallbackTitle) {
        return firstPresent(session.getOriginalTopic(), session.getTopic(), fallbackTitle);
    }

    private String sharePostBody(String userBody) {
        return blankToNull(userBody);
    }

    private List<AiMessagePayload> toPayload(List<DebateMessage> messages) {
        return messages.stream()
                .map(message -> new AiMessagePayload(message.getSpeaker(), message.getRoundNo(), message.getContent()))
                .toList();
    }

    private void saveAiTurnLog(Long debateId, DebateMessage message, AiNextTurnResponse aiResponse) {
        if (aiResponse.promptName() == null) {
            return;
        }
        aiDebateTurnLogMapper.insert(AiDebateTurnLog.builder()
                .debateSessionId(debateId)
                .debateMessageId(message.getId())
                .roundNo(message.getRoundNo())
                .speaker(message.getSpeaker())
                .promptName(aiResponse.promptName())
                .model(aiModel)
                .renderedPrompt(aiResponse.renderedPrompt())
                .rawResponse(aiResponse.rawResponse())
                .parsedResponseJson(aiResponse.parsedResponseJson())
                .status("SUCCEEDED")
                .latencyMs(aiResponse.latencyMs())
                .build());
    }

    private void saveAiTurnLog(Long debateId, DebateMessage message, AiRoundResponse aiResponse) {
        if (aiResponse.promptName() == null) {
            return;
        }
        aiDebateTurnLogMapper.insert(AiDebateTurnLog.builder()
                .debateSessionId(debateId)
                .debateMessageId(message.getId())
                .roundNo(message.getRoundNo())
                .speaker(message.getSpeaker())
                .promptName(aiResponse.promptName())
                .model(aiModel)
                .renderedPrompt(aiResponse.renderedPrompt())
                .rawResponse(aiResponse.rawResponse())
                .parsedResponseJson(aiResponse.parsedResponseJson())
                .status("SUCCEEDED")
                .latencyMs(aiResponse.latencyMs())
                .build());
    }
}

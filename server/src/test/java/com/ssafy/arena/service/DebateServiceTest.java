package com.ssafy.arena.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ssafy.arena.ai.AiClient;
import com.ssafy.arena.common.ApiException;
import com.ssafy.arena.domain.AiDebateTurnLog;
import com.ssafy.arena.domain.DebateMode;
import com.ssafy.arena.domain.DebateMessage;
import com.ssafy.arena.domain.DebateSession;
import com.ssafy.arena.domain.DebateStatus;
import com.ssafy.arena.domain.DebateSummary;
import com.ssafy.arena.domain.Post;
import com.ssafy.arena.domain.Speaker;
import com.ssafy.arena.dto.ai.AiNextTurnRequest;
import com.ssafy.arena.dto.ai.AiNextTurnResponse;
import com.ssafy.arena.dto.ai.AiGeneratedTurnResponse;
import com.ssafy.arena.dto.ai.AiRoundResponse;
import com.ssafy.arena.dto.debate.CreateDebateRequest;
import com.ssafy.arena.dto.debate.DebateListItem;
import com.ssafy.arena.dto.debate.DebateMessageResponse;
import com.ssafy.arena.dto.debate.ShareDebateRequest;
import com.ssafy.arena.dto.debate.ShareDebateResponse;
import com.ssafy.arena.dto.debate.StopDebateRequest;
import com.ssafy.arena.mapper.AiDebateTurnLogMapper;
import com.ssafy.arena.mapper.DebateMapper;
import com.ssafy.arena.mapper.PostMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class DebateServiceTest {
    @Mock
    private DebateMapper debateMapper;

    @Mock
    private PostMapper postMapper;

    @Mock
    private AiClient aiClient;

    @Mock
    private AiDebateTurnLogMapper aiDebateTurnLogMapper;

    @InjectMocks
    private DebateService debateService;

    private ArgumentCaptor<DebateSession> sessionCaptor;

    @BeforeEach
    void setUp() {
        sessionCaptor = ArgumentCaptor.forClass(DebateSession.class);
    }

    @Test
    void createStoresOriginalTopicSeparatelyFromSelectedTopic() {
        DebateSession session = debateService.create(7L, new CreateDebateRequest(
                "오늘 점심 제육 vs 돈까스",
                "오늘 점심 제육 vs 돈까스, 지금 바로 선택해야 한다면 무엇이 더 나은가",
                DebateMode.PRACTICAL
        ));

        verify(debateMapper).insertSession(sessionCaptor.capture());
        DebateSession savedSession = sessionCaptor.getValue();

        assertThat(savedSession.getUserId()).isEqualTo(7L);
        assertThat(savedSession.getOriginalTopic()).isEqualTo("오늘 점심 제육 vs 돈까스");
        assertThat(savedSession.getTopic()).isEqualTo("오늘 점심 제육 vs 돈까스, 지금 바로 선택해야 한다면 무엇이 더 나은가");
        assertThat(session.getOriginalTopic()).isEqualTo("오늘 점심 제육 vs 돈까스");
    }

    @Test
    void listMyDebatesReturnsOnlySessionsOwnedByUser() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 6, 11, 12, 0);
        when(debateMapper.findSessionsByUserId(7L)).thenReturn(List.of(
                DebateSession.builder()
                        .id(3L)
                        .userId(7L)
                        .originalTopic("오늘 점심 제육 vs 돈까스")
                        .topic("점심 메뉴")
                        .candidateRunId(100L)
                        .selectedCandidateId(11L)
                        .mode(DebateMode.PRACTICAL)
                        .status(DebateStatus.ACTIVE)
                        .createdAt(createdAt)
                        .build()
        ));

        List<DebateListItem> debates = debateService.listMyDebates(7L);

        assertThat(debates).hasSize(1);
        assertThat(debates.get(0).debateId()).isEqualTo(3L);
        assertThat(debates.get(0).originalTopic()).isEqualTo("오늘 점심 제육 vs 돈까스");
        assertThat(debates.get(0).topic()).isEqualTo("점심 메뉴");
        assertThat(debates.get(0).candidateRunId()).isEqualTo(100L);
        assertThat(debates.get(0).selectedCandidateId()).isEqualTo(11L);
        assertThat(debates.get(0).status()).isEqualTo(DebateStatus.ACTIVE);
        assertThat(debates.get(0).updatedAt()).isEqualTo(createdAt);
    }

    @Test
    void listMyDebatesKeepsOnlyLatestDebatePerOriginalQuestion() {
        when(debateMapper.findSessionsByUserId(7L)).thenReturn(List.of(
                DebateSession.builder()
                        .id(3L)
                        .userId(7L)
                        .originalTopic("오늘 점심 제육 vs 돈까스")
                        .topic("안정성 기준")
                        .mode(DebateMode.PRACTICAL)
                        .status(DebateStatus.STOPPED)
                        .createdAt(LocalDateTime.of(2026, 6, 11, 12, 0))
                        .stoppedAt(LocalDateTime.of(2026, 6, 11, 12, 30))
                        .build(),
                DebateSession.builder()
                        .id(4L)
                        .userId(7L)
                        .originalTopic("오늘 점심 제육 vs 돈까스")
                        .topic("만족감 기준")
                        .mode(DebateMode.PRACTICAL)
                        .status(DebateStatus.STOPPED)
                        .createdAt(LocalDateTime.of(2026, 6, 11, 13, 0))
                        .stoppedAt(LocalDateTime.of(2026, 6, 11, 13, 30))
                        .build(),
                DebateSession.builder()
                        .id(5L)
                        .userId(7L)
                        .originalTopic("출근 지하철 vs 버스")
                        .topic("정시성 기준")
                        .mode(DebateMode.PRACTICAL)
                        .status(DebateStatus.ACTIVE)
                        .createdAt(LocalDateTime.of(2026, 6, 11, 13, 10))
                        .build()
        ));

        List<DebateListItem> debates = debateService.listMyDebates(7L);

        assertThat(debates)
                .extracting(DebateListItem::debateId)
                .containsExactly(4L, 5L);
        assertThat(debates)
                .extracting(DebateListItem::originalTopic)
                .containsExactly("오늘 점심 제육 vs 돈까스", "출근 지하철 vs 버스");
    }

    @Test
    void listMyDebatesIncludesRoundProgressAndSelectionScore() {
        when(debateMapper.findSessionsByUserId(7L)).thenReturn(List.of(
                DebateSession.builder()
                        .id(3L)
                        .userId(7L)
                        .originalTopic("오늘 점심 제육 vs 돈까스")
                        .topic("안정성 기준")
                        .sideALabel("제육")
                        .sideBLabel("돈까스")
                        .mode(DebateMode.PRACTICAL)
                        .status(DebateStatus.STOPPED)
                        .selectedSide(Speaker.COOL_HEADED)
                        .selectedRoundNo(1)
                        .createdAt(LocalDateTime.of(2026, 6, 11, 12, 0))
                        .stoppedAt(LocalDateTime.of(2026, 6, 11, 12, 30))
                        .build(),
                DebateSession.builder()
                        .id(4L)
                        .userId(7L)
                        .originalTopic("오늘 점심 제육 vs 돈까스")
                        .topic("만족감 기준")
                        .sideALabel("제육")
                        .sideBLabel("돈까스")
                        .mode(DebateMode.PRACTICAL)
                        .status(DebateStatus.STOPPED)
                        .selectedSide(Speaker.PASSIONATE)
                        .selectedRoundNo(2)
                        .createdAt(LocalDateTime.of(2026, 6, 11, 13, 0))
                        .stoppedAt(LocalDateTime.of(2026, 6, 11, 13, 30))
                        .build(),
                DebateSession.builder()
                        .id(5L)
                        .userId(7L)
                        .originalTopic("오늘 점심 제육 vs 돈까스")
                        .topic("회복 가능성 기준")
                        .sideALabel("제육")
                        .sideBLabel("돈까스")
                        .mode(DebateMode.PRACTICAL)
                        .status(DebateStatus.ACTIVE)
                        .createdAt(LocalDateTime.of(2026, 6, 11, 14, 0))
                        .build()
        ));

        DebateListItem item = debateService.listMyDebates(7L).get(0);

        assertThat(item.debateId()).isEqualTo(5L);
        assertThat(item.roundCount()).isEqualTo(3);
        assertThat(item.coolCount()).isEqualTo(1);
        assertThat(item.hotCount()).isEqualTo(1);
        assertThat(item.sideALabel()).isEqualTo("제육");
        assertThat(item.sideBLabel()).isEqualTo("돈까스");
    }

    @Test
    void createStoresPersistedSideLabels() {
        debateService.create(7L, new CreateDebateRequest(
                "Lunch",
                "Which lunch standard is stronger?",
                DebateMode.PRACTICAL,
                "Choose A",
                "Choose B"
        ));

        verify(debateMapper).insertSession(sessionCaptor.capture());
        DebateSession savedSession = sessionCaptor.getValue();
        assertThat(savedSession.getSideALabel()).isEqualTo("Choose A");
        assertThat(savedSession.getSideBLabel()).isEqualTo("Choose B");
    }

    @Test
    void createNormalizesSentenceLikeSideLabelsBeforePersisting() {
        debateService.create(7L, new CreateDebateRequest(
                "오타니 10명 vs 북극곰",
                "오타니 10명과 북극곰이 싸우면 누가 이길까?",
                DebateMode.ENTERTAINMENT,
                "오타니 10명이 이긴다",
                "북극곰이 이긴다"
        ));

        verify(debateMapper).insertSession(sessionCaptor.capture());
        DebateSession savedSession = sessionCaptor.getValue();
        assertThat(savedSession.getSideALabel()).isEqualTo("오타니 10명");
        assertThat(savedSession.getSideBLabel()).isEqualTo("북극곰");
    }

    @Test
    void createStoresSelectedCandidateFramesForAiReuse() {
        debateService.create(7L, new CreateDebateRequest(
                "Lunch",
                "Which lunch standard is stronger?",
                DebateMode.PRACTICAL,
                "Choose A",
                "Choose B",
                "Risk versus satisfaction",
                "A lowers risk.",
                "B gives satisfaction.",
                "R1",
                "Stability vs thrill",
                "Same budget and lunch break.",
                100L,
                11L
        ));

        verify(debateMapper).insertSession(sessionCaptor.capture());
        DebateSession savedSession = sessionCaptor.getValue();
        assertThat(savedSession.getDebateAxis()).isEqualTo("Risk versus satisfaction");
        assertThat(savedSession.getSideAFrame()).isEqualTo("A lowers risk.");
        assertThat(savedSession.getSideBFrame()).isEqualTo("B gives satisfaction.");
        assertThat(savedSession.getSelectedRoundId()).isEqualTo("R1");
        assertThat(savedSession.getRoundTitle()).isEqualTo("Stability vs thrill");
        assertThat(savedSession.getBasicConditions()).isEqualTo("Same budget and lunch break.");
        assertThat(savedSession.getCandidateRunId()).isEqualTo(100L);
        assertThat(savedSession.getSelectedCandidateId()).isEqualTo(11L);
    }

    @Test
    void deleteOwnedDebateRemovesDependentDataBeforeSession() {
        when(debateMapper.findSessionById(3L)).thenReturn(DebateSession.builder()
                .id(3L)
                .userId(7L)
                .originalTopic("original")
                .topic("selected")
                .mode(DebateMode.PRACTICAL)
                .status(DebateStatus.ACTIVE)
                .build());

        debateService.delete(7L, 3L);

        InOrder inOrder = inOrder(debateMapper);
        inOrder.verify(debateMapper).findSessionById(3L);
        inOrder.verify(debateMapper).deletePostVotesByDebateId(3L);
        inOrder.verify(debateMapper).deleteCommentsByDebateId(3L);
        inOrder.verify(debateMapper).deletePostByDebateId(3L);
        inOrder.verify(debateMapper).deleteSummaryByDebateId(3L);
        inOrder.verify(debateMapper).deleteMessagesByDebateId(3L);
        inOrder.verify(debateMapper).deleteSessionById(3L);
    }

    @Test
    void sharePersistsUserWrittenBodySeparatelyFromGeneratedSummary() {
        when(debateMapper.findSessionById(3L)).thenReturn(DebateSession.builder()
                .id(3L)
                .userId(7L)
                .originalTopic("오늘 점심 제육 vs 돈까스")
                .topic("오늘 점심 제육 vs 돈까스, 지금 바로 선택해야 한다면 무엇이 더 나은가")
                .roundTitle("점심 안정성")
                .status(DebateStatus.STOPPED)
                .build());
        when(debateMapper.findSessionsByUserId(7L)).thenReturn(List.of(DebateSession.builder()
                .id(3L)
                .userId(7L)
                .originalTopic("오늘 점심 제육 vs 돈까스")
                .topic("오늘 점심 제육 vs 돈까스, 지금 바로 선택해야 한다면 무엇이 더 나은가")
                .roundTitle("점심 안정성")
                .status(DebateStatus.STOPPED)
                .build()));
        when(debateMapper.findSummary(3L)).thenReturn(DebateSummary.builder()
                .debateSessionId(3L)
                .summaryText("AI가 생성한 토론 요약")
                .build());
        doAnswer(invocation -> {
            Post post = invocation.getArgument(0);
            post.setId(77L);
            return null;
        }).when(postMapper).insert(any(Post.class));

        debateService.share(7L, 3L, new ShareDebateRequest(
                "오늘 점심 제육 vs 돈까스",
                "제육파",
                "돈까스파",
                true,
                1,
                "내가 게시글에 직접 작성한 본문"
        ));

        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postMapper).insert(postCaptor.capture());
        Post savedPost = postCaptor.getValue();
        assertThat(savedPost.getTitle()).isEqualTo("오늘 점심 제육 vs 돈까스");
        assertThat(savedPost.getShareRoundNo()).isEqualTo(1);
        assertThat(savedPost.getSummaryCard()).isEqualTo("AI가 생성한 토론 요약");
        assertThat(savedPost.getShareBody()).isEqualTo("내가 게시글에 직접 작성한 본문");
        assertThat(savedPost.getShareBody()).doesNotContain("세부주제");
        assertThat(savedPost.getShareBody()).doesNotContain("상세설명");
        assertThat(savedPost.getShareBody()).doesNotContain("제육파:");
    }

    @Test
    void shareAllowsAnotherPostAfterDebateWasAlreadyShared() {
        DebateSession sharedSession = DebateSession.builder()
                .id(3L)
                .userId(7L)
                .originalTopic("오늘 점심 제육 vs 돈까스")
                .topic("오늘 점심 제육 vs 돈까스, 지금 바로 선택해야 한다면 무엇이 더 나은가")
                .roundTitle("점심 안정성")
                .status(DebateStatus.SHARED)
                .build();
        when(debateMapper.findSessionById(3L)).thenReturn(sharedSession);
        when(debateMapper.findSessionsByUserId(7L)).thenReturn(List.of(sharedSession));
        when(debateMapper.findSummary(3L)).thenReturn(DebateSummary.builder()
                .debateSessionId(3L)
                .summaryText("AI가 생성한 토론 요약")
                .build());
        doAnswer(invocation -> {
            Post post = invocation.getArgument(0);
            post.setId(78L);
            return null;
        }).when(postMapper).insert(any(Post.class));

        ShareDebateResponse response = debateService.share(7L, 3L, new ShareDebateRequest(
                "잘못 들어온 제목",
                "제육파",
                "돈까스파",
                true,
                1,
                "두 번째 공유 본문"
        ));

        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postMapper).insert(postCaptor.capture());
        assertThat(response.postId()).isEqualTo(78L);
        assertThat(postCaptor.getValue().getShareBody()).isEqualTo("두 번째 공유 본문");
    }

    @Test
    void nextTurnStoresAiPromptAndResponseArtifactsAfterSavingMessage() {
        when(debateMapper.findSessionById(3L)).thenReturn(DebateSession.builder()
                .id(3L)
                .userId(7L)
                .originalTopic("Lunch")
                .topic("Which lunch is more defensible today?")
                .sideALabel("Choose A")
                .sideBLabel("Choose B")
                .debateAxis("Risk versus satisfaction")
                .sideAFrame("A lowers risk.")
                .sideBFrame("B gives satisfaction.")
                .mode(DebateMode.PRACTICAL)
                .status(DebateStatus.ACTIVE)
                .build());
        when(debateMapper.findMessages(3L)).thenReturn(List.of());
        when(debateMapper.findNextRoundNo(3L)).thenReturn(1);
        when(aiClient.nextTurn(any())).thenReturn(new AiNextTurnResponse(
                Speaker.COOL_HEADED,
                "A presses the premise.",
                false,
                "debate-single-round-fast-generator.txt",
                "rendered prompt",
                "{\"content\":\"A presses the premise.\"}",
                "{\"content\":\"A presses the premise.\",\"peakReached\":false}",
                123L
        ));
        doAnswer(invocation -> {
            DebateMessage message = invocation.getArgument(0);
            message.setId(77L);
            return null;
        }).when(debateMapper).insertMessage(any(DebateMessage.class));

        debateService.nextTurn(7L, 3L);

        ArgumentCaptor<AiNextTurnRequest> aiRequestCaptor = ArgumentCaptor.forClass(AiNextTurnRequest.class);
        verify(aiClient).nextTurn(aiRequestCaptor.capture());
        assertThat(aiRequestCaptor.getValue().sideALabel()).isEqualTo("Choose A");
        assertThat(aiRequestCaptor.getValue().sideBLabel()).isEqualTo("Choose B");
        assertThat(aiRequestCaptor.getValue().debateAxis()).isEqualTo("Risk versus satisfaction");
        assertThat(aiRequestCaptor.getValue().sideAFrame()).isEqualTo("A lowers risk.");
        assertThat(aiRequestCaptor.getValue().sideBFrame()).isEqualTo("B gives satisfaction.");

        ArgumentCaptor<AiDebateTurnLog> logCaptor = ArgumentCaptor.forClass(AiDebateTurnLog.class);
        verify(aiDebateTurnLogMapper).insert(logCaptor.capture());
        AiDebateTurnLog log = logCaptor.getValue();
        assertThat(log.getDebateSessionId()).isEqualTo(3L);
        assertThat(log.getDebateMessageId()).isEqualTo(77L);
        assertThat(log.getRoundNo()).isEqualTo(1);
        assertThat(log.getSpeaker()).isEqualTo(Speaker.COOL_HEADED);
        assertThat(log.getPromptName()).isEqualTo("debate-single-round-fast-generator.txt");
        assertThat(log.getModel()).isEqualTo("gpt-5.4-mini");
        assertThat(log.getRenderedPrompt()).isEqualTo("rendered prompt");
        assertThat(log.getRawResponse()).isEqualTo("{\"content\":\"A presses the premise.\"}");
        assertThat(log.getParsedResponseJson()).contains("peakReached");
        assertThat(log.getStatus()).isEqualTo("SUCCEEDED");
        assertThat(log.getLatencyMs()).isEqualTo(123L);
    }

    @Test
    void generateInitialTurnsStoresAllTurnsFromOneAiCall() {
        when(debateMapper.findSessionById(3L)).thenReturn(DebateSession.builder()
                .id(3L)
                .userId(7L)
                .originalTopic("Lunch")
                .topic("Which lunch standard is stronger?")
                .sideALabel("Choose A")
                .sideBLabel("Choose B")
                .debateAxis("Risk versus satisfaction")
                .sideAFrame("A lowers risk.")
                .sideBFrame("B gives satisfaction.")
                .mode(DebateMode.PRACTICAL)
                .status(DebateStatus.ACTIVE)
                .build());
        when(debateMapper.findMessages(3L)).thenReturn(List.of());
        when(aiClient.generateRound(any())).thenReturn(new AiRoundResponse(
                generatedTurns(),
                "debate-single-round-fast-generator.txt",
                "rendered prompt",
                "{\"turns\":[]}",
                "[{\"content\":\"Turn 1.\"}]",
                200L
        ));
        doAnswer(invocation -> {
            DebateMessage message = invocation.getArgument(0);
            message.setId(100L + messageCaptorIndex(message.getContent()));
            return null;
        }).when(debateMapper).insertMessage(any(DebateMessage.class));

        List<DebateMessageResponse> responses = debateService.generateInitialTurns(7L, 3L);

        verify(aiClient, times(1)).generateRound(any());
        ArgumentCaptor<DebateMessage> messageCaptor = ArgumentCaptor.forClass(DebateMessage.class);
        verify(debateMapper, times(10)).insertMessage(messageCaptor.capture());
        assertThat(messageCaptor.getAllValues()).extracting(DebateMessage::getRoundNo)
                .containsOnly(1);
        assertThat(messageCaptor.getAllValues()).extracting(DebateMessage::getSpeaker)
                .containsExactly(
                        Speaker.COOL_HEADED,
                        Speaker.PASSIONATE,
                        Speaker.COOL_HEADED,
                        Speaker.PASSIONATE,
                        Speaker.COOL_HEADED,
                        Speaker.PASSIONATE,
                        Speaker.COOL_HEADED,
                        Speaker.PASSIONATE,
                        Speaker.COOL_HEADED,
                        Speaker.PASSIONATE
                );
        assertThat(messageCaptor.getAllValues()).extracting(DebateMessage::getContent)
                .containsExactly("Turn 1.", "Turn 2.", "Turn 3.", "Turn 4.", "Turn 5.",
                        "Turn 6.", "Turn 7.", "Turn 8.", "Turn 9.", "Turn 10.");
        assertThat(responses).extracting(DebateMessageResponse::messageId)
                .containsExactly(101L, 102L, 103L, 104L, 105L, 106L, 107L, 108L, 109L, 110L);
        verify(aiDebateTurnLogMapper, times(10)).insert(any(AiDebateTurnLog.class));
    }

    @Test
    void generateInitialTurnsStoresNextDetailedTopicAsNextRound() {
        DebateSession firstRound = DebateSession.builder()
                .id(3L)
                .userId(7L)
                .originalTopic("Lunch")
                .topic("Which lunch standard is stronger?")
                .roundTitle("Stability vs thrill")
                .mode(DebateMode.PRACTICAL)
                .status(DebateStatus.STOPPED)
                .selectedSide(Speaker.COOL_HEADED)
                .selectedRoundNo(1)
                .createdAt(LocalDateTime.of(2026, 6, 11, 12, 0))
                .build();
        DebateSession secondRound = DebateSession.builder()
                .id(4L)
                .userId(7L)
                .originalTopic("Lunch")
                .topic("Which lunch tradeoff matters more?")
                .roundTitle("Speed vs comfort")
                .mode(DebateMode.PRACTICAL)
                .status(DebateStatus.ACTIVE)
                .createdAt(LocalDateTime.of(2026, 6, 11, 13, 0))
                .build();
        when(debateMapper.findSessionById(4L)).thenReturn(secondRound);
        when(debateMapper.findSessionsByUserId(7L)).thenReturn(List.of(secondRound, firstRound));
        when(debateMapper.findMessages(4L)).thenReturn(List.of());
        when(aiClient.generateRound(any())).thenReturn(new AiRoundResponse(
                generatedTurns(),
                "debate-single-round-fast-generator.txt",
                "rendered prompt",
                "{\"turns\":[]}",
                "[{\"content\":\"Turn 1.\"}]",
                200L
        ));
        doAnswer(invocation -> {
            DebateMessage message = invocation.getArgument(0);
            message.setId(100L + messageCaptorIndex(message.getContent()));
            return null;
        }).when(debateMapper).insertMessage(any(DebateMessage.class));

        List<DebateMessageResponse> responses = debateService.generateInitialTurns(7L, 4L);

        ArgumentCaptor<DebateMessage> messageCaptor = ArgumentCaptor.forClass(DebateMessage.class);
        verify(debateMapper, times(10)).insertMessage(messageCaptor.capture());
        assertThat(messageCaptor.getAllValues()).extracting(DebateMessage::getRoundNo)
                .containsOnly(2);
        assertThat(responses).extracting(DebateMessageResponse::roundNo)
                .containsOnly(2);
    }

    @Test
    void detailCombinesDetailedTopicSessionsAsRoundHistory() {
        DebateSession firstRound = DebateSession.builder()
                .id(3L)
                .userId(7L)
                .originalTopic("Lunch")
                .topic("Which lunch standard is stronger?")
                .roundTitle("Stability vs thrill")
                .debateAxis("Risk versus satisfaction")
                .mode(DebateMode.PRACTICAL)
                .status(DebateStatus.STOPPED)
                .selectedSide(Speaker.COOL_HEADED)
                .selectedRoundNo(1)
                .createdAt(LocalDateTime.of(2026, 6, 11, 12, 0))
                .build();
        DebateSession secondRound = DebateSession.builder()
                .id(4L)
                .userId(7L)
                .originalTopic("Lunch")
                .topic("Which lunch tradeoff matters more?")
                .roundTitle("Speed vs comfort")
                .debateAxis("Speed versus comfort")
                .mode(DebateMode.PRACTICAL)
                .status(DebateStatus.STOPPED)
                .selectedSide(Speaker.PASSIONATE)
                .selectedRoundNo(2)
                .createdAt(LocalDateTime.of(2026, 6, 11, 13, 0))
                .build();
        when(debateMapper.findSessionById(4L)).thenReturn(secondRound);
        when(debateMapper.findSessionsByUserId(7L)).thenReturn(List.of(secondRound, firstRound));
        when(debateMapper.findMessages(3L)).thenReturn(List.of(
                DebateMessage.builder().id(31L).debateSessionId(3L).speaker(Speaker.COOL_HEADED).roundNo(1).content("Round 1 A.").build(),
                DebateMessage.builder().id(32L).debateSessionId(3L).speaker(Speaker.PASSIONATE).roundNo(1).content("Round 1 B.").build()
        ));
        when(debateMapper.findMessages(4L)).thenReturn(List.of(
                DebateMessage.builder().id(41L).debateSessionId(4L).speaker(Speaker.COOL_HEADED).roundNo(1).content("Round 2 A.").build(),
                DebateMessage.builder().id(42L).debateSessionId(4L).speaker(Speaker.PASSIONATE).roundNo(1).content("Round 2 B.").build()
        ));
        when(debateMapper.findSummary(3L)).thenReturn(DebateSummary.builder()
                .debateSessionId(3L)
                .summaryText("Round 1 summary.")
                .build());
        when(debateMapper.findSummary(4L)).thenReturn(DebateSummary.builder()
                .debateSessionId(4L)
                .summaryText("Round 2 summary.")
                .build());

        var response = debateService.detail(7L, 4L);

        assertThat(response.rounds()).extracting(round -> round.roundNo())
                .containsExactly(1, 2);
        assertThat(response.rounds()).extracting(round -> round.title())
                .containsExactly("Stability vs thrill", "Speed vs comfort");
        assertThat(response.rounds()).extracting(round -> round.selectedSide())
                .containsExactly(Speaker.COOL_HEADED, Speaker.PASSIONATE);
        assertThat(response.rounds()).extracting(round -> round.summary().summaryText())
                .containsExactly("Round 1 summary.", "Round 2 summary.");
        assertThat(response.messages()).extracting(DebateMessage::getRoundNo)
                .containsExactly(1, 1, 2, 2);
        assertThat(response.messages()).extracting(DebateMessage::getContent)
                .containsExactly("Round 1 A.", "Round 1 B.", "Round 2 A.", "Round 2 B.");
    }

    @Test
    void detailExcludesUnfinishedSiblingSessionsFromRoundHistoryAndRenumbersCompletedRounds() {
        DebateSession firstCompletedRound = DebateSession.builder()
                .id(3L)
                .userId(7L)
                .originalTopic("Lunch")
                .topic("Which lunch standard is stronger?")
                .roundTitle("Stability vs thrill")
                .mode(DebateMode.PRACTICAL)
                .status(DebateStatus.STOPPED)
                .selectedSide(Speaker.COOL_HEADED)
                .selectedRoundNo(1)
                .createdAt(LocalDateTime.of(2026, 6, 11, 12, 0))
                .build();
        DebateSession unfinishedSibling = DebateSession.builder()
                .id(4L)
                .userId(7L)
                .originalTopic("Lunch")
                .topic("Unfinished candidate")
                .roundTitle("Unfinished")
                .mode(DebateMode.PRACTICAL)
                .status(DebateStatus.ACTIVE)
                .createdAt(LocalDateTime.of(2026, 6, 11, 12, 30))
                .build();
        DebateSession currentCompletedRound = DebateSession.builder()
                .id(5L)
                .userId(7L)
                .originalTopic("Lunch")
                .topic("Which lunch tradeoff matters more?")
                .roundTitle("Speed vs comfort")
                .mode(DebateMode.PRACTICAL)
                .status(DebateStatus.STOPPED)
                .selectedSide(Speaker.PASSIONATE)
                .selectedRoundNo(3)
                .createdAt(LocalDateTime.of(2026, 6, 11, 13, 0))
                .build();
        DebateSession futureCompletedRound = DebateSession.builder()
                .id(6L)
                .userId(7L)
                .originalTopic("Lunch")
                .topic("Future completed candidate")
                .roundTitle("Future")
                .mode(DebateMode.PRACTICAL)
                .status(DebateStatus.STOPPED)
                .selectedSide(Speaker.COOL_HEADED)
                .selectedRoundNo(4)
                .createdAt(LocalDateTime.of(2026, 6, 11, 14, 0))
                .build();
        when(debateMapper.findSessionById(5L)).thenReturn(currentCompletedRound);
        when(debateMapper.findSessionsByUserId(7L)).thenReturn(List.of(
                futureCompletedRound,
                currentCompletedRound,
                unfinishedSibling,
                firstCompletedRound
        ));
        when(debateMapper.findMessages(3L)).thenReturn(List.of(
                DebateMessage.builder().id(31L).debateSessionId(3L).speaker(Speaker.COOL_HEADED).roundNo(1).content("Round 1 A.").build()
        ));
        when(debateMapper.findMessages(5L)).thenReturn(List.of(
                DebateMessage.builder().id(51L).debateSessionId(5L).speaker(Speaker.COOL_HEADED).roundNo(1).content("Round 2 A.").build()
        ));

        var response = debateService.detail(7L, 5L);

        assertThat(response.rounds()).extracting(round -> round.debateId())
                .containsExactly(3L, 5L);
        assertThat(response.rounds()).extracting(round -> round.roundNo())
                .containsExactly(1, 2);
        assertThat(response.rounds()).extracting(round -> round.selectedSide())
                .containsExactly(Speaker.COOL_HEADED, Speaker.PASSIONATE);
        assertThat(response.messages()).extracting(DebateMessage::getRoundNo)
                .containsExactly(1, 2);
        verify(debateMapper, never()).findMessages(4L);
        verify(debateMapper, never()).findMessages(6L);
    }

    @Test
    void generateInitialTurnsReturnsCompleteExistingMessagesWithoutCallingAi() {
        when(debateMapper.findSessionById(3L)).thenReturn(DebateSession.builder()
                .id(3L)
                .userId(7L)
                .topic("Existing debate")
                .mode(DebateMode.PRACTICAL)
                .status(DebateStatus.ACTIVE)
                .build());
        when(debateMapper.findMessages(3L)).thenReturn(existingMessages(10));

        List<DebateMessageResponse> responses = debateService.generateInitialTurns(7L, 3L);

        verify(aiClient, never()).generateRound(any());
        assertThat(responses).hasSize(10);
        assertThat(responses.get(0).content()).isEqualTo("Existing 1.");
    }

    @Test
    void generateInitialTurnsCompletesPartialExistingMessagesWithOneAiCall() {
        when(debateMapper.findSessionById(3L)).thenReturn(DebateSession.builder()
                .id(3L)
                .userId(7L)
                .topic("Existing debate")
                .mode(DebateMode.PRACTICAL)
                .status(DebateStatus.ACTIVE)
                .build());
        when(debateMapper.findMessages(3L)).thenReturn(existingMessages(6));
        when(aiClient.generateRound(any())).thenReturn(new AiRoundResponse(
                generatedTurns(),
                "debate-single-round-fast-generator.txt",
                "rendered prompt",
                "{\"turns\":[]}",
                "[{\"content\":\"Turn 1.\"}]",
                200L
        ));
        doAnswer(invocation -> {
            DebateMessage message = invocation.getArgument(0);
            message.setId(100L + messageCaptorIndex(message.getContent()));
            return null;
        }).when(debateMapper).insertMessage(any(DebateMessage.class));

        List<DebateMessageResponse> responses = debateService.generateInitialTurns(7L, 3L);

        verify(aiClient, times(1)).generateRound(any());
        ArgumentCaptor<DebateMessage> messageCaptor = ArgumentCaptor.forClass(DebateMessage.class);
        verify(debateMapper, times(4)).insertMessage(messageCaptor.capture());
        assertThat(messageCaptor.getAllValues()).extracting(DebateMessage::getRoundNo)
                .containsOnly(1);
        assertThat(responses).hasSize(10);
        assertThat(responses).extracting(DebateMessageResponse::content)
                .containsExactly("Existing 1.", "Existing 2.", "Existing 3.", "Existing 4.", "Existing 5.",
                        "Existing 6.", "Turn 7.", "Turn 8.", "Turn 9.", "Turn 10.");
    }

    @Test
    void stopPersistsSelectedSideAndRoundForResultReloads() {
        when(debateMapper.findSessionById(3L)).thenReturn(DebateSession.builder()
                .id(3L)
                .userId(7L)
                .originalTopic("Lunch")
                .topic("Which lunch standard is stronger?")
                .mode(DebateMode.PRACTICAL)
                .status(DebateStatus.ACTIVE)
                .build());
        when(debateMapper.findMessages(3L)).thenReturn(existingMessages(10));
        when(aiClient.summarize(any())).thenReturn(new com.ssafy.arena.dto.ai.AiSummaryResponse(
                "core",
                "highlight",
                "criteria",
                "issue",
                "summary"
        ));

        var response = debateService.stop(7L, 3L, new StopDebateRequest(Speaker.COOL_HEADED, 2));

        verify(debateMapper).updateSelection(3L, Speaker.COOL_HEADED, 2);
        assertThat(response.selectedSide()).isEqualTo(Speaker.COOL_HEADED);
        assertThat(response.selectedRoundNo()).isEqualTo(2);
    }

    @Test
    void stopRejectsDebatesWithoutCompleteGeneratedMessages() {
        when(debateMapper.findSessionById(3L)).thenReturn(DebateSession.builder()
                .id(3L)
                .userId(7L)
                .topic("Which lunch standard is stronger?")
                .mode(DebateMode.PRACTICAL)
                .status(DebateStatus.ACTIVE)
                .build());
        when(debateMapper.findMessages(3L)).thenReturn(List.of());

        assertThatThrownBy(() -> debateService.stop(7L, 3L, new StopDebateRequest(Speaker.COOL_HEADED, 1)))
                .isInstanceOf(ApiException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.CONFLICT);
    }

    private List<AiGeneratedTurnResponse> generatedTurns() {
        return java.util.stream.IntStream.rangeClosed(1, 10)
                .mapToObj(index -> new AiGeneratedTurnResponse(
                        index % 2 == 1 ? Speaker.COOL_HEADED : Speaker.PASSIONATE,
                        index,
                        "Turn " + index + ".",
                        index >= 6
                ))
                .toList();
    }

    private List<DebateMessage> existingMessages(int count) {
        return java.util.stream.IntStream.rangeClosed(1, count)
                .mapToObj(index -> DebateMessage.builder()
                        .id(10L + index)
                        .debateSessionId(3L)
                        .speaker(index % 2 == 1 ? Speaker.COOL_HEADED : Speaker.PASSIONATE)
                        .roundNo(1)
                        .content("Existing " + index + ".")
                        .build())
                .toList();
    }

    private int messageCaptorIndex(String content) {
        return Integer.parseInt(content.replace("Turn ", "").replace(".", ""));
    }
}

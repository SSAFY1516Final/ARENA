package com.ssafy.arena.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ssafy.arena.common.ApiException;
import com.ssafy.arena.domain.AiRoundCandidate;
import com.ssafy.arena.domain.AiPromptCallLog;
import com.ssafy.arena.domain.AiRoundCandidateRun;
import com.ssafy.arena.domain.DebateMode;
import com.ssafy.arena.domain.DebateSession;
import com.ssafy.arena.dto.ai.RoundCandidateResponse;
import com.ssafy.arena.dto.ai.RoundCandidatesRequest;
import com.ssafy.arena.dto.ai.RoundCandidatesResponse;
import com.ssafy.arena.dto.ai.TopicFrameResponse;
import com.ssafy.arena.mapper.AiPromptCallLogMapper;
import com.ssafy.arena.mapper.AiRoundCandidateMapper;
import com.ssafy.arena.mapper.AiRoundCandidateRunMapper;
import com.ssafy.arena.mapper.DebateMapper;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class AiRoundCandidateServiceTest {
    @Mock
    private SpringAiClient springAiClient;

    @Mock
    private PromptTemplateLoader promptTemplateLoader;

    @Mock
    private AiResponseParser responseParser;

    @Mock
    private AiRoundCandidateRunMapper runMapper;

    @Mock
    private AiRoundCandidateMapper candidateMapper;

    @Mock
    private AiPromptCallLogMapper logMapper;

    @Mock
    private DebateMapper debateMapper;

    @InjectMocks
    private AiRoundCandidateService service;

    @Test
    void generateCandidatesPersistsRunPromptLogsAndFinalResponse() {
        RoundCandidatesRequest request = new RoundCandidatesRequest("lunch", DebateMode.PRACTICAL, 5);
        TopicFrameResponse frame = new TopicFrameResponse(
                "Lunch choice", "Choose A", "Choose B", "Same budget", true, List.of(), List.of(), null);
        List<RoundCandidateResponse> generated = List.of(candidate("R1"));
        List<RoundCandidateResponse> validated = List.of(
                candidate("R1"), candidate("R2"), candidate("R3"), candidate("R4"), candidate("R5"));

        doAnswer(invocation -> {
            AiRoundCandidateRun run = invocation.getArgument(0);
            run.setId(100L);
            return null;
        }).when(runMapper).insert(any(AiRoundCandidateRun.class));
        when(promptTemplateLoader.loadAndRender(eq("topic-frame-generator.txt"), any())).thenReturn("frame prompt");
        when(promptTemplateLoader.loadAndRender(eq("topic-round-candidates-generator.txt"), any())).thenReturn("generator prompt");
        when(promptTemplateLoader.loadAndRender(eq("topic-round-candidates-validator.txt"), any())).thenReturn("validator prompt");
        when(springAiClient.call(any(), eq("frame prompt"))).thenReturn("frame raw");
        when(springAiClient.call(any(), eq("generator prompt"))).thenReturn("generator raw");
        when(springAiClient.call(any(), eq("validator prompt"))).thenReturn("validator raw");
        when(responseParser.parseTopicFrame("frame raw")).thenReturn(frame);
        when(responseParser.parseGeneratedCandidates("generator raw")).thenReturn(generated);
        when(responseParser.parseValidatedCandidates("validator raw")).thenReturn(validated);
        when(responseParser.toJson(any())).thenReturn("{\"json\":true}");
        AtomicLong candidateId = new AtomicLong(10L);
        doAnswer(invocation -> {
            AiRoundCandidate candidate = invocation.getArgument(0);
            candidate.setId(candidateId.incrementAndGet());
            return null;
        }).when(candidateMapper).insert(any(AiRoundCandidate.class));

        RoundCandidatesResponse response = service.generate(7L, request);

        assertThat(response.candidateRunId()).isEqualTo(100L);
        assertThat(response.topicFrame()).isEqualTo(frame);
        assertThat(response.roundCandidates()).hasSize(5);
        assertThat(response.roundCandidates()).extracting(RoundCandidateResponse::candidateId)
                .containsExactly(11L, 12L, 13L, 14L, 15L);
        ArgumentCaptor<AiRoundCandidate> candidateCaptor = ArgumentCaptor.forClass(AiRoundCandidate.class);
        verify(candidateMapper, org.mockito.Mockito.times(5)).insert(candidateCaptor.capture());
        assertThat(candidateCaptor.getAllValues()).extracting(AiRoundCandidate::getRunId)
                .containsOnly(100L);
        assertThat(candidateCaptor.getAllValues()).extracting(AiRoundCandidate::getSortOrder)
                .containsExactly(1, 2, 3, 4, 5);
        assertThat(candidateCaptor.getAllValues()).extracting(AiRoundCandidate::getTitle)
                .containsExactly("Title R1", "Title R2", "Title R3", "Title R4", "Title R5");
        verify(runMapper).markSucceeded(100L, "{\"json\":true}", "{\"json\":true}");
        ArgumentCaptor<AiPromptCallLog> logCaptor = ArgumentCaptor.forClass(AiPromptCallLog.class);
        verify(logMapper, org.mockito.Mockito.times(3)).insert(logCaptor.capture());
        assertThat(logCaptor.getAllValues()).extracting(AiPromptCallLog::getStage)
                .containsExactly("TOPIC_FRAME", "CANDIDATE_GENERATION", "CANDIDATE_VALIDATION");
    }

    @Test
    void nonDebatableTopicFailsWithUnprocessableEntityAndPersistsFailure() {
        RoundCandidatesRequest request = new RoundCandidatesRequest("capital of korea", DebateMode.PRACTICAL, 5);
        TopicFrameResponse frame = new TopicFrameResponse(
                "Fact check", null, null, null, false, List.of(), List.of(), "not debatable");

        doAnswer(invocation -> {
            AiRoundCandidateRun run = invocation.getArgument(0);
            run.setId(101L);
            return null;
        }).when(runMapper).insert(any(AiRoundCandidateRun.class));
        when(promptTemplateLoader.loadAndRender(eq("topic-frame-generator.txt"), any())).thenReturn("frame prompt");
        when(springAiClient.call(any(), eq("frame prompt"))).thenReturn("frame raw");
        when(responseParser.parseTopicFrame("frame raw")).thenReturn(frame);
        when(responseParser.toJson(frame)).thenReturn("{\"isDebatable\":false}");

        assertThatThrownBy(() -> service.generate(7L, request))
                .isInstanceOf(ApiException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);

        verify(runMapper).markFailed(101L, "{\"isDebatable\":false}", "not debatable");
    }

    @Test
    void getExistingCandidateRunReturnsPersistedCandidatesWithoutCallingAi() {
        TopicFrameResponse frame = new TopicFrameResponse(
                "Lunch choice", "Choose A", "Choose B", "Same budget", true, List.of(), List.of(), null);
        AiRoundCandidateRun run = AiRoundCandidateRun.builder()
                .id(100L)
                .userId(7L)
                .originalTopic("lunch")
                .mode(DebateMode.PRACTICAL)
                .candidateCount(5)
                .status("SUCCEEDED")
                .topicFrameJson("{\"frame\":true}")
                .build();
        when(runMapper.findById(100L)).thenReturn(run);
        when(responseParser.parseTopicFrame("{\"frame\":true}")).thenReturn(frame);
        when(candidateMapper.findByRunId(100L)).thenReturn(List.of(
                persistedCandidate(11L, "R1", 1),
                persistedCandidate(12L, "R2", 2)
        ));
        when(debateMapper.findSessionsByUserId(7L)).thenReturn(List.of(
                DebateSession.builder().id(31L).candidateRunId(100L).selectedCandidateId(11L).build(),
                DebateSession.builder().id(32L).candidateRunId(100L).selectedCandidateId(12L).build(),
                DebateSession.builder().id(33L).candidateRunId(101L).selectedCandidateId(99L).build()
        ));
        when(debateMapper.findMessages(31L)).thenReturn(List.of(
                com.ssafy.arena.domain.DebateMessage.builder().id(301L).debateSessionId(31L).content("stored").build()
        ));
        when(debateMapper.findMessages(32L)).thenReturn(List.of());

        RoundCandidatesResponse response = service.getExistingRun(7L, 100L);

        assertThat(response.candidateRunId()).isEqualTo(100L);
        assertThat(response.topicFrame()).isEqualTo(frame);
        assertThat(response.roundCandidates()).extracting(RoundCandidateResponse::candidateId)
                .containsExactly(11L, 12L);
        assertThat(response.roundCandidates()).extracting(RoundCandidateResponse::title)
                .containsExactly("Title R1", "Title R2");
        assertThat(response.usedCandidateIds()).containsExactly(11L);
        verify(springAiClient, org.mockito.Mockito.never()).call(any(), any());
    }

    private RoundCandidateResponse candidate(String id) {
        return new RoundCandidateResponse(
                null,
                id,
                "Title " + id,
                "Core question " + id,
                "Debate axis " + id,
                "Side A frame " + id,
                "Side B frame " + id
        );
    }

    private AiRoundCandidate persistedCandidate(Long id, String roundId, int sortOrder) {
        return AiRoundCandidate.builder()
                .id(id)
                .runId(100L)
                .roundId(roundId)
                .title("Title " + roundId)
                .coreQuestion("Core question " + roundId)
                .debateAxis("Debate axis " + roundId)
                .sideAFrame("Side A frame " + roundId)
                .sideBFrame("Side B frame " + roundId)
                .sortOrder(sortOrder)
                .build();
    }
}

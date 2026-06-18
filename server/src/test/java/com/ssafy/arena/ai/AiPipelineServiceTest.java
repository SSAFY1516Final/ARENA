package com.ssafy.arena.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ssafy.arena.domain.DebateMode;
import com.ssafy.arena.domain.Speaker;
import com.ssafy.arena.dto.ai.AiNextTurnRequest;
import com.ssafy.arena.dto.ai.AiNextTurnResponse;
import com.ssafy.arena.dto.ai.AiSummaryRequest;
import com.ssafy.arena.dto.ai.AiSummaryResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AiPipelineServiceTest {
    @Mock
    private SpringAiClient springAiClient;

    @Mock
    private AiPromptFactory promptFactory;

    @Mock
    private AiResponseParser responseParser;

    @InjectMocks
    private AiPipelineService aiPipelineService;

    @Test
    void nextTurnBuildsPromptCallsProviderAndParsesSelectedSpeaker() {
        AiNextTurnRequest request = new AiNextTurnRequest("점심 메뉴", DebateMode.PRACTICAL, 1, List.of());
        AiNextTurnResponse expected = new AiNextTurnResponse(Speaker.COOL_HEADED, "돈까스가 안전합니다.", false);
        when(promptFactory.nextTurnSystemPrompt()).thenReturn("system");
        when(promptFactory.nextTurnUserPrompt(request, Speaker.COOL_HEADED)).thenReturn("user");
        when(springAiClient.call("system", "user")).thenReturn("{\"content\":\"돈까스가 안전합니다.\"}");
        when(responseParser.parseNextTurn("{\"content\":\"돈까스가 안전합니다.\"}", Speaker.COOL_HEADED, false))
                .thenReturn(expected);

        AiNextTurnResponse response = aiPipelineService.nextTurn(request);

        assertThat(response).isSameAs(expected);
        verify(promptFactory).nextTurnUserPrompt(request, Speaker.COOL_HEADED);
    }

    @Test
    void nextTurnDefaultsPeakWhenRoundIsAtLeastSix() {
        AiNextTurnRequest request = new AiNextTurnRequest("점심 메뉴", DebateMode.PRACTICAL, 6, List.of());
        AiNextTurnResponse expected = new AiNextTurnResponse(Speaker.PASSIONATE, "제육은 보상입니다.", true);
        when(promptFactory.nextTurnSystemPrompt()).thenReturn("system");
        when(promptFactory.nextTurnUserPrompt(request, Speaker.PASSIONATE)).thenReturn("user");
        when(springAiClient.call("system", "user")).thenReturn("{\"content\":\"제육은 보상입니다.\"}");
        when(responseParser.parseNextTurn("{\"content\":\"제육은 보상입니다.\"}", Speaker.PASSIONATE, true))
                .thenReturn(expected);

        AiNextTurnResponse response = aiPipelineService.nextTurn(request);

        assertThat(response.speaker()).isEqualTo(Speaker.PASSIONATE);
        assertThat(response.peakReached()).isTrue();
    }

    @Test
    void nextTurnTreatsMissingRoundAsFirstRound() {
        AiNextTurnRequest request = new AiNextTurnRequest("점심 메뉴", DebateMode.PRACTICAL, null, List.of());
        AiNextTurnResponse expected = new AiNextTurnResponse(Speaker.COOL_HEADED, "기본 라운드입니다.", false);
        when(promptFactory.nextTurnSystemPrompt()).thenReturn("system");
        when(promptFactory.nextTurnUserPrompt(request, Speaker.COOL_HEADED)).thenReturn("user");
        when(springAiClient.call("system", "user")).thenReturn("{\"content\":\"기본 라운드입니다.\"}");
        when(responseParser.parseNextTurn("{\"content\":\"기본 라운드입니다.\"}", Speaker.COOL_HEADED, false))
                .thenReturn(expected);

        AiNextTurnResponse response = aiPipelineService.nextTurn(request);

        assertThat(response.speaker()).isEqualTo(Speaker.COOL_HEADED);
        assertThat(response.peakReached()).isFalse();
    }

    @Test
    void summarizeBuildsPromptCallsProviderAndParsesSummary() {
        AiSummaryRequest request = new AiSummaryRequest("점심 메뉴", DebateMode.PRACTICAL, List.of());
        AiSummaryResponse expected = new AiSummaryResponse("핵심", "하이라이트", "기준", "쟁점", "요약");
        when(promptFactory.summarySystemPrompt()).thenReturn("system");
        when(promptFactory.summaryUserPrompt(request)).thenReturn("user");
        when(springAiClient.call("system", "user")).thenReturn("{\"summaryText\":\"요약\"}");
        when(responseParser.parseSummary("{\"summaryText\":\"요약\"}")).thenReturn(expected);

        AiSummaryResponse response = aiPipelineService.summarize(request);

        assertThat(response).isSameAs(expected);
        verify(promptFactory).summaryUserPrompt(request);
    }
}

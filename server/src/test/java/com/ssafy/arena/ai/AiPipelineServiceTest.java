package com.ssafy.arena.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ssafy.arena.common.ApiException;
import com.ssafy.arena.domain.DebateMode;
import com.ssafy.arena.domain.Speaker;
import com.ssafy.arena.dto.ai.AiGeneratedTurnResponse;
import com.ssafy.arena.dto.ai.AiNextTurnRequest;
import com.ssafy.arena.dto.ai.AiNextTurnResponse;
import com.ssafy.arena.dto.ai.AiRoundResponse;
import com.ssafy.arena.dto.ai.AiSummaryRequest;
import com.ssafy.arena.dto.ai.AiSummaryResponse;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class AiPipelineServiceTest {
    @Mock
    private SpringAiClient springAiClient;

    @Mock
    private AiPromptFactory promptFactory;

    @Mock
    private PromptTemplateLoader promptTemplateLoader;

    @Mock
    private AiResponseParser responseParser;

    @InjectMocks
    private AiPipelineService aiPipelineService;

    @Test
    @SuppressWarnings("unchecked")
    void nextTurnUsesFastRoundPromptAndKeepsProviderArtifactsForPersistence() {
        AiNextTurnRequest request = new AiNextTurnRequest(
                "Lunch decision",
                DebateMode.PRACTICAL,
                1,
                List.of(),
                "Choose A",
                "Choose B",
                "Risk versus satisfaction",
                "A lowers risk.",
                "B gives satisfaction.",
                "Stability vs thrill",
                "Same budget and lunch break."
        );
        AiNextTurnResponse parsed = new AiNextTurnResponse(Speaker.COOL_HEADED, "A presses the premise.", false);
        when(promptFactory.nextTurnSystemPrompt()).thenReturn("system");
        when(promptTemplateLoader.loadAndRender(eq("debate-single-round-fast-generator.txt"), anyMap()))
                .thenReturn("rendered fast round prompt");
        when(springAiClient.call("system", "rendered fast round prompt"))
                .thenReturn("{\"content\":\"A presses the premise.\"}");
        when(responseParser.parseNextTurn("{\"content\":\"A presses the premise.\"}", Speaker.COOL_HEADED, false, 1))
                .thenReturn(parsed);
        when(responseParser.toJson(parsed)).thenReturn("{\"content\":\"A presses the premise.\",\"peakReached\":false}");

        AiNextTurnResponse response = aiPipelineService.nextTurn(request);

        assertThat(response.speaker()).isEqualTo(Speaker.COOL_HEADED);
        assertThat(response.content()).isEqualTo("A presses the premise.");
        assertThat(response.peakReached()).isFalse();
        assertThat(response.promptName()).isEqualTo("debate-single-round-fast-generator.txt");
        assertThat(response.renderedPrompt()).isEqualTo("rendered fast round prompt");
        assertThat(response.rawResponse()).isEqualTo("{\"content\":\"A presses the premise.\"}");
        assertThat(response.parsedResponseJson()).isEqualTo("{\"content\":\"A presses the premise.\",\"peakReached\":false}");
        assertThat(response.latencyMs()).isNotNegative();

        ArgumentCaptor<Map<String, ?>> variablesCaptor = ArgumentCaptor.forClass(Map.class);
        verify(promptTemplateLoader).loadAndRender(eq("debate-single-round-fast-generator.txt"), variablesCaptor.capture());
        Map<String, ?> variables = variablesCaptor.getValue();
        assertThat(variables.get("bigTopic")).isEqualTo("Lunch decision");
        assertThat(variables.get("coreQuestion")).isEqualTo("Lunch decision");
        assertThat(variables.get("totalTurnCount")).isEqualTo(10);
        assertThat(variables.get("debateAxis")).isEqualTo("Risk versus satisfaction");
        assertThat(variables.get("sideA")).isEqualTo("Choose A");
        assertThat(variables.get("sideB")).isEqualTo("Choose B");
        assertThat(variables.get("sideAFrame")).isEqualTo("A lowers risk.");
        assertThat(variables.get("sideBFrame")).isEqualTo("B gives satisfaction.");
        assertThat(variables.get("roundTitle")).isEqualTo("Stability vs thrill");
        assertThat(variables.get("basicConditions")).isEqualTo("Same budget and lunch break.");
    }

    @Test
    void nextTurnDefaultsPeakWhenRoundIsAtLeastSix() {
        AiNextTurnRequest request = new AiNextTurnRequest("Lunch decision", DebateMode.PRACTICAL, 6, List.of());
        AiNextTurnResponse expected = new AiNextTurnResponse(Speaker.PASSIONATE, "B leaves pressure.", true);
        when(promptFactory.nextTurnSystemPrompt()).thenReturn("system");
        when(promptTemplateLoader.loadAndRender(eq("debate-single-round-fast-generator.txt"), anyMap()))
                .thenReturn("rendered fast round prompt");
        when(springAiClient.call("system", "rendered fast round prompt"))
                .thenReturn("{\"content\":\"B leaves pressure.\"}");
        when(responseParser.parseNextTurn("{\"content\":\"B leaves pressure.\"}", Speaker.PASSIONATE, true, 6))
                .thenReturn(expected);
        when(responseParser.toJson(expected)).thenReturn("{\"content\":\"B leaves pressure.\",\"peakReached\":true}");

        AiNextTurnResponse response = aiPipelineService.nextTurn(request);

        assertThat(response.speaker()).isEqualTo(Speaker.PASSIONATE);
        assertThat(response.peakReached()).isTrue();
    }

    @Test
    void nextTurnTreatsMissingRoundAsFirstRound() {
        AiNextTurnRequest request = new AiNextTurnRequest("Lunch decision", DebateMode.PRACTICAL, null, List.of());
        AiNextTurnResponse expected = new AiNextTurnResponse(Speaker.COOL_HEADED, "First round.", false);
        when(promptFactory.nextTurnSystemPrompt()).thenReturn("system");
        when(promptTemplateLoader.loadAndRender(eq("debate-single-round-fast-generator.txt"), anyMap()))
                .thenReturn("rendered fast round prompt");
        when(springAiClient.call("system", "rendered fast round prompt"))
                .thenReturn("{\"content\":\"First round.\"}");
        when(responseParser.parseNextTurn("{\"content\":\"First round.\"}", Speaker.COOL_HEADED, false, 1))
                .thenReturn(expected);
        when(responseParser.toJson(expected)).thenReturn("{\"content\":\"First round.\",\"peakReached\":false}");

        AiNextTurnResponse response = aiPipelineService.nextTurn(request);

        assertThat(response.speaker()).isEqualTo(Speaker.COOL_HEADED);
        assertThat(response.peakReached()).isFalse();
    }

    @Test
    void generateRoundCallsProviderOnceAndKeepsEveryParsedTurn() {
        AiNextTurnRequest request = new AiNextTurnRequest(
                "Lunch decision",
                DebateMode.PRACTICAL,
                1,
                List.of(),
                "Choose A",
                "Choose B",
                "Risk versus satisfaction",
                "A lowers risk.",
                "B gives satisfaction.",
                "Stability vs thrill",
                "Same budget and lunch break."
        );
        List<AiGeneratedTurnResponse> turns = List.of(
                new AiGeneratedTurnResponse(Speaker.COOL_HEADED, 1, "A opens.", false),
                new AiGeneratedTurnResponse(Speaker.PASSIONATE, 2, "B answers.", false)
        );
        when(promptFactory.nextTurnSystemPrompt()).thenReturn("system");
        when(promptTemplateLoader.loadAndRender(eq("debate-single-round-fast-generator.txt"), anyMap()))
                .thenReturn("rendered fast round prompt");
        when(springAiClient.call("system", "rendered fast round prompt"))
                .thenReturn("{\"turns\":[]}");
        when(responseParser.parseRound("{\"turns\":[]}")).thenReturn(turns);
        when(responseParser.toJson(turns)).thenReturn("[{\"content\":\"A opens.\"},{\"content\":\"B answers.\"}]");

        AiRoundResponse response = aiPipelineService.generateRound(request);

        assertThat(response.turns()).containsExactlyElementsOf(turns);
        assertThat(response.promptName()).isEqualTo("debate-single-round-fast-generator.txt");
        assertThat(response.renderedPrompt()).isEqualTo("rendered fast round prompt");
        assertThat(response.rawResponse()).isEqualTo("{\"turns\":[]}");
        assertThat(response.parsedResponseJson()).contains("B answers");
        assertThat(response.latencyMs()).isNotNegative();
        verify(springAiClient).call("system", "rendered fast round prompt");
    }

    @Test
    void generateRoundRetriesWhenFirstProviderResponseCannotBeParsed() {
        AiNextTurnRequest request = new AiNextTurnRequest("Lunch decision", DebateMode.PRACTICAL, 1, List.of());
        List<AiGeneratedTurnResponse> turns = List.of(
                new AiGeneratedTurnResponse(Speaker.COOL_HEADED, 1, "A opens.", false),
                new AiGeneratedTurnResponse(Speaker.PASSIONATE, 2, "B answers.", false)
        );
        when(promptFactory.nextTurnSystemPrompt()).thenReturn("system");
        when(promptTemplateLoader.loadAndRender(eq("debate-single-round-fast-generator.txt"), anyMap()))
                .thenReturn("rendered fast round prompt");
        when(springAiClient.call("system", "rendered fast round prompt"))
                .thenReturn("{\"turns\":[]}")
                .thenReturn("{\"turns\":[{\"message\":\"A opens.\"},{\"message\":\"B answers.\"}]}");
        when(responseParser.parseRound("{\"turns\":[]}"))
                .thenThrow(new ApiException(HttpStatus.BAD_GATEWAY, "Spring AI round generation failed"));
        when(responseParser.parseRound("{\"turns\":[{\"message\":\"A opens.\"},{\"message\":\"B answers.\"}]}"))
                .thenReturn(turns);
        when(responseParser.toJson(turns)).thenReturn("[{\"content\":\"A opens.\"},{\"content\":\"B answers.\"}]");

        AiRoundResponse response = aiPipelineService.generateRound(request);

        assertThat(response.turns()).containsExactlyElementsOf(turns);
        assertThat(response.rawResponse()).contains("B answers");
        verify(springAiClient, times(2)).call("system", "rendered fast round prompt");
    }

    @Test
    @SuppressWarnings("unchecked")
    void summarizeBuildsPromptCallsProviderAndParsesSummary() {
        AiSummaryRequest request = new AiSummaryRequest("Lunch decision", DebateMode.PRACTICAL, List.of());
        AiSummaryResponse expected = new AiSummaryResponse("arguments", "highlight", "criteria", "issue", "summary");
        when(responseParser.toJson(request.messages())).thenReturn("[]");
        when(promptTemplateLoader.loadAndRender(eq("debate-summary-highlight-generator.txt"), anyMap()))
                .thenReturn("rendered summary prompt");
        when(springAiClient.call("Return valid JSON only. Do not use markdown.", "rendered summary prompt"))
                .thenReturn("{\"summaryText\":\"summary\"}");
        when(responseParser.parseSummary("{\"summaryText\":\"summary\"}")).thenReturn(expected);

        AiSummaryResponse response = aiPipelineService.summarize(request);

        assertThat(response).isSameAs(expected);
        ArgumentCaptor<Map<String, ?>> variablesCaptor = ArgumentCaptor.forClass(Map.class);
        verify(promptTemplateLoader).loadAndRender(eq("debate-summary-highlight-generator.txt"), variablesCaptor.capture());
        Map<String, ?> variables = variablesCaptor.getValue();
        assertThat(variables.get("topic")).isEqualTo("Lunch decision");
        assertThat(variables.get("mode")).isEqualTo(DebateMode.PRACTICAL);
        assertThat(variables.get("messagesJson")).isEqualTo("[]");
    }
}

package com.ssafy.arena.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ssafy.arena.domain.Speaker;
import com.ssafy.arena.dto.debate.DebateMessageResponse;
import com.ssafy.arena.dto.debate.InitialTurnGenerationResponse;
import com.ssafy.arena.dto.debate.InitialTurnGenerationStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class InitialTurnGenerationServiceTest {
    @Mock
    private DebateService debateService;

    private CapturingExecutor executor;
    private InitialTurnGenerationService generationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        executor = new CapturingExecutor();
        generationService = new InitialTurnGenerationService(debateService, executor);
    }

    @Test
    void requestGenerationReturnsImmediatelyAndRunsAiWorkInBackground() {
        when(debateService.initialTurnGenerationSnapshot(7L, 3L)).thenReturn(
                new InitialTurnGenerationResponse(InitialTurnGenerationStatus.GENERATING, List.of())
        );

        InitialTurnGenerationResponse response = generationService.requestGeneration(7L, 3L);

        assertThat(response.status()).isEqualTo(InitialTurnGenerationStatus.GENERATING);
        assertThat(executor.tasks).hasSize(1);
        verify(debateService, never()).generateInitialTurns(7L, 3L);

        executor.tasks.get(0).run();

        verify(debateService).generateInitialTurns(7L, 3L);
    }

    @Test
    void requestGenerationDoesNotQueueDuplicateJobWhileOneIsRunning() {
        when(debateService.initialTurnGenerationSnapshot(7L, 3L)).thenReturn(
                new InitialTurnGenerationResponse(InitialTurnGenerationStatus.GENERATING, List.of())
        );

        generationService.requestGeneration(7L, 3L);
        generationService.requestGeneration(7L, 3L);

        assertThat(executor.tasks).hasSize(1);
    }

    @Test
    void requestGenerationReturnsCompleteSnapshotWithoutStartingBackgroundWork() {
        List<DebateMessageResponse> messages = List.of(new DebateMessageResponse(
                1L,
                Speaker.COOL_HEADED,
                1,
                "Already generated.",
                true
        ));
        when(debateService.initialTurnGenerationSnapshot(7L, 3L)).thenReturn(
                new InitialTurnGenerationResponse(InitialTurnGenerationStatus.COMPLETE, messages)
        );

        InitialTurnGenerationResponse response = generationService.requestGeneration(7L, 3L);

        assertThat(response.status()).isEqualTo(InitialTurnGenerationStatus.COMPLETE);
        assertThat(response.messages()).isEqualTo(messages);
        assertThat(executor.tasks).isEmpty();
    }

    @Test
    void failedBackgroundWorkIsRemovedSoLaterRequestsCanRetry() {
        when(debateService.initialTurnGenerationSnapshot(7L, 3L)).thenReturn(
                new InitialTurnGenerationResponse(InitialTurnGenerationStatus.GENERATING, List.of())
        );
        org.mockito.Mockito.doThrow(new IllegalStateException("provider failed"))
                .when(debateService)
                .generateInitialTurns(7L, 3L);

        generationService.requestGeneration(7L, 3L);
        executor.tasks.get(0).run();
        generationService.requestGeneration(7L, 3L);

        assertThat(executor.tasks).hasSize(2);
    }

    private static final class CapturingExecutor implements Executor {
        private final List<Runnable> tasks = new ArrayList<>();

        @Override
        public void execute(Runnable command) {
            tasks.add(command);
        }
    }
}

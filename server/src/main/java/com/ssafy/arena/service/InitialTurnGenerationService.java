package com.ssafy.arena.service;

import com.ssafy.arena.dto.debate.InitialTurnGenerationResponse;
import com.ssafy.arena.dto.debate.InitialTurnGenerationStatus;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class InitialTurnGenerationService {
    private final DebateService debateService;
    private final Executor executor;
    private final ConcurrentMap<Long, Boolean> runningJobs = new ConcurrentHashMap<>();

    public InitialTurnGenerationService(
            DebateService debateService,
            @Qualifier("initialTurnGenerationExecutor") Executor executor
    ) {
        this.debateService = debateService;
        this.executor = executor;
    }

    public InitialTurnGenerationResponse requestGeneration(Long userId, Long debateId) {
        InitialTurnGenerationResponse snapshot = debateService.initialTurnGenerationSnapshot(userId, debateId);
        if (snapshot.status() == InitialTurnGenerationStatus.COMPLETE) {
            runningJobs.remove(debateId);
            return snapshot;
        }

        runningJobs.computeIfAbsent(debateId, key -> {
            executor.execute(() -> runGeneration(userId, debateId));
            return true;
        });
        return snapshot;
    }

    private void runGeneration(Long userId, Long debateId) {
        try {
            debateService.generateInitialTurns(userId, debateId);
        } catch (RuntimeException error) {
            log.warn("Initial debate turn generation failed. debateId={}, userId={}", debateId, userId, error);
        } finally {
            runningJobs.remove(debateId);
        }
    }
}

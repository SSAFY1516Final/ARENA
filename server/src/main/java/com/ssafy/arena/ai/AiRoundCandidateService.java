package com.ssafy.arena.ai;

import com.ssafy.arena.common.ApiException;
import com.ssafy.arena.domain.AiRoundCandidate;
import com.ssafy.arena.domain.AiPromptCallLog;
import com.ssafy.arena.domain.AiRoundCandidateRun;
import com.ssafy.arena.domain.DebateSession;
import com.ssafy.arena.dto.ai.RoundCandidateResponse;
import com.ssafy.arena.dto.ai.RoundCandidatesRequest;
import com.ssafy.arena.dto.ai.RoundCandidatesResponse;
import com.ssafy.arena.dto.ai.TopicFrameResponse;
import com.ssafy.arena.mapper.AiPromptCallLogMapper;
import com.ssafy.arena.mapper.AiRoundCandidateMapper;
import com.ssafy.arena.mapper.AiRoundCandidateRunMapper;
import com.ssafy.arena.mapper.DebateMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AiRoundCandidateService {
    private static final String SYSTEM_PROMPT = "Return valid JSON only. Do not use markdown unless the user prompt requires it.";

    private final SpringAiClient springAiClient;
    private final PromptTemplateLoader promptTemplateLoader;
    private final AiResponseParser responseParser;
    private final AiRoundCandidateRunMapper runMapper;
    private final AiRoundCandidateMapper candidateMapper;
    private final AiPromptCallLogMapper logMapper;
    private final DebateMapper debateMapper;

    @Value("${spring.ai.openai.chat.options.model:gpt-5.4-mini}")
    private String aiModel = "gpt-5.4-mini";

    @Transactional
    public RoundCandidatesResponse generate(Long userId, RoundCandidatesRequest request) {
        AiRoundCandidateRun run = AiRoundCandidateRun.builder()
                .userId(userId)
                .originalTopic(request.topic())
                .mode(request.modeOrDefault())
                .candidateCount(request.candidateCountOrDefault())
                .status("STARTED")
                .build();
        runMapper.insert(run);

        String topicFrameJson = null;
        try {
            TopicFrameResponse topicFrame = callStage(
                    run.getId(),
                    "TOPIC_FRAME",
                    "topic-frame-generator.txt",
                    Map.of("mode", request.modeOrDefault().name(), "topic", request.topic()),
                    responseParser::parseTopicFrame
            );
            topicFrame = normalizeTopicFrame(topicFrame);
            topicFrameJson = responseParser.toJson(topicFrame);

            if (!topicFrame.debatable()) {
                String reason = topicFrame.problemReason() == null ? "not debatable" : topicFrame.problemReason();
                runMapper.markFailed(run.getId(), topicFrameJson, reason);
                throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, reason);
            }

            List<RoundCandidateResponse> generatedCandidates = callStage(
                    run.getId(),
                    "CANDIDATE_GENERATION",
                    "topic-round-candidates-generator.txt",
                    Map.of(
                            "mode", request.modeOrDefault().name(),
                            "bigTopic", topicFrame.normalizedBigTopic(),
                            "sideA", topicFrame.sideA(),
                            "sideB", topicFrame.sideB(),
                            "basicConditions", topicFrame.basicConditions(),
                            "candidateCount", request.candidateCountOrDefault()
                    ),
                    responseParser::parseGeneratedCandidates
            );

            List<RoundCandidateResponse> validatedCandidates = callStage(
                    run.getId(),
                    "CANDIDATE_VALIDATION",
                    "topic-round-candidates-validator.txt",
                    Map.of(
                            "mode", request.modeOrDefault().name(),
                            "bigTopic", topicFrame.normalizedBigTopic(),
                            "sideA", topicFrame.sideA(),
                            "sideB", topicFrame.sideB(),
                            "basicConditions", topicFrame.basicConditions(),
                            "topK", request.candidateCountOrDefault(),
                            "candidatesJson", responseParser.toJson(generatedCandidates)
                    ),
                    responseParser::parseValidatedCandidates
            );

            validateCandidates(validatedCandidates, request.candidateCountOrDefault());
            List<RoundCandidateResponse> persistedCandidates = persistCandidates(run.getId(), validatedCandidates);
            RoundCandidatesResponse response = new RoundCandidatesResponse(
                    run.getId(), topicFrame, persistedCandidates, usedCandidateIds(userId, run.getId()), List.of());
            runMapper.markSucceeded(run.getId(), topicFrameJson, responseParser.toJson(response));
            return response;
        } catch (ApiException ex) {
            if (ex.getStatus() != HttpStatus.UNPROCESSABLE_ENTITY) {
                runMapper.markFailed(run.getId(), topicFrameJson, ex.getMessage());
            }
            throw ex;
        } catch (RuntimeException ex) {
            runMapper.markFailed(run.getId(), topicFrameJson, ex.getMessage());
            throw ex;
        }
    }

    public RoundCandidatesResponse getExistingRun(Long userId, Long runId) {
        AiRoundCandidateRun run = runMapper.findById(runId);
        if (run == null || !run.getUserId().equals(userId) || !"SUCCEEDED".equals(run.getStatus())) {
            throw new ApiException(HttpStatus.NOT_FOUND, "round candidate run not found");
        }
        TopicFrameResponse topicFrame = responseParser.parseTopicFrame(run.getTopicFrameJson());
        List<RoundCandidateResponse> candidates = candidateMapper.findByRunId(runId).stream()
                .map(this::toResponse)
                .toList();
        return new RoundCandidatesResponse(runId, topicFrame, candidates, usedCandidateIds(userId, runId), List.of());
    }

    private List<Long> usedCandidateIds(Long userId, Long runId) {
        if (runId == null) {
            return List.of();
        }
        List<DebateSession> sessions = debateMapper.findSessionsByUserId(userId);
        if (sessions == null) {
            return List.of();
        }
        return sessions.stream()
                .filter(session -> runId.equals(session.getCandidateRunId()))
                .filter(session -> session.getId() != null && !debateMapper.findMessages(session.getId()).isEmpty())
                .map(DebateSession::getSelectedCandidateId)
                .filter(candidateId -> candidateId != null)
                .distinct()
                .toList();
    }

    private List<RoundCandidateResponse> persistCandidates(Long runId, List<RoundCandidateResponse> candidates) {
        List<RoundCandidateResponse> persistedCandidates = new ArrayList<>();
        int sortOrder = 1;
        for (RoundCandidateResponse candidate : candidates) {
            AiRoundCandidate entity = AiRoundCandidate.builder()
                    .runId(runId)
                    .roundId(candidate.roundId())
                    .title(candidate.title())
                    .coreQuestion(candidate.coreQuestion())
                    .debateAxis(candidate.debateAxis())
                    .sideAFrame(candidate.sideAFrame())
                    .sideBFrame(candidate.sideBFrame())
                    .sortOrder(sortOrder)
                    .candidateJson(responseParser.toJson(candidate))
                    .build();
            candidateMapper.insert(entity);
            persistedCandidates.add(withCandidateId(entity.getId(), candidate));
            sortOrder += 1;
        }
        return persistedCandidates;
    }

    private RoundCandidateResponse toResponse(AiRoundCandidate candidate) {
        return new RoundCandidateResponse(
                candidate.getId(),
                candidate.getRoundId(),
                candidate.getTitle(),
                candidate.getCoreQuestion(),
                candidate.getDebateAxis(),
                candidate.getSideAFrame(),
                candidate.getSideBFrame()
        );
    }

    private RoundCandidateResponse withCandidateId(Long candidateId, RoundCandidateResponse candidate) {
        return new RoundCandidateResponse(
                candidateId,
                candidate.roundId(),
                candidate.title(),
                candidate.coreQuestion(),
                candidate.debateAxis(),
                candidate.sideAFrame(),
                candidate.sideBFrame()
        );
    }

    private <T> T callStage(
            Long runId,
            String stage,
            String promptName,
            Map<String, ?> variables,
            Function<String, T> parser
    ) {
        String renderedPrompt = promptTemplateLoader.loadAndRender(promptName, variables);
        long startedAt = System.nanoTime();
        String rawResponse = null;
        try {
            rawResponse = springAiClient.call(SYSTEM_PROMPT, renderedPrompt);
            T parsed = parser.apply(rawResponse);
            logMapper.insert(AiPromptCallLog.builder()
                    .runId(runId)
                    .stage(stage)
                    .promptName(promptName)
                    .model(aiModel)
                    .renderedPrompt(renderedPrompt)
                    .rawResponse(rawResponse)
                    .parsedResponseJson(responseParser.toJson(parsed))
                    .status("SUCCEEDED")
                    .latencyMs(elapsedMs(startedAt))
                    .build());
            return parsed;
        } catch (RuntimeException ex) {
            logMapper.insert(AiPromptCallLog.builder()
                    .runId(runId)
                    .stage(stage)
                    .promptName(promptName)
                    .model(aiModel)
                    .renderedPrompt(renderedPrompt)
                    .rawResponse(rawResponse)
                    .status("FAILED")
                    .errorMessage(ex.getMessage())
                    .latencyMs(elapsedMs(startedAt))
                    .build());
            throw ex;
        }
    }

    private void validateCandidates(List<RoundCandidateResponse> candidates, int expectedCount) {
        if (candidates.size() != expectedCount || candidates.stream().anyMatch(this::hasMissingRequiredField)) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Spring AI round candidate validation failed");
        }
    }

    private boolean hasMissingRequiredField(RoundCandidateResponse candidate) {
        return isBlank(candidate.roundId())
                || isBlank(candidate.title())
                || isBlank(candidate.coreQuestion())
                || isBlank(candidate.debateAxis())
                || isBlank(candidate.sideAFrame())
                || isBlank(candidate.sideBFrame());
    }

    private TopicFrameResponse normalizeTopicFrame(TopicFrameResponse topicFrame) {
        return new TopicFrameResponse(
                topicFrame.normalizedBigTopic(),
                TopicSideLabelNormalizer.normalize(topicFrame.sideA()),
                TopicSideLabelNormalizer.normalize(topicFrame.sideB()),
                topicFrame.basicConditions(),
                topicFrame.debatable(),
                topicFrame.assumptions(),
                topicFrame.missingFields(),
                topicFrame.problemReason()
        );
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private long elapsedMs(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000L;
    }
}

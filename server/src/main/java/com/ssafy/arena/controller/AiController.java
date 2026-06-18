package com.ssafy.arena.controller;

import com.ssafy.arena.ai.topic.TopicSuggestionUseCase;
import com.ssafy.arena.dto.ai.TopicCandidateRequest;
import com.ssafy.arena.dto.ai.TopicCandidateResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {
    private final TopicSuggestionUseCase topicSuggestionUseCase;

    @PostMapping("/topic-candidates")
    public TopicCandidateResponse topicCandidates(@Valid @RequestBody TopicCandidateRequest request) {
        return topicSuggestionUseCase.suggest(request);
    }
}

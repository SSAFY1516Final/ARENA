package com.ssafy.arena.ai.topic;

import com.ssafy.arena.dto.ai.TopicCandidateItem;
import com.ssafy.arena.dto.ai.TopicCandidateRequest;
import com.ssafy.arena.dto.ai.TopicCandidateResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TopicSuggestionUseCase {
    private final TopicCandidateGenerator generator;
    private final TopicCandidateValidator validator;
    private final TopicCandidateRanker ranker;

    public TopicCandidateResponse suggest(TopicCandidateRequest request) {
        List<TopicCandidateItem> validated = generator.generate(request).stream()
                .filter(candidate -> validator.isValid(request, candidate))
                .toList();
        return new TopicCandidateResponse(ranker.rank(validated));
    }
}

package com.ssafy.arena.ai.topic;

import com.ssafy.arena.ai.AiPromptFactory;
import com.ssafy.arena.ai.AiResponseParser;
import com.ssafy.arena.ai.SpringAiClient;
import com.ssafy.arena.dto.ai.TopicCandidateItem;
import com.ssafy.arena.dto.ai.TopicCandidateRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TopicCandidateGenerator {
    private final SpringAiClient springAiClient;
    private final AiPromptFactory promptFactory;
    private final AiResponseParser responseParser;

    public List<TopicCandidateItem> generate(TopicCandidateRequest request) {
        String content = springAiClient.call(
                promptFactory.topicCandidateSystemPrompt(),
                promptFactory.topicCandidateUserPrompt(request)
        );
        return responseParser.parseTopicCandidates(content).items();
    }
}

package com.ssafy.arena.ai.topic;

import com.ssafy.arena.dto.ai.TopicCandidateItem;
import com.ssafy.arena.dto.ai.TopicCandidateRequest;
import org.springframework.stereotype.Component;

@Component
public class TopicCandidateValidator {
    public boolean isValid(TopicCandidateRequest request, TopicCandidateItem item) {
        if (item == null || item.title() == null || item.title().isBlank()) {
            return false;
        }
        return safeScore(item.fitScore()) >= 50;
    }

    private int safeScore(Integer score) {
        return score == null ? 0 : score;
    }
}

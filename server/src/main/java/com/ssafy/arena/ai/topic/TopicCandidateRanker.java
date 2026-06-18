package com.ssafy.arena.ai.topic;

import com.ssafy.arena.dto.ai.TopicCandidateItem;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class TopicCandidateRanker {
    public List<TopicCandidateItem> rank(List<TopicCandidateItem> candidates) {
        return candidates.stream()
                .sorted(Comparator
                        .comparingInt(TopicCandidateItem::totalScore)
                        .thenComparing(item -> item.title() == null ? "" : item.title())
                        .reversed())
                .limit(10)
                .toList();
    }
}

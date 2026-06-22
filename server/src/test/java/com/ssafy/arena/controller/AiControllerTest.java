package com.ssafy.arena.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.ssafy.arena.ai.topic.TopicSuggestionUseCase;
import com.ssafy.arena.domain.DebateMode;
import com.ssafy.arena.dto.ai.TopicCandidateItem;
import com.ssafy.arena.dto.ai.TopicCandidateRequest;
import com.ssafy.arena.dto.ai.TopicCandidateResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AiControllerTest {
    @Mock
    private TopicSuggestionUseCase topicSuggestionUseCase;

    @InjectMocks
    private AiController aiController;

    @Test
    void topicCandidatesDelegatesToUseCase() {
        TopicCandidateRequest request = new TopicCandidateRequest(
                "점심 메뉴",
                DebateMode.PRACTICAL,
                List.of("1만원 이하"),
                "회사 근처"
        );
        TopicCandidateResponse expected = new TopicCandidateResponse(List.of(
                new TopicCandidateItem("오후 집중력을 기준으로 제육 vs 돈까스", "조건 반영", 88, 94, 72)
        ));
        when(topicSuggestionUseCase.suggest(request)).thenReturn(expected);

        TopicCandidateResponse response = aiController.topicCandidates(request);

        assertThat(response).isSameAs(expected);
    }
}

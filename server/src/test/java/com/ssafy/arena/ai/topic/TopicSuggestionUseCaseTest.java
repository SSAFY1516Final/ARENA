package com.ssafy.arena.ai.topic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

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
class TopicSuggestionUseCaseTest {
    @Mock
    private TopicCandidateGenerator generator;

    @Mock
    private TopicCandidateValidator validator;

    @Mock
    private TopicCandidateRanker ranker;

    @InjectMocks
    private TopicSuggestionUseCase useCase;

    @Test
    void suggestsValidatedAndRankedCandidates() {
        TopicCandidateRequest request = new TopicCandidateRequest(
                "점심 메뉴",
                DebateMode.PRACTICAL,
                List.of("1만원 이하", "오후 집중력"),
                "회사 근처에서 빠르게 먹어야 함"
        );
        TopicCandidateItem weak = new TopicCandidateItem("그냥 아무거나 먹기", "조건 반영이 약함", 10, 20, 10);
        TopicCandidateItem strong = new TopicCandidateItem("오후 집중력을 기준으로 제육 vs 돈까스", "조건과 갈등이 명확함", 88, 94, 72);
        TopicCandidateItem funny = new TopicCandidateItem("회의 전 생존식으로 김밥 vs 라면", "재미와 조건이 모두 있음", 92, 80, 91);

        when(generator.generate(request)).thenReturn(List.of(weak, strong, funny));
        when(validator.isValid(request, weak)).thenReturn(false);
        when(validator.isValid(request, strong)).thenReturn(true);
        when(validator.isValid(request, funny)).thenReturn(true);
        when(ranker.rank(List.of(strong, funny))).thenReturn(List.of(funny, strong));

        TopicCandidateResponse response = useCase.suggest(request);

        assertThat(response.items()).containsExactly(funny, strong);
    }
}

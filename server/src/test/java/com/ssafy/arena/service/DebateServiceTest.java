package com.ssafy.arena.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.ssafy.arena.ai.AiClient;
import com.ssafy.arena.domain.DebateMode;
import com.ssafy.arena.domain.DebateSession;
import com.ssafy.arena.domain.DebateStatus;
import com.ssafy.arena.dto.debate.DebateListItem;
import com.ssafy.arena.mapper.DebateMapper;
import com.ssafy.arena.mapper.PostMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DebateServiceTest {
    @Mock
    private DebateMapper debateMapper;

    @Mock
    private PostMapper postMapper;

    @Mock
    private AiClient aiClient;

    @InjectMocks
    private DebateService debateService;

    @Test
    void listMyDebatesReturnsOnlySessionsOwnedByUser() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 6, 11, 12, 0);
        when(debateMapper.findSessionsByUserId(7L)).thenReturn(List.of(
                DebateSession.builder()
                        .id(3L)
                        .userId(7L)
                        .topic("점심 메뉴")
                        .mode(DebateMode.PRACTICAL)
                        .status(DebateStatus.ACTIVE)
                        .createdAt(createdAt)
                        .build()
        ));

        List<DebateListItem> debates = debateService.listMyDebates(7L);

        assertThat(debates).hasSize(1);
        assertThat(debates.get(0).debateId()).isEqualTo(3L);
        assertThat(debates.get(0).topic()).isEqualTo("점심 메뉴");
        assertThat(debates.get(0).status()).isEqualTo(DebateStatus.ACTIVE);
        assertThat(debates.get(0).updatedAt()).isEqualTo(createdAt);
    }
}
